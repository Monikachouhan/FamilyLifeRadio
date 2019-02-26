package com.familyliferadio.myflrnew.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


/**
 * Created by ntf-19 on 13/9/18.
 */

class ActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();

        val action = intent.getStringExtra("action")
        Log.d("bharti", "value action$action")
        if (action != null)
            if (action.equals("Stop")) {
                context.stopService(Intent(context, AlarmSoundService::class.java))

                // If you want to cancel the notification:
                //alarmNotificationManager!!.cancel(NOTIFICATION_ID)
                val alarmNotificationManager = context
                        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                alarmNotificationManager.cancel(AlarmNotificationService.NOTIFICATION_ID)
                //  }

            }
        /*else if(action.equals("action2")){
            performAction2();

        }*/
        //This is used to close the notification tray
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context.sendBroadcast(it)
    }


}