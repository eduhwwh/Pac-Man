package com.example.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Ghost {

    public int gridX, gridY;
    private int tileSize;

    private int direction = 0;
    private Random random = new Random();

    private long lastMove = 0;
    private final long moveDelay = 400;

    // DISTÂNCIA DE PERSEGUIÇÃO (em tiles)
    private final int chaseDistance = 5;

    public Ghost(int x, int y, int tileSize) {
        this.gridX = x;
        this.gridY = y;
        this.tileSize = tileSize;
        direction = random.nextInt(4);
    }

    public void update(boolean[][] blocked, int cols, int rows, PacMan pacMan) {

        long now = System.currentTimeMillis();
        if (now - lastMove < moveDelay) return;
        lastMove = now;

        // distância Manhattan (usada em jogos de grid)
        int dx = pacMan.gridX - gridX;
        int dy = pacMan.gridY - gridY;
        int distance = Math.abs(dx) + Math.abs(dy);

        boolean chasing = distance <= chaseDistance;

        if (chasing) {
            // 🔴 PERSEGUE O PAC-MAN
            if (Math.abs(dx) > Math.abs(dy)) {
                direction = dx > 0 ? 0 : 2;
            } else {
                direction = dy > 0 ? 1 : 3;
            }
        } else {
            // 🔵 ANDA ALEATORIAMENTE
            if (random.nextInt(4) == 0) {
                direction = random.nextInt(4);
            }
        }

        int nextX = gridX;
        int nextY = gridY;

        switch (direction) {
            case 0: nextX++; break;
            case 1: nextY++; break;
            case 2: nextX--; break;
            case 3: nextY--; break;
        }

        // bateu parede? troca direção
        if (nextX < 0 || nextY < 0 || nextX >= cols || nextY >= rows || blocked[nextX][nextY]) {
            direction = random.nextInt(4);
            return;
        }

        gridX = nextX;
        gridY = nextY;
    }

    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        paint.setColor(Color.RED);
        canvas.drawCircle(
                offsetX + gridX * tileSize + tileSize / 2f,
                offsetY + gridY * tileSize + tileSize / 2f,
                tileSize / 2.2f,
                paint
        );
    }

    public void reset() {
    }
}
