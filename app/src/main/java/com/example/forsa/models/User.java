package com.example.forsa.models;


public class User {

    private long id;
    private String fullName;
    private String username;
    private String email;
    private String passwordHash;

    private String phone;
    private String birthDate;
    private String city;
    private String institution;
    private String major;
    private String educationLevel;
    private String interests;
    private String profileImage;

    private boolean profileCompleted;

    public User(
            String fullName,
            String username,
            String email,
            String passwordHash
    ) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.profileCompleted = false;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getCity() {
        return city;
    }

    public String getInstitution() {
        return institution;
    }

    public String getMajor() {
        return major;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public String getInterests() {
        return interests;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }
}