package com.btyang.infinitesketch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 画布
 * Created by BTyang on 2017/7/27.
 */

public class InfiniteCanvas extends View {

    private final static int DEFAULT_COLOR = Color.BLACK;

    private Paint drawPaint = new Paint();
    private PointF startPoint;
    private float offsetX, offsetY;
    private int curColor = DEFAULT_COLOR;
    private Stroke curStroke;
    private List<Stroke> strokes = new ArrayList<>();

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
    }

    private void addPoint(float x, float y) {
        if (curStroke == null && strokes.size() > 0) {
            curStroke = strokes.get(strokes.size() - 1);
        }
        if (curStroke == null) {
            return;
        }
        curStroke.addPoint(x - offsetX, y - offsetY);
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
                    offsetX += event.getX() - startPoint.x;
                    offsetY += event.getY() - startPoint.y;
                    startPoint = new PointF(event.getX(), event.getY());
                } else {
                    addPoint(event.getX(), event.getY());
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                startPoint = null;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.translate(offsetX, offsetY);

        for (Stroke stroke : strokes) {
            drawPaint.setColor(stroke.getColor());
            drawStroke(canvas, stroke);
        }
    }

    private void drawStroke(Canvas canvas, Stroke stroke) {
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
