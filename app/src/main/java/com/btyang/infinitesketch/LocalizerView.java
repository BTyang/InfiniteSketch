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

    private int mWidth, mHeight;//当前view的宽高
    private int mBoardWidth, mBoardHeight;//画板的物理宽高
    private float sketchOffsetX, sketchOffsetY;
    private Bitmap thumbnailBitmap;//缩略图文件
    private RectF localizerRect;//定位器范围
    private RectF thumbnailRect;//缩略图范围
    private RectF canvasRect;//画板的虚拟画布范围
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
        canvasRect = new RectF();
        borderRect = new RectF();
        outlinePaint.setStyle(Paint.Style.STROKE);
    }

    public void initialize(int boardWidth, int canvasHeight) {
        mBoardWidth = boardWidth;
        mBoardHeight = canvasHeight;
        //缩略图view的大小为画板view的1/4
        mWidth = (int) (mBoardWidth / 4f);
        mHeight = (int) (mBoardHeight / 4f);
        borderRect.right = borderRect.left + mWidth;
        borderRect.bottom = borderRect.top + mHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
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
            float canvasWidth = canvasRect.right - canvasRect.left;
            float canvasHeight = canvasRect.bottom - canvasRect.top;
            float widthRatio = 1f * mWidth / canvasWidth;
            float heightRatio = 1f * mHeight / canvasHeight;
            float scaleRatio = widthRatio < heightRatio ? widthRatio : heightRatio;

            float thumbnailWidth = canvasWidth * scaleRatio;
            float thumbnailHeight = canvasHeight * scaleRatio;
            int padding = 1;//可能是由于数值计算上的舍入误差，缩略图在某些情况下会盖住边框，所有这里预留1个像素的padding
            thumbnailRect.left = (mWidth - thumbnailWidth) / 2 + padding;
            thumbnailRect.right = thumbnailRect.left + (thumbnailWidth - padding * 2);
            thumbnailRect.top = (mHeight - thumbnailHeight) / 2 + padding;
            thumbnailRect.bottom = thumbnailRect.top + (thumbnailHeight - padding * 2);
            //绘制缩略图
            canvas.drawBitmap(thumbnailBitmap, null, thumbnailRect, null);
            //绘制缩略图边框
//            canvas.drawRect(thumbnailRect, outlinePaint);
            //绘制定位器
            float leftPercent = 1f * (sketchOffsetX - canvasRect.left) / getCanvasWidth();
            float topPercent = 1f * (sketchOffsetY - canvasRect.top) / getCanvasHeight();
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

    public void notifyPositionChange(RectF canvasRect, float sketchOffsetX, float sketchOffsetY, Bitmap thumbnailBitmap) {
        this.canvasRect = canvasRect;
        this.sketchOffsetX = sketchOffsetX;
        this.sketchOffsetY = sketchOffsetY;
        this.thumbnailBitmap = thumbnailBitmap;
        invalidate();
    }

    public void setOnPositionChangeListener(OnPositionChangeListener onPositionChangeListener) {
        this.onPositionChangeListener = onPositionChangeListener;
    }

    interface OnPositionChangeListener {
        void onPositionChanged(float sketchOffsetX, float sketchOffsetY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPoint = new PointF(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float offsetX = startPoint.x - event.getX();
                float offsetY = startPoint.y - event.getY();
                startPoint = new PointF(event.getX(), event.getY());
                calculateOffset(offsetX, offsetY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startPoint = null;
                break;
        }
        invalidate();
        return true;
    }
    //计算因拖动定位器产生的画布偏移
    private void calculateOffset(float offsetX, float offsetY) {
        float curLeft = localizerRect.left;
        float curTop = localizerRect.top;
        float width = localizerRect.right - localizerRect.left;
        float height = localizerRect.bottom - localizerRect.top;
        //定位器不能拖出缩略图的边界
        if ((curLeft - offsetX) > thumbnailRect.left && (curLeft - offsetX) + width < thumbnailRect.right) {
            sketchOffsetX = ((curLeft - offsetX) - thumbnailRect.left) / (thumbnailRect.right - thumbnailRect.left) * getCanvasWidth() + canvasRect.left;
        }
        if ((curTop - offsetY) > thumbnailRect.top && (curTop - offsetY) + height < thumbnailRect.bottom) {
            sketchOffsetY = ((curTop - offsetY) - thumbnailRect.top) / (thumbnailRect.bottom - thumbnailRect.top) * getCanvasHeight() + canvasRect.top;

        }
        Log.e("calculateOffset", offsetX + "," + offsetY);
        if (onPositionChangeListener != null) {
            onPositionChangeListener.onPositionChanged(sketchOffsetX, sketchOffsetY);
        }
    }
}
