package com.example.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Wall {

    RectF rect;

    public Wall(float x, float y, float w, float h) {
        rect = new RectF(x, y, x + w, y + h);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.BLUE);
        canvas.drawRect(rect, paint);
    }

    public RectF getRect() {
        return rect;
    }
}
