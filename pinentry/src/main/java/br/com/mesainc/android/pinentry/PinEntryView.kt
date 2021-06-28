package br.com.mesainc.android.pinentry

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.core.content.res.use


class PinEntryView : androidx.appcompat.widget.AppCompatEditText {

    private val secretPinEntryUnitCharPaint = Paint().apply {
        this.textAlign = Paint.Align.CENTER
        this.flags = Paint.ANTI_ALIAS_FLAG
    }

    var pin
        get() = text?.toString() ?: ""
        set(value) {
            setText(value)
        }

    var length: Int = 0
        set(value) {
            field = value

            if (pin.length > length) {
                pin = pin.take(length)
            }

            invalidate()
            requestLayout()
        }

    @Px
    var entryUnitSpacing: Int = 0
        set(value) {
            field = value

            invalidate()
            requestLayout()
        }

    var secret = true
        set(value) {
            field = value

            invalidate()
        }

    var secretPinEntryUnitChar = EMPTY_CHAR
        set(value) {
            field = value

            invalidate()
        }

    var digitsOnly
        get() = inputType == InputType.TYPE_CLASS_NUMBER
        set(value) {
            inputType = if (value) {
                InputType.TYPE_CLASS_NUMBER
            } else {
                InputType.TYPE_CLASS_TEXT
            }
        }

    var entryUnitWidth = 0
        set(value) {
            field = value

            invalidate()
            requestLayout()
        }

    var entryUnitHeight = 0
        set(value) {
            field = value

            invalidate()
            requestLayout()
        }

    private var defaultPinEntryUnitAttrs = PinEntryUnitAttrs()
    private var currentPinEntryUnitAttrs = PinEntryUnitAttrs()

    private var entryUnitDimensionRectArray = emptyArray<RectF>()
    private var entryUnitCenterPointArray = emptyArray<PointF>()

    private val currentPinEntryUnitIndex
        get() = if (isFocused) {
            pin.length
        } else {
            -1
        }

    constructor(context: Context) : super(context) {
        commonInit(context = context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        commonInit(context = context, attrs = attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        commonInit(context = context, attrs = attrs, defStyleAttr = defStyleAttr)
    }

    private fun commonInit(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.pinEntryViewStyle,
        defStyleRes: Int = R.style.Widget_PinEntryView_Pin
    ) {
        applyAttrs(
            context = context,
            attrs = attrs,
            defStyleAttr = defStyleAttr,
            defStyleRes = defStyleRes
        )

        background = null

        isFocusableInTouchMode = true

        if (isInEditMode) {
            return
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text == null) {
            super.setText("", type)

            return
        }

        if (text.length >= length) {
            return
        }

        super.setText(text, type)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode != KeyEvent.KEYCODE_DEL && pin.length >= length) {
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)

        if (gainFocus) {
            showSoftKeyboard()
        } else {
            hideSoftKeyboard()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            requestFocus()
            showSoftKeyboard()
        }

        return true
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)

        if (!isFocused) {
            return
        }

        if (visibility == VISIBLE) {
            showSoftKeyboard()
        } else {
            hideSoftKeyboard()
        }
    }

