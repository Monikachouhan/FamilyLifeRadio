package com.familyliferadio.myflrnew.alarm

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

import com.familyliferadio.myflrnew.R

/**
 * Created by sonu on 10/04/17.
 */

class AlarmSoundService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        //Start media player
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        mediaPlayer!!.start()
        mediaPlayer!!.isLooping = true//set looping true to run it infinitely
    }

    override fun onDestroy() {
        super.onDestroy()

        //On destory stop and release the media player
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
        }
    }
}
