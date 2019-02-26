package com.familyliferadio.myflrnew.adapter

import android.app.AlarmManager
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.familyliferadio.myflrnew.AlarmActivity
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.db.AlarmDatabase
import com.familyliferadio.myflrnew.dto.AlarmDto
import com.familyliferadio.myflrnew.utils.AlarmUtil
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup
import kotlinx.android.synthetic.main.alarm_item_view.view.*

/**
 * Created by ntf-19 on 6/9/18.
 */
class AlarmAdapter(val alarmList: MutableList<AlarmDto>, val context: Context, var alarmManager: AlarmManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val contentItemView = LayoutInflater.from(parent.context).inflate(
                R.layout.alarm_item_view, parent, false)
        return ViewHolder(contentItemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemsData = alarmList[position]
        val viewHolder = holder as ViewHolder
        viewHolder.apply {
            monDay.isChecked = itemsData.monDay
            alamTimeText.text = itemsData.sheduleTime.substringBefore(" ")
            am_pmText.text = itemsData.sheduleTime.substringAfter(" ")
            tuesDay.isChecked = itemsData.tuesDay
            wedDay.isChecked = itemsData.wednessDay
            thursDay.isChecked = itemsData.thurseDay
            friDay.isChecked = itemsData.friDay
            saturday.isChecked = itemsData.saturDay
            sunday.isChecked = itemsData.sunDay
            repeatCheck.isChecked = itemsData.isRepeat
            alarmSwitch.isChecked = itemsData.isAlarmSet
            if (itemsData.msg != "")
                alarmMsgText.setText(itemsData.msg)

            updateAlarmTextColor(alarmSwitch.isChecked, alamTimeText, am_pmText)
            weekDaysGroupVisibility(repeatCheck.isChecked, weekDaysGroup)

            AlarmUtil.hideKeyBoard(context = context as AlarmActivity)
            alarmMsgText.afterTextChanged {
                itemsData.msg = it
                updateLabel(itemsData, position)
            }

            this.itemView.setOnClickListener(null)
            //Click Events
            deleteBTN.setOnClickListener {
                removeAlarm(position)
            }

            expandBtn.setOnClickListener {
                if (!expandable_layout.isExpanded) {
                    expandable_layout.expand(true)
                }
                expandaleVisibility(expandable_layout.isExpanded, collapseLay)

            }
            collapsBtn.setOnClickListener {
                if (expandable_layout.isExpanded) {
                    expandable_layout.collapse(true)
                }
                expandaleVisibility(expandable_layout.isExpanded, collapseLay)
            }
            repeatCheck.setOnClickListener {
                weekDaysGroupVisibility(repeatCheck.isChecked, weekDaysGroup)
                itemsData.isRepeat = repeatCheck.isChecked
                updateDaysData(itemsData, position)
            }
            alarmSwitch.setOnClickListener {
                itemsData.isAlarmSet = alarmSwitch.isChecked
                // updateTrackColor(alarmSwitch.isChecked,alarmSwitch)
                updateAlarm(itemsData, position)
            }
            weekDaysGroup.setOnCheckedChangeListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.mon -> {
                        itemsData.monDay = isChecked
                        updateDaysData(itemsData, position)
                    }
                    R.id.tues -> {
                        itemsData.tuesDay = isChecked
                        updateDaysData(itemsData, position)
                    }
                    R.id.wed -> {

                        itemsData.wednessDay = isChecked
                        updateDaysData(itemsData, position)
                    }
                    R.id.thurse -> {
                        itemsData.thurseDay = isChecked
                        updateDaysData(itemsData, position)
                    }
                    R.id.fri -> {
                        itemsData.friDay = isChecked
                        updateDaysData(itemsData, position)
                    }
                    R.id.satur -> {
                        itemsData.saturDay = isChecked
                        updateDaysData(itemsData, position)
                    }
                    R.id.sun -> {
                        itemsData.sunDay = isChecked
                        updateDaysData(itemsData, position)
                    }
                }
            }
        }


    }

    //extention Funtion
    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun weekDaysGroupVisibility(isChecked: Boolean, weekDaysGroup: MultiSelectToggleGroup) {
        if (isChecked) weekDaysGroup.visibility = View.VISIBLE else weekDaysGroup.visibility = View.GONE
    }

    private fun expandaleVisibility(isExpanded: Boolean, linearLayout: LinearLayout) {
        if (!isExpanded) linearLayout.visibility = View.VISIBLE else linearLayout.visibility = View.GONE
    }

    private fun updateAlarmTextColor(isAlrmOn: Boolean, timeText: TextView, am_pm: TextView) {
        if (isAlrmOn) {
            timeText.setTextColor(Color.WHITE)
            am_pm.setTextColor(Color.WHITE)
        } else {
            timeText.setTextColor(Color.parseColor("#FFC7C7C7"))
            am_pm.setTextColor(Color.parseColor("#FFC7C7C7"))

        }

    }

    fun addAlarm(alarmDto: AlarmDto) {
        alarmList.add(alarmDto)
        var id = AlarmDatabase.getInstance(context).daoAccess().insertAlarmData(alarmDto)
        Log.d("AlarmActivity", "adapter addAlarm id $id")

        this.notifyDataSetChanged()
    }

    fun updateAlarm(alarmDto: AlarmDto, position: Int) {
        try {
            alarmList[position] = alarmDto
            AlarmDatabase.getInstance(context).daoAccess().updateAlarmData(alarmDto)
            AlarmUtil.setAlarm(context, alarmDto, alarmManager)
            notifyItemChanged(position)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
        Log.d("AlarmActivity", "adapter updateAlarm ")

    }

    private fun updateDaysData(alarmDto: AlarmDto, position: Int) {
        alarmList[position] = alarmDto
        AlarmDatabase.getInstance(context).daoAccess().updateAlarmData(alarmDto)
        //  Log.d("AlarmActivity", "adapter updateAlarm ")
       AlarmUtil.setAlarm(context, alarmDto, alarmManager)

    }
    private fun updateLabel(alarmDto: AlarmDto, position: Int) {

        AlarmDatabase.getInstance(context).daoAccess().updateAlarmData(alarmDto)
        //  Log.d("AlarmActivity", "adapter updateAlarm ")
       //AlarmUtil.setAlarm(context, alarmDto, alarmManager)

    }

    private fun removeAlarm(position: Int) {
        AlarmDatabase.getInstance(context).daoAccess().deleteAlarmByObj(alarmList[position])
        alarmList.removeAt(position)
        notifyDataSetChanged()
    }

    fun updateTrackColor(isChecked: Boolean, switch: Switch) {
        if (isChecked) {
            switch.trackDrawable.setColorFilter(ContextCompat.getColor(context, android.R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            switch.trackDrawable.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_light), PorterDuff.Mode.SRC_IN);
        }
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val alarmMsgText = view.alarmLable!!
        val alamTimeText = view.timeText!!
        val am_pmText = view.timeStatus!!
        val monDay = view.mon!!
        val tuesDay = view.tues!!
        val wedDay = view.wed!!
        val thursDay = view.thurse!!
        val friDay = view.fri!!
        val saturday = view.satur!!
        val sunday = view.sun!!
        val repeatCheck = view.repeatCheckBox!!
        val alarmSwitch = view.alarmSwitch!!
        val deleteBTN = view.deleteBTN!!
        val collapseLay = view.collapseLayout!!
        val expandBtn = view.expandBtn!!
        val collapsBtn = view.collapsBtn!!
        val weekDaysGroup = view.group_weekdays!!
        val expandable_layout = view.expandable_layout!!

init {
    alarmMsgText.clearFocus()
}
    }

}