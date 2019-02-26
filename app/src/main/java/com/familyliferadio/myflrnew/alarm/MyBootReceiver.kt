package com.familyliferadio.myflrnew.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.Time
import android.util.Log
import com.familyliferadio.myflrnew.db.AlarmDatabase
import com.familyliferadio.myflrnew.utils.AlarmUtil

class MyBootReceiver : BroadcastReceiver() {

    var alarmDatabase: AlarmDatabase? = null
    private lateinit var alarmManager: AlarmManager

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        // Toast.makeText(context,"Device Rebooted ",Toast.LENGTH_LONG).show()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager //get instance of alarm manager
        val alarmList = AlarmDatabase.getInstance(context)
                .daoAccess()
                .fetchAllAlarms()

        alarmList.forEach {
            if (it.isAlarmSet)
            {
                AlarmUtil.setAlarm(context,it,alarmManager)
            }
        }


    }

    companion object {
        private val TAG = "MyBootReceiver"
    }


}