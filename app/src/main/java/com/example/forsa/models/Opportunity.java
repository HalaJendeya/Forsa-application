package com.example.forsa.models;

import java.io.Serializable;

public class Opportunity implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long userId;
    private String title;
    private String organization;
    private String category;
    private String description;
    private String deadline;
    private String location;
    private String applicationLink;
    private String contactEmail;
    private String status;
    private String priority;
    private String notes;
    private String imageUri;

    // Constructor without id (for creating new opportunities)
    public Opportunity(
            long userId,
            String title,
            String organization,
            String category,
            String description,
            String deadline,
            String location,
            String applicationLink,
            String contactEmail,
            String status,
            String priority,
            String notes,
            String imageUri
    ) {
        this.userId = userId;
        this.title = title;
        this.organization = organization;
        this.category = category;
        this.description = description;
        this.deadline = deadline;
        this.location = location;
        this.applicationLink = applicationLink;
        this.contactEmail = contactEmail;
        this.status = status;
        this.priority = priority;
        this.notes = notes;
        this.imageUri = imageUri;
    }

    // Full constructor including id (for reading from SQLite)
    public Opportunity(
            long id,
            long userId,
            String title,
            String organization,
            String category,
            String description,
            String deadline,
            String location,
            String applicationLink,
            String contactEmail,
            String status,
            String priority,
            String notes,
            String imageUri
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.organization = organization;
        this.category = category;
        this.description = description;
        this.deadline = deadline;
        this.location = location;
        this.applicationLink = applicationLink;
        this.contactEmail = contactEmail;
        this.status = status;
        this.priority = priority;
        this.notes = notes;
        this.imageUri = imageUri;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getApplicationLink() {
        return applicationLink;
    }

    public void setApplicationLink(String applicationLink) {
        this.applicationLink = applicationLink;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
