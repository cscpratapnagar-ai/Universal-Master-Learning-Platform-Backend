package com.masterlearning.platform.modules.organization.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "organizations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_organizations_code", columnNames = "code")
})
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 120)
    private String slug;

    @Column(name = "legal_name", length = 250)
    private String legalName;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "organization_type", length = 80)
    private String organizationType;

    @Column(name = "registration_number", length = 120)
    private String registrationNumber;

    @Column(name = "established_date")
    private LocalDate establishedDate;

    @Column(name = "primary_email", length = 254)
    private String primaryEmail;

    @Column(name = "primary_phone", length = 40)
    private String primaryPhone;

    @Column(name = "alternate_phone", length = 40)
    private String alternatePhone;

    @Column(length = 500)
    private String website;

    @Column(name = "address_line", length = 500)
    private String addressLine;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "secondary_color", length = 20)
    private String secondaryColor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    protected Organization() {}

    public Organization(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.legalName = name;
        this.displayName = name;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public String getSlug() { return slug; }
    public String getLegalName() { return legalName; }
    public String getDisplayName() { return displayName; }
    public String getOrganizationType() { return organizationType; }
    public String getRegistrationNumber() { return registrationNumber; }
    public LocalDate getEstablishedDate() { return establishedDate; }
    public String getPrimaryEmail() { return primaryEmail; }
    public String getPrimaryPhone() { return primaryPhone; }
    public String getAlternatePhone() { return alternatePhone; }
    public String getWebsite() { return website; }
    public String getAddressLine() { return addressLine; }
    public String getCountry() { return country; }
    public String getState() { return state; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getPostalCode() { return postalCode; }
    public String getLogoUrl() { return logoUrl; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getPrimaryColor() { return primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public OrganizationStatus getStatus() { return status; }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        if (this.displayName == null) this.displayName = name;
        if (this.legalName == null) this.legalName = name;
    }

    public void updateProfile(
            String slug, String legalName, String displayName, String organizationType,
            String registrationNumber, LocalDate establishedDate, String primaryEmail,
            String primaryPhone, String alternatePhone, String website, String addressLine,
            String country, String state, String city, String district, String postalCode,
            String logoUrl, String coverImageUrl, String primaryColor, String secondaryColor
    ) {
        this.slug = slug;
        this.legalName = legalName;
        this.displayName = displayName;
        this.organizationType = organizationType;
        this.registrationNumber = registrationNumber;
        this.establishedDate = establishedDate;
        this.primaryEmail = primaryEmail;
        this.primaryPhone = primaryPhone;
        this.alternatePhone = alternatePhone;
        this.website = website;
        this.addressLine = addressLine;
        this.country = country;
        this.state = state;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.logoUrl = logoUrl;
        this.coverImageUrl = coverImageUrl;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public void changeStatus(OrganizationStatus status) {
        this.status = status;
        this.active = status == OrganizationStatus.ACTIVE;
    }

    public void deactivate() { changeStatus(OrganizationStatus.INACTIVE); }
    public void activate() { changeStatus(OrganizationStatus.ACTIVE); }
}