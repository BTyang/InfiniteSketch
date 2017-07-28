package com.btyang.infinitesketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.btyang.infinitesketch.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 画布
 * Created by BTyang on 2017/7/27.
 */

public class InfiniteCanvas extends View {

    private final static int DEFAULT_COLOR = Color.BLACK;

    private Paint drawPaint = new Paint();
    private Paint outlinePaint = new Paint();
    private PointF startPoint;
    private float offsetX, offsetY;
    private int curColor = DEFAULT_COLOR;
    private Stroke curStroke;
    private Bitmap thumbnailBM;//缩略图文件
    private Rect localizerRect;
    private Rect thumbnailRect;
    private RectF strokeRangeRect;
    private List<Stroke> strokes = new ArrayList<>();
    private int mWidth, mHeight;

    public InfiniteCanvas(Context context) {
        super(context);
        init();
    }

    public InfiniteCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InfiniteCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        drawPaint.setStrokeWidth(0);
        drawPaint.setAntiAlias(true);
        thumbnailRect = new Rect();
        localizerRect = new Rect();
        strokeRangeRect = new RectF();
        outlinePaint.setStyle(Paint.Style.STROKE);
    }

    private void addPoint(float x, float y) {
        if (curStroke == null && strokes.size() > 0) {
            curStroke = strokes.get(strokes.size() - 1);
        }
        if (curStroke == null) {
            return;
        }
        curStroke.addPoint(x + offsetX, y + offsetY);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = right - left;
        mHeight = bottom - top;
        thumbnailRect.right = (int) (mWidth / 4f);
        thumbnailRect.bottom = (int) (mHeight / 4f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPoint = new PointF(event.getX(), event.getY());
                curStroke = new Stroke(curColor);
                strokes.add(curStroke);
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() > 1) {
                    offsetX += startPoint.x - event.getX();
                    offsetY += startPoint.y - event.getY();
                    startPoint = new PointF(event.getX(), event.getY());
                } else {
                    addPoint(event.getX(), event.getY());
                }
                updateStrokeRange(event);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startPoint = null;
                createCurThumbnailBM();
                updateStrokeRange(event);
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        canvas.translate(-offsetX, -offsetY);
        drawRecord(canvas);
        drawThumbnail(canvas);
        drawTestArgs(canvas);
    }


    private void updateStrokeRange(MotionEvent event) {
        float curX = event.getX() + offsetX;
        float curY = event.getY() + offsetY;
        if (curX < strokeRangeRect.left) {
            strokeRangeRect.left = (int) curX;
        }
        if (curX > strokeRangeRect.right) {
            strokeRangeRect.right = (int) curX;
        }
        if (curY > strokeRangeRect.bottom) {
            strokeRangeRect.bottom = (int) curY;
        }
        if (curY < strokeRangeRect.top) {
            strokeRangeRect.top = (int) curY;
        }
        if (strokeRangeRect.right < mWidth) {
            strokeRangeRect.right = mWidth;
        }
        if (strokeRangeRect.bottom < mHeight) {
            strokeRangeRect.bottom = mHeight;
        }
    }

    private void drawTestArgs(Canvas canvas) {
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.drawText("range :  " + strokeRangeRect.left + "  ,  " + strokeRangeRect.top + "  ,  " + strokeRangeRect.right + "  ,  " + strokeRangeRect.bottom, mWidth - 300, 50, drawPaint);
        canvas.restore();
    }

    private void drawThumbnail(Canvas canvas) {
        canvas.save();
        canvas.translate(offsetX, offsetY);
        if (thumbnailBM != null && !thumbnailBM.isRecycled()) {
            int left = 50, top = 50;
            //绘制缩略图
            canvas.drawBitmap(thumbnailBM, left, top, drawPaint);
            //绘制缩略图边框
            thumbnailRect.left = left;
            thumbnailRect.right = left + thumbnailBM.getWidth();
            thumbnailRect.top = top;
            thumbnailRect.bottom = top + thumbnailBM.getHeight();
            canvas.drawRect(thumbnailRect, outlinePaint);
            //绘制定位器
            float leftPercent = 1f * (offsetX - strokeRangeRect.left) / getBoardWidth();
            float topPercent = 1f * (offsetY - strokeRangeRect.top) / getBoardHeight();
            float sizePercent = 1f * mWidth / getBoardWidth();

            localizerRect.left = thumbnailRect.left;
            localizerRect.right = thumbnailRect.right;
            localizerRect.top = thumbnailRect.top;
            localizerRect.bottom = thumbnailRect.bottom;
        }
        canvas.restore();
    }

    private void drawRecord(Canvas canvas) {
        for (Stroke stroke : strokes) {
            drawPaint.setColor(stroke.getColor());
            PointF lastPoint = null;
            for (PointF point : stroke.getPoints()) {
                if (lastPoint == null) {
                    lastPoint = point;
                    continue;
                }
                canvas.drawLine(lastPoint.x, lastPoint.y, point.x, point.y, drawPaint);
                lastPoint = point;
            }
        }

    }

    /**
     * 绘制背景
     *
     * @param canvas
     */
    public void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }

    @NonNull
    public Bitmap getResultBitmap() {
        return getResultBitmap(null);
    }

    private int getBoardWidth() {
        int boardWidth = (int) (strokeRangeRect.right - strokeRangeRect.left);
        if (boardWidth == 0) {
            boardWidth = mWidth;
        }
        return boardWidth;
    }

    private int getBoardHeight() {
        int boardHeight = (int) (strokeRangeRect.bottom - strokeRangeRect.top);
        if (boardHeight == 0) {
            boardHeight = mHeight;
        }
        return boardHeight;
    }

    @NonNull
    public Bitmap getResultBitmap(Bitmap addBitmap) {
        Bitmap newBM = Bitmap.createBitmap(getBoardWidth(), getBoardHeight(), Bitmap.Config.RGB_565);
//        Bitmap newBM = Bitmap.createBitmap(1280, 800, Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(newBM);
//        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));//抗锯齿
        canvas.translate(-strokeRangeRect.left, -strokeRangeRect.top);
        //绘制背景
        drawBackground(canvas);
        drawRecord(canvas);

        if (addBitmap != null) {
            canvas.drawBitmap(addBitmap, 0, 0, null);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
//        Bitmap bitmap = BitmapUtils.createBitmapThumbnail(newBM, true, 800, 1280);//        return newBM;
        return newBM;
    }

    @NonNull
    public void createCurThumbnailBM() {
        thumbnailBM = getThumbnailResultBitmap();
    }

    @NonNull
    public Bitmap getThumbnailResultBitmap() {
        Bitmap bitmap = BitmapUtils.createBitmapThumbnail(getResultBitmap(), true, Math.round(getBoardWidth() / 4f), Math.round(getBoardHeight() / 4f));
        return bitmap;
    }

}
