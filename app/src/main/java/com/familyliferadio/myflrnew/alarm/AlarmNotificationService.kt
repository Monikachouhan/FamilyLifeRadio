package com.familyliferadio.myflrnew.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.JobIntentService
import android.support.v4.app.NotificationCompat
import com.familyliferadio.myflrnew.PlayerActivity
import com.familyliferadio.myflrnew.ProgramScheduleActivity
import com.familyliferadio.myflrnew.R


class AlarmNotificationService : JobIntentService() {

    private var alarmNotificationManager: NotificationManager? = null
    private val PRIMARY_CHANNEL = "PRIMARY_CHANNEL_ID"
    private val PRIMARY_CHANNEL_NAME = "PRIMARY"

    override fun onHandleWork(p0: Intent) {
        val content = getString(R.string.app_name)

        //Send notification
        sendNotification(content.toString())
    }


    //handle notification
    private fun sendNotification(msg: String) {
        alarmNotificationManager = this
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //get pending intent
        val contentIntent1 = PendingIntent.getActivity(this, 0,
                Intent(this, ProgramScheduleActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        val stopSoundIntent = Intent(this, ActionReceiver::class.java)
        val playerIntent = Intent(this, PlayerActivity::class.java)
        val playerPnendingIntent = PendingIntent.getActivity(this, 1100,
                playerIntent, PendingIntent.FLAG_UPDATE_CURRENT)



        stopSoundIntent.putExtra("action", "Stop")
        //get pending intent
        val contentIntent = PendingIntent.getBroadcast(this, 0,
                stopSoundIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(channel)
        }
        val alamNotificationBuilder = NotificationCompat.Builder(this, PRIMARY_CHANNEL)
                .setContentTitle("Alarm")
                .setSmallIcon(R.drawable.ic_alarm_on)
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setOngoing(true)
                .setLargeIcon(largeIcon)
                .setAutoCancel(false)
                .addAction(NotificationCompat.Action(R.drawable.ic_close_black_30dp,
                        "Dismiss", contentIntent))
        alamNotificationBuilder.setContentIntent(contentIntent)


        //notiy notification manager about new notification
        alarmNotificationManager!!.notify(NOTIFICATION_ID, alamNotificationBuilder.build())



    }

    companion object {

        //Notification ID for Alarm
        val NOTIFICATION_ID = 1
    }


}
