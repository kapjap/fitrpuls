package com.example.gymnotes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.RectEvaluator;
import android.graphics.Rect;
import android.provider.Settings;
import android.view.animation.DecelerateInterpolator;
import android.view.accessibility.AccessibilityManager;
import android.animation.ObjectAnimator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ObjectAnimator entrance;
    private View poster;
    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(LOG_TAG, "AuthStateListener: Пользователь вошёл");
                startActivity(new Intent(MainActivity.this, HomeMenuActivity.class));
                finish();
            }
        };

        AuthScreenUi.install(this);
        poster = findViewById(R.id.welcomePoster);
        Button buttonLogIn = findViewById(R.id.buttonLogin);
        Button buttonSignUp = findViewById(R.id.buttonSignup);

        if (savedInstanceState == null) poster.post(this::revealPoster);

        buttonLogIn.setOnClickListener(v -> launchLogInScreen());
        buttonSignUp.setOnClickListener(v -> launchSignUpScreen());

    }

    private void revealPoster() {
        AccessibilityManager accessibility = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        boolean reduceMotion = Settings.Global.getFloat(getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f;
        if (isFinishing() || isDestroyed() || reduceMotion
                || (accessibility != null && accessibility.isTouchExplorationEnabled())) return;
        // A single editorial wipe; actions stay visible and usable throughout.
        int width = poster.getWidth(), height = poster.getHeight();
        if (width == 0 || height == 0) return;
        entrance = ObjectAnimator.ofObject(poster, "clipBounds", new RectEvaluator(),
                new Rect(0, 0, width, 0), new Rect(0, 0, width, height));
        entrance.setDuration(420);
        entrance.setInterpolator(new DecelerateInterpolator(1.4f));
        entrance.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) { poster.setClipBounds(null); }
            @Override public void onAnimationCancel(Animator animation) { poster.setClipBounds(null); }
        });
        entrance.start();
    }

    @Override protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override protected void onStop() {
        auth.removeAuthStateListener(authStateListener);
        if (entrance != null) entrance.cancel();
        if (poster != null) poster.setClipBounds(null);
        super.onStop();
    }

    private void launchSignUpScreen() {
        Intent intent = new Intent(this, Sign_Up_Activity.class);
        AuthScreenUi.open(this, intent);
    }

    private void launchLogInScreen() {
        Intent intent = new Intent(this, Log_in_Activity.class);
        AuthScreenUi.open(this, intent);
    }
}