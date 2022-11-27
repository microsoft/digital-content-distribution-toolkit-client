package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class RectangleOverlayView extends LinearLayout {
    private  Bitmap bitmap;
    private Rect overlayRect = new Rect();
    private Paint mBackgroundPaint;
    //private ToolTip toolTip;

    public RectangleOverlayView(Context context) {
        super(context);
    }

    public RectangleOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RectangleOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RectangleOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes,Rect rect) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setRect(rect);
    }

    private void setRect(Rect rect)
    {
        overlayRect = rect;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (bitmap == null) {
            createWindowFrame(overlayRect);
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    public void DrawRectangleOverlay(final ToolTip toolTip) {
        //this.toolTip =  toolTip;
        int[] loc = new int[2];
        toolTip.getAnchorView().getLocationOnScreen(loc);
        overlayRect.left = 0;
        overlayRect.top = 0;//loc[1]==0 ?  toolTip.getAnchorViewY()-120 : loc[1]-120;
        overlayRect.right= getWidth();
        overlayRect.bottom = getHeight();
        new RectangleOverlayView(getContext(),null,0,0,overlayRect);
    }

    protected void createWindowFrame(Rect rect) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#00000000"));
        paint.setColor(Color.TRANSPARENT);
        //paint.setAlpha(99);
        osCanvas.drawRect(outerRectangle, paint);

        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        osCanvas.drawRect(rect.left,rect.top,getWidth(),rect.bottom,paint);


        /*// Load the bitmap as a shader to the paint.
        paint.setColor(Color.parseColor("#00000000"));
        paint.setAlpha(99);
        final Shader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        osCanvas.drawRect(toolTip.getAnchorView().getLeft(),
                toolTip.getAnchorView().getTop(),
                toolTip.getAnchorView().getRight(),
                toolTip.getAnchorView().getBottom(),
                paint);*/
    }

    /*private int getRelativeLeft(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    private int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }*/

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }
}
