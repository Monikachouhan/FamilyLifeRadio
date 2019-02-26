package com.familyliferadio.myflrnew.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.familyliferadio.myflrnew.db.AlarmDatabase
import com.familyliferadio.myflrnew.dto.AlarmDto
import java.util.*
import com.familyliferadio.myflrnew.PlayerActivity




class MyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
          //Toast.makeText(context, "Alarm is Received ", Toast.LENGTH_LONG).show()
        val alarmId = intent!!.getIntExtra("ALARMID", 0)
        Log.d("AlarmReceiver", " AlarmReceiver onReceive obj $alarmId")
        //  context!!.startService(Intent(context, AlarmSoundService::class.java))
        alarmId.let {
            AlarmDatabase.getInstance(context)
                    .daoAccess()
                    .getByID(alarmId)?.let {
                        if (it.isAlarmSet) {
                            Log.d("AlarmReceiver", " AlarmReceiver it.isAlarmSet obj ")
                            ringAlarm(context!!, it)
                        } else {
                            Log.d("AlarmReceiver", " AlarmReceiver it.isAlarmSet not set obj")

                        }
                    }
        }

    }

    private fun ringAlarm(context: Context, alarmDto: AlarmDto) {
        val now = Calendar.getInstance()
        now.timeInMillis = System.currentTimeMillis()
        val day = now.get(Calendar.DAY_OF_WEEK)

        Log.d("AlarmReceiver", " list upper ${alarmDto.isRepeat}")

        if (alarmDto.isRepeat) {
            val list = getSelectedDays(alarmDto)
            Log.d("AlarmReceiver", " list $list $day ${alarmDto.isRepeat} ${list!!.contains(day)}")
            if (list != null && list.contains(day)) {
                Log.d("AlarmReceiver", " list inner if " + list.contains(day))
                performAction(context)
            }
        } else {
            performAction(context)
            Log.d("AlarmReceiver", " list else obj ")

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

    fun performAction(context: Context?) {
    //    context!!.startService(Intent(context, AlarmSoundService::class.java))
        val i = Intent()
        i.setClassName(context!!.packageName, PlayerActivity::class.java.name)
       // i.setClassName("com.familyliferadio.myflrnew", "com.familyliferadio.myflrnew.PlayerActivity")
        i.putExtra("isAlarm", true)
          i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
          i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)

    }

}