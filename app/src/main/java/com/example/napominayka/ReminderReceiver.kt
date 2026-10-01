package com.example.napominayka

import android.app.*
import android.content.*
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("id", 0L).toInt()
        val message=intent.getStringExtra("text") ?: "У тебя есть напоминание!"
        val open=Intent(context,ReminderActivity::class.java).apply{
            putExtra("text",message)
            putExtra("id",id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val full=PendingIntent.getActivity(context,id,open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val nm=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel=NotificationChannel("reminders","Напоминания",NotificationManager.IMPORTANCE_HIGH).apply{
            description="Напоминания приложения"
            lockscreenVisibility=Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(channel)
        val settings = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val vibrationOn = settings.getBoolean("vibration", true)
        val soundOn = settings.getBoolean("sound", true)

        val builder = Notification.Builder(context, "reminders")
            .setSmallIcon(com.example.napominayka.R.drawable.ic_notification)
            .setContentTitle("Напоминайка")
            .setContentText(message)
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(Notification.PRIORITY_MAX)
            .setAutoCancel(true)
            .setFullScreenIntent(full, true)
            .setContentIntent(full)

        if (!soundOn) {
            builder.setSound(null)
        }
        if (vibrationOn) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE)
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        val n = builder.build()
        nm.notify(id.toInt(), n)

        val featureId = intent.getIntExtra("id", 0)
        val featureText = intent.getStringExtra("text") ?: "У тебя есть напоминание!"
        ReminderHistory.add(context, featureId, featureText, "fired")
        if (!intent.getBooleanExtra("snooze", false)) {
            ReminderFeatureScheduler.scheduleNextRepeat(context, featureId, featureText)
        }

    }
}
