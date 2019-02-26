package com.familyliferadio.myflrnew.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.familyliferadio.myflrnew.dto.AlarmDto;

import java.util.List;

@Dao
public interface DaoAccessOperations {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAlarmData(AlarmDto alarmData);

    @Update
    void updateAlarmData(AlarmDto alarmData);

    @Query("DELETE FROM AlarmDto WHERE id =:id")
    void deleteAlarmById(int id);

    @Delete
    void deleteAlarmByObj(AlarmDto alarmData);

    @Query("SELECT * FROM AlarmDto WHERE id =:id")
    AlarmDto getByID(int id);

    @Query("SELECT * FROM AlarmDto")
    List<AlarmDto> fetchAllAlarms();


}
