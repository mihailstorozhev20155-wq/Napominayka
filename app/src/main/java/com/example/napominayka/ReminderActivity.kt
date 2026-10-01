package com.example.napominayka

import android.app.*
import android.content.*
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.text.InputType
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

private const val BW = 484f
private const val BH = 1024f
private val CREAM2 = Color.rgb(255, 249, 233)
private val GREEN2 = Color.rgb(110, 173, 82)
private val ORANGE2 = Color.rgb(244, 103, 28)
private val DARK2 = Color.rgb(24, 24, 24)

class ReminderActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("reminders", MODE_PRIVATE) }
    private lateinit var root: FrameLayout
    private lateinit var input: EditText
    private lateinit var dateField: TextView
    private lateinit var timeField: TextView
    private var date = Calendar.getInstance()
    private var time = Calendar.getInstance()
    private var editIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = CREAM2
        window.navigationBarColor = CREAM2
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (intent.hasExtra("text")) {
            showAlert(intent.getStringExtra("text") ?: "У тебя есть напоминание!")
        } else {
            editIndex = intent.getIntExtra("edit_index", -1)
            showCreate()
        }
    }

    private fun showCreate() {
        root = FrameLayout(this)
        root.setBackgroundColor(CREAM2)
        val bg = CreateView(this)
        root.addView(bg, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)

        input = EditText(this).apply {
            setText("")
            hint = "Например: Прочитать книгу"
            textSize = 18f
            setTextColor(DARK2)
            setHintTextColor(Color.rgb(180, 180, 180))
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(14, 10, 14, 10)
            background = ColorDrawable(Color.TRANSPARENT)
        }
        root.addView(input)
        dateField = fieldText()
        timeField = fieldText()
        root.addView(dateField)
        root.addView(timeField)
        val save = TextView(this).apply {
            text = "Сохранить"
            gravity = Gravity.CENTER
            textSize = 20f
            setTextColor(Color.TRANSPARENT)
            setOnClickListener { saveReminder() }
        }
        val cancel = TextView(this).apply {
            text = "Отмена"
            gravity = Gravity.CENTER
            textSize = 17f
            setTextColor(Color.TRANSPARENT)
            setTypeface(typeface, Typeface.BOLD)
            setOnClickListener { finish() }
        }
        root.addView(save)
        root.addView(cancel)
        dateField.setOnClickListener { pickDate() }
        timeField.setOnClickListener { pickTime() }
        bg.setOnClickListener {
            if (bg.lastX < 70f && bg.lastY < 100f) finish()
        }

        // Prefill for edit mode
        if (editIndex >= 0) {
            val count = prefs.getInt("count", 0)
            if (editIndex < count) {
                input.setText(prefs.getString("text_$editIndex", "") ?: "")
                val whenMs = prefs.getLong("time_$editIndex", 0L)
                if (whenMs > 0) {
                    date.timeInMillis = whenMs
                    time.timeInMillis = whenMs
                }
            }
        }

        root.post {
            layoutCreateViews()
            updateLabels()
        }
    }

    private fun fieldText() = TextView(this).apply {
        textSize = 17f
        setTextColor(DARK2)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(14, 0, 14, 0)
        background = ColorDrawable(Color.TRANSPARENT)
    }

    private fun layoutCreateViews() {
        val sx = root.width / BW
        val sy = root.height / BH
        fun lp(x: Float, y: Float, w: Float, h: Float): FrameLayout.LayoutParams {
            return FrameLayout.LayoutParams((w * sx).toInt(), (h * sy).toInt()).apply {
                leftMargin = (x * sx).toInt()
                topMargin = (y * sy).toInt()
            }
        }
        input.layoutParams = lp(56f, 168f, 370f, 80f)
        dateField.layoutParams = lp(56f, 307f, 370f, 60f)
        timeField.layoutParams = lp(56f, 423f, 370f, 60f)
        root.getChildAt(4).layoutParams = lp(61f, 513f, 313f, 56f)
        root.getChildAt(5).layoutParams = lp(180f, 581f, 124f, 45f)
    }

    private fun updateLabels() {
        dateField.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date.time)
        timeField.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(time.time)
    }

    private fun pickDate() {
        DatePickerDialog(this, { _, y, m, d ->
            date.set(y, m, d)
            updateLabels()
        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime() {
        TimePickerDialog(this, { _, h, m ->
            time.set(Calendar.HOUR_OF_DAY, h)
            time.set(Calendar.MINUTE, m)
            time.set(Calendar.SECOND, 0)
            updateLabels()
        }, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), true).show()
    }

    private fun saveReminder() {
        val msg = input.text.toString().trim()
        if (msg.isBlank()) {
            input.error = "Напиши, что нужно напомнить"
            return
        }
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, this@ReminderActivity.date.get(Calendar.YEAR))
            set(Calendar.MONTH, this@ReminderActivity.date.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, this@ReminderActivity.date.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, this@ReminderActivity.time.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, this@ReminderActivity.time.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "Выбери время в будущем", Toast.LENGTH_SHORT).show()
            return
        }

        if (editIndex >= 0) {
            // Update existing
            val oldId = prefs.getLong("id_$editIndex", 0L)
            AlarmHelper.cancel(this, oldId)
            val newId = System.currentTimeMillis()
            prefs.edit()
                .putLong("id_$editIndex", newId)
                .putString("text_$editIndex", msg)
                .putLong("time_$editIndex", cal.timeInMillis)
                .putBoolean("enabled_$editIndex", true)
                .apply()
            AlarmHelper.schedule(this, newId, cal.timeInMillis, msg)
        } else {
            // Create new
            val count = prefs.getInt("count", 0)
            val id = System.currentTimeMillis()
            prefs.edit()
                .putInt("count", count + 1)
                .putLong("id_$count", id)
                .putString("text_$count", msg)
                .putLong("time_$count", cal.timeInMillis)
                .putBoolean("enabled_$count", true)
                .apply()
            AlarmHelper.schedule(this, id, cal.timeInMillis, msg)
        }
        finish()
    }

    private inner class CreateView(context: Context) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private val ill = BitmapFactory.decodeResource(resources, R.drawable.concept_create_illustration)
        var lastX = 0f
        var lastY = 0f

        override fun onDraw(c: Canvas) {
            c.drawColor(CREAM2)
            val sx = width / BW
            val sy = height / BH
            c.save()
            c.scale(sx, sy)
            text(c, "‹", 43f, 67f, 50f, DARK2, false)
            val title = if (editIndex >= 0) "Редактировать" else "Новое напоминание"
            text(c, title, 82f, 65f, 29f, DARK2, true)
            text(c, "Что напомнить?", 44f, 125f, 18f, DARK2, true)
            field(c, 36f, 141f, 452f, 225f)
            text(c, "Дата", 44f, 281f, 18f, DARK2, true)
            field(c, 36f, 291f, 452f, 356f)
            drawCalendar(c, 431f, 324f)
            text(c, "Время", 44f, 399f, 18f, DARK2, true)
            field(c, 36f, 407f, 452f, 472f)
            drawClock(c, 430f, 440f)
            round(c, 60f, 514f, 422f, 570f, 30f, GREEN2)
            textCenter(c, "Сохранить", 241f, 550f, 20f, Color.WHITE, false)
            textCenter(c, "Отмена", 241f, 607f, 17f, GREEN2, true)
            c.drawBitmap(ill, null, RectF(0f, 620f, 484f, 1024f), p)
            c.restore()
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            if (e.action == MotionEvent.ACTION_UP) {
                lastX = e.x / (width / BW)
                lastY = e.y / (height / BH)
                if (lastX < 72 && lastY < 95) {
                    finish()
                    return true
                }
            }
            return true
        }

        private fun field(c: Canvas, l: Float, t: Float, r: Float, b: Float) {
            p.style = Paint.Style.FILL
            p.color = Color.WHITE
            c.drawRoundRect(l, t, r, b, 14f, 14f, p)
            p.style = Paint.Style.STROKE
            p.strokeWidth = 1.5f
            p.color = Color.rgb(218, 213, 203)
            c.drawRoundRect(l, t, r, b, 14f, 14f, p)
        }

        private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, col: Int, b: Boolean) {
            p.style = Paint.Style.FILL
            p.color = col
            p.textSize = size
            p.typeface = Typeface.create("sans-serif", if (b) Typeface.BOLD else Typeface.NORMAL)
            c.drawText(s, x, y, p)
        }

        private fun textCenter(c: Canvas, s: String, x: Float, y: Float, size: Float, col: Int, b: Boolean) {
            p.textSize = size
            p.typeface = Typeface.create("sans-serif", if (b) Typeface.BOLD else Typeface.NORMAL)
            text(c, s, x - p.measureText(s) / 2f, y, size, col, b)
        }

        private fun round(c: Canvas, l: Float, t: Float, r: Float, b: Float, rad: Float, col: Int) {
            p.style = Paint.Style.FILL
            p.color = col
            c.drawRoundRect(l, t, r, b, rad, rad, p)
        }

        private fun drawCalendar(c: Canvas, x: Float, y: Float) {
            p.style = Paint.Style.STROKE
            p.strokeWidth = 2.5f
            p.color = DARK2
            c.drawRoundRect(x - 10, y - 10, x + 10, y + 11, 2f, 2f, p)
            c.drawLine(x - 10, y - 3, x + 10, y - 3, p)
            c.drawLine(x - 5, y - 15, x - 5, y - 7, p)
            c.drawLine(x + 5, y - 15, x + 5, y - 7, p)
        }

        private fun drawClock(c: Canvas, x: Float, y: Float) {
            p.style = Paint.Style.STROKE
            p.strokeWidth = 2.5f
            p.color = DARK2
            c.drawCircle(x, y, 10f, p)
            c.drawLine(x, y, x, y - 6, p)
            c.drawLine(x, y, x + 5, y + 3, p)
        }
    }

    // ===================== ALERT SCREEN =====================

    private fun showAlert(message: String) {
        root = FrameLayout(this)
        root.setBackgroundColor(CREAM2)
        val bg = AlertView(this, message)
        root.addView(bg, FrameLayout.LayoutParams(-1, -1))
        val ok = TextView(this).apply {
            text = "Понятно! 👍"
            gravity = Gravity.CENTER
            textSize = 20f
            setTextColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }
        root.addView(ok)
        setContentView(root)
        val keep = getSharedPreferences("app_settings", MODE_PRIVATE).getBoolean("keep_screen_on", true)
        if (keep) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root.post {
            val sx = root.width / BW
            val sy = root.height / BH
            ok.layoutParams = FrameLayout.LayoutParams((422 * sx).toInt(), (76 * sy).toInt()).apply {
                leftMargin = (32 * sx).toInt()
                topMargin = (865 * sy).toInt()
            }
        }
    }

    private inner class AlertView(context: Context, private val message: String) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private val couple = BitmapFactory.decodeResource(resources, R.drawable.concept_alert_couple)
        private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ORANGE2
            textSize = 26f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        private var scrollY = 0f
        private var lastTouchY = 0f
        private var layout: StaticLayout? = null
        private val msgMaxWidth = 320f   // logical width inside bubble
        private val msgAreaTop = 230f
        private val msgAreaBottom = 340f
        private val msgAreaHeight = msgAreaBottom - msgAreaTop

        init {
            // Build StaticLayout for multi-line wrapping
            layout = if (Build.VERSION.SDK_INT >= 23) {
                StaticLayout.Builder.obtain(message, 0, message.length, textPaint, msgMaxWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(4f, 1f)
                    .setIncludePad(false)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(message, textPaint, msgMaxWidth.toInt(), Layout.Alignment.ALIGN_CENTER, 1f, 4f, false)
            }
        }

        override fun onDraw(c: Canvas) {
            c.drawColor(CREAM2)
            val sx = width / BW
            val sy = height / BH
            c.save()
            c.scale(sx, sy)

            // Bubble background
            p.style = Paint.Style.FILL
            p.color = CREAM2
            val path = Path()
            path.moveTo(74f, 132f)
            path.cubicTo(62f, 94f, 98f, 74f, 135f, 84f)
            path.cubicTo(166f, 45f, 216f, 60f, 242f, 79f)
            path.cubicTo(275f, 50f, 326f, 65f, 347f, 91f)
            path.cubicTo(395f, 78f, 427f, 108f, 410f, 148f)
            path.cubicTo(447f, 179f, 424f, 226f, 385f, 231f)
            path.cubicTo(385f, 278f, 342f, 286f, 310f, 263f)
            path.cubicTo(280f, 299f, 242f, 284f, 229f, 261f)
            path.cubicTo(190f, 286f, 148f, 268f, 150f, 237f)
            path.cubicTo(106f, 241f, 76f, 213f, 88f, 181f)
            path.cubicTo(51f, 166f, 52f, 142f, 74f, 132f)
            c.drawPath(path, p)
            p.style = Paint.Style.STROKE
            p.strokeWidth = 3f
            p.color = Color.BLACK
            c.drawPath(path, p)

            // Fixed title lines
            textCenter(c, "Настюша! Милая", 242f, 175f, 22f, DARK2, true)
            textCenter(c, "ты просила напомнить:", 242f, 205f, 22f, DARK2, true)

            // Scrollable message area
            val lay = layout
            if (lay != null) {
                c.save()
                // Clip to message area inside bubble
                c.clipRect(80f, msgAreaTop, 404f, msgAreaBottom)
                val totalH = lay.height.toFloat()
                val maxScroll = maxOf(0f, totalH - msgAreaHeight)
                scrollY = scrollY.coerceIn(0f, maxScroll)
                val startX = 242f - msgMaxWidth / 2f
                c.translate(startX, msgAreaTop - scrollY)
                lay.draw(c)
                c.restore()

                // Small scroll indicator if content is long
                if (totalH > msgAreaHeight + 4f) {
                    p.style = Paint.Style.FILL
                    p.color = Color.argb(80, 0, 0, 0)
                    val barH = (msgAreaHeight * msgAreaHeight / totalH).coerceAtLeast(12f)
                    val barY = msgAreaTop + (scrollY / maxScroll) * (msgAreaHeight - barH)
                    c.drawRoundRect(398f, barY, 404f, barY + barH, 3f, 3f, p)
                }
            }

            // Couple image
            c.drawBitmap(couple, null, RectF(0f, 360f, 484f, 850f), p)

            // Button
            round(c, 32f, 865f, 452f, 941f, 34f, GREEN2)
            textCenter(c, "Понятно! 👍", 242f, 914f, 20f, Color.WHITE, false)

            c.restore()
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            val sy = height / BH
            val logicalY = e.y / sy
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchY = logicalY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = lastTouchY - logicalY
                    scrollY += dy
                    lastTouchY = logicalY
                    invalidate()
                    return true
                }
            }
            return super.onTouchEvent(e)
        }

        private fun round(c: Canvas, l: Float, t: Float, r: Float, b: Float, rad: Float, col: Int) {
            p.style = Paint.Style.FILL
            p.color = col
            c.drawRoundRect(l, t, r, b, rad, rad, p)
        }

        private fun textCenter(c: Canvas, s: String, x: Float, y: Float, size: Float, col: Int, b: Boolean) {
            p.style = Paint.Style.FILL
            p.color = col
            p.textSize = size
            p.typeface = Typeface.create("sans-serif", if (b) Typeface.BOLD else Typeface.NORMAL)
            c.drawText(s, x - p.measureText(s) / 2f, y, p)
        }
    }
}
