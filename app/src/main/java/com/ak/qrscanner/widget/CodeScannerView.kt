package com.ak.qrscanner.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Nullable
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.ak.qrscanner.R
import kotlin.math.roundToInt


class CodeScannerView : ViewGroup {

    private val DEFAULT_MASK_VISIBLE = true
    private val DEFAULT_FRAME_VISIBLE = true
    private val DEFAULT_FRAME_CORNERS_CAP_ROUNDED = false
    private val DEFAULT_MASK_COLOR = 0x55000000
    private val DEFAULT_FRAME_COLOR = Color.WHITE
    private var mViewFinderView: ViewFinderView? = null

    private val DEFAULT_FRAME_THICKNESS_DP = 2f
    private val DEFAULT_FRAME_ASPECT_RATIO_WIDTH = 1f
    private val DEFAULT_FRAME_ASPECT_RATIO_HEIGHT = 1f
    private val DEFAULT_FRAME_CORNER_SIZE_DP = 50f
    private val DEFAULT_FRAME_CORNERS_RADIUS_DP = 0f
    private val DEFAULT_FRAME_SIZE = 0.5f
    private val DEFAULT_FRAME_VERTICAL_BIAS = 0.25f


    constructor(context: Context) : super(context) {
        initialize(context, null, 0, 0)
    }


    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs, 0, 0)
    }


    constructor(
        context: Context, @Nullable attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs, defStyleAttr, 0)
    }


    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun initialize(
        context: Context, @Nullable attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int
    ) {

        val density: Float = context.resources.displayMetrics.density
        mViewFinderView = ViewFinderView(context)
        if (attrs == null) {
            setFrameAspectRatio(
                DEFAULT_FRAME_ASPECT_RATIO_WIDTH,
                DEFAULT_FRAME_ASPECT_RATIO_HEIGHT
            )
            maskColor = DEFAULT_MASK_COLOR
            isMaskVisible = DEFAULT_MASK_VISIBLE
            frameColor = DEFAULT_FRAME_COLOR
            isFrameVisible = DEFAULT_FRAME_VISIBLE
            frameThickness =
                (DEFAULT_FRAME_THICKNESS_DP * density).roundToInt()
            frameCornersSize =
                (DEFAULT_FRAME_CORNER_SIZE_DP * density).roundToInt()
            frameCornersRadius =
                (DEFAULT_FRAME_CORNERS_RADIUS_DP * density).roundToInt()
            isFrameCornersCapRounded =
                DEFAULT_FRAME_CORNERS_CAP_ROUNDED
            frameSize = DEFAULT_FRAME_SIZE
            frameVerticalBias = DEFAULT_FRAME_VERTICAL_BIAS
        } else {
            var a: TypedArray? = null
            try {
                a = context.theme.obtainStyledAttributes(attrs, R.styleable.CodeScannerView, defStyleAttr, defStyleRes)
                maskColor = a.getColor(
                    R.styleable.CodeScannerView_maskColor,
                    DEFAULT_MASK_COLOR
                )
                isMaskVisible = a.getBoolean(
                    R.styleable.CodeScannerView_maskVisible,
                    DEFAULT_MASK_VISIBLE
                )
                frameColor = a.getColor(
                    R.styleable.CodeScannerView_frameColor,
                    DEFAULT_FRAME_COLOR
                )
                isFrameVisible = a.getBoolean(
                    R.styleable.CodeScannerView_frameVisible,
                    DEFAULT_FRAME_VISIBLE
                )
                frameThickness = a.getDimensionPixelOffset(
                    R.styleable.CodeScannerView_frameThickness,
                    Math.round(DEFAULT_FRAME_THICKNESS_DP * density)
                )
                frameCornersSize = a.getDimensionPixelOffset(
                    R.styleable.CodeScannerView_frameCornersSize,
                    (DEFAULT_FRAME_CORNER_SIZE_DP * density).roundToInt()
                )
                frameCornersRadius = a.getDimensionPixelOffset(
                    R.styleable.CodeScannerView_frameCornersRadius,
                    Math.round(DEFAULT_FRAME_CORNERS_RADIUS_DP * density)
                )
                isFrameCornersCapRounded = a.getBoolean(
                    R.styleable.CodeScannerView_frameCornersCapRounded,
                    DEFAULT_FRAME_CORNERS_CAP_ROUNDED
                )
                setFrameAspectRatio(
                    a.getFloat(R.styleable.CodeScannerView_frameAspectRatioWidth, DEFAULT_FRAME_ASPECT_RATIO_WIDTH),
                    a.getFloat(R.styleable.CodeScannerView_frameAspectRatioHeight, DEFAULT_FRAME_ASPECT_RATIO_HEIGHT)
                )
                frameSize = a.getFloat(
                    R.styleable.CodeScannerView_frameSize,
                    DEFAULT_FRAME_SIZE
                )
                frameVerticalBias = a.getFloat(
                    R.styleable.CodeScannerView_frameVerticalBias,
                    DEFAULT_FRAME_VERTICAL_BIAS
                )
            } finally {
                a?.recycle()
            }
        }

        addView(mViewFinderView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(
        changed: Boolean, left: Int, top: Int, right: Int,
        bottom: Int
    ) {
        val width = right - left
        val height = bottom - top
        mViewFinderView?.layout(0, 0, width, height)
    }

    override fun onSizeChanged(
        width: Int, height: Int, oldWidth: Int,
        oldHeight: Int
    ) {
    }


    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return true
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return if (p is MarginLayoutParams) {
            LayoutParams(p)
        } else {
            LayoutParams(p)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    @get:ColorInt
    var maskColor: Int
        get() = mViewFinderView?.maskColor!!
        set(color) {
            mViewFinderView?.maskColor = color
        }

    var isMaskVisible: Boolean
        get() = mViewFinderView!!.isMaskVisible
        set(visible) {
            mViewFinderView!!.isMaskVisible = visible
        }

    @get:ColorInt
    var frameColor: Int
        get() = mViewFinderView!!.frameColor
        set(color) {
            mViewFinderView!!.frameColor = color
        }

    var isFrameVisible: Boolean
        get() = mViewFinderView!!.isFrameVisible
        set(visible) {
            mViewFinderView!!.isFrameVisible = visible
        }

    @get:Px
    var frameThickness: Int
        get() = mViewFinderView!!.frameThickness
        set(thickness) {
            require(thickness >= 0) { "Frame thickness can't be negative" }
            mViewFinderView!!.frameThickness = thickness
        }

    @get:Px
    var frameCornersSize: Int
        get() = mViewFinderView!!.frameCornersSize
        set(size) {
            require(size >= 0) { "Frame corners size can't be negative" }
            mViewFinderView!!.frameCornersSize = size
        }

    @get:Px
    var frameCornersRadius: Int
        get() = mViewFinderView!!.frameCornersRadius
        set(radius) {
            require(radius >= 0) { "Frame corners radius can't be negative" }
            mViewFinderView!!.frameCornersRadius = radius
        }

    var isFrameCornersCapRounded: Boolean
        get() = mViewFinderView!!.isFrameCornersCapRounded
        set(rounded) {
            mViewFinderView!!.isFrameCornersCapRounded = rounded
        }

    @get:FloatRange(from = 0.1, to = 1.0)
    var frameSize: Float
        get() = mViewFinderView!!.frameSize
        set(size) {
            require(!(size < 0.1 || size > 1)) { "Max frame size value should be between 0.1 and 1, inclusive" }
            mViewFinderView!!.frameSize = size
        }

    @get:FloatRange(from = 0.0, to = 1.0)
    var frameVerticalBias: Float
        get() = mViewFinderView!!.frameVerticalBias
        set(bias) {
            require(!(bias < 0f || bias > 1f)) { "Max frame size value should be between 0 and 1, inclusive" }
            mViewFinderView!!.frameVerticalBias = bias
        }

    @get:FloatRange(from = 0.0, fromInclusive = false)
    var frameAspectRatioWidth: Float
        get() = mViewFinderView!!.frameAspectRatioWidth
        set(ratioWidth) {
            require(ratioWidth > 0) { "Frame aspect ratio values should be greater than zero" }
            mViewFinderView!!.frameAspectRatioWidth = ratioWidth
        }

    @get:FloatRange(from = 0.0, fromInclusive = false)
    var frameAspectRatioHeight: Float
        get() = mViewFinderView!!.frameAspectRatioHeight
        set(ratioHeight) {
            require(ratioHeight > 0) { "Frame aspect ratio values should be greater than zero" }
            mViewFinderView!!.frameAspectRatioHeight = ratioHeight
        }


    fun setFrameAspectRatio(
        @FloatRange(from = 0.0, fromInclusive = false) ratioWidth: Float,
        @FloatRange(from = 0.0, fromInclusive = false) ratioHeight: Float
    ) {
        require(!(ratioWidth <= 0 || ratioHeight <= 0)) { "Frame aspect ratio values should be greater than zero" }
        mViewFinderView!!.setFrameAspectRatio(ratioWidth, ratioHeight)
    }

    internal class ViewFinderView(context: Context) : View(context) {
        private val mMaskPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val mFramePaint: Paint
        private val mPath: Path
        var frameRect: Rect? = null
            private set
        private var mFrameCornersSize = 0
        private var mFrameCornersRadius = 0
        private var mFrameRatioWidth = 1f
        private var mFrameRatioHeight = 1f
        private var mFrameSize = 0.75f
        private var mFrameVerticalBias = 0.5f
        private var mMaskVisible = true
        var isFrameVisible = true

        private var animator: ValueAnimator? = null
        private var linePosition: Float = 0f

        init {
            mMaskPaint.style = Paint.Style.FILL
            mFramePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mFramePaint.style = Paint.Style.STROKE
            val path = Path()
            path.fillType = Path.FillType.EVEN_ODD
            mPath = path
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            invalidateFrameRect(measuredWidth, measuredHeight)
        }

        override fun onLayout(
            changed: Boolean, left: Int, top: Int, right: Int,
            bottom: Int
        ) {
            invalidateFrameRect(right - left, bottom - top)
        }


        override fun onDraw(canvas: Canvas) {
            val frame = frameRect ?: return
            val width = width.toFloat()
            val height = height.toFloat()
            val top: Float = frame.top.toFloat()
            val left: Float = frame.left.toFloat()
            val right: Float = frame.right.toFloat()
            val bottom: Float = frame.bottom.toFloat()
            val frameCornersSize = mFrameCornersSize.toFloat()-50
            val frameCornersRadius = mFrameCornersRadius.toFloat()
            val maskVisible = mMaskVisible
            val frameVisible = isFrameVisible
            val path: Path = mPath
            if (frameCornersRadius > 0) {
                val normalizedRadius = frameCornersRadius.coerceAtMost((frameCornersSize - 1).coerceAtLeast(0f))
                if (maskVisible) {
                    path.reset()
                    path.moveTo(left, top + normalizedRadius)
                    path.quadTo(left, top, left + normalizedRadius, top)
                    path.lineTo(right - normalizedRadius, top)
                    path.quadTo(right, top, right, top + normalizedRadius)
                    path.lineTo(right, bottom - normalizedRadius)
                    path.quadTo(right, bottom, right - normalizedRadius, bottom)
                    path.lineTo(left + normalizedRadius, bottom)
                    path.quadTo(left, bottom, left, bottom - normalizedRadius)
                    path.lineTo(left, top + normalizedRadius)
                    path.moveTo(0f, 0f)
                    path.lineTo(width, 0f)
                    path.lineTo(width, height)
                    path.lineTo(0f, height)
                    path.lineTo(0f, 0f)
                    canvas.drawPath(path, mMaskPaint)
                }
                if (frameVisible) {
                    path.reset()
                    path.moveTo(left, top + frameCornersSize)
                    path.lineTo(left, top + normalizedRadius)
                    path.quadTo(left, top, left + normalizedRadius, top)
                    path.lineTo(left + frameCornersSize, top)
                    path.moveTo(right - frameCornersSize, top)
                    path.lineTo(right - normalizedRadius, top)
                    path.quadTo(right, top, right, top + normalizedRadius)
                    path.lineTo(right, top + frameCornersSize)
                    path.moveTo(right, bottom - frameCornersSize)
                    path.lineTo(right, bottom - normalizedRadius)
                    path.quadTo(right, bottom, right - normalizedRadius, bottom)
                    path.lineTo(right - frameCornersSize, bottom)
                    path.moveTo(left + frameCornersSize, bottom)
                    path.lineTo(left + normalizedRadius, bottom)
                    path.quadTo(left, bottom, left, bottom - normalizedRadius)
                    path.lineTo(left, bottom - frameCornersSize)
                    canvas.drawPath(path, mFramePaint)
                }
            }
            else {
                if (maskVisible) {
                    path.reset()
                    path.moveTo(left, top)
                    path.lineTo(right, top)
                    path.lineTo(right, bottom)
                    path.lineTo(left, bottom)
                    path.lineTo(left, top)
                    path.moveTo(0f, 0f)
                    path.lineTo(width, 0f)
                    path.lineTo(width, height)
                    path.lineTo(0f, height)
                    path.lineTo(0f, 0f)
                    canvas.drawPath(path, mMaskPaint)
                }
                if (frameVisible) {
                    path.reset()
                    path.moveTo(left, top + frameCornersSize)
                    path.lineTo(left, top)
                    path.lineTo(left + frameCornersSize, top)
                    path.moveTo(right - frameCornersSize, top)
                    path.lineTo(right, top)
                    path.lineTo(right, top + frameCornersSize)
                    path.moveTo(right, bottom - frameCornersSize)
                    path.lineTo(right, bottom)
                    path.lineTo(right - frameCornersSize, bottom)
                    path.moveTo(left + frameCornersSize, bottom)
                    path.lineTo(left, bottom)
                    path.lineTo(left, bottom - frameCornersSize)
                    canvas.drawPath(path, mFramePaint)
                }
            }

            if (isFrameVisible){
                frameRect?.let {
                    val centerY = (it.top + it.bottom) / 2f
                    val topY = centerY - linePosition
                    val bottomY = centerY - linePosition
                    canvas.drawLine(
                        it.left.toFloat()-50,
                        topY,
                        it.right.toFloat()+50,
                        bottomY,
                        mFramePaint
                    )
                    startLineAnimation()
                }
            }

        }

        fun setFrameAspectRatio(
            @FloatRange(from = 0.0, fromInclusive = false) ratioWidth: Float,
            @FloatRange(from = 0.0, fromInclusive = false) ratioHeight: Float
        ) {
            mFrameRatioWidth = ratioWidth
            mFrameRatioHeight = ratioHeight
            invalidateFrameRect()
            if (isLaidOut) {
                invalidate()
            }
        }

        private fun startLineAnimation() {
            val frameHeight = frameRect?.height() ?: 0
            if (frameHeight > 0 && animator == null) {
                animator = ValueAnimator.ofFloat( 0f,frameHeight/3f)
                animator?.duration = 1000 // Set the animation duration as needed
                animator?.repeatMode = ValueAnimator.REVERSE
                animator?.repeatCount = ValueAnimator.INFINITE
                animator?.addUpdateListener { animation ->
                    linePosition = animation.animatedValue as Float - 100
                    invalidate()
                }
                animator?.start()
            }
        }



        @get:FloatRange(from = 0.0, fromInclusive = false)
        var frameAspectRatioWidth: Float
            get() = mFrameRatioWidth
            set(ratioWidth) {
                mFrameRatioWidth = ratioWidth
                invalidateFrameRect()
                if (isLaidOut) {
                    invalidate()
                }
            }

        @get:FloatRange(from = 0.0, fromInclusive = false)
        var frameAspectRatioHeight: Float
            get() = mFrameRatioHeight
            set(ratioHeight) {
                mFrameRatioHeight = ratioHeight
                invalidateFrameRect()
                if (isLaidOut) {
                    invalidate()
                }
            }


        @get:ColorInt
        var maskColor: Int
            get() = mMaskPaint.color
            set(color) {
                mMaskPaint.color = color
                if (isLaidOut) {
                    invalidate()
                }
            }
        var isMaskVisible: Boolean
            get() = mMaskVisible
            set(visible) {
                mMaskVisible = visible
                if (isLaidOut) {
                    invalidate()
                }
            }

        @get:ColorInt
        var frameColor: Int
            get() = mFramePaint.color
            set(color) {
                mFramePaint.color = color
                if (isLaidOut) {
                    invalidate()
                }
            }

        @get:Px
        var frameThickness: Int
            get() = mFramePaint.strokeWidth.toInt()
            set(thickness) {
                mFramePaint.strokeWidth = thickness.toFloat()
                if (isLaidOut) {
                    invalidate()
                }
            }
        var isFrameCornersCapRounded: Boolean
            get() = mFramePaint.strokeCap == Paint.Cap.ROUND
            set(rounded) {
                mFramePaint.strokeCap = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
                invalidate()
            }

        @get:Px
        var frameCornersSize: Int
            get() = mFrameCornersSize
            set(size) {
                mFrameCornersSize = size
                if (isLaidOut) {
                    invalidate()
                }
            }

        @get:Px
        var frameCornersRadius: Int
            get() = mFrameCornersRadius
            set(radius) {
                mFrameCornersRadius = radius
                if (isLaidOut) {
                    invalidate()
                }
            }

        @get:FloatRange(from = 0.1, to = 1.0)
        var frameSize: Float
            get() = mFrameSize
            set(size) {
                mFrameSize = size
                invalidateFrameRect()
                if (isLaidOut) {
                    invalidate()
                }
            }

        @get:FloatRange(from = 0.0, to = 1.0)
        var frameVerticalBias: Float
            get() = mFrameVerticalBias
            set(bias) {
                mFrameVerticalBias = bias
                invalidateFrameRect()
                if (isLaidOut) {
                    invalidate()
                }
            }

        private fun invalidateFrameRect(width: Int = getWidth(), height: Int = getHeight()) {
            if (width > 0 && height > 0) {
                val viewAR = width.toFloat() / height.toFloat()
                val frameAR = mFrameRatioWidth / mFrameRatioHeight
                val frameSize = mFrameSize
                val frameWidth: Int
                val frameHeight: Int
                if (viewAR <= frameAR) {
                    frameWidth = (width * frameSize).roundToInt()
                    frameHeight = (frameWidth / frameAR).roundToInt()
                } else {
                    frameHeight = (height * frameSize).roundToInt()
                    frameWidth = (frameHeight * frameAR).roundToInt()
                }
                val frameLeft = (width - frameWidth) / 2
                val frameTop = ((height - frameHeight) * mFrameVerticalBias).roundToInt()
                frameRect = Rect(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight)
            }
        }
    }
}