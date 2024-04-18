package com.ppp.domain.pet;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "pet_image")
public class PetImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    private String url;

    private String thumbnailUrl;

    @Builder
    public PetImage(Pet pet, String url, String thumbnailUrl) {
        this.pet = pet;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String findThumbnailPath() {
        return getThumbnailUrl() != null ? getThumbnailUrl() : getUrl();
    }
}