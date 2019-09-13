package com.example.micua.licenseapp;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String joinedCollegeName;
    private String joinedFacultyName;
    private String joinedGroupsAccessTokens;
    private String password;
    private boolean profileSetupComplete;
    private boolean joinedCollege;
    private boolean joinedFaculty;
    private boolean joinedGroup;
    private boolean verified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJoinedCollegeName() {
        return joinedCollegeName;
    }

    public void setJoinedCollegeName(String joinedCollegeName) {
        this.joinedCollegeName = joinedCollegeName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isProfileSetupComplete() {
        return profileSetupComplete;
    }

    public void setProfileSetupComplete(boolean profileSetupComplete) {
        this.profileSetupComplete = profileSetupComplete;
    }

    public boolean isJoinedCollege() {
        return joinedCollege;
    }

    public void setJoinedCollege(boolean joinedCollege) {
        this.joinedCollege = joinedCollege;
    }

    public String getJoinedFacultyName() {
        return joinedFacultyName;
    }

    public void setJoinedFacultyName(String joinedFacultyName) {
        this.joinedFacultyName = joinedFacultyName;
    }

    public boolean isJoinedFaculty() {
        return joinedFaculty;
    }

    public void setJoinedFaculty(boolean joinedFaculty) {
        this.joinedFaculty = joinedFaculty;
    }

    public String getJoinedGroupsAccessTokens() {
        return joinedGroupsAccessTokens;
    }

    public void setJoinedGroupsAccessTokens(String joinedGroupsAccessTokens) {
        this.joinedGroupsAccessTokens = joinedGroupsAccessTokens;
    }

    public boolean isJoinedGroup() {
        return joinedGroup;
    }

    public void setJoinedGroup(boolean joinedGroup) {
        this.joinedGroup = joinedGroup;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
