package com.example.micua.licenseapp;

import java.io.Serializable;

public class Faculty implements Serializable {
    private String id;
    private String referencedCollegeName;
    private String facultyName;
    private String facultyDescription;
    private String originallyAddedBy;
    private String lastEditedBy;
    private int numGroups;
    private int numMembers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReferencedCollegeName() {
        return referencedCollegeName;
    }

    public void setReferencedCollegeName(String referencedCollegeName) {
        this.referencedCollegeName = referencedCollegeName;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public int getNumGroups() {
        return numGroups;
    }

    public void setNumGroups(int numGroups) {
        this.numGroups = numGroups;
    }

    public int getNumMembers() {
        return numMembers;
    }

    public void setNumMembers(int numMembers) {
        this.numMembers = numMembers;
    }

    public String getFacultyDescription() {
        return facultyDescription;
    }

    public void setFacultyDescription(String facultyDescription) {
        this.facultyDescription = facultyDescription;
    }

    public String getOriginallyAddedBy() {
        return originallyAddedBy;
    }

    public void setOriginallyAddedBby(String originallyAddedBy) {
        this.originallyAddedBy = originallyAddedBy;
    }

    public String getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }
}
