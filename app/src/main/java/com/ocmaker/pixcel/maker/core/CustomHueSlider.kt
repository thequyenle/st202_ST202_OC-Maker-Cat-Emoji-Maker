package com.ocmaker.pixcel.maker.core

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.ocmaker.pixcel.maker.R
import kotlin.math.floor

class CustomHueSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val thumbStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = dp(3f)
    }

    private val thumbFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.TRANSPARENT // muốn che nền thì đổi màu nền dialog
    }

    private val hueBitmap: Bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.full_hue_bitmap,
        BitmapFactory.Options().apply { inScaled = false }
    )

    private val hueMatrix = Matrix()
    private lateinit var hueShader: BitmapShader

    private val hsv = floatArrayOf(0f, 1f, 1f)

    var hue: Float = 0f
        private set

    var onHueChanged: ((hue: Float, argbColor: Int) -> Unit)? = null

    private var barCenterY = 0f
    private var barStartX = 0f
    private var barEndX = 0f
    private var barHeightHalf = 0f

    private var thumbX = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val contentW = w - paddingStart - paddingEnd
        val contentH = h - paddingTop - paddingBottom

        barHeightHalf = contentH * 0.5f
        barCenterY = paddingTop + barHeightHalf

        linePaint.strokeWidth = barHeightHalf * 2f

        barStartX = paddingStart + barHeightHalf
        barEndX = paddingStart + contentW - barHeightHalf

        // shader scale theo chiều ngang
        hueShader = BitmapShader(hueBitmap, Shader.TileMode.MIRROR, Shader.TileMode.REPEAT)
        hueMatrix.reset()
        hueMatrix.setTranslate(barStartX, 0f)
        hueMatrix.postScale(
            (barEndX - barStartX) / hueBitmap.width,
            1f,
            barStartX,
            0f
        )
        hueShader.setLocalMatrix(hueMatrix)
        linePaint.shader = hueShader

        // init thumb ở cuối (360)
        if (thumbX == 0f) thumbX = barEndX
        updateHueFromX(thumbX)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // bar
        canvas.drawLine(barStartX, barCenterY, barEndX, barCenterY, linePaint)

        // thumb vuông
        drawSquareThumb(canvas)
    }

    private fun drawSquareThumb(canvas: Canvas) {
        val thumbW = barHeightHalf * 0.9f
        val thumbH = barHeightHalf * 1.6f

        val left = thumbX - thumbW / 2f
        val top = barCenterY - thumbH / 2f
        val right = thumbX + thumbW / 2f
        val bottom = barCenterY + thumbH / 2f

        val rect = RectF(left, top, right, bottom)
        val corner = dp(4f) // muốn vuông hẳn thì 0f

        canvas.drawRoundRect(rect, corner, corner, thumbFillPaint)
        canvas.drawRoundRect(rect, corner, corner, thumbStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                thumbX = clamp(event.x, barStartX, barEndX)
                updateHueFromX(thumbX)
                invalidate()
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateHueFromX(x: Float) {
        val t = ((x - barStartX) / (barEndX - barStartX)).coerceIn(0f, 1f)
        hue = floor(360f * t)

        hsv[0] = hue
        val color = Color.HSVToColor(hsv)
        onHueChanged?.invoke(hue, color)
    }

    private fun clamp(v: Float, min: Float, max: Float): Float =
        v.coerceIn(min, max)

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}
