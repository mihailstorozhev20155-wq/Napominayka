package com.example.napominayka

import android.content.Context

object ReminderActions {
    fun snooze(context:Context,id:Int,text:String,minutes:Int){
        ReminderFeatureStore.setSnooze(context,id,minutes)
        ReminderFeatureScheduler.scheduleSnooze(context,id,text,minutes)
        ReminderHistory.add(context,id,text,"snooze_$minutes")
    }
    fun acknowledge(context:Context,id:Int,text:String){ReminderHistory.add(context,id,text,"acknowledged")}
    fun skipped(context:Context,id:Int,text:String){ReminderHistory.add(context,id,text,"skipped")}
    fun repeatLabel(context:Context,id:Int):String=when(ReminderFeatureStore.getRepeat(context,id)){
        ReminderFeatureStore.Repeat.NONE->"Не повторять"
        ReminderFeatureStore.Repeat.DAILY->"Каждый день"
        ReminderFeatureStore.Repeat.WEEKDAYS->"По будням"
        ReminderFeatureStore.Repeat.WEEKLY->"Каждую неделю"
        ReminderFeatureStore.Repeat.MONTHLY->"Каждый месяц"
    }
}
