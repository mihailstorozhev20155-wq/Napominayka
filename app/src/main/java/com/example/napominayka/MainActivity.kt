package com.example.napominayka

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.*
import android.graphics.*
import android.os.*
import android.provider.Settings
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

private const val W0 = 484f
private const val H0 = 1024f
private val CREAM = Color.rgb(255,249,233)
private val ORANGE = Color.rgb(244,103,28)
private val GREEN = Color.rgb(110,173,82)
private val DARK = Color.rgb(24,24,24)

private data class SettingsRow(val title: String, val value: String, val action: () -> Unit)

class MainActivity : AppCompatActivity() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val prefs by lazy { getSharedPreferences("reminders", MODE_PRIVATE) }
    private lateinit var view: HomeView
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = CREAM
        window.navigationBarColor = CREAM
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        seedSamples()
        view = HomeView(this)
        setContentView(view)
    }

    override fun onResume() { super.onResume(); if (::view.isInitialized) view.invalidate() }

    private fun seedSamples() {
        if (prefs.contains("seeded")) return
        val cal = Calendar.getInstance()
        fun t(day:Int,h:Int,m:Int):Long { cal.set(2025, Calendar.MAY, day, h, m, 0); cal.set(Calendar.MILLISECOND,0); return cal.timeInMillis }
        val e = prefs.edit()
        val data = listOf(
            Triple("Прочитать книгу", t(25,20,30), true),
            Triple("Купить продукты", t(26,18,0), true),
            Triple("Тренировка", t(27,7,0), true),
            Triple("Оплатить интернет", t(27,12,0), false)
        )
        e.putInt("count", data.size)
        data.forEachIndexed { i, v -> e.putLong("id_$i", 1000L+i).putString("text_$i",v.first).putLong("time_$i",v.second).putBoolean("enabled_$i",v.third) }
        e.putBoolean("seeded", true).apply()
    }

    private inner class HomeView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val illustration = BitmapFactory.decodeResource(resources, R.drawable.concept_home_illustration)
        private val mascotIcon = BitmapFactory.decodeResource(resources, R.drawable.mascot_face)
        private val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(CREAM)
            val sx = width / W0
            val sy = height / H0
            canvas.save(); canvas.scale(sx, sy)
            drawHome(canvas)
            canvas.restore()
        }

        private fun drawHome(c: Canvas) {
            text(c,"Напоминайка",46f,52f,30f,DARK,true)
            text(c,"Я напомню тебе вовремя 😊",45f,82f,20f,DARK,false)
            drawGear(c,438f,57f)

            round(c,44f,113f,454f,168f,30f,ORANGE)
            textCenter(c,"＋  Добавить напоминание",249f,148f,20f,Color.WHITE,true)

            val count = prefs.getInt("count",0)
            if (count == 0) {
                textCenter(c,"Пока нет напоминаний",242f,265f,20f,DARK,false)
            } else {
                var y = 191f
                val shown = minOf(count, 8)
                for (i in 0 until shown) {
                    drawCard(c,i,y)
                    y += 101f
                }
                val illY = if (count <= 4) 620f else y + 12f
                drawBitmapFit(c, illustration, 0f, illY, 484f, 404f)
            }
        }

        private fun drawCard(c:Canvas,i:Int,y:Float) {
            val text = prefs.getString("text_$i","Напоминание") ?: "Напоминание"
            val whenMs = prefs.getLong("time_$i",0L)
            val enabled = prefs.getBoolean("enabled_$i",true)
            roundStroke(c,35f,y,461f,y+91f,14f,Color.rgb(255,253,246),Color.rgb(224,218,205),1.5f)
            val iconBg = when(i) {0->Color.rgb(249,220,229);1->Color.rgb(251,227,205);2->Color.rgb(224,224,244);3->Color.rgb(222,221,236);else->Color.rgb(236,230,214)}
            circle(c,75f,y+45f,31f,iconBg)
            c.drawBitmap(mascotIcon,null,RectF(47f,y+17f,103f,y+73f),paint)
            text(c,text,125f,y+37f,17f,DARK,true)
            text(c,"▣  ${fmt.format(Date(whenMs))}",126f,y+67f,13f,Color.DKGRAY,false)
            text(c,"◷  ${timeFmt.format(Date(whenMs))}",258f,y+67f,13f,Color.DKGRAY,false)
            drawSwitch(c,417f,y+28f,enabled)
            drawTrash(c,434f,y+68f)
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            if (e.action != MotionEvent.ACTION_UP) return true
            val x=e.x/(width/W0); val y=e.y/(height/H0)
            if (x in 40f..458f && y in 108f..174f) {
                startActivity(Intent(this@MainActivity, ReminderActivity::class.java)); return true
            }
            val count=prefs.getInt("count",0)
            for(i in 0 until minOf(count,8)) {
                val top=191f+i*101f
                // Switch
                if (x in 385f..458f && y in top+8f..top+59f) {
                    val id=prefs.getLong("id_$i",i.toLong()); val checked=!prefs.getBoolean("enabled_$i",true)
                    prefs.edit().putBoolean("enabled_$i",checked).apply()
                    if (checked) AlarmHelper.schedule(this@MainActivity,id,prefs.getLong("time_$i",0),prefs.getString("text_$i","") ?: "") else AlarmHelper.cancel(this@MainActivity,id)
                    invalidate(); return true
                }
                // Trash
                if (x in 410f..455f && y in top+58f..top+91f) { deleteAt(i); return true }
                // Card body → edit
                if (x in 35f..380f && y in top..top+91f) {
                    val intent = Intent(this@MainActivity, ReminderActivity::class.java)
                    intent.putExtra("edit_index", i)
                    startActivity(intent)
                    return true
                }
            }
            if (x>410f && y<90f) showSettings()
            return true
        }

        private fun deleteAt(index:Int) {
            val count=prefs.getInt("count",0); if(index !in 0 until count)return
            AlarmHelper.cancel(this@MainActivity,prefs.getLong("id_$index",index.toLong()))
            val e=prefs.edit()
            for(i in index until count-1){
                e.putLong("id_$i",prefs.getLong("id_${i+1}",0))
                e.putString("text_$i",prefs.getString("text_${i+1}","") ?: "")
                e.putLong("time_$i",prefs.getLong("time_${i+1}",0))
                e.putBoolean("enabled_$i",prefs.getBoolean("enabled_${i+1}",false))
            }
            e.remove("id_${count-1}").remove("text_${count-1}").remove("time_${count-1}").remove("enabled_${count-1}").putInt("count",count-1).apply()
            invalidate()
        }
    }

    private fun iconFor(s:String)=when {
        s.contains("книг",true)||s.contains("читать",true)->"📖"
        s.contains("куп",true)||s.contains("продукт",true)->"🛍"
        s.contains("трен",true)||s.contains("спорт",true)->"🏋"
        s.contains("интернет",true)->"💻"
        else->"🔔"
    }
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=DARK,bold:Boolean=false){ paintText(c,s,x,y,size,color,bold) }
    private fun paintText(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int,bold:Boolean){ paint.style=Paint.Style.FILL;paint.color=color;paint.textSize=size;paint.typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL);c.drawText(s,x,y,paint) }
    private fun textCenter(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int,bold:Boolean){paintText(c,s,x-paint.measureText(s)/2f,y,size,color,bold)}
    private fun round(c:Canvas,l:Float,t:Float,r:Float,b:Float,rad:Float,color:Int){paint.style=Paint.Style.FILL;paint.color=color;c.drawRoundRect(l,t,r,b,rad,rad,paint)}
    private fun roundStroke(c:Canvas,l:Float,t:Float,r:Float,b:Float,rad:Float,fill:Int,stroke:Int,sw:Float){round(c,l,t,r,b,rad,fill);paint.style=Paint.Style.STROKE;paint.strokeWidth=sw;paint.color=stroke;c.drawRoundRect(l,t,r,b,rad,rad,paint)}
    private fun circle(c:Canvas,x:Float,y:Float,r:Float,color:Int){paint.style=Paint.Style.FILL;paint.color=color;c.drawCircle(x,y,r,paint)}
    private fun drawSwitch(c:Canvas,x:Float,y:Float,on:Boolean){round(c,x-27f,y-15f,x+27f,y+15f,17f,if(on)GREEN else Color.rgb(205,203,194));circle(c,x+(if(on)10f else -10f),y,11f,Color.WHITE)}
    private fun drawTrash(c:Canvas,x:Float,y:Float){paint.style=Paint.Style.STROKE;paint.strokeWidth=2.5f;paint.color=DARK;c.drawRect(x-6,y-6,x+6,y+8,paint);c.drawLine(x-9,y-10,x+9,y-10,paint);c.drawLine(x-4,y-14,x+4,y-14,paint)}
    private fun drawGear(c:Canvas,x:Float,y:Float){paint.style=Paint.Style.STROKE;paint.strokeWidth=3f;paint.color=DARK;c.drawCircle(x,y,13f,paint);c.drawCircle(x,y,5f,paint);for(i in 0 until 8){val a=Math.toRadians(i*45.0);c.drawLine(x+(Math.cos(a)*14).toFloat(),y+(Math.sin(a)*14).toFloat(),x+(Math.cos(a)*19).toFloat(),y+(Math.sin(a)*19).toFloat(),paint)}}
    private fun drawBitmapFit(c:Canvas,b:Bitmap,l:Float,t:Float,w:Float,h:Float){c.drawBitmap(b,null,RectF(l,t,l+w,t+h),paint)}


    private fun showSettings() {
        setContentView(SettingsView(this))
    }

    private inner class SettingsView(context: android.content.Context) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private val settingsPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        private var exact = if (Build.VERSION.SDK_INT >= 31)
            getSystemService(AlarmManager::class.java).canScheduleExactAlarms() else true
        private var defaultSnooze = settingsPrefs.getInt("default_snooze", 10)
        private var vibration = settingsPrefs.getBoolean("vibration", true)
        private var sound = settingsPrefs.getBoolean("sound", true)
        private var keepScreen = settingsPrefs.getBoolean("keep_screen_on", true)

        private fun rows(): List<SettingsRow> = listOf(
            SettingsRow("Точные будильники", if (exact) "разрешены ✓" else "нужно разрешение") {
                if (!exact && Build.VERSION.SDK_INT >= 31) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                } else {
                    android.widget.Toast.makeText(this@MainActivity, "Уже разрешены", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            SettingsRow("Отложить по умолчанию", "$defaultSnooze мин") {
                val choices = arrayOf("5 мин", "10 мин", "15 мин", "30 мин", "60 мин")
                val values = intArrayOf(5, 10, 15, 30, 60)
                val idx = values.indexOf(defaultSnooze).coerceAtLeast(0)
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Время отложения")
                    .setSingleChoiceItems(choices, idx) { d, i ->
                        defaultSnooze = values[i]
                        settingsPrefs.edit().putInt("default_snooze", defaultSnooze).apply()
                        d.dismiss()
                        invalidate()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            },
            SettingsRow("Вибрация", if (vibration) "вкл" else "выкл") {
                vibration = !vibration
                settingsPrefs.edit().putBoolean("vibration", vibration).apply()
                invalidate()
            },
            SettingsRow("Звук уведомления", if (sound) "вкл" else "выкл") {
                sound = !sound
                settingsPrefs.edit().putBoolean("sound", sound).apply()
                invalidate()
            },
            SettingsRow("Экран при напоминании", if (keepScreen) "вкл" else "выкл") {
                keepScreen = !keepScreen
                settingsPrefs.edit().putBoolean("keep_screen_on", keepScreen).apply()
                invalidate()
            },
            SettingsRow("Очистить историю", "все события") {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Очистить историю?")
                    .setMessage("Все записи о срабатываниях и отложениях будут удалены.")
                    .setPositiveButton("Очистить") { _, _ ->
                        getSharedPreferences("reminder_history", MODE_PRIVATE).edit().clear().apply()
                        android.widget.Toast.makeText(this@MainActivity, "История очищена", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            },
            SettingsRow("Удалить все напоминания", "необратимо") {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Удалить все?")
                    .setMessage("Это действие нельзя отменить.")
                    .setPositiveButton("Удалить") { _, _ ->
                        val count = prefs.getInt("count", 0)
                        for (i in 0 until count) {
                            val id = prefs.getLong("id_$i", 0L)
                            if (id != 0L) AlarmHelper.cancel(this@MainActivity, id)
                        }
                        prefs.edit().clear().apply()
                        getSharedPreferences("reminder_features", MODE_PRIVATE).edit().clear().apply()
                        android.widget.Toast.makeText(this@MainActivity, "Всё удалено", android.widget.Toast.LENGTH_SHORT).show()
                        // go back to home
                        setContentView(view)
                        view.invalidate()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            },
            SettingsRow("О приложении", "v1.2") {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("О приложении")
                    .setMessage("Напоминайка\nВерсия 1.2\n\nМилые напоминания от Настюши.\nРедактирование, прокрутка длинных текстов, настройки.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        )

        override fun onDraw(c: Canvas) {
            c.drawColor(CREAM)
            val sx = width / W0
            val sy = height / H0
            c.save()
            c.scale(sx, sy)

            // Header
            p.style = Paint.Style.FILL
            p.color = DARK
            p.textSize = 50f
            p.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            c.drawText("‹", 30f, 67f, p)
            p.textSize = 28f
            p.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            c.drawText("Настройки", 80f, 65f, p)

            // Rows as cards
            val list = rows()
            var y = 110f
            for ((idx, row) in list.withIndex()) {
                // card
                p.style = Paint.Style.FILL
                p.color = Color.rgb(255, 253, 246)
                c.drawRoundRect(28f, y, 456f, y + 72f, 16f, 16f, p)
                p.style = Paint.Style.STROKE
                p.strokeWidth = 1.5f
                p.color = Color.rgb(224, 218, 205)
                c.drawRoundRect(28f, y, 456f, y + 72f, 16f, 16f, p)

                // title
                p.style = Paint.Style.FILL
                p.color = DARK
                p.textSize = 17f
                p.typeface = Typeface.create("sans-serif", Typeface.BOLD)
                c.drawText(row.title, 48f, y + 30f, p)

                // value
                p.color = Color.rgb(120, 120, 120)
                p.textSize = 14f
                p.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                c.drawText(row.value, 48f, y + 54f, p)

                // chevron
                p.color = Color.rgb(180, 180, 180)
                p.textSize = 22f
                c.drawText("›", 430f, y + 44f, p)

                y += 84f
            }

            c.restore()
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            if (e.action != MotionEvent.ACTION_UP) return true
            val x = e.x / (width / W0)
            val y = e.y / (height / H0)

            // Back
            if (x < 90f && y < 90f) {
                setContentView(view)
                view.invalidate()
                return true
            }

            val list = rows()
            var top = 110f
            for (row in list) {
                if (y in top..(top + 72f) && x in 28f..456f) {
                    row.action()
                    return true
                }
                top += 84f
            }
            return true
        }
    }
}
