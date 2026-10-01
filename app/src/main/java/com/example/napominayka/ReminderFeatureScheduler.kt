package com.example.napominayka

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderFeatureScheduler {
    const val ACTION_SNOOZE = "com.example.napominayka.SNOOZE"

    fun scheduleSnooze(context: Context, id: Int, text: String, minutes: Int) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("id", id)
            putExtra("text", text)
            putExtra("snooze", true)
        }
        val pi = PendingIntent.getBroadcast(
            context, id.coerceAtLeast(1), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val at = System.currentTimeMillis() + minutes.coerceIn(1, 240) * 60_000L
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }

    fun scheduleNextRepeat(context: Context, id: Int, text: String) {
        val repeat = ReminderFeatureStore.getRepeat(context, id)
        val next = ReminderFeatureStore.nextOccurrence(Calendar.getInstance(), repeat) ?: return
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("text", text)
            putExtra("repeat", true)
        }
        val pi = PendingIntent.getBroadcast(
            context, id.coerceAtLeast(1), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pi)
    }
}
