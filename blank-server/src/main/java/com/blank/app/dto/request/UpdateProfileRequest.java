package com.blank.app.dto.request;

public class UpdateProfileRequest {
    private String username;
    private String gender;
    private String birthday;
    private String location;
    private String profileVisibility;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(String profileVisibility) { this.profileVisibility = profileVisibility; }
}
