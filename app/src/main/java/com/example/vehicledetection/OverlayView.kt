package com.example.vehicledetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var results: List<BoundingBox> = emptyList()
    private var frameWidth: Int = 1
    private var frameHeight: Int = 1

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 44f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#AA00C853")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setResults(boxes: List<BoundingBox>, width: Int, height: Int) {
        results = boxes
        frameWidth = width
        frameHeight = height
        invalidate()
    }

    fun clear() {
        results = emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (results.isEmpty()) return

        // Hitung skala agar kotak sesuai dengan PreviewView (ScaleType.FILL_CENTER)
        val scale = maxOf(width.toFloat() / frameWidth, height.toFloat() / frameHeight)
        val offsetX = (width - frameWidth * scale) / 2f
        val offsetY = (height - frameHeight * scale) / 2f

        for (box in results) {
            // Koordinat box dari detector adalah 0..640
            // Pertama, kembalikan ke ukuran frame asli
            val x1 = (box.x1 / 640f) * frameWidth
            val y1 = (box.y1 / 640f) * frameHeight
            val x2 = (box.x2 / 640f) * frameWidth
            val y2 = (box.y2 / 640f) * frameHeight

            // Kemudian terapkan skala dan offset FILL_CENTER
            val left = x1 * scale + offsetX
            val top = y1 * scale + offsetY
            val right = x2 * scale + offsetX
            val bottom = y2 * scale + offsetY

            canvas.drawRect(left, top, right, bottom, boxPaint)

            val text = "${box.label} ${(box.score * 100).toInt()}%"

            val textWidth = textPaint.measureText(text)
            val textHeight = textPaint.textSize

            val bgTop = maxOf(0f, top - textHeight - 10)

            canvas.drawRect(
                RectF(
                    left,
                    bgTop,
                    left + textWidth + 20,
                    bgTop + textHeight + 10
                ),
                bgPaint
            )

            canvas.drawText(
                text,
                left + 10,
                bgTop + textHeight,
                textPaint
            )
        }
    }

}