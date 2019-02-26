package com.familyliferadio.myflrnew

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.familyliferadio.myflrnew.R
import com.familyliferadio.myflrnew.utils.ConnectionDetector
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnListenNow.setOnClickListener {
            if (ConnectionDetector.getInstance(this).isConnectingToInternet) {
                startActivity(Intent(this, PlayerActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "No internet connection ", Toast.LENGTH_SHORT).show();
            }
        }

        btnSchedule.setOnClickListener {
            if (ConnectionDetector.getInstance(this).isConnectingToInternet) {
                startActivity(Intent(this, ProgramScheduleActivity::class.java))
            } else {
                Toast.makeText(this, "No internet connection ", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
