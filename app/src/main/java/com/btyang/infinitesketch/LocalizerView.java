package com.btyang.infinitesketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 定位器
 * Created by Mr.Yang on 2017/7/31.
 */

public class LocalizerView extends View {

    private int mWidth, mHeight;
    private int mBoardWidth, mBoardHeight;
    private float offsetX, offsetY;
    private float offsetX2, offsetY2;
    private Bitmap thumbnailBitmap;//缩略图文件
    private RectF localizerRect;
    private RectF thumbnailRect;
    private RectF strokeRangeRect;
    private RectF canvasRect;
    private RectF borderRect;//边框
    private Paint outlinePaint = new Paint();
    private PointF startPoint;

    private OnPositionChangeListener onPositionChangeListener;

    public LocalizerView(Context context) {
        super(context);
        init();
    }

    public LocalizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LocalizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        thumbnailRect = new RectF();
        localizerRect = new RectF();
        strokeRangeRect = new RectF();
        canvasRect = new RectF();
        borderRect = new RectF();
        outlinePaint.setStyle(Paint.Style.STROKE);
    }

    public void initialize(int boardWidth, int canvasHeight) {
        mBoardWidth = boardWidth;
        mBoardHeight = canvasHeight;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        borderRect.right = borderRect.left + (right - left);
        borderRect.bottom = borderRect.top + (bottom - top);
        mWidth = right - left - 1;
        mHeight = bottom - top - 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawBorder(canvas);
        drawThumbnail(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }

    private void drawBorder(Canvas canvas) {
        canvas.drawRect(borderRect, outlinePaint);
    }

    private void drawThumbnail(Canvas canvas) {
        if (thumbnailBitmap != null && !thumbnailBitmap.isRecycled()) {

            float widthRatio = 1f * mWidth / thumbnailBitmap.getWidth();
            float heightRatio = 1f * mHeight / thumbnailBitmap.getHeight();
            float scaleRatio = widthRatio < heightRatio ? widthRatio : heightRatio;

            float thumbnailWidth = thumbnailBitmap.getWidth() * scaleRatio;
            float thumbnailHeight = thumbnailBitmap.getHeight() * scaleRatio;
            thumbnailRect.left = (mWidth - thumbnailWidth) / 2;
            thumbnailRect.right = thumbnailRect.left + thumbnailWidth;
            thumbnailRect.top = (mHeight - thumbnailHeight) / 2;
            thumbnailRect.bottom = thumbnailRect.top + thumbnailHeight;
            //绘制缩略图
            canvas.drawBitmap(thumbnailBitmap, null, thumbnailRect, null);
            //绘制缩略图边框
//            canvas.drawRect(thumbnailRect, outlinePaint);
            //绘制定位器
            float leftPercent = 1f * (offsetX - canvasRect.left) / getCanvasWidth();
            float topPercent = 1f * (offsetY - canvasRect.top) / getCanvasHeight();
            float widthPercent = 1f * mBoardWidth / getCanvasWidth();
            float heightPercent = 1f * mBoardHeight / getCanvasHeight();

            float localizerWidth = (thumbnailRect.right - thumbnailRect.left) * widthPercent;
            float localizerHeight = (thumbnailRect.bottom - thumbnailRect.top) * heightPercent;
            localizerRect.left = (int) (leftPercent * (thumbnailRect.right - thumbnailRect.left) + thumbnailRect.left);
            localizerRect.right = (int) (localizerRect.left + localizerWidth);
            localizerRect.top = (int) (topPercent * (thumbnailRect.bottom - thumbnailRect.top) + thumbnailRect.top);
            localizerRect.bottom = (int) (localizerRect.top + localizerHeight);
            canvas.drawRect(localizerRect, outlinePaint);

        }
    }

    /**
     * external canvas width
     *
     * @return
     */
    private int getCanvasWidth() {
        int canvasWidth = (int) (canvasRect.right - canvasRect.left);
        if (canvasWidth == 0) {
            canvasWidth = mWidth;
        }
        return canvasWidth;
    }

    private int getCanvasHeight() {
        int canvasHeight = (int) (canvasRect.bottom - canvasRect.top);
        if (canvasHeight == 0) {
            canvasHeight = mHeight;
        }
        return canvasHeight;
    }

    public void notifyPositionChange(RectF canvasRect, float offsetX, float offsetY, Bitmap thumbnailBitmap) {
        this.canvasRect = canvasRect;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.thumbnailBitmap = thumbnailBitmap;
        invalidate();
    }

    public void setOnPositionChangeListener(OnPositionChangeListener onPositionChangeListener) {
        this.onPositionChangeListener = onPositionChangeListener;
    }

    interface OnPositionChangeListener {
        void onPositionChanged(float offsetX, float offsetY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPoint = new PointF(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float tempX = offsetX2 + startPoint.x - event.getX();
                float tempY = offsetY2 + startPoint.y - event.getY();
                startPoint = new PointF(event.getX(), event.getY());
//                Log.e("onTouchEvent", offsetX2 + "," + offsetY2);
                calculateOffset(tempX, tempY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startPoint = null;
                break;
        }
        invalidate();
        return true;
    }

    private void calculateOffset(float tempX, float tempY) {
        tempX *= 0.5;
        tempY *= 0.5;
        float curLeft = localizerRect.left;
        float curTop = localizerRect.top;
        float width = localizerRect.right - localizerRect.left;
        float height = localizerRect.bottom - localizerRect.top;
        if ((curLeft - tempX) > thumbnailRect.left && (curLeft - tempX) + width < thumbnailRect.right) {
            offsetX2 = tempX;
            offsetX = ((curLeft - offsetX2) - thumbnailRect.left) / (thumbnailRect.right - thumbnailRect.left) * getCanvasWidth() + canvasRect.left;
        }
        if ((curTop - tempY) > thumbnailRect.top && (curTop - tempY) + height < thumbnailRect.bottom) {
            offsetY2 = tempY;
            offsetY = ((curTop - offsetY2) - thumbnailRect.top) / (thumbnailRect.bottom - thumbnailRect.top) * getCanvasHeight() + canvasRect.top;

        }
        Log.e("calculateOffset", offsetX2 + "," + offsetY2);
        if (onPositionChangeListener != null) {
            onPositionChangeListener.onPositionChanged(offsetX, offsetY);
        }
    }
}
