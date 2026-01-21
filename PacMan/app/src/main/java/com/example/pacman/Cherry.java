package com.example.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Cherry {
    public float x, y;
    public boolean eaten = false;
    private float radius = 12f;

    public Cherry(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public RectF getRect() {
        return new RectF(x - radius, y - radius, x + radius, y + radius);
    }

    public void draw(Canvas canvas, Paint paint) {
        if (eaten) return;

        paint.setColor(Color.RED);
        canvas.drawCircle(x, y, radius, paint);

        paint.setColor(Color.GREEN);
        canvas.drawRect(x - 4, y - radius - 6, x + 4, y - radius + 2, paint);
    }
}
