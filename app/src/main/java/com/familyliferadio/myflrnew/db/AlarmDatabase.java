package com.familyliferadio.myflrnew.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.familyliferadio.myflrnew.dto.AlarmDto;

@Database(entities = {AlarmDto.class}, version = 5)
public abstract class AlarmDatabase extends RoomDatabase {
    private static final String DB_NAME = "alarm-db";
    private static volatile AlarmDatabase instance;

    public abstract DaoAccessOperations daoAccess();

   public static synchronized AlarmDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static AlarmDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                AlarmDatabase.class,
                DB_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }


}

