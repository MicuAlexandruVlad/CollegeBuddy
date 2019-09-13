package com.example.micua.licenseapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MessageDAO {
    @Insert
    long insertMessage(Message message);

    @Query("SELECT * FROM Message")
    List<Message> getMessages();

    @Query("SELECT * FROM Message WHERE referencedGroupId = :groupId ORDER BY id DESC LIMIT 1")
    Message getLastMessageForGroup(String groupId);

    @Query("UPDATE Message SET imagePath= :path WHERE id= :id")
    void updateImagePath(String path, int id);

    @Query("UPDATE Message SET localFileData= :data WHERE id= :id")
    void updateLocalFileData(String data, int id);
}
