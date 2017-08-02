package com.btyang.infinitesketch;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * 笔迹
 * Created by BTyang on 2017/7/27.
 */

public class Stroke {

    private final static int DEFAULT_COLOR = Color.BLACK;
    private final static int DEFAULT_STROKE_WIDTH = 5;

    public Paint paint;//笔类
    public Path path = new Path();//画笔路径数据

    public Stroke() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(DEFAULT_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
    }

    public void setColor(int color) {
        this.paint.setColor(color);
    }

    public boolean isValid() {
        return !path.isEmpty();
    }

}
