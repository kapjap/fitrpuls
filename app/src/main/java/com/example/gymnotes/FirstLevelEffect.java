package com.example.gymnotes;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import androidx.core.view.WindowInsetsControllerCompat;

/** Optional 1.3-second overlay; the real dashboard is already loaded underneath. */
final class FirstLevelEffect {
    static final String CREATED_UID = "anti_gym_created_uid";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeout = this::dismiss;
    private Dialog dialog;
    private View content, number, active;

    static FirstLevelEffect show(Activity activity) {
        return show(activity, null);
    }
    static FirstLevelEffect showWelcomeBack(Activity activity, int level) {
        return show(activity, level);
    }
    private static FirstLevelEffect show(Activity activity, Integer returningLevel) {
        if (!AuthScreenUi.motionAllowed(activity) || !new CharacterPreferences(activity).effects()) return null;
        FirstLevelEffect effect = new FirstLevelEffect();
        try {
            if (activity.isFinishing() || activity.isDestroyed()) return null;
            effect.dialog = new Dialog(activity, R.style.Theme_GymNotes);
            effect.dialog.setContentView(returningLevel == null ? R.layout.first_level : R.layout.welcome_back);
            effect.dialog.setCancelable(true);
            effect.dialog.findViewById(R.id.firstLevelSkip).setOnClickListener(v -> effect.dismiss());
            effect.dialog.setOnDismissListener(d -> effect.cleanup());
            effect.content = effect.dialog.findViewById(R.id.firstLevelContent);
            effect.number = effect.dialog.findViewById(R.id.firstLevelNumber);
            effect.active = effect.dialog.findViewById(R.id.firstLevelActive);
            if (returningLevel != null)
                ((android.widget.TextView) effect.number).setText(activity.getString(R.string.welcome_back_level, returningLevel));
            effect.dialog.show();
            Window window = effect.dialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.setStatusBarColor(activity.getColor(R.color.welcome_ink));
                window.setNavigationBarColor(activity.getColor(R.color.welcome_ink));
                WindowInsetsControllerCompat bars = new WindowInsetsControllerCompat(window, window.getDecorView());
                bars.setAppearanceLightStatusBars(false); bars.setAppearanceLightNavigationBars(false);
            }
            effect.content.setAlpha(0f);
            effect.content.setTranslationY(12 * activity.getResources().getDisplayMetrics().density);
            effect.content.animate().alpha(1f).translationY(0).setDuration(280)
                    .setInterpolator(new DecelerateInterpolator()).start();
            effect.number.setAlpha(0f);
            effect.active.setAlpha(0f);
            effect.number.animate().alpha(1f).setStartDelay(120).setDuration(220).start();
            effect.active.animate().alpha(1f).setStartDelay(250).setDuration(220).start();
            effect.handler.postDelayed(effect.timeout, returningLevel == null ? 1300 : 1000);
            return effect;
        } catch (RuntimeException unavailable) {
            effect.dismiss();
            return null;
        }
    }
    void dismiss() {
        cleanup();
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }
    private void cleanup() {
        handler.removeCallbacks(timeout);
        if (content != null) content.animate().cancel();
        if (number != null) number.animate().cancel();
        if (active != null) active.animate().cancel();
    }
}