    override fun onPreDraw(): Boolean {
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val pinEntryUnitCharPaint = if (secret) {
            secretPinEntryUnitCharPaint
        } else {
            defaultPinEntryUnitAttrs.charPaint
        }

        this.entryUnitDimensionRectArray = calculateEntryUnitDimensionRectArray(
            entryUnitSpacing = entryUnitSpacing,
            entryUnitWidth = entryUnitWidth,
            entryUnitHeight = entryUnitHeight,
            pinEntryUnitAttrs = defaultPinEntryUnitAttrs
        )

        this.entryUnitCenterPointArray = calculatePinEntryUnitCenterPointArray(
            pinEntryUnitStrokeRectArray = entryUnitDimensionRectArray,
            pinEntryUnitCharPaint = pinEntryUnitCharPaint
        )

        val width = calculateViewWidth(
            pinEntryUnitDimensionRectArray = entryUnitDimensionRectArray,
            pinEntryUnitAttrs = defaultPinEntryUnitAttrs
        )
        val height = calculateViewHeight(
            pinEntryUnitDimensionRectArray = entryUnitDimensionRectArray,
            pinEntryUnitAttrs = defaultPinEntryUnitAttrs
        )

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        var pinEntryUnitAttrs: PinEntryUnitAttrs
        var pinEntryUnitChar: Char
        var pinEntryUnitCharPaint: Paint

        for (i in 0 until length) {
            pinEntryUnitAttrs = if (i == currentPinEntryUnitIndex) {
                currentPinEntryUnitAttrs
            } else {
                defaultPinEntryUnitAttrs
            }

            pinEntryUnitChar = pin.getOrNull(i)
                ?.let { char ->
                    if (secret) {
                        secretPinEntryUnitChar
                    } else {
                        char
                    }
                } ?: EMPTY_CHAR

            pinEntryUnitCharPaint = if (secret) {
                secretPinEntryUnitCharPaint
            } else {
                pinEntryUnitAttrs.charPaint
            }

            drawPinEntryUnitBackground(
                canvas = canvas,
                pinEntryUnitAttrs = pinEntryUnitAttrs,
                pinEntryUnitRect = entryUnitDimensionRectArray[i]
            )

            drawPinEntryUnitStroke(
                canvas = canvas,
                pinEntryUnitAttrs = pinEntryUnitAttrs,
                pinEntryUnitRect = entryUnitDimensionRectArray[i]
            )

            drawPinEntryUnitChar(
                canvas = canvas,
                pinEntryUnitCharPaint = pinEntryUnitCharPaint,
                pinEntryUnitChar = pinEntryUnitChar,
                centerPoint = entryUnitCenterPointArray[i]
            )
        }
    }

