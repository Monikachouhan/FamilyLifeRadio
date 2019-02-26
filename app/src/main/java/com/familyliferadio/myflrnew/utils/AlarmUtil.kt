package com.familyliferadio.myflrnew.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.alarm.MyAlarmReceiver
import com.familyliferadio.myflrnew.dto.AlarmDto
import com.fastaccess.datetimepicker.DateTimeBuilder
import com.fastaccess.datetimepicker.TimePickerFragmentDialog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object AlarmUtil {
    val time24Formate: String = "HH:mm"
    val time12Formate: String = "hh:mm a"


    @JvmStatic
    fun getTimeOnly(time: Long): String {
        val sample = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sample.format(time)
    }

    fun showTimePicker(hrs: Int, min: Int, supportFragmentManager: FragmentManager) {
        TimePickerFragmentDialog.newInstance(DateTimeBuilder.newInstance()
                .with24Hours(false)
                .withCurrentHour(hrs)
                .withCurrentMinute(min).withTheme(R.style.PickersTheme)
        ).show(supportFragmentManager, "TimePickerFragmentDialog");

    }

    fun setAlarm(context: Context, alarmDto: AlarmDto, alarmManager: AlarmManager) {
        //cancel alarm if exist
        cancelAlarm(alarmDto.id, context, alarmManager)

        val time = alarmDto.sheduleTime
        val alarmCalendar = Calendar.getInstance()

        val hrs = getHrsOnly(time)
        val min = getMinsOnly(time)
        val amOrpm = if (time.contains("AM", true)) Calendar.AM else Calendar.PM

        // alarmCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        alarmCalendar.set(Calendar.HOUR, hrs.toInt())
        alarmCalendar.set(Calendar.MINUTE, min.toInt())
        alarmCalendar.set(Calendar.SECOND, 0)
        alarmCalendar.set(Calendar.MILLISECOND, 0)
        alarmCalendar.set(Calendar.AM_PM, amOrpm)

        val now = Calendar.getInstance()
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        if (alarmCalendar.before(now)) {    //this condition is used for future reminder that means your reminder not fire for past time
            alarmCalendar.add(Calendar.DATE, 1)
        }

        val _id: Int = alarmDto.id
        val alarmTime = alarmCalendar.timeInMillis
        val alarmIntent = Intent(context, MyAlarmReceiver::class.java)
        alarmIntent.putExtra("ALARMID", _id)
        //   alarmIntent.setClass(context,AlarmReceiver::class.java)
        //  alarmIntent.putExtra("DAY", getDayString(dayOfWeek))
        Log.d("TabFragment", "setAlarm  $hrs ${alarmCalendar.time} $_id")
        val pendingIntent = PendingIntent.getBroadcast(context, _id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (alarmDto.isRepeat) {
            Log.d("TabFragment", "setAlarm is repeat ${alarmCalendar.time} ${alarmDto.id}")
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY, pendingIntent)
        } else {
            Log.d("TabFragment", "setAlarm is not repeat ${alarmCalendar.time} ${alarmDto.id}")
            when {
                Build.VERSION.SDK_INT >= 23 -> alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                Build.VERSION.SDK_INT >= 19 -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                else -> alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
            }

        }

    }

    fun hideKeyBoard(context: Activity) {
        // Check if no view has focus:
        val view = context.currentFocus
        if (view != null) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun cancelAlarm(alarmId: Int, context: Context, alarmManager: AlarmManager) {
        val myIntent = Intent(context,
                MyAlarmReceiver::class.java)
        myIntent.putExtra("ALARMID", alarmId)
        val pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    @SuppressLint("SimpleDateFormat")
    @JvmStatic
    fun formateTime(date: String, outFormat: String): String? {
        try {
            val dateFormat = SimpleDateFormat(outFormat)
            val dateObj = dateFormat.parse(date);
            return dateFormat.format(dateObj)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    fun parseTime(date: String): String? {
        try {
            val date1 = SimpleDateFormat(time12Formate, Locale.getDefault()).parse(date)
            val dateFormat = SimpleDateFormat(time24Formate, Locale.getDefault())
            Log.d("mAdapter", " parseTime $date1 $dateFormat ${dateFormat.format(date1)}")
            return dateFormat.format(date1)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    @JvmStatic
    fun getCurrenTime(): String {
        val df = SimpleDateFormat(time24Formate, Locale.getDefault())
        return df.format(Calendar.getInstance().time)
    }

    @JvmStatic
    fun getHrsOnly(time: String): String {
        return time.substringBefore(":")
    }


    @JvmStatic
    fun getMinsOnly(time: String): String {
        return time.substringAfter(":").substringBefore(" ")
    }

    @JvmStatic
    fun getAM_PM(time: String): String {
        return time.substringAfter(" ")
    }

}