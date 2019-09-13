package com.example.micua.licenseapp;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@android.arch.persistence.room.Database(entities = {Message.class, QuizAsJSON.class},
        version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {
    private static Database INSTANCE;
    private static final Object sLock = new Object();

    public abstract MessageDAO messageDAO();
    public abstract QuizAsJSONDAO quizAsJSONDAO();


    public static Database getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        Database.class, "test23.db")
                        .build();
            }
            return INSTANCE;
        }
    }

}
