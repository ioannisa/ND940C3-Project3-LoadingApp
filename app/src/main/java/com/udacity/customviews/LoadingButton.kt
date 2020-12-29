package com.udacity.customviews

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val textPosition: PointF = PointF(0.0f, 0.0f)

    private var buttonColor = 0
    private var fillColor = 0
    private var arcColor = 0
    private var textColor = 0

    private var buttonText = context.getString(R.string.button_state_download)

    private var idleText = ""
    private var loadingText = ""

    var perc:    Float = 0.0f
    var degrees: Float = 0.0f // going from 0 to 360 degrees on our Arc filling

    private val textBounds = Rect()  // the text rectangle
    private val fillRect   = Rect()  // the fill rectangle
    private val arcRect    = RectF() // the arc's rectangle

    // initialize the animator to have it ready for use when required
    private var valueAnimator: ValueAnimator =
        ValueAnimator.ofFloat(0.0f, 100.0f).setDuration(1000).apply {
            addUpdateListener { valueAnimator ->
                perc = valueAnimator.animatedValue as Float     // perc as a value between 0.0f and 100.0f
                degrees = perc * 3.6f                           // multiply perc by 3.6f to turn it to circle degrees
                fillRect.set(0, 0, (width * perc / 100).toInt(), height)
                invalidate()
            }
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, newState ->
        // set the button text depending on the state of the button
        buttonText = context.getString(buttonState.labelResourceId)

        // act on the animator depending on the state
        when (newState) {
            ButtonState.Loading -> {
                valueAnimator.start()
            }

            ButtonState.Completed, ButtonState.Clicked -> {
                valueAnimator.cancel()
                perc = 0.0f
                invalidate()
            }
        }
    }

    // handlig clicks from inside custom button won't bee used in this app
    // but left it as it can be useful for future code reference

    override fun performClick(): Boolean {
        //when (buttonState){
        //    ButtonState.Completed -> buttonState = ButtonState.Loading
        //    ButtonState.Loading   -> buttonState = ButtonState.Clicked
        //    ButtonState.Clicked   -> buttonState = ButtonState.Loading
       // }

        if (super.performClick()) return true
        return true
    }


    init {
        val typedArray = context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground))
        val themeRippleDrawable = typedArray.getDrawable(0)
        typedArray.recycle()

        // provide built in default Ripple Effect for this view
        foreground = themeRippleDrawable

        // allow managing clicks
        isClickable = true
        isFocusable = true


        // initially start with a Completed State
        buttonState = ButtonState.Completed

        // Register values from xml attributes
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonColor = getColor(
                    R.styleable.LoadingButton_backgroundColor,
                context.getColor(R.color.colorPrimary)
            )
            fillColor   = getColor(
                    R.styleable.LoadingButton_fillColor,
                context.getColor(R.color.colorPrimaryDark)
            )
            arcColor    = getColor(
                    R.styleable.LoadingButton_arcCColor,
                context.getColor(R.color.colorAccent)
            )
            textColor   = getColor(
                    R.styleable.LoadingButton_textColor,
                context.getColor(R.color.white)
            )
            idleText    = getString(R.styleable.LoadingButton_idleText) ?: ""
            loadingText = getString(R.styleable.LoadingButton_loadingText) ?: ""
        }
    }

    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = textColor
    }

    private var fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
    }

    private var arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = arcColor
    }



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawColor(buttonColor)                                                  // draw background color of canvas

            if (buttonState == ButtonState.Loading) {
                drawRect(fillRect, fillPaint)                                       // draw the animated bar
                drawArc(arcRect, 0f, degrees, true, arcPaint)    // draw the animated arc
            }

            drawText(buttonText, textPosition.x, textPosition.y, textPaint)         // draw the button text
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        textPaint.getTextBounds(buttonText, 0, buttonText.length, textBounds)
        textPosition.x = width.toFloat() / 2
        textPosition.y = (height.toFloat() / 2) + (textBounds.height().toFloat() / 2)

        val arcLeftTop = textBounds.height().toFloat()
        val arcBottomRight = height - paddingBottom - arcLeftTop

        arcRect.set(
            paddingStart + arcLeftTop,
            paddingTop + arcLeftTop,
            arcBottomRight,
            arcBottomRight
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}