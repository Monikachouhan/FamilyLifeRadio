package com.familyliferadio.myflrnew

import android.app.Application
import com.parse.Parse

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Parse.initialize(this)
    }
}