    private fun applyAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.PinEntryView,
            defStyleAttr,
            defStyleRes
        )

        typedArray.use {
            R.styleable.PinEntryView_pin_pin
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getString(it) }
                ?.let { this.pin = it }

            R.styleable.PinEntryView_pin_length
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getInt(it, 0) }
                ?.let { this.length = it }

            R.styleable.PinEntryView_pin_entryUnitSpacing
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getDimensionPixelSize(it, 0) }
                ?.let { this.entryUnitSpacing = it }

            R.styleable.PinEntryView_pin_secret
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getBoolean(it, false) }
                ?.let { this.secret = it }

            R.styleable.PinEntryView_pin_entryUnitSecretChar
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getString(it) }
                ?.trim()
                ?.firstOrNull()
                ?.let { this.secretPinEntryUnitChar = it }

            R.styleable.PinEntryView_pin_secretEntryUnitTextAppearance
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getResourceId(it, 0) }
                ?.let { this.setSecretTextAppearance(context = context, textAppearanceResId = it) }

            R.styleable.PinEntryView_pin_entryUnitWidth
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getDimensionPixelSize(it, 0) }
                ?.let { this.entryUnitWidth = it }

            R.styleable.PinEntryView_pin_digits_only
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getBoolean(it, false) }
                ?.let { this.digitsOnly = it }

            R.styleable.PinEntryView_pin_entryUnitHeight
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getDimensionPixelSize(it, 0) }
                ?.let { this.entryUnitHeight = it }

            R.styleable.PinEntryView_pin_defaultPinEntryUnitStyle
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getResourceId(it, 0) }
                ?.let {
                    defaultPinEntryUnitAttrs =
                        PinEntryUnitAttrs.invoke(context = context, resId = it)
                }

            R.styleable.PinEntryView_pin_currentPinEntryUnitStyle
                .takeIf { typedArray.hasValue(it) }
                ?.let { typedArray.getResourceId(it, 0) }
                ?.let {
                    currentPinEntryUnitAttrs =
                        PinEntryUnitAttrs.invoke(context = context, resId = it)
                }
        }
    }

    private fun calculateViewWidth(
        pinEntryUnitDimensionRectArray: Array<RectF>,
        pinEntryUnitAttrs: PinEntryUnitAttrs
    ): Int {
        if (pinEntryUnitDimensionRectArray.isEmpty()) {
            return 0
        }

        val firstRect = pinEntryUnitDimensionRectArray.first()
        val lastRect = pinEntryUnitDimensionRectArray.last()

        val width = lastRect.right - firstRect.left + (2 * pinEntryUnitAttrs.strokeThickness)

        return width.toInt()
    }

    private fun calculateViewHeight(
        pinEntryUnitDimensionRectArray: Array<RectF>,
        pinEntryUnitAttrs: PinEntryUnitAttrs
    ): Int {
        if (pinEntryUnitDimensionRectArray.isEmpty()) {
            return 0
        }

        val firstRect = pinEntryUnitDimensionRectArray.first()

        val height = firstRect.bottom - firstRect.top + (2 * pinEntryUnitAttrs.strokeThickness)

        return height.toInt()
    }

    private fun calculatePinEntryUnitCenterPointArray(
        pinEntryUnitStrokeRectArray: Array<RectF>,
        pinEntryUnitCharPaint: Paint
    ): Array<PointF> {
        return pinEntryUnitStrokeRectArray.map { rect ->
            val centerX = rect.centerX()
            val centerY = rect.centerY() -
                    ((pinEntryUnitCharPaint.descent() + pinEntryUnitCharPaint.ascent()) / 2)

            PointF(centerX, centerY)
        }.toTypedArray()
    }

    private fun calculateEntryUnitDimensionRectArray(
        entryUnitSpacing: Int,
        entryUnitWidth: Int,
        entryUnitHeight: Int,
        pinEntryUnitAttrs: PinEntryUnitAttrs
    ): Array<RectF> {
        val newEntryUnitDimensionRectArray = mutableListOf<RectF>()

        val top = 0 + pinEntryUnitAttrs.strokeThickness
        val bottom = entryUnitHeight - pinEntryUnitAttrs.strokeThickness

        for (i in 1..length) {
            val leftEntryUnitSpacingMargin = (i - 1) * entryUnitSpacing
            val leftEntryUnitWidthMargin = (i - 1) * entryUnitWidth

            val left =
                leftEntryUnitSpacingMargin + leftEntryUnitWidthMargin + pinEntryUnitAttrs.strokeThickness
            val right = left + entryUnitWidth - pinEntryUnitAttrs.strokeThickness

            val rect = RectF(left, top, right, bottom)
            newEntryUnitDimensionRectArray.add(rect)
        }

        return newEntryUnitDimensionRectArray.toTypedArray()
    }

    private fun drawPinEntryUnitBackground(
        canvas: Canvas,
        pinEntryUnitAttrs: PinEntryUnitAttrs,
        pinEntryUnitRect: RectF
    ) {
        pinEntryUnitAttrs.backgroundPaint.color = pinEntryUnitAttrs.backgroundColor
        pinEntryUnitAttrs.backgroundPaint.style = Paint.Style.FILL

        canvas.drawRoundRect(
            pinEntryUnitRect,
            pinEntryUnitAttrs.backgroundCornerRadius,
            pinEntryUnitAttrs.backgroundCornerRadius,
            pinEntryUnitAttrs.backgroundPaint
        )
    }

    private fun drawPinEntryUnitStroke(
        canvas: Canvas,
        pinEntryUnitAttrs: PinEntryUnitAttrs,
        pinEntryUnitRect: RectF
    ) {
        pinEntryUnitAttrs.backgroundPaint.color = pinEntryUnitAttrs.strokeColor
        pinEntryUnitAttrs.backgroundPaint.style = Paint.Style.STROKE

        canvas.drawRoundRect(
            pinEntryUnitRect,
            pinEntryUnitAttrs.backgroundCornerRadius,
            pinEntryUnitAttrs.backgroundCornerRadius,
            pinEntryUnitAttrs.backgroundPaint
        )
    }

    private fun drawPinEntryUnitChar(
        canvas: Canvas,
        pinEntryUnitCharPaint: Paint,
        pinEntryUnitChar: Char,
        centerPoint: PointF
    ) {
        canvas.drawText(
            pinEntryUnitChar.toString(),
            centerPoint.x,
            centerPoint.y,
            pinEntryUnitCharPaint
        )
    }

    private fun showSoftKeyboard() {
        val imm = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        postDelayed(
            { imm.showSoftInput(this, 0) },
            SHOW_KEYBOARD_DELAY
        )
    }

    private fun hideSoftKeyboard() {
        val imm = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun setSecretTextAppearance(context: Context, @StyleRes textAppearanceResId: Int) {
        val textView = TextView(context).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.setTextAppearance(textAppearanceResId)
            } else {
                this.setTextAppearance(context, textAppearanceResId)
            }
        }

        secretPinEntryUnitCharPaint.color = textView.currentTextColor
        secretPinEntryUnitCharPaint.textSize = textView.textSize
        secretPinEntryUnitCharPaint.typeface = textView.typeface
    }

    companion object {
        // used due to an Android bug where the keyboard might not be shown when requested
        private const val SHOW_KEYBOARD_DELAY = 150L
        private const val EMPTY_CHAR = ' '
    }
}