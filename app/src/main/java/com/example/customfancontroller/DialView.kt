package com.example.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/*
* Enum class to define different fan speed labels
*/
private enum class FanSpeed(val label : Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when(this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

//consts for drawing dial indicator labels
private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

/*
* A Custom view that extends View class
*/
class DialView @JvmOverloads constructor(
    context : Context,
    attrs : AttributeSet? = null,
    defStyleAttr : Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius = 0.0f               //radius of the circle
    private var fanSpeed = FanSpeed.OFF     //the start selection

    //Position variable which will be used to draw label and indicator circle positions
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    //paint object to help speed up the painting step
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.DEFAULT_BOLD
    }

    // vars for retrieving colors from view attributes
    private var fanSpeedSlowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedMaxColor = 0

    //make the view clickable
    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedSlowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSpeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }

    //define clickingR logic
    override fun performClick(): Boolean {
        if(super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        invalidate()
        return true
    }

    //to calculate the size of the view
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        radius = (min(width, height) / 2 * 0.8).toFloat()

    }

    //compute the position of each fan speed
    private fun PointF.computeXYForSpeed(pos : FanSpeed, radius : Float){
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    //method to draw view each time the screen refreshes
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //set background color according to fan speed
        paint.color = when(fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedSlowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedMaxColor
        }

        //draw the dial (background)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        //draw indicator circle
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        //draw text labels
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i , labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }
}