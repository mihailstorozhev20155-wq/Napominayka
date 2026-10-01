package com.example.napominayka

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmHelper {
    fun schedule(context: Context, id: Long, at: Long, text: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pending(context,id,text)
        if (at <= System.currentTimeMillis()) return
        try {
            if (Build.VERSION.SDK_INT >= 31 && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }
    fun cancel(context: Context,id:Long) {
        val am=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(context,id,""))
    }
    private fun pending(context:Context,id:Long,text:String):PendingIntent {
        val i=Intent(context,ReminderReceiver::class.java).apply {
            putExtra("id",id); putExtra("text",text)
        }
        return PendingIntent.getBroadcast(context,id.toInt(),i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}


// Functional V1 helpers.
fun scheduleReminderSnooze(context: android.content.Context, id: Int, text: String, minutes: Int) =
    ReminderFeatureScheduler.scheduleSnooze(context, id, text, minutes)

fun scheduleReminderNextRepeat(context: android.content.Context, id: Int, text: String) =
    ReminderFeatureScheduler.scheduleNextRepeat(context, id, text)

// Functional V2 helpers.
fun snoozeReminder(context: android.content.Context,id:Int,text:String,minutes:Int)=ReminderActions.snooze(context,id,text,minutes)
fun acknowledgeReminder(context: android.content.Context,id:Int,text:String)=ReminderActions.acknowledge(context,id,text)
