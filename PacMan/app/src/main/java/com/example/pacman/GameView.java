package com.example.pacman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.media.MediaPlayer;

import java.util.ArrayList;

public class GameView extends SurfaceView
        implements Runnable, SurfaceHolder.Callback {

    private Thread gameThread;
    private volatile boolean running = false; // volatile para thread-safety
    private boolean gameOver = false;

    private PacMan pacMan;
    private ArrayList<Ghost> ghosts;
    private ArrayList<Pallet> pellets;
    private ArrayList<Wall> walls;

    // ------------------- CEREJAS -------------------
    private ArrayList<Cherry> cherries; // 🍒

    private Paint paint;
    private SurfaceHolder holder;

    private int score = 0;
    private int level = 1;

    // vidas
    private int lives = 3;
    private final int maxLives = 3;

    // GRID
    private int tileSize;       // agora DINÂMICO
    private final int cols = 15;
    private final int rows = 13;

    private boolean[][] blocked;

    private int offsetX = 0;
    private int offsetY = 0;

    // posições iniciais
    private final int pacStartX = 1;
    private final int pacStartY = 1;

    //efeitos sonoros
    private MediaPlayer musicPlayer;


    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);

        pellets = new ArrayList<>();
        walls = new ArrayList<>();
        ghosts = new ArrayList<>();
        cherries = new ArrayList<>(); // inicializa lista de cerejas

        // 🎵 INICIAR MÚSICA (se existir R.raw.game_music)
        try {
            musicPlayer = MediaPlayer.create(getContext(), R.raw.game_music);
            if (musicPlayer != null) {
                musicPlayer.setLooping(true);
                musicPlayer.start();
            }
        } catch (Exception e) {
            // se não encontrar recurso de áudio, ignora (não quebra a execução)
            musicPlayer = null;
            e.printStackTrace();
        }

        // tileSize será definido em surfaceChanged()
    }

    // ================= MAPA =================
    private void createMap() {
        // certificar que tileSize já foi calculado
        if (tileSize <= 0) return;

        blocked = new boolean[cols][rows];

        for (int x = 0; x < cols; x++) {
            blocked[x][0] = true;
            blocked[x][rows - 1] = true;
        }
        for (int y = 0; y < rows; y++) {
            blocked[0][y] = true;
            blocked[cols - 1][y] = true;
        }

        for (int x = 3; x <= 11; x++) {
            blocked[x][3] = true;
            blocked[x][9] = true;
        }

        blocked[6][5] = true;
        blocked[8][5] = true;
        blocked[6][7] = true;
        blocked[8][7] = true;

        walls.clear();
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (blocked[x][y]) {
                    walls.add(new Wall(
                            x * tileSize,
                            y * tileSize,
                            tileSize,
                            tileSize
                    ));
                }
            }
        }

        pellets.clear();
        for (int x = 1; x < cols - 1; x++) {
            for (int y = 1; y < rows - 1; y++) {
                if (!blocked[x][y]) {
                    pellets.add(new Pallet(
                            x * tileSize + tileSize / 2f,
                            y * tileSize + tileSize / 2f
                    ));
                }
            }
        }

        // criar cerejas ao recriar mapa (se nível >= 2)
        createCherries();
    }

    // ================= FANTASMAS =================
    private void createGhosts() {
        ghosts.clear();

        if (tileSize <= 0) return;

        if (level == 1) {
            ghosts.add(new Ghost(cols - 2, rows - 2, tileSize));
        } else {
            ghosts.add(new Ghost(cols - 2, rows - 2, tileSize));
            ghosts.add(new Ghost(1, rows - 2, tileSize));
        }
    }

    private void resetPositions() {
        if (pacMan != null) pacMan.reset(pacStartX, pacStartY);
        for (Ghost ghost : ghosts) {
            if (ghost != null) ghost.reset();
        }
    }

    // ================= CEREJAS =================
    private void createCherries() {
        cherries.clear();

        if (tileSize <= 0) return;
        if (level < 2) return; // só a partir do nível 2

        int created = 0;
        int attempts = 0;

        while (created < 2 && attempts < 200) {
            attempts++;

            int rx = 1 + (int) (Math.random() * (cols - 2));
            int ry = 1 + (int) (Math.random() * (rows - 2));

            if (blocked[rx][ry]) continue;

            // evita spawn exatamente onde está o PacMan
            if (pacMan != null && pacMan.gridX == rx && pacMan.gridY == ry) continue;

            float cx = rx * tileSize + tileSize / 2f;
            float cy = ry * tileSize + tileSize / 2f;

            // evita spawn muito próximo de outras cerejas
            boolean ok = true;
            for (Cherry c : cherries) {
                float dx = c.x - cx;
                float dy = c.y - cy;
                if (dx * dx + dy * dy < (tileSize * tileSize * 0.25f)) { // distância mínima
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            cherries.add(new Cherry(cx, cy));
            created++;
        }
    }

    // ================= CICLO =================

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // NÃO iniciamos a thread aqui: esperamos surfaceChanged() calcular tileSize e criar o mapa.
        // Apenas garantimos que holder existe.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // pausa e aguarda thread terminar
        pause();

        //Parar Musica
        if (musicPlayer != null) {
            try {
                if (musicPlayer.isPlaying()) musicPlayer.stop();
                musicPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                musicPlayer = null;
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int f, int width, int height) {

        // —————————————— CALCULA TILESIZE CERTO ——————————————
        float tileW = (float) width / cols;
        float tileH = (float) height / rows;
        tileSize = (int) Math.max(8, Math.min(tileW, tileH)); // mínimo de 8px para evitar zero

        offsetX = (width - cols * tileSize) / 2;
        offsetY = (height - rows * tileSize) / 2;
        if (offsetX < 0) offsetX = 0;
        if (offsetY < 0) offsetY = 0;

        // —————————————— RECRIA TUDO NO NOVO TAMANHO ——————————————
        createMap();
        pacMan = new PacMan(pacStartX, pacStartY, tileSize);
        createGhosts();

        // só inicia a thread *uma vez* quando tudo estiver pronto
        if (!running) {
            resume();
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                if (!holder.getSurface().isValid()) {
                    // pequena espera para evitar tight-loop quando surface inválida
                    try { Thread.sleep(16); } catch (InterruptedException ignored) {}
                    continue;
                }

                // proteção: se pacMan ainda for null, pula o update/desenho
                if (pacMan == null) {
                    try { Thread.sleep(16); } catch (InterruptedException ignored) {}
                    continue;
                }

                update();

                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas == null) continue;

                    // limpa tela inteira (não usar drawColor dentro translate)
                    canvas.drawColor(Color.BLACK);

                    canvas.save();
                    canvas.translate(offsetX, offsetY);
                    drawGame(canvas);
                    canvas.restore();

                } finally {
                    if (canvas != null) {
                        try {
                            holder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {
                            // proteção extra: às vezes unlock pode lançar se surface invalida — ignorar
                            e.printStackTrace();
                        }
                    }
                }

                // ~60 FPS
                try { Thread.sleep(16); } catch (InterruptedException ignored) {}
            }
        } catch (Throwable t) {
            // captura qualquer erro para evitar matar a thread silenciosamente
            t.printStackTrace();
            running = false;
        }
    }

    // ================= UPDATE =================

    private void update() {
        if (gameOver || pacMan == null) return;

        pacMan.update(blocked, cols, rows);

        // cuidado se ghosts for vazio
        for (Ghost ghost : ghosts) {
            if (ghost == null) continue;

            ghost.update(blocked, cols, rows, pacMan);

            if (ghost.gridX == pacMan.gridX &&
                    ghost.gridY == pacMan.gridY) {

                lives--;

                if (lives <= 0) {
                    gameOver = true;
                } else {
                    resetPositions();
                }
                return;
            }
        }

        for (Pallet p : pellets) {
            if (!p.eaten &&
                    android.graphics.RectF.intersects(pacMan.getRect(), p.getRect())) {
                p.eaten = true;
                score += 10;
            }
        }

        // 🍒 Colisão com cerejas (prioritário antes do nextLevel)
        for (Cherry c : cherries) {
            if (!c.eaten &&
                    android.graphics.RectF.intersects(pacMan.getRect(), c.getRect())) {
                c.eaten = true;

                if (lives < maxLives) {
                    lives++;
                }

                score += 50; // bônus por pegar cereja
            }
        }

        if (allPelletsEaten()) {
            nextLevel();
        }
    }

    private boolean allPelletsEaten() {
        for (Pallet p : pellets) {
            if (!p.eaten) return false;
        }
        return true;
    }

    private void nextLevel() {
        level++;
        createMap();
        createGhosts();
        resetPositions();
    }

    // ================= DRAW =================

    private void drawGame(Canvas canvas) {
        if (tileSize <= 0) return;

        // paredes (desenha com stroke fill)
        for (Wall w : walls) {
            if (w != null) w.draw(canvas, paint);
        }

        // pellets
        for (Pallet p : pellets) {
            if (p != null && !p.eaten) p.draw(canvas, paint);
        }

        // 🍒 desenhar cerejas
        for (Cherry c : cherries) {
            if (c != null && !c.eaten) c.draw(canvas, paint);
        }

        // personagens
        if (pacMan != null) pacMan.draw(canvas, paint);
        for (Ghost ghost : ghosts) {
            if (ghost != null) ghost.draw(canvas, paint, 0, 0);
        }

        // HUD: score / level / lives — desenhados dentro do mapa (y positivo)
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(20, tileSize / 3)); // tamanho de texto proporcional
        float hudY = Math.max(24, tileSize / 2f);      // distancia do topo do mapa
        canvas.drawText("Score: " + score, 10, hudY, paint);
        canvas.drawText("Level: " + level, cols * tileSize / 2f - 40, hudY, paint);

        drawLives(canvas);

        if (gameOver) {
            paint.setColor(Color.YELLOW);
            paint.setTextSize(Math.max(40, tileSize));
            canvas.drawText(
                    "GAME OVER",
                    cols * tileSize / 2f - 120,
                    rows * tileSize / 2f,
                    paint
            );
        }
    }

    private void drawLives(Canvas canvas) {
        // desenha ícones de vida no canto superior direito do mapa
        float radius = Math.max(8, tileSize * 0.12f);
        float startX = cols * tileSize - 20 - radius * 2 * Math.min(3, Math.max(1, lives));
        float y = Math.max(24, tileSize / 2f);

        paint.setColor(Color.YELLOW);

        for (int i = 0; i < lives; i++) {
            float cx = startX + i * (radius * 2 + 8);
            canvas.drawCircle(cx, y, radius, paint);
        }
    }

    // ================= CONTROLE =================

    public synchronized void resume() {

        // 🎵 Retomar música quando voltar para o jogo
        if (musicPlayer != null) {
            musicPlayer.start();
        }

        if (running) return; // já rodando
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }

    public synchronized void pause() {
        running = false;

        //Pausar musica
        if (musicPlayer != null && musicPlayer.isPlaying()){
            musicPlayer.pause();
        }

        // esperamos a thread terminar (join) com timeout curto
        try {
            if (gameThread != null) gameThread.join(100);
        } catch (InterruptedException ignored) {}
        gameThread = null;
    }

    public void restartGame() {
        gameOver = false;
        score = 0;
        level = 1;
        lives = 3;

        // recria tudo
        createMap();
        createGhosts();

        pacMan.reset(pacStartX, pacStartY);
        for (Ghost g : ghosts) g.reset();

        // retomar o loop caso estivesse parado
        resume();
    }


    public void setDirection(int direction) {
        if (!gameOver && pacMan != null)
            pacMan.setDirection(direction);
    }

    // ================= CLASSE INTERNA Cherry =================
    // (para não precisar criar arquivo separado)
    private static class Cherry {
        float x, y;
        float radius = 12f;
        boolean eaten = false;

        Cherry(float x, float y) {
            this.x = x;
            this.y = y;
        }

        android.graphics.RectF getRect() {
            return new android.graphics.RectF(
                    x - radius, y - radius,
                    x + radius, y + radius
            );
        }

        void draw(Canvas c, Paint p) {
            if (eaten) return;

            // cereja (círculo vermelho) + cabinho verde
            p.setColor(Color.RED);
            c.drawCircle(x, y, radius, p);

            p.setColor(Color.rgb(34, 139, 34)); // verde
            c.drawRect(x - 4, y - radius - 6, x + 4, y - radius + 2, p);
        }
    }
}
