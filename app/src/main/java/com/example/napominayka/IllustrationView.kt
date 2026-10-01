package com.example.napominayka

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class IllustrationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val skin = Color.rgb(244, 190, 145)
    private val skin2 = Color.rgb(235, 171, 126)
    private val hair = Color.rgb(224, 185, 124)
    private val beard = Color.rgb(86, 63, 42)
    private val shirt = Color.rgb(52, 61, 42)
    private val stripe = Color.rgb(48, 91, 116)
    private val green = Color.rgb(117, 178, 83)
    private val cream = Color.rgb(255, 249, 233)
    private val red = Color.rgb(165, 35, 39)

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat()
        val h = height.toFloat()
        val s = min(w / 420f, h / 470f)
        c.save()
        c.scale(s, s)
        val W = 420f
        val H = 470f

        // soft background
        p.style = Paint.Style.FILL
        p.color = cream
        c.drawRect(0f, 0f, W, H, p)
        p.color = Color.rgb(232, 239, 196)
        c.drawCircle(28f, 455f, 45f, p)
        c.drawCircle(390f, 455f, 55f, p)
        p.color = Color.rgb(191, 213, 137)
        c.drawCircle(75f, 463f, 38f, p)
        c.drawCircle(345f, 466f, 48f, p)

        // woman body
        p.color = Color.WHITE
        c.drawRoundRect(22f, 322f, 190f, 500f, 40f, 40f, p)
        p.color = stripe
        var y = 338f
        while (y < 500f) {
            c.drawRect(25f, y, 188f, y + 13f, p)
            y += 34f
        }
        // woman head
        p.color = skin
        c.drawOval(48f, 255f, 157f, 375f, p)
        // hair mass
        p.color = hair
        c.drawOval(35f, 236f, 169f, 350f, p)
        p.color = skin
        c.drawOval(55f, 263f, 151f, 366f, p)
        // hair curls
        p.color = hair
        for (x in listOf(42f,55f,70f,139f,153f,164f)) c.drawCircle(x, 315f, 24f, p)
        // eyes
        p.color = Color.DKGRAY
        c.drawCircle(84f, 309f, 4f, p); c.drawCircle(125f,309f,4f,p)
        p.style=Paint.Style.STROKE; p.strokeWidth=2.5f
        c.drawArc(96f, 323f, 116f, 340f, 0f, 180f, false, p)
        p.style=Paint.Style.FILL
        // cup
        p.color = Color.rgb(235,103,38)
        c.drawRoundRect(58f, 392f, 112f, 447f, 9f, 9f, p)
        p.style=Paint.Style.STROKE; p.strokeWidth=7f
        c.drawArc(103f, 403f, 133f, 436f, -80f, 180f, false, p)
        p.style=Paint.Style.FILL
        p.color=Color.WHITE
        c.drawCircle(85f,420f,10f,p)

        // man body
        p.color = shirt
        c.drawRoundRect(166f, 292f, 416f, 505f, 52f, 52f, p)
        // arm
        p.color = shirt
        c.drawOval(265f, 350f, 420f, 455f, p)
        // head
        p.color = skin2
        c.drawOval(206f, 202f, 344f, 340f, p)
        // hair/beard
        p.color = beard
        c.drawOval(201f, 192f, 350f, 293f, p)
        c.drawOval(205f, 255f, 347f, 352f, p)
        // bandana
        p.color = red
        c.drawRoundRect(211f, 190f, 342f, 234f, 12f, 12f, p)
        p.color = Color.WHITE
        p.style=Paint.Style.STROKE; p.strokeWidth=2f
        for (x in listOf(228f,255f,286f,318f)) c.drawCircle(x,210f,3f,p)
        p.style=Paint.Style.FILL
        // eyes
        p.color=Color.DKGRAY
        c.drawCircle(245f,257f,4f,p); c.drawCircle(300f,257f,4f,p)
        p.style=Paint.Style.STROKE; p.strokeWidth=2.5f
        c.drawArc(265f, 269f, 290f, 291f, 0f, 180f, false, p)
        p.style=Paint.Style.FILL
        // necklace
        p.color=Color.LTGRAY
        p.style=Paint.Style.STROKE; p.strokeWidth=2f
        c.drawArc(235f, 288f, 315f, 370f, 0f, 180f, false, p)
        p.style=Paint.Style.FILL
        // thumbs-up
        p.color=skin2
        c.drawOval(333f, 321f, 382f, 382f, p)
        c.drawRoundRect(342f, 337f, 382f, 399f, 18f, 18f, p)

        c.restore()
    }
}
