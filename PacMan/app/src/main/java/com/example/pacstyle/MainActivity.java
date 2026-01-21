package com.example.pacstyle;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


import com.example.pacman.GameView;
import com.example.pacman.R;

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

        up.setOnClickListener(v -> gameView.setDirection(3));    // cima
        down.setOnClickListener(v -> gameView.setDirection(1));  // baixo
        left.setOnClickListener(v -> gameView.setDirection(2));  // esquerda
        right.setOnClickListener(v -> gameView.setDirection(0)); // direita
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
