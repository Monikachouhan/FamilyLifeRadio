package com.familyliferadio.myflrnew.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.familyliferadio.myflrnew.fragment.TabFragment

/**
 * Created by ntf-19 on 7/9/18.
 */
class PagerAdapter(fm: FragmentManager, var tabCount: Int)//Initializing tab count
    : FragmentStatePagerAdapter(fm) {

    internal var title = arrayOf("Monday-Friday", "Saturday", "Sunday")

    override fun getItem(position: Int): Fragment? {
        return TabFragment.newInstance(title[position])
    }


    override fun getCount(): Int {
        return tabCount
    }

}