package com.familyliferadio.myflrnew.dto

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity
 class AlarmDto : Serializable {
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int = 0
    @ColumnInfo(name = "sheduleTime")
    var sheduleTime: String = ""
    @ColumnInfo(name = "monDay")
    var monDay: Boolean = true
    @ColumnInfo(name = "tuesDay")
    var tuesDay: Boolean = true
    @ColumnInfo(name = "wednessDay")
    var wednessDay: Boolean = true
    @ColumnInfo(name = "thurseDay")
    var thurseDay: Boolean = true
    @ColumnInfo(name = "friDay")
    var friDay: Boolean = true
    @ColumnInfo(name = "saturDay")
    var saturDay: Boolean = true
    @ColumnInfo(name = "sunDay")
    var sunDay: Boolean = true
    @ColumnInfo(name = "isRepeat")
    var isRepeat: Boolean = false
    @ColumnInfo(name = "isAlarmSet")
    var isAlarmSet: Boolean = false
    @ColumnInfo(name = "msg")
    var msg: String = ""

}