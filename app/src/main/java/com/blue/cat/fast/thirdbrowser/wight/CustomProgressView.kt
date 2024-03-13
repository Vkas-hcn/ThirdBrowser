package com.blue.cat.fast.thirdbrowser.wight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import android.graphics.*

class CustomProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 60f
        strokeCap = Paint.Cap.ROUND
    }
    private val path = Path()
    private lateinit var pathMeasure: PathMeasure
    private var sweepGradient: SweepGradient? = null
    private val rectF = RectF()


    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 100f)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sweepGradient = SweepGradient(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            intArrayOf(
                Color.parseColor("#FF19C9ED"),
                Color.parseColor("#FF19C9ED"),
                Color.parseColor("#FF19C9ED"),
                Color.parseColor("#B32B73F5"),
                Color.parseColor("#B32B73F5"),
                Color.parseColor("#FF19C9ED")
            ),
            null
        )
        paint.shader = sweepGradient
        val radius = w.coerceAtMost(h) / 2f - paint.strokeWidth / 2
        rectF.set(
            width / 2f - radius,
            height / 2f - radius,
            width / 2f + radius,
            height / 2f + radius
        )
        path.addArc(rectF, 270f, -359.9f)
        pathMeasure = PathMeasure(path, false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val length = pathMeasure.length * progress / 100
        val start = (pathMeasure.length - length) / 2
        val stop = start + length
        val progressPath = Path()
        pathMeasure.getSegment(start, stop, progressPath, true)
        canvas.drawPath(progressPath, paint)
    }
}

