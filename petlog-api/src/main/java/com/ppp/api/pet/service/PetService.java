package com.ppp.api.pet.service;

import com.ppp.api.guardian.exception.GuardianException;
import com.ppp.api.guardian.service.GuardianService;
import com.ppp.api.pet.dto.request.PetRequest;
import com.ppp.api.pet.dto.response.MyPetResponse;
import com.ppp.api.pet.dto.response.MyPetsResponse;
import com.ppp.api.pet.exception.ErrorCode;
import com.ppp.api.pet.exception.PetException;
import com.ppp.common.service.CacheManageService;
import com.ppp.common.service.FileStorageManageService;
import com.ppp.common.service.ThumbnailService;
import com.ppp.domain.common.constant.Domain;
import com.ppp.domain.common.constant.FileType;
import com.ppp.domain.common.util.GenerationUtil;
import com.ppp.domain.guardian.Guardian;
import com.ppp.domain.guardian.constant.GuardianRole;
import com.ppp.domain.guardian.dto.MyPetDto;
import com.ppp.domain.guardian.repository.GuardianQuerydslRepository;
import com.ppp.domain.guardian.repository.GuardianRepository;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.pet.PetImage;
import com.ppp.domain.pet.repository.PetImageRepository;
import com.ppp.domain.pet.repository.PetRepository;
import com.ppp.domain.subscription.Subscription;
import com.ppp.domain.subscription.repository.SubscriptionRepository;
import com.ppp.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ppp.api.guardian.exception.ErrorCode.GUARDIAN_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;
    private final PetImageRepository petImageRepository;
    private final FileStorageManageService fileStorageManageService;
    private final GuardianService guardianService;
    private final GuardianQuerydslRepository guardianQuerydslRepository;
    private final GuardianRepository guardianRepository;
    private final ThumbnailService thumbnailService;
    private final SubscriptionRepository subscriptionRepository;
    private final CacheManageService cacheManageService;

    @Transactional
    public void createPet(PetRequest petRequest, User user, MultipartFile petImage) {
        String inviteCode = GenerationUtil.generateCode();

        Pet pet = Pet.builder()
                .user(user)
                .name(petRequest.getName())
                .type(petRequest.getType())
                .breed(petRequest.getBreed())
                .gender(petRequest.getToGender())
                .isNeutered(petRequest.getIsNeutered() != null ? petRequest.getIsNeutered() : null)
                .birth(petRequest.convertToBirthLocalDateTime())
                .firstMeetDate(petRequest.convertToFirstMeetDateLocalDateTime())
                .weight(petRequest.getWeight())
                .registeredNumber(petRequest.getRegisteredNumber())
                .isDeleted(false)
                .invitedCode(inviteCode)
                .build();

        Pet savedPet = petRepository.save(pet);
        guardianService.createGuardian(savedPet, user, GuardianRole.LEADER);

        savePetImage(pet, petImage);
        savePetThumbnail(pet);
    }

    private void savePetImage(Pet pet, MultipartFile petImage) {
        if (petImage != null && !petImage.isEmpty()) {
            petImageRepository.findByPet(pet).ifPresent(
                    image -> fileStorageManageService.deleteImage(image.getUrl()));
            String savedPath = fileStorageManageService.uploadImage(petImage, Domain.PET)
                    .orElseThrow(() -> new PetException(ErrorCode.PET_IMAGE_REGISTRATION_FAILED));
            uploadPetImage(pet, savedPath);
        }
    }

    private void savePetThumbnail(Pet pet) {
        petImageRepository.findByPet(pet).ifPresent(
            image -> {
                try {
                    if (image.getThumbnailUrl() != null) {
                        fileStorageManageService.deleteImage(image.getThumbnailUrl());
                    }
                    String thumbnailUrl = thumbnailService.uploadThumbnailFromStorageFile(image.getUrl(), FileType.IMAGE, Domain.PET);
                    image.addThumbnail(thumbnailUrl);
                } catch (Exception e) {
                    log.warn("{} is null thumbnail", pet.getId());
                }
            }
        );
    }

    private void uploadPetImage(Pet pet, String path) {
        Optional<PetImage> existingImage = petImageRepository.findByPet(pet);
        if (existingImage.isPresent()) {
            PetImage image = existingImage.get();
            image.setUrl(path);
        } else {
            PetImage newImage = PetImage.builder().pet(pet).url(path).build();
            petImageRepository.save(newImage);
        }
    }

    private void deletePetImage(PetImage petImage) {
        if (petImage.getUrl() != null) {
            fileStorageManageService.deleteImage(petImage.getUrl());
            petImageRepository.delete(petImage);
        }
    }

    private void deletePetThumbnail(PetImage petImage) {
        if (petImage.getThumbnailUrl() != null) {
            fileStorageManageService.deleteImage(petImage.getThumbnailUrl());
            petImageRepository.delete(petImage);
        }
    }

    public MyPetsResponse findMyPetByInGuardian(User user) {
        List<MyPetResponse> myPetResponseList = new ArrayList<>();

        List<MyPetDto> myPetDtos = guardianQuerydslRepository.findMyPetByInGuardian(user.getId());
        myPetDtos.forEach(
                myPetDto -> myPetResponseList.add(MyPetResponse.from(myPetDto))
        );

        return new MyPetsResponse(myPetResponseList.size(), myPetResponseList);
    }

    public MyPetResponse findMyPetById(Long petId, User user) {
        MyPetDto myPetDto = guardianQuerydslRepository.findOneMyPetByInGuardian(petId, user.getId());
        if (myPetDto == null) throw new PetException(ErrorCode.PET_NOT_FOUND);
        return MyPetResponse.from(myPetDto);
    }

    public String findPetCode(Long petId) {
        return petRepository.findPetCodeByIdAndIsDeletedFalse(petId).orElseThrow(() -> new PetException(ErrorCode.PET_NOT_FOUND));
    }

    @Transactional
    public void updatePet(Long petId, PetRequest petRequest, User user, MultipartFile petImage) {
        if (!guardianRepository.existsByUserIdAndPetId(user.getId(), petId))
            throw new GuardianException(GUARDIAN_NOT_FOUND);

        Pet pet = petRepository.findByIdAndIsDeletedFalse(petId)
                .orElseThrow(() -> new PetException(ErrorCode.PET_NOT_FOUND));

        pet.updatePet(petRequest.getName(), petRequest.getType(), petRequest.getBreed(), petRequest.getGender()
                , petRequest.getIsNeutered(), petRequest.getBirth(), petRequest.getFirstMeetDate(), petRequest.getWeight(), petRequest.getRegisteredNumber());

        savePetImage(pet, petImage);
        savePetThumbnail(pet);
    }

    @Transactional
    public void deleteMyPet(Long petId, User user) {
        Guardian guardian = guardianService.findByUserIdAndPetId(user.getId(), petId);
        guardianService.deleteReaderGuardian(guardian, petId);
        deleteSubscriptionsOfPet(petId);

        petRepository.findMyPetByIdAndIsDeletedFalse(petId, user.getId()).ifPresent(pet -> {
            PetImage petImage = petImageRepository.findByPet(pet).orElse(new PetImage());
            deletePetImage(petImage);
            deletePetThumbnail(petImage);
            pet.delete();
        });
    }

    private void deleteSubscriptionsOfPet(Long petId) {
        List<Subscription> subscriptions = subscriptionRepository.findByPetId(petId);
        if (!subscriptions.isEmpty()) {
            subscriptions.forEach(s -> cacheManageService.deleteCachedSubscriptionInfo(s.getSubscriber().getId()));
            subscriptionRepository.deleteAllInBatch(subscriptions);
        }
    }

    public void validatePetName(String name) {
        if (petRepository.existsByNameAndIsDeletedFalse(name)) {
            throw new PetException(ErrorCode.PET_NAME_DUPLICATION);
        }
    }
}
