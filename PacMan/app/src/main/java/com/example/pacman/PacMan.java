package com.example.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class PacMan {

    // posição em células (grid)
    int gridX;
    int gridY;

    // posição em pixels
    private float x, y;

    private int tileSize;
    private int size;
    private float speed;

    // direções: 0-dir, 1-baixo, 2-esq, 3-cima, -1 parado
    private int dir = -1;
    private int queuedDir = -1;

    private final int[] dx = {1, 0, -1, 0};
    private final int[] dy = {0, 1, 0, -1};

    public PacMan(int startGridX, int startGridY, int tileSize) {
        this.tileSize = tileSize;
        this.size = tileSize - 12;
        this.speed = Math.max(2f, tileSize / 12f);

        setGridPosition(startGridX, startGridY);
    }

    // ===============================
    // UPDATE
    // ===============================
    public void update(boolean[][] blocked, int cols, int rows) {

        boolean centerAligned =
                Math.abs((x + size / 2f) - (gridX * tileSize + tileSize / 2f)) < 1f &&
                        Math.abs((y + size / 2f) - (gridY * tileSize + tileSize / 2f)) < 1f;

        if (centerAligned) {
            // trava no centro do tile
            alignToGrid();

            if (queuedDir != -1 && canMove(queuedDir, blocked, cols, rows)) {
                dir = queuedDir;
                queuedDir = -1;
            } else if (dir != -1 && !canMove(dir, blocked, cols, rows)) {
                dir = -1;
            }
        }

        if (dir != -1) {
            x += dx[dir] * speed;
            y += dy[dir] * speed;

            int newGridX = (int) ((x + size / 2f) / tileSize);
            int newGridY = (int) ((y + size / 2f) / tileSize);

            if (newGridX >= 0 && newGridX < cols) gridX = newGridX;
            if (newGridY >= 0 && newGridY < rows) gridY = newGridY;
        }
    }

    private boolean canMove(int d, boolean[][] blocked, int cols, int rows) {
        int nx = gridX + dx[d];
        int ny = gridY + dy[d];
        if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) return false;
        return !blocked[nx][ny];
    }

    // ===============================
    // CONTROLES
    // ===============================
    public void setDirection(int direction) {
        queuedDir = direction;
    }

    // ===============================
    // RESPAWN / RESET ✅
    // ===============================
    public void setGridPosition(int gx, int gy) {
        this.gridX = gx;
        this.gridY = gy;
        resetPixelPosition();
        dir = -1;
        queuedDir = -1;
    }

    public void resetPixelPosition() {
        alignToGrid();
    }

    private void alignToGrid() {
        x = gridX * tileSize + (tileSize - size) / 2f;
        y = gridY * tileSize + (tileSize - size) / 2f;
    }

    public void reset(int startX, int startY) {
        setGridPosition(startX, startY);
    }

    // ===============================
    // DRAW
    // ===============================
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(
                x + size / 2f,
                y + size / 2f,
                size / 2f,
                paint
        );
    }

    public RectF getRect() {
        return new RectF(x, y, x + size, y + size);
    }
}
