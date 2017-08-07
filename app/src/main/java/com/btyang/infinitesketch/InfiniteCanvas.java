package com.btyang.infinitesketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 画布
 * Created by BTyang on 2017/7/27.
 */

public class InfiniteCanvas extends View implements LocalizerView.OnPositionChangeListener {

    private Paint drawPaint = new Paint();
    private Paint outlinePaint = new Paint();
    private PointF startPoint;
    private float offsetX, offsetY;
    private Stroke curStroke;
    private Bitmap thumbnailBM;//缩略图文件
    private RectF strokeRangeRect;
    private RectF canvasRect;
    private List<Stroke> strokes = new ArrayList<>();
    private int mWidth, mHeight;
    private LocalizerView localizerView;
    private POINT_MODE mode = POINT_MODE.DRAW;
    public float downX, downY, preX, preY, curX, curY;

    @Override
    public void onPositionChanged(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        invalidate();
    }

    enum POINT_MODE {
        DRAG,
        DRAW
    }

    public void setMode(POINT_MODE mode) {
        this.mode = mode;
    }

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
        strokeRangeRect = new RectF();
        canvasRect = new RectF();
        outlinePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = right - left;
        mHeight = bottom - top;
        getLocalizerView().initialize(mWidth, mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        curX = event.getX() + offsetX;
        curY = event.getY() + offsetY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_down(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touch_up(event);
                break;
        }
        preX = curX;
        preY = curY;
        return true;
    }

    public void touch_down(MotionEvent event) {
        downX = curX;
        downY = curY;
        startPoint = new PointF(event.getX(), event.getY());
        curStroke = new Stroke();
        curStroke.path.moveTo(downX, downY);
        strokes.add(curStroke);
    }

    public void touch_move(MotionEvent event) {
        if (event.getPointerCount() > 1 || mode == POINT_MODE.DRAG) {
            offsetX += startPoint.x - event.getX();
            offsetY += startPoint.y - event.getY();
            startPoint = new PointF(event.getX(), event.getY());
        } else {
            addPoint();
            updateStrokeRange(event);
        }
        // TODO: 2017/8/2 每次拖动都更新缩略图的bitmap可以实时更新，但是会导致卡顿
        updateBoardRect();
        invalidate();
        getLocalizerView().notifyPositionChange(canvasRect, offsetX, offsetY, getThumbnailBitmap());
    }

    private void addPoint() {
        if (curStroke == null && strokes.size() > 0) {
            curStroke = strokes.get(strokes.size() - 1);
        }
        if (curStroke == null) {
            return;
        }
        curStroke.path.quadTo(preX, preY, (curX + preX) / 2, (curY + preY) / 2);
    }

    public void touch_up(MotionEvent event) {
        startPoint = null;
//        createCurThumbnailBM();
        updateBoardRect();
        invalidate();
        getLocalizerView().notifyPositionChange(canvasRect, offsetX, offsetY, getThumbnailBitmap());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        canvas.translate(-offsetX, -offsetY);
        drawRecord(canvas);
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

    private void updateBoardRect() {
        float minX = offsetX;
        float minY = offsetY;
        float maxX = mWidth + offsetX;
        float maxY = mHeight + offsetY;
        if (minX < canvasRect.left) {
            canvasRect.left = (int) minX;
        }
        if (maxX > canvasRect.right) {
            canvasRect.right = (int) maxX;
        }
        if (maxY > canvasRect.bottom) {
            canvasRect.bottom = (int) maxY;
        }
        if (minY < canvasRect.top) {
            canvasRect.top = (int) minY;
        }
        if (canvasRect.right < mWidth) {
            canvasRect.right = mWidth;
        }
        if (canvasRect.bottom < mHeight) {
            canvasRect.bottom = mHeight;
        }
        if (canvasRect.left < minX && canvasRect.left < strokeRangeRect.left) {
            canvasRect.left = minX;
        }
        if (canvasRect.top < minY && canvasRect.top < strokeRangeRect.top) {
            canvasRect.top = minY;
        }
        if (canvasRect.right > maxX && canvasRect.right > strokeRangeRect.right) {
            canvasRect.right = maxX;
        }
        if (canvasRect.bottom > maxY && canvasRect.bottom > strokeRangeRect.bottom) {
            canvasRect.bottom = maxY;
        }
    }

    private void drawTestArgs(Canvas canvas) {
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.drawText("range :  " + strokeRangeRect.left + "  ,  " + strokeRangeRect.top + "  ,  " + strokeRangeRect.right + "  ,  " + strokeRangeRect.bottom, mWidth - 300, 50, drawPaint);
        canvas.restore();
    }


    private void drawRecord(Canvas canvas) {
        for (Stroke stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }

    }

    private void drawScaledRecord(Canvas canvas, float scaleRatio) {
        canvas.save();
        canvas.translate(-canvasRect.left * scaleRatio, -canvasRect.top * scaleRatio);
        for (Stroke stroke : strokes) {
            canvas.drawPath(scalePath(stroke.path, scaleRatio), stroke.paint);
        }
        canvas.restore();

    }

    /**
     * 绘制背景
     *
     * @param canvas
     */
    public void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }

