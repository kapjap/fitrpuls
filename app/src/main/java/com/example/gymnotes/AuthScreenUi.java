package com.example.gymnotes;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** Presentation only: shared auth surfaces, keyboard insets and short transitions. */
final class AuthScreenUi {
    private AuthScreenUi() { }

    static void install(AppCompatActivity activity) {
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().setNavigationBarColor(activity.getColor(R.color.welcome_ink));
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        WindowInsetsControllerCompat bars = new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView());
        bars.setAppearanceLightStatusBars(false);
        bars.setAppearanceLightNavigationBars(false);
        View root = activity.findViewById(R.id.main);
        int[] lastKeyboard = {-1};
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            v.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            if (keyboard > 0 && keyboard != lastKeyboard[0]) {
                v.post(() -> {
                    View focused = activity.getCurrentFocus();
                    if (focused instanceof EditText) {
                        Rect rect = new Rect(); focused.getDrawingRect(rect);
                        focused.requestRectangleOnScreen(rect, false);
                    }
                });
            }
            lastKeyboard[0] = keyboard;
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    static void back(AppCompatActivity activity) {
        activity.getOnBackPressedDispatcher().addCallback(activity, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                activity.finish();
                boolean motion = motionAllowed(activity);
                activity.overridePendingTransition(motion ? R.anim.auth_back_enter : 0,
                        motion ? R.anim.auth_back_exit : 0);
            }
        });
    }

    static void open(Activity activity, Intent intent) {
        boolean motion = motionAllowed(activity);
        activity.startActivity(intent, ActivityOptions.makeCustomAnimation(activity,
                motion ? R.anim.auth_enter : 0, motion ? R.anim.auth_exit : 0).toBundle());
    }

    static boolean motionAllowed(Activity activity) {
        AccessibilityManager manager = (AccessibilityManager) activity.getSystemService(Activity.ACCESSIBILITY_SERVICE);
        return Settings.Global.getFloat(activity.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
                && Settings.Global.getFloat(activity.getContentResolver(), Settings.Global.TRANSITION_ANIMATION_SCALE, 1f) > 0f
                && (manager == null || !manager.isTouchExplorationEnabled());
    }

    static void keyboardDone(Activity activity, EditText lastField, View action) {
        lastField.setOnEditorActionListener((v, id, event) -> {
            if (id != EditorInfo.IME_ACTION_DONE) return false;
            InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
            v.clearFocus();
            action.post(() -> {
                Rect rect = new Rect(); action.getDrawingRect(rect);
                action.requestRectangleOnScreen(rect, false);
            });
            return true;
        });
    }
}
