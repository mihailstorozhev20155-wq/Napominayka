package com.example.napominayka

import android.content.Context
import java.util.Calendar

object ReminderFeatureStore {
    private const val PREFS = "reminder_features"
    enum class Repeat { NONE, DAILY, WEEKDAYS, WEEKLY, MONTHLY }

    private fun p(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun setRepeat(ctx: Context, id: Int, repeat: Repeat) {
        p(ctx).edit().putString("repeat_$id", repeat.name).apply()
    }

    fun getRepeat(ctx: Context, id: Int): Repeat =
        runCatching {
            Repeat.valueOf(p(ctx).getString("repeat_$id", Repeat.NONE.name)!!)
        }.getOrDefault(Repeat.NONE)

    fun setSnooze(ctx: Context, id: Int, minutes: Int) {
        p(ctx).edit().putInt("snooze_$id", minutes.coerceIn(1, 240)).apply()
    }

    fun getSnooze(ctx: Context, id: Int): Int =
        p(ctx).getInt("snooze_$id", 10).coerceIn(1, 240)

    fun remove(ctx: Context, id: Int) {
        p(ctx).edit()
            .remove("repeat_$id")
            .remove("snooze_$id")
            .apply()
    }

    fun nextOccurrence(now: Calendar, repeat: Repeat): Calendar? {
        val c = now.clone() as Calendar
        when (repeat) {
            Repeat.NONE -> return null
            Repeat.DAILY -> c.add(Calendar.DAY_OF_YEAR, 1)
            Repeat.WEEKLY -> c.add(Calendar.WEEK_OF_YEAR, 1)
            Repeat.MONTHLY -> c.add(Calendar.MONTH, 1)
            Repeat.WEEKDAYS -> {
                do {
                    c.add(Calendar.DAY_OF_YEAR, 1)
                } while (
                    c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                )
            }
        }
        return c
    }
}
