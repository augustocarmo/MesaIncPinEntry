package br.com.mesainc.android.pinentry

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.res.use

class PinEntryUnitAttrs {

    val backgroundPaint = Paint().apply {
        this.style = Paint.Style.FILL_AND_STROKE
        this.flags = Paint.ANTI_ALIAS_FLAG
    }

    val charPaint = Paint().apply {
        this.textAlign = Paint.Align.CENTER
        this.flags = Paint.ANTI_ALIAS_FLAG
    }

    var strokeThickness
        get() = backgroundPaint.strokeWidth
        set(value) {
            backgroundPaint.strokeWidth = value
        }

    @ColorInt
    var backgroundColor = Color.TRANSPARENT

    @ColorInt
    var strokeColor = Color.TRANSPARENT

    var backgroundCornerRadius = 0f

    fun setTextAppearance(context: Context, @StyleRes textAppearanceResId: Int) {
        val textView = TextView(context).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.setTextAppearance(textAppearanceResId)
            } else {
                this.setTextAppearance(context, textAppearanceResId)
            }
        }

        charPaint.color = textView.currentTextColor
        charPaint.textSize = textView.textSize
        charPaint.typeface = textView.typeface
    }

    companion object {
        operator fun invoke(context: Context, resId: Int): PinEntryUnitAttrs {
            val pinCharAttrs = PinEntryUnitAttrs()

            val typedArray = context.obtainStyledAttributes(
                resId,
                R.styleable.PinEntryView_EntryUnit
            )

            typedArray.use {
                R.styleable.PinEntryView_EntryUnit_pinEntryUnit_textAppearance
                    .takeIf { typedArray.hasValue(it) }
                    ?.let { typedArray.getResourceId(it, 0) }
                    ?.let { pinCharAttrs.setTextAppearance(context, it) }

                R.styleable.PinEntryView_EntryUnit_pinEntryUnit_strokeThickness
                    .takeIf { typedArray.hasValue(it) }
                    ?.let { typedArray.getDimension(it, 0f) }
                    ?.let { pinCharAttrs.strokeThickness = it }

                R.styleable.PinEntryView_EntryUnit_pinEntryUnit_strokeColor
                    .takeIf { typedArray.hasValue(it) }
                    ?.let { typedArray.getColor(it, 0) }
                    ?.let { pinCharAttrs.strokeColor = it }

                R.styleable.PinEntryView_EntryUnit_pinEntryUnit_backgroundCornerRadius
                    .takeIf { typedArray.hasValue(it) }
                    ?.let { typedArray.getDimension(it, 0f) }
                    ?.let { pinCharAttrs.backgroundCornerRadius = it }

                R.styleable.PinEntryView_EntryUnit_pinEntryUnit_backgroundColor
                    .takeIf { typedArray.hasValue(it) }
                    ?.let { typedArray.getColor(it, 0) }
                    ?.let { pinCharAttrs.backgroundColor = it }
            }

            return pinCharAttrs
        }
    }
}