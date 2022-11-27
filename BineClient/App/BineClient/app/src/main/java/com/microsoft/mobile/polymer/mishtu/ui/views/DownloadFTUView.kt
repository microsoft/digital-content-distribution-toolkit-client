package com.microsoft.mobile.polymer.mishtu.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import android.graphics.PorterDuff

import android.graphics.PorterDuffXfermode

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.microsoft.mobile.polymer.mishtu.R
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class DownloadFTUView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyle: Int = 0,
                                                private val anchorView: View):
    FrameLayout(context, attrs, defStyle) {

    private var bitmap: Bitmap? = null
    private val overlayRect = Rect()

    init {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        overlayRect.left = 0
        overlayRect.top = 0
        overlayRect.right = width
        overlayRect.bottom = height
    }

    private fun createWindowFrame(rect: Rect) {
        if (width == 0 || height == 0) return
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val osCanvas = Canvas(bitmap!!)
        val outerRectangle = RectF(0F, 0F, width.toFloat(), height.toFloat())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#00000000")
        paint.alpha = 200
        osCanvas.drawRect(outerRectangle, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        osCanvas.drawRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            paint
        )

        // Load the bitmap as a shader to the paint.
        paint.color = Color.parseColor("#00000000")
        paint.alpha = 99
        val shader = BitmapShader(bitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader

        val loc = IntArray(2)
        anchorView.getLocationOnScreen(loc)

        osCanvas.drawRect(anchorView.left.toFloat(),
            loc[1].toFloat() - context.resources.getDimension(R.dimen.dp_30).roundToInt(),
            anchorView.right.toFloat(),
            loc[1].toFloat() + anchorView.height,
            paint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (bitmap == null) {
            createWindowFrame(overlayRect)
        }
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }
}