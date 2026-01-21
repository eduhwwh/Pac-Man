package com.example.pacman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);

        Button up = findViewById(R.id.btnUp);
        Button down = findViewById(R.id.btnDown);
        Button left = findViewById(R.id.btnLeft);
        Button right = findViewById(R.id.btnRight);
        Button home = findViewById(R.id.btnHome);

        // CONTROLES
        up.setOnClickListener(v -> gameView.setDirection(3));
        down.setOnClickListener(v -> gameView.setDirection(1));
        left.setOnClickListener(v -> gameView.setDirection(2));
        right.setOnClickListener(v -> gameView.setDirection(0));

        // BOTÃO HOME
        home.setOnClickListener(v -> {
            gameView.pause();

            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish(); // encerra a tela do jogo
        });

        Button restart = findViewById(R.id.btnRestart);
        restart.setOnClickListener(v -> {
            gameView.restartGame();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}
