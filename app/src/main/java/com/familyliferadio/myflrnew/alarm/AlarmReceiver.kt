package com.familyliferadio.myflrnew.alarm

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.content.WakefulBroadcastReceiver.startWakefulService
import android.util.Log
import android.widget.Toast
import com.familyliferadio.myflrnew.ProgramScheduleActivity
import com.familyliferadio.myflrnew.dto.AlarmDto
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by sonu on 09/04/17.
 */

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("AlarmReceiver", " AlarmReceiver obj ")
        Toast.makeText(context, "Alarm is Received ", Toast.LENGTH_LONG).show()
        var obj: AlarmDto = intent.getSerializableExtra("OBJ") as AlarmDto

        context.startService(Intent(context, AlarmSoundService::class.java))


    }

    private fun startnewActivity(context: Context) {
        context.startActivity(Intent(context, ProgramScheduleActivity::class.java))
    }

    private fun ringAlarm(context: Context, intent: Intent, alarmDto: AlarmDto) {
        val now = Calendar.getInstance()
        now.timeInMillis = System.currentTimeMillis()
        val day = now.get(Calendar.DAY_OF_WEEK)

        val list = getSelectedDays(alarmDto)
        if (list != null && list.contains(day) && alarmDto.isRepeat) {
            //Do things!
            Log.d("AlarmReceiver", " AlarmReceiver in if obj $day")
            context.startService(Intent(context, AlarmSoundService::class.java))

            //This will send a notification message and show notification in notification tray
            val comp = ComponentName(context.packageName,
                    AlarmNotificationService::class.java.name)
            startWakefulService(context, intent.setComponent(comp))

        } else if (list == null) {
            Toast.makeText(context, "list null ", Toast.LENGTH_SHORT).show()

            //Stop sound service to play sound for alarm
            context.startService(Intent(context, AlarmSoundService::class.java))

            //This will send a notification message and show notification in notification tray
            val comp = ComponentName(context.packageName,
                    AlarmNotificationService::class.java.name)
            startWakefulService(context, intent.setComponent(comp))

        }
    }

    private fun getSelectedDays(alarmDto: AlarmDto): ArrayList<Int>? {
        return if (alarmDto.monDay || alarmDto.tuesDay || alarmDto.wednessDay || alarmDto.thurseDay || alarmDto.friDay || alarmDto.saturDay || alarmDto.sunDay) {
            prepareSelectedDaysList(alarmDto)
        } else null
    }

    private fun prepareSelectedDaysList(alarmDto: AlarmDto): ArrayList<Int> {
        val selectedDays = ArrayList<Int>()
        addInList(selectedDays, alarmDto.monDay, Calendar.MONDAY)
        addInList(selectedDays, alarmDto.tuesDay, Calendar.TUESDAY)
        addInList(selectedDays, alarmDto.wednessDay, Calendar.WEDNESDAY)
        addInList(selectedDays, alarmDto.thurseDay, Calendar.THURSDAY)
        addInList(selectedDays, alarmDto.friDay, Calendar.FRIDAY)
        addInList(selectedDays, alarmDto.saturDay, Calendar.SATURDAY)
        addInList(selectedDays, alarmDto.sunDay, Calendar.SUNDAY)
        return selectedDays
    }

    fun addInList(list: ArrayList<Int>, isSelected: Boolean, day: Int) {
        if (isSelected) {
            list.add(day)
        }
    }
}

