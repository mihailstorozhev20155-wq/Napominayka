package com.example.napominayka

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReminderHistory {
    private const val PREFS = "reminder_history"
    private const val KEY = "events"
    private const val MAX_EVENTS = 200
    data class Event(val id:Int,val text:String,val time:Long,val type:String)

    fun add(context:Context,id:Int,text:String,type:String){
        val prefs=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
        val old=runCatching{JSONArray(prefs.getString(KEY,"[]"))}.getOrDefault(JSONArray())
        val out=JSONArray()
        val start=maxOf(0,old.length()-MAX_EVENTS+1)
        for(i in start until old.length()) out.put(old.getJSONObject(i))
        out.put(JSONObject().apply{put("id",id);put("text",text);put("time",System.currentTimeMillis());put("type",type)})
        prefs.edit().putString(KEY,out.toString()).apply()
    }
    fun get(context:Context):List<Event>{
        val a=runCatching{JSONArray(context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(KEY,"[]"))}.getOrDefault(JSONArray())
        val r=mutableListOf<Event>()
        for(i in a.length()-1 downTo 0){val o=a.optJSONObject(i)?:continue;r+=Event(o.optInt("id"),o.optString("text"),o.optLong("time"),o.optString("type"))}
        return r
    }
    fun clear(context:Context){context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().remove(KEY).apply()}
    fun formatTime(time:Long):String=SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(Date(time))
}
