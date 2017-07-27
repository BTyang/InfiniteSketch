package com.btyang.infinitesketch;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * 笔迹
 * Created by BTyang on 2017/7/27.
 */

public class Stroke {

    private int color;
    private List<PointF> points = new ArrayList<>();

    public Stroke(int color) {
        this.color = color;
    }

    public void addPoint(float x, float y) {
        points.add(new PointF(x, y));
    }

    public int getLength() {
        return points.size();
    }


    public List<PointF> getPoints() {
        return points;
    }

    public boolean isValid() {
        return points.size() >= 2;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
