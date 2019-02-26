package com.familyliferadio.myflrnew

import android.app.AlarmManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.TextView
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.adapter.AlarmAdapter
import com.familyliferadio.myflrnew.db.AlarmDatabase
import com.familyliferadio.myflrnew.dto.AlarmDto
import com.familyliferadio.myflrnew.utils.AlarmUtil
import com.familyliferadio.myflrnew.utils.RecyclerItemClickListener
import com.fastaccess.datetimepicker.callback.TimePickerCallback
import kotlinx.android.synthetic.main.activity_alarm.*


class AlarmActivity : AppCompatActivity(), TimePickerCallback {
    private lateinit var alarmList: MutableList<AlarmDto>
    private var alarmOBJ: AlarmDto? = null
    private lateinit var alarmManager: AlarmManager
    private var mAdapter: AlarmAdapter? = null
    private var isEdited: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        AlarmUtil.hideKeyBoard(this)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.elevation = 0f
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.title = "Alarm"
        }

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        initList()
        setUpList()

        addAlarm.setOnClickListener {
            alarmOBJ = AlarmDto()
            alarmOBJ!!.id = System.currentTimeMillis().toInt()
            Log.d("mAdapter", " in add new alarm ${System.currentTimeMillis()}" + alarmOBJ!!.id)

            val time = AlarmUtil.getCurrenTime()
            AlarmUtil.showTimePicker(AlarmUtil.getHrsOnly(time).toInt(), AlarmUtil.getMinsOnly(time).toInt(), supportFragmentManager)
        }

    }

    override fun onResume() {
        super.onResume()
        AlarmUtil.hideKeyBoard(this)

    }

    private fun initList() {
        alarmList = AlarmDatabase.getInstance(this@AlarmActivity).daoAccess().fetchAllAlarms()

    }


    private fun setUpList() {
        alarmRecycler?.apply {
            layoutManager = LinearLayoutManager(this@AlarmActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            mAdapter = AlarmAdapter(alarmList, this@AlarmActivity, alarmManager)
            adapter = mAdapter
            AlarmUtil.hideKeyBoard(this@AlarmActivity)

            addOnItemTouchListener(
                    RecyclerItemClickListener(this@AlarmActivity, RecyclerItemClickListener.OnItemClickListener { view, position ->
                        val timeText = view.findViewById<View>(R.id.timeText) as TextView
                        timeText.setOnClickListener {
                            alarmOBJ = mAdapter!!.alarmList[position]
                            isEdited = true
                            val time = AlarmUtil.parseTime(alarmOBJ!!.sheduleTime)
                            AlarmUtil.showTimePicker(AlarmUtil.getHrsOnly(time!!).toInt(), AlarmUtil.getHrsOnly(time).toInt(), supportFragmentManager)
                        }


                    }))
        }
    }


    override fun onTimeSet(timeOnly: Long, dateWithTime: Long) {
        Log.d("mAdapter", " onTimeSet in activity onTimeSet")
        val time = AlarmUtil.getTimeOnly(timeOnly)
        // timeText?.let { it.text=time.substringBefore(" ") }
        alarmOBJ?.let {
            if (isEdited) {
                it.sheduleTime = time
                //  AlarmUtil.setAlarm(this, it, alarmManager)
                Log.d("mAdapter", " onTimeSet in activity isEdited " + mAdapter!!.alarmList.indexOf(it))
                mAdapter!!.updateAlarm(it, mAdapter!!.alarmList.indexOf(it))
                isEdited = false
            } else {
                Log.d("mAdapter", " onTimeSet in new alar " + it.id)
                it.isAlarmSet = true
                it.sheduleTime = time
                mAdapter?.let { it.addAlarm(alarmOBJ!!) }
                AlarmUtil.setAlarm(this, it, alarmManager)
            }
        }
        AlarmUtil.hideKeyBoard(this@AlarmActivity)

    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }


}
