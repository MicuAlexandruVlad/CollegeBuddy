package com.example.micua.licenseapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Group implements Serializable, Parcelable {
    private String id;
    private String groupName;
    private String groupType;
    private String groupAdministrator;
    private String groupSecondaryAdministrator;
    private String accessToken;
    private String referencedFacultyName;
    private String referencedCollegeName;
    private String groupDescription;
    private Message lastMessage;
    private int visibility;
    private boolean secondaryAdministrator;
    private int numMembers;
    private boolean groupSelected = false;

    public Group() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getGroupAdministrator() {
        return groupAdministrator;
    }

    public void setGroupAdministrator(String groupAdministrator) {
        this.groupAdministrator = groupAdministrator;
    }

    public String getGroupSecondaryAdministrator() {
        return groupSecondaryAdministrator;
    }

    public void setGroupSecondaryAdministrator(String groupSecondaryAdministrator) {
        this.groupSecondaryAdministrator = groupSecondaryAdministrator;
    }

    public boolean hasSecondaryAdministrator() {
        return secondaryAdministrator;
    }

    public void setSecondaryAdministrator(boolean secondaryAdministrator) {
        this.secondaryAdministrator = secondaryAdministrator;
    }

    public int getNumMembers() {
        return numMembers;
    }

    public void setNumMembers(int numMembers) {
        this.numMembers = numMembers;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isSecondaryAdministrator() {
        return secondaryAdministrator;
    }

    public String getReferencedFacultyName() {
        return referencedFacultyName;
    }

    public void setReferencedFacultyName(String referencedFacultyName) {
        this.referencedFacultyName = referencedFacultyName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getReferencedCollegeName() {
        return referencedCollegeName;
    }

    public void setReferencedCollegeName(String referencedCollegeName) {
        this.referencedCollegeName = referencedCollegeName;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public boolean isGroupSelected() {
        return groupSelected;
    }

    public void setGroupSelected(boolean groupSelected) {
        this.groupSelected = groupSelected;
    }

    protected Group(Parcel in) {
        id = in.readString();
        groupName = in.readString();
        groupType = in.readString();
        groupAdministrator = in.readString();
        groupSecondaryAdministrator = in.readString();
        accessToken = in.readString();
        referencedFacultyName = in.readString();
        referencedCollegeName = in.readString();
        groupDescription = in.readString();
        lastMessage = (Message) in.readValue(Message.class.getClassLoader());
        visibility = in.readInt();
        secondaryAdministrator = in.readByte() != 0x00;
        numMembers = in.readInt();
        groupSelected = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(groupName);
        dest.writeString(groupType);
        dest.writeString(groupAdministrator);
        dest.writeString(groupSecondaryAdministrator);
        dest.writeString(accessToken);
        dest.writeString(referencedFacultyName);
        dest.writeString(referencedCollegeName);
        dest.writeString(groupDescription);
        dest.writeValue(lastMessage);
        dest.writeInt(visibility);
        dest.writeByte((byte) (secondaryAdministrator ? 0x01 : 0x00));
        dest.writeInt(numMembers);
        dest.writeByte((byte) (groupSelected ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
}
