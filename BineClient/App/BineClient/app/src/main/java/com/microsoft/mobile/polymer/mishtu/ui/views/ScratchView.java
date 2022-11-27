package com.microsoft.mobile.polymer.mishtu.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.microsoft.mobile.polymer.mishtu.R;
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils;

public class ScratchView extends View {
    private Drawable mDrawable;
    private float mScratchWidth;
    private Bitmap mBitmap;
    //A `Canvas` is a class holds the "draw" calls. To draw something, you
    // need 4 basic components: 1. A Bitmap to hold the pixels, 2. a Canvas
    // to host the draw calls (writing into the bitmap), 3. a drawing
    // primitive (e.g. Rect, Path, text, Bitmap), and 4. a paint
    // (to describe the colors and styles for the drawing).
    private Canvas mCanvas;
    //`Path` is a class that encapsulates compound geometric paths
    // comprising straight line segments, quadratic curves, and cubic curves.
    private Path mPath;
    //`Paint` is a class that holds the style and color information about
    // how to draw geometries, text and bitmaps.
    private Paint mInnerPaint;
    private Paint mOuterPaint;
    private OnScratchListener mListener;
    /**
     * We start by defining an OnScratchListener interface. It has a
     * method signature of the method we will implement.
     */
    public interface OnScratchListener {
        void onScratch(ScratchView scratchCard, float visiblePercent);
    }


    public ScratchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttr(context, attrs);
    }
    public ScratchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttr(context, attrs);
    }
    public ScratchView(Context context) {
        super(context);
        resolveAttr(context, null);
    }

    private void resolveAttr(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScratchView);
        mDrawable = typedArray.getDrawable(R.styleable.ScratchView_scratchDrawable);
        mScratchWidth = typedArray.getDimension(R.styleable.ScratchView_scratchWidth, AppUtils.Companion.dipToPx(context, 20));
        typedArray.recycle();
    }

    /**
     * Let's now create several public methods that can be used with
     * our ScratchCard
     * @param drawable - Drawable object
     */
    public void setScratchDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
    public void setScratchWidth(float width) {
        mScratchWidth = width;
    }
    public void setOnScratchListener(OnScratchListener listener) {
        mListener = listener;
    }

    /**
     * If the size of our ScratchCard changes.
     * @param width - new width
     * @param height - new height
     * @param oldWidth - oldWidth
     * @param oldHeight - old height
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        if (mBitmap != null)
            mBitmap.recycle();

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        if (mDrawable != null) {
            mDrawable.setBounds(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            mDrawable.draw(mCanvas);
        } else {
            mCanvas.drawColor(0xFFC0C0C0);
        }

        if (mPath == null) {
            mPath = new Path();
        }

        if (mInnerPaint == null) {
            mInnerPaint = new Paint();
            mInnerPaint.setAntiAlias(true);
            mInnerPaint.setDither(true);
            mInnerPaint.setStyle(Paint.Style.STROKE);
            mInnerPaint.setFilterBitmap(true);
            mInnerPaint.setStrokeJoin(Paint.Join.ROUND);
            mInnerPaint.setStrokeCap(Paint.Cap.ROUND);
            mInnerPaint.setStrokeWidth(mScratchWidth);
            mInnerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        if (mOuterPaint == null) {
            mOuterPaint = new Paint();
        }
    }

    private float mLastTouchX;
    private float mLastTouchY;

    /**
     * Now we need to implement a method to allow us handle touch screen
     * motion events.
     * @param event - MotionEvent, an Object used to report movement
     *             (mouse, pen, finger, trackball) events.
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentTouchX = event.getX();
        float currentTouchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.reset();
                mPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(currentTouchX - mLastTouchX);
                float dy = Math.abs(currentTouchY - mLastTouchY);
                if (dx >= 4 || dy >= 4) {
                    float x1 = mLastTouchX;
                    float y1 = mLastTouchY;
                    float x2 = (currentTouchX + mLastTouchX) / 2;
                    float y2 = (currentTouchY + mLastTouchY) / 2;
                    mPath.quadTo(x1, y1, x2, y2);
                }
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(currentTouchX, currentTouchY);
                int width = mBitmap.getWidth();
                int height = mBitmap.getHeight();
                int total = width * height;
                int count = 0;
                for (int i = 0; i < width; i += 3) {
                    for (int j = 0; j < height; j += 3) {
                        if (mBitmap.getPixel(i, j) == 0x00000000)
                            count++;
                    }
                }
                float revealPerc = ((float) count) / total * 9;
                if (revealPerc < 25) clear();
                if (mListener != null) {
                    mListener.onScratch(this, revealPerc);
                }
                break;
        }

        mCanvas.drawPath(mPath, mInnerPaint);

        mLastTouchX = currentTouchX;
        mLastTouchY = currentTouchY;

        //Invalidate the whole view.
        invalidate();
        return true;
    }

    public void clear() {
        int left = 0;
        int top = 0;
        int right = getWidth();
        int bottom = getHeight();
        int width = right - left;
        int height = bottom - top;
        int centerX = left + width / 2;
        int centerY = top + height / 2;
        left = centerX - width / 2;
        top = centerY - height / 2;
        right = left + width;
        bottom = top + height;
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(
                PorterDuff.Mode.CLEAR
        ));
        mCanvas.drawRect(left, top, right, bottom, paint);
        invalidate();
    }

    /**
     * Now we implement a method allowing us do the drawing.
     * @param canvas - Canvas Object
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mOuterPaint);
        super.onDraw(canvas);
    }

    /**
     * Let's override a method to be called when the view is detached from a window.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
