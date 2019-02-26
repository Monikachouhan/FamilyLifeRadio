package com.familyliferadio.myflrnew.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.adapter.ProgScheduleAdapter
import com.familyliferadio.myflrnew.dto.ProgramData
import com.familyliferadio.myflrnew.alarm.AlarmReceiver
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.*

/**
 * Created by ntf-19 on 7/9/18.
 */
class TabFragment : Fragment() {

    lateinit var title: String
    private lateinit var itemlist: ArrayList<ProgramData>
    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmManager: AlarmManager



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view1: View = inflater.inflate(R.layout.tab_layout, container, false)
        title = arguments!!.getString("title")!!
        dialog.show()

        itemlist = ArrayList()
        recyclerView = view1.findViewById<View>(R.id.recyclerview) as RecyclerView
        getData()

        return view1
    }

    private fun getDayOFWeek(key: String): Int {
        if (key.equals("sunday", true)) return Calendar.SUNDAY
        if (key.equals("saturday", true)) return Calendar.SATURDAY
        return -1
    }



    companion object {
        @JvmStatic
        fun newInstance(title: String): TabFragment {
            val args = Bundle()
            val fragment = TabFragment()
            args.putString("title", title)
            fragment.arguments = args
            return fragment
        }

    }

    private val dialog by lazy {
        ProgressDialog(activity).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }
    }

    private fun getData() {
        val query = ParseQuery<ParseObject>("Shedule_Task")
        query.findInBackground { list, e ->
            if (e == null) {
                if (list.isNotEmpty()) {
                    val dataList = list.filter { it ->
                        it.getString("Date").equals(title, true)
                    }
                    dataList.forEach {
                        val progTitle = it.getString("Program")
                        val progImage = it.getParseFile("image").url
                        val sheduledTime = it.getString("shedule_time")
                        val progDiscrip = it.getString("Program_dec")
                         val programDataObj = ProgramData(progTitle, sheduledTime, progImage, progDiscrip)
                        itemlist.add(programDataObj)

                    }
                    recyclerView.layoutManager = LinearLayoutManager(activity)
                    recyclerView.itemAnimator = DefaultItemAnimator()
                    dialog.dismiss()
                    recyclerView.adapter = activity?.let { ProgScheduleAdapter(itemlist, it) }
                }

            }
        }
    }

    private fun setAlarm(dayOfWeek: Int, programData: ProgramData) {
        // Add this day of the week line to your existing code

        val alarmCalendar = Calendar.getInstance()

        val hrs = programData.time.substringBefore(":").replace(" ", "")
        val min = programData.time.substringAfter(":").substringBefore(" ")
        val amOrpm = if (programData.time.contains("AM", true)) Calendar.AM else Calendar.PM

        alarmCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        alarmCalendar.set(Calendar.HOUR, hrs.toInt())
        alarmCalendar.set(Calendar.MINUTE, min.toInt())
        alarmCalendar.set(Calendar.SECOND, 0)
        alarmCalendar.set(Calendar.MILLISECOND, 0)
        alarmCalendar.set(Calendar.AM_PM, amOrpm)

        val now = Calendar.getInstance()
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        if (alarmCalendar.before(now)) {    //this condition is used for future reminder that means your reminder not fire for past time
            alarmCalendar.add(Calendar.DATE, 7)
        }

        val _id: Int = System.currentTimeMillis().toInt()
        val alarmTime = alarmCalendar.timeInMillis
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        alarmIntent.putExtra("TITLE", programData.programTitle)
      //  alarmIntent.putExtra("DAY", getDayString(dayOfWeek))
        Log.d("TabFragment", "setAlarm  $hrs ${alarmCalendar.time} $amOrpm")
        val pendingIntent = PendingIntent.getBroadcast(context, _id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY * 7, pendingIntent)

       // programData.intentId = _id
     //   insertProgramInDB(programData)

    }


    private fun setMonToFriAlarm(dayOfWeek: Int, programData: ProgramData) {
        // Add this day of the week line to your existing code

        val alarmCalendar = Calendar.getInstance()

        val hrs = programData.time.substringBefore(":").replace(" ", "")
        val min = programData.time.substringAfter(":").substringBefore(" ")
        val amOrpm = if (programData.time.contains("AM", true)) Calendar.AM else Calendar.PM

        alarmCalendar.set(Calendar.HOUR, hrs.toInt())
        alarmCalendar.set(Calendar.MINUTE, min.toInt())
        alarmCalendar.set(Calendar.SECOND, 0)
        alarmCalendar.set(Calendar.MILLISECOND, 0)
        alarmCalendar.set(Calendar.AM_PM, amOrpm)

        val _id: Int = System.currentTimeMillis().toInt()
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        alarmIntent.putExtra("TITLE", programData.programTitle)
        //alarmIntent.putExtra("DAY", getDayString(dayOfWeek))
        alarmIntent.putExtra("OBJ", programData)

        val pendingIntent = PendingIntent.getBroadcast(context, _id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        val now = Calendar.getInstance()
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        if (alarmCalendar.before(now)) {    //this condition is used for future reminder that means your reminder not fire for past time
            alarmCalendar.add(Calendar.DATE, 1)
        }

        val alarmTime = alarmCalendar.timeInMillis
        Log.d("TabFragment", "setMon2FriAlarm i $hrs ${alarmCalendar.time} $amOrpm")
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY, pendingIntent)


     //   programData.intentId = _id
     //   insertProgramInDB(programData)

    }



   /* private fun getMon2FriObj(dayId: Int, intentId: Int, obj: MonToFridayEntity): MonToFridayEntity {
        when (dayId) {
            2 -> {
                obj.monId = intentId
            }
            3 -> {
                obj.tuesId = intentId
            }
            4 -> {
                obj.wedId = intentId
            }
            5 -> {
                obj.thursId = intentId
            }
            6 -> {
                obj.friId = intentId
            }
        }
        return obj
    }


    private fun getDayString(dayId: Int): String {
        return when (dayId) {
            2 -> {
                "Monday"
            }
            3 -> {
                "Tuesday"
            }
            4 -> {
                "Wednesday"
            }
            5 -> {
                "Thursday"
            }
            6 -> {
                "Friday"
            }
            7 -> {
                "Saturday"
            }
            1 -> {
                "Sunday"
            }
            else -> {
                ""
            }
        }
    }*/
}