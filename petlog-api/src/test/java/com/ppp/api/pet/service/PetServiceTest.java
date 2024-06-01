package com.ppp.api.pet.service;

import com.ppp.api.guardian.service.GuardianService;
import com.ppp.api.pet.dto.request.PetRequest;
import com.ppp.api.pet.dto.response.MyPetResponse;
import com.ppp.api.pet.dto.response.MyPetsResponse;
import com.ppp.api.pet.exception.PetException;
import com.ppp.common.service.FileStorageManageService;
import com.ppp.domain.guardian.constant.RepStatus;
import com.ppp.domain.guardian.dto.MyPetDto;
import com.ppp.domain.guardian.repository.GuardianQuerydslRepository;
import com.ppp.domain.guardian.repository.GuardianRepository;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.pet.PetImage;
import com.ppp.domain.pet.constant.Gender;
import com.ppp.domain.pet.repository.PetImageRepository;
import com.ppp.domain.pet.repository.PetRepository;
import com.ppp.domain.subscription.repository.SubscriptionRepository;
import com.ppp.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {
    @Mock
    private PetRepository petRepository;

    @Mock
    private PetImageRepository petImageRepository;

    @Mock
    private FileStorageManageService fileStorageManageService;

    @Mock
    private GuardianQuerydslRepository guardianQuerydslRepository;

    @Mock
    private GuardianService guardianService;

    @Mock
    private GuardianRepository guardianRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private PetService petService;

    private User user = null;
    private MockMultipartFile file = null;
    private Pet pet = null;

    @BeforeEach
    void init() {
        user = User.builder()
                .id("randomstring")
                .nickname("nickname")
                .build();
        file = new MockMultipartFile("petImage", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "test data" .getBytes());
        pet = Pet.builder().id(1L).user(user).isNeutered(false).build();
    }

    @Test
    @DisplayName("반려동물 추가")
    void createPetTest() {
        //given
        PetRequest petRequest = PetRequest.builder()
                .name("name")
                .type("type")
                .breed("breed")
                .gender("MALE")
                .isNeutered(true)
                .birth(LocalDate.of(2023, 1, 1))
                .firstMeetDate(LocalDate.of(2022, 1, 1))
                .weight(5.0)
                .registeredNumber("1234")
                .build();

        //when
        petService.createPet(petRequest, user, null);

        //then
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    @DisplayName("반려동물 수정")
    void updatePetTest() {
        //given
        PetRequest petRequest = PetRequest.builder()
                .name("name")
                .type("type")
                .breed("breed")
                .gender("MALE")
                .isNeutered(true)
                .birth(LocalDate.of(2023, 1, 1))
                .firstMeetDate(LocalDate.of(2022, 1, 1))
                .weight(5.0)
                .registeredNumber("1234")
                .build();
        when(petRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(pet));
        when(guardianRepository.existsByUserIdAndPetId(user.getId(), 1L)).thenReturn(true);
        //when
        petService.updatePet(1L, petRequest, user, null);

        //then
        assertEquals("name", pet.getName());
    }

    @Test
    @DisplayName("반려동물 조회")
    void displayPetTest() {
        //given
        MyPetDto myPetDto = MyPetDto.builder()
                .petId(15L)
                .ownerId("j22031")
                .invitedCode("1021vc9h")
                .name("이름")
                .type("타입")
                .breed("품종")
                .gender(Gender.FEMALE)
                .isNeutered(true)
                .birth(LocalDateTime.parse("2020-01-01T00:00:00"))
                .firstMeetDate(LocalDateTime.parse("2020-01-01T00:00:00"))
                .weight(5.5)
                .registeredNumber("1234")
                .petImageUrl(null)
                .repStatus(RepStatus.NORMAL)
                .build();
        when(guardianQuerydslRepository.findOneMyPetByInGuardian(1L, user.getId())).thenReturn(myPetDto);

        //when
        MyPetResponse myPetResponse = petService.findMyPetById(1L, user);

        //then
        Assertions.assertThat(myPetResponse).isNotNull();
    }

    @Test
    @DisplayName("반려동물 리스트")
    void displayPetsTest() {
        //given
        MyPetDto myPetDto = MyPetDto.builder()
                .petId(15L)
                .ownerId("j22031")
                .invitedCode("1021vc9h")
                .name("이름")
                .type("타입")
                .breed("품종")
                .gender(Gender.FEMALE)
                .isNeutered(true)
                .birth(LocalDateTime.parse("2020-01-01T00:00:00"))
                .firstMeetDate(LocalDateTime.parse("2020-01-01T00:00:00"))
                .weight(5.5)
                .registeredNumber("1234")
                .petImageUrl(null)
                .repStatus(RepStatus.NORMAL)
                .build();
        when(guardianQuerydslRepository.findMyPetByInGuardian(user.getId())).thenReturn(
                List.of(myPetDto)
        );

        //when
        MyPetsResponse MyPetsResponse = petService.findMyPetByInGuardian(user);

        //then
        Assertions.assertThat(MyPetsResponse).isNotNull();
    }

    @Test
    @DisplayName("반려동물 삭제 - 이미지 있을 때")
    void deleteMyPet() {
        //given
        Pet pet = Pet.builder().id(1L).user(user).isDeleted(false).build();
        when(petRepository.findMyPetByIdAndIsDeletedFalse(pet.getId(), user.getId())).thenReturn(Optional.of(pet));

        PetImage petImage = PetImage.builder().url("url").build();
        when(petImageRepository.findByPet(pet)).thenReturn(Optional.of(petImage));

        //when
        petService.deleteMyPet(pet.getId(), user);

        //then
        assertEquals(true, pet.getIsDeleted());
        verify(petImageRepository, times(1)).delete(petImage);
    }

    @Test
    @DisplayName("반려동물 삭제 - 이미지 없을 때")
    void deleteMyPet_noImage() {
        //given
        Pet pet = Pet.builder().id(1L).user(user).isDeleted(false).build();
        when(petRepository.findMyPetByIdAndIsDeletedFalse(pet.getId(), user.getId())).thenReturn(Optional.of(pet));

        PetImage petImage = PetImage.builder().build();
        when(petImageRepository.findByPet(pet)).thenReturn(Optional.of(petImage));

        //when
        petService.deleteMyPet(pet.getId(), user);

        //then
        assertEquals(true, pet.getIsDeleted());
        verify(petImageRepository, times(0)).delete(petImage);
    }

    @Test
    @DisplayName("반려동물 이름 중복 검사")
    void validatePetName_duplication() {
        //given
        String name = "pet_name";

        //when
        when(petRepository.existsByNameAndIsDeletedFalse(name)).thenReturn(true);

        //then
        assertThrows(PetException.class, () -> petService.validatePetName(name), "펫 이름 중복");
    }
}