    private int getBoardWidth() {
        int boardWidth = (int) (canvasRect.right - canvasRect.left);
        if (boardWidth == 0) {
            boardWidth = mWidth;
        }
        return boardWidth;
    }

    private int getBoardHeight() {
        int boardHeight = (int) (canvasRect.bottom - canvasRect.top);
        if (boardHeight == 0) {
            boardHeight = mHeight;
        }
        return boardHeight;
    }

    float thumbnailScaleRatio = 1f;

    @NonNull
    public Bitmap getThumbnailBitmap() {
        thumbnailScaleRatio = Math.min(1f * mWidth / getBoardWidth(), 1f * mHeight / getBoardHeight());
        Bitmap newBM = Bitmap.createBitmap(getThumbnailWidth(), getThumbnailHeight(), Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(newBM);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));//抗锯齿
        //绘制背景
        drawBackground(canvas);
        drawScaledRecord(canvas, thumbnailScaleRatio);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newBM;
    }

    @NonNull
    public void createCurThumbnailBM() {
        thumbnailBM = getThumbnailBitmap();
    }

    private int getThumbnailWidth() {
        return (int) (getBoardWidth() * thumbnailScaleRatio);
    }

    private int getThumbnailHeight() {
        return (int) (getBoardHeight() * thumbnailScaleRatio);
    }

    /**
     * 缩放图形
     *
     * @param srcMatrix
     * @param scaleRatio
     * @return
     */
    private Matrix scaleMatrix(Matrix srcMatrix, float scaleRatio) {
        Matrix scaleMatrix = new Matrix(srcMatrix);
        scaleMatrix.postScale(scaleRatio, scaleRatio);
        return scaleMatrix;
    }


    /**
     * 缩放笔迹
     *
     * @param srcPath
     * @param scaleRatio
     * @return
     */
    private Path scalePath(Path srcPath, float scaleRatio) {
        Path newPath = new Path(srcPath);
        Matrix scaleMatrix = new Matrix();
//        RectF rectF = new RectF();
//        newPath.computeBounds(rectF, true);
        scaleMatrix.setScale(scaleRatio, scaleRatio, 0, 0);
        newPath.transform(scaleMatrix);
        return newPath;
    }

    /**
     * 缩放形状（直线、圆）
     *
     * @param srcRect
     * @param scaleRatio
     * @return
     */
    private RectF scaleRect(RectF srcRect, float scaleRatio) {
        RectF newRect = new RectF(srcRect);
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleRatio, scaleRatio, 0, 0);
        scaleMatrix.mapRect(newRect, srcRect);
        return newRect;
    }


    public LocalizerView getLocalizerView() {
        return localizerView;
    }

    public void setLocalizerView(LocalizerView localizerView) {
        this.localizerView = localizerView;
        getLocalizerView().setOnPositionChangeListener(this);
    }
}
