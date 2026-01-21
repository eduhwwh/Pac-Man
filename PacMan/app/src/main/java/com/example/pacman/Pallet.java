package com.example.pacman;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Pallet {

    float x, y;
    boolean eaten = false;

    public Pallet(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, 10, paint);
    }

    public RectF getRect() {
        return new RectF(x - 10, y - 10, x + 10, y + 10);
    }
}
