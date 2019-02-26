package com.familyliferadio.myflrnew

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.adapter.PagerAdapter


/**
 * Created by ntf-19 on 6/9/18.
 */
class ProgramScheduleActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {


    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.programeschedule_layout)


        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "Program Schedule"
            it.elevation = 0f
        }
        //sets tabs
        setUpTabs()
        //Set up vie
        // wPager and adapter
        setViewPager()


    }

    private fun setViewPager() {
        viewPager = findViewById<View>(R.id.pager) as ViewPager?
        //Creating our pager adapter
        val adapter = PagerAdapter(supportFragmentManager, tabLayout!!.tabCount)
        //swipe page with tab
        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        //Adding adapter to pager
        viewPager!!.adapter = adapter
    }

    private fun setUpTabs() {
        tabLayout = findViewById<View>(R.id.tabLayout) as TabLayout?
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Monday-Friday"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Saturday"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Sunday"))
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout!!.isSmoothScrollingEnabled = true
        tabLayout!!.setTabTextColors(Color.parseColor("#DCDADA"), Color.parseColor("#ffffff"));
        //Adding onTabSelectedListener to swipe views
        tabLayout!!.addOnTabSelectedListener(this)

    }

    override fun onTabReselected(p0: TabLayout.Tab?) {
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {
    }

    override fun onTabSelected(p0: TabLayout.Tab?) {
        if (p0 != null) {
            viewPager!!.setCurrentItem(p0.position, true)
        }; }


    private val dialog by lazy {
        ProgressDialog(this).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


}