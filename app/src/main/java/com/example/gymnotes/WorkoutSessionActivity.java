package com.example.gymnotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutSessionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "fitpulse_settings";
    private static final String KEY_AUTO_REST = "auto_rest";
    private static final String KEY_REST_SOUND = "rest_sound";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_REST_DURATION = "rest_duration";

    private View rootView;

    private ImageView buttonBack;
    private ImageView buttonPause;
    private TextView textWorkoutTitle;
    private TextView textWorkoutTimer;
    private RecyclerView recyclerWorkoutExercises;
    private TextView textRestTimer;
    private TextView textRestTotal;
    private TextView buttonStartRest;
    private TextView buttonSkipRest;
    private TextView buttonFinishWorkout;

    private WorkoutProgressAdapter adapter;
    private ArrayList<SessionSnapshot.Entry> exerciseList;

    private AppDatabase appDatabase;
    private SessionRepository repository;
    private SessionSnapshot session;
    private String sessionUid;
    private boolean loaded;
    private long lastTick;
    private int saveRevision;
    private TextView saveStatus;
    private TextView sessionProgress;


    private int workoutId;
    private String workoutTitle;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private int workoutSeconds = 0;
    private int totalRestSeconds = 60;
    private int restSeconds = 0;

    private boolean isPaused = false;
    private boolean isRestRunning = false;
    private boolean isFinishingWorkout = false;

    private boolean autoRestEnabled = false;
    private boolean vibrationEnabled = false;
    private boolean restSoundEnabled = false;
    private boolean keepScreenOnEnabled = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (loaded && !isPaused) {
                long now = android.os.SystemClock.elapsedRealtime();
                int elapsed = (int) ((now - lastTick) / 1000);
                workoutSeconds += elapsed;
                lastTick += elapsed * 1000L;

                if (isRestRunning && restSeconds > 0) {
                    restSeconds = Math.max(0, restSeconds - elapsed);

                    if (restSeconds <= 0) {
                        restSeconds = 0;
                        isRestRunning = false;

                        runOnUiThread(() -> {
                            updateTimers();
                            updateRestButtons();
                            onRestFinished();
                        });
                    }
                }

                updateTimers();
                updateRestButtons();
            }

            if (isPaused) lastTick = android.os.SystemClock.elapsedRealtime();
            if (loaded && workoutSeconds % 10 == 0) persistSession();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_session);

        rootView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            boolean keyboard = insets.isVisible(WindowInsetsCompat.Type.ime());
            findViewById(R.id.restPanel).setVisibility(keyboard ? View.GONE : View.VISIBLE);
            findViewById(R.id.buttonFinishWorkout).setVisibility(keyboard ? View.GONE : View.VISIBLE);
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    systemBars.bottom
            );
            return insets;
        });

        saveStatus = findViewById(R.id.saveStatus);
        sessionProgress = findViewById(R.id.sessionProgress);
        saveStatus.setOnClickListener(v -> persistSession());
        buttonBack = findViewById(R.id.buttonBack);
        buttonPause = findViewById(R.id.buttonPause);
        textWorkoutTitle = findViewById(R.id.textWorkoutTitle);
        textWorkoutTimer = findViewById(R.id.textWorkoutTimer);
        recyclerWorkoutExercises = findViewById(R.id.recyclerWorkoutExercises);
        textRestTimer = findViewById(R.id.textRestTimer);
        textRestTotal = findViewById(R.id.textRestTotal);
        buttonStartRest = findViewById(R.id.buttonStartRest);
        buttonSkipRest = findViewById(R.id.buttonSkipRest);
        buttonFinishWorkout = findViewById(R.id.buttonFinishWorkout);

        appDatabase = DatabaseProvider.get(this);

        repository = new SessionRepository(appDatabase);
        FirebaseUser owner = FirebaseAuth.getInstance().getCurrentUser();
        if (owner == null) { finish(); return; }
        sessionUid = owner.getUid();
        buttonFinishWorkout.setEnabled(false);
        workoutId = getIntent().getIntExtra("workout_id", -1);
        workoutTitle = getIntent().getStringExtra("workout_title");

        if (workoutTitle == null || workoutTitle.trim().isEmpty()) {
            workoutTitle = "Тренировка";
        }

        textWorkoutTitle.setText(workoutTitle);

        loadUserSettings();
        applyKeepScreenOn();

        exerciseList = new ArrayList<>();
        adapter = new WorkoutProgressAdapter(exerciseList, (position, setIndex) -> {
            if (!loaded || isFinishingWorkout) return;
            SessionSnapshot.Entry item = exerciseList.get(position);
            if(setIndex < 0) { updateSessionProgress(); persistSession(); return; }
            SessionSnapshot.SetEntry set=item.sets.get(setIndex);
            set.done=!set.done;
            updateSessionProgress();
            if(set.done && autoRestEnabled && !isPaused) startRestAutomatically();
            persistSession();
        });

        recyclerWorkoutExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerWorkoutExercises.setAdapter(adapter);

        loadWorkoutRest();
        loadExercises();

        updateTimers();
        updateRestButtons();
        lastTick = android.os.SystemClock.elapsedRealtime();
        handler.post(timerRunnable);

        buttonBack.setOnClickListener(v -> finish());

        buttonPause.setOnClickListener(v -> {
            if (!loaded || isFinishingWorkout) return;
            isPaused = !isPaused;
            lastTick = android.os.SystemClock.elapsedRealtime();
            persistSession();
            updateRestButtons();
            updateTimers();

            if (isPaused) {
                buttonPause.setImageResource(R.drawable.baseline_play_arrow_24);
                Toast.makeText(this, "Тренировка на паузе", Toast.LENGTH_SHORT).show();
            } else {
                buttonPause.setImageResource(R.drawable.baseline_pause_24);
                Toast.makeText(this, "Тренировка продолжена", Toast.LENGTH_SHORT).show();
            }
        });

        buttonStartRest.setOnClickListener(v -> {
            if (!loaded || isFinishingWorkout) return;
            if (isPaused) {
                Toast.makeText(this, "Сначала снимите тренировку с паузы", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRestRunning) {
                Toast.makeText(this, "Отдых уже идёт", Toast.LENGTH_SHORT).show();
                return;
            }

            startRest();
            Toast.makeText(this, "Отдых начался", Toast.LENGTH_SHORT).show();
        });

        buttonSkipRest.setOnClickListener(v -> {
            if (!isRestRunning) {
                Toast.makeText(this, "Сейчас отдых не запущен", Toast.LENGTH_SHORT).show();
                return;
            }

            restSeconds = 0;
            isRestRunning = false;
            updateTimers();
            updateRestButtons();
            persistSession();
            Toast.makeText(this, "Отдых пропущен", Toast.LENGTH_SHORT).show();
        });

        buttonFinishWorkout.setOnClickListener(v -> finishWorkoutAndSave());
    }

    private SessionSnapshot captureSession() {
        session.seconds = workoutSeconds;
        session.restSeconds = restSeconds;
        session.paused = isPaused;
        session.savedAt = System.currentTimeMillis();
        return new com.google.gson.Gson().fromJson(new com.google.gson.Gson().toJson(session), SessionSnapshot.class);
    }

    private void updateSessionProgress() {
        if (session == null) return;
        int done = 0, total = 0;
        for (SessionSnapshot.Entry entry : session.entries) {
            done += entry.doneCount(); total += entry.sets.size();
        }
        sessionProgress.setText(getString(R.string.session_progress, done, total));
    }

    private void persistSession() {
        if (!loaded || isFinishingWorkout) return;
        int revision = ++saveRevision;
        saveStatus.setText(adapter.hasInvalidInput() ? R.string.session_input_error : R.string.session_saving);
        SessionSnapshot snapshot = captureSession();
        SessionRepository.IO.execute(() -> {
            try {
                repository.save(sessionUid, workoutId, snapshot);
                runOnUiThread(() -> {
                    if (!isDestroyed() && revision == saveRevision && !isFinishingWorkout)
                        saveStatus.setText(adapter.hasInvalidInput() ? R.string.session_input_error : R.string.session_saved);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!isDestroyed() && revision == saveRevision) saveStatus.setText(R.string.session_save_failed);
                });
            }
        });
    }

    private void finishWorkoutAndSave() {
        if (!loaded || isFinishingWorkout) return;
        if (adapter.hasInvalidInput()) {
            Toast.makeText(this, R.string.session_fix_fields, Toast.LENGTH_LONG).show();
            return;
        }
        if (session.completedExercises() == 0) {
            Toast.makeText(this, "Отметьте хотя бы один выполненный подход", Toast.LENGTH_SHORT).show();
            return;
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Завершить тренировку?")
                .setMessage("В историю попадут только отмеченные подходы.")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Сохранить", (dialog, which) -> saveCompletedSession()).show();
    }

    private void saveCompletedSession() {
        if (isFinishingWorkout) return;
        isFinishingWorkout = true;
        adapter.setLocked(true);
        updateRestButtons();
        buttonFinishWorkout.setEnabled(false);
        SessionSnapshot snapshot = captureSession();
        SessionRepository.IO.execute(() -> {
            try {
                repository.finish(sessionUid, workoutId, snapshot);
                runOnUiThread(() -> { if (!isDestroyed()) {
                    Toast.makeText(this, "Тренировка сохранена на устройстве", Toast.LENGTH_SHORT).show();
                    finish();
                }});
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isFinishingWorkout = false;
                    adapter.setLocked(false);
                    updateRestButtons();
                    buttonFinishWorkout.setEnabled(true);
                    Toast.makeText(this, "Не удалось завершить. Тренировка остаётся открытой; попробуйте ещё раз.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadUserSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        autoRestEnabled = prefs.getBoolean(getUserKey(KEY_AUTO_REST), false);
        vibrationEnabled = prefs.getBoolean(getUserKey(KEY_VIBRATION), false);
        restSoundEnabled = prefs.getBoolean(getUserKey(KEY_REST_SOUND), false);
        keepScreenOnEnabled = prefs.getBoolean(getUserKey(KEY_KEEP_SCREEN_ON), false);

        int settingsRestDuration = prefs.getInt(getUserKey(KEY_REST_DURATION), 60);

        if (totalRestSeconds <= 0) {
            totalRestSeconds = settingsRestDuration;
        }
    }

    private String getUserKey(String baseKey) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "guest";
        return baseKey + "_" + uid;
    }

    private void applyKeepScreenOn() {
        if (keepScreenOnEnabled) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (rootView != null) {
                rootView.setKeepScreenOn(true);
            }
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (rootView != null) {
                rootView.setKeepScreenOn(false);
            }
        }
    }

    private void startRestAutomatically() {
        if (isRestRunning) {
            return;
        }

        startRest();
        Toast.makeText(this, "Автоотдых начался", Toast.LENGTH_SHORT).show();
    }

    private void startRest() {
        restSeconds = totalRestSeconds;
        isRestRunning = true;
        updateTimers();
        updateRestButtons();
        persistSession();
    }

    private void onRestFinished() {
        playRestSoundIfNeeded();
        vibrateIfNeeded();
        Toast.makeText(this, "Отдых завершён", Toast.LENGTH_SHORT).show();
    }

    private void playRestSoundIfNeeded() {
        if (!restSoundEnabled) return;

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }

            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibrateIfNeeded() {
        if (!vibrationEnabled) return;

        try {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(400);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadWorkoutRest() {
        SessionRepository.IO.execute(() -> {
            WorkoutEntity workout = appDatabase.workoutDao().getWorkoutById(workoutId);

            if (workout != null && sessionUid.equals(workout.getUserId())) {
                int workoutRest = workout.getRestSeconds();

                if (workoutRest > 0) {
                    totalRestSeconds = workoutRest;
                } else {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    totalRestSeconds = prefs.getInt(getUserKey(KEY_REST_DURATION), 60);
                }
            } else {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                totalRestSeconds = prefs.getInt(getUserKey(KEY_REST_DURATION), 60);
            }

            runOnUiThread(this::updateTimers);
        });
    }

    private void loadExercises() {
        SessionRepository.IO.execute(() -> {
            try {
                SessionSnapshot restored = repository.load(sessionUid, workoutId);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    session = restored;
                    workoutSeconds = session.seconds;
                    restSeconds = session.restSeconds;
                    isPaused = session.paused;
                    if (!isPaused && session.savedAt > 0) {
                        int away = (int) Math.min(Integer.MAX_VALUE / 2, Math.max(0, (System.currentTimeMillis() - session.savedAt) / 1000));
                        workoutSeconds += away;
                        restSeconds = Math.max(0, restSeconds - away);
                    }
                    isRestRunning = restSeconds > 0;
                    textWorkoutTitle.setText(session.title);
                    exerciseList.clear();
                    exerciseList.addAll(session.entries);
                    loaded = true;
                    lastTick = android.os.SystemClock.elapsedRealtime();
                    buttonPause.setImageResource(isPaused ? R.drawable.baseline_play_arrow_24 : R.drawable.baseline_pause_24);
                    buttonFinishWorkout.setEnabled(true);
                    adapter.notifyDataSetChanged();
                    updateTimers();
                    updateRestButtons();
                    updateSessionProgress();
                    saveStatus.setText(R.string.session_saved);
                    if (exerciseList.isEmpty()) Toast.makeText(this, "Добавьте упражнения в тренировку", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!isDestroyed()) new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Тренировка недоступна")
                            .setMessage("Не удалось прочитать данные. Они не удалены. Вернитесь и попробуйте снова.")
                            .setPositiveButton(android.R.string.ok, (d, w) -> finish()).show();
                });
            }
        });
    }

    private void updateTimers() {
        textWorkoutTimer.setText(formatTime(workoutSeconds));
        textRestTimer.setText(formatTime(restSeconds));
        textRestTotal.setText(isPaused ? getString(R.string.session_paused)
                : isRestRunning ? getString(R.string.session_rest_running)
                : getString(R.string.session_rest_ready, totalRestSeconds));
        buttonPause.setContentDescription(getString(isPaused ? R.string.session_resume : R.string.session_pause));
    }

    private void updateRestButtons() {
        boolean editable = loaded && !isFinishingWorkout;
        buttonPause.setEnabled(editable);
        buttonStartRest.setEnabled(editable && !isPaused && !isRestRunning);
        buttonSkipRest.setEnabled(editable && isRestRunning);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserSettings();
        applyKeepScreenOn();
    }

    @Override
    protected void onPause() {
        persistSession();
        super.onPause();
        if (rootView != null) {
            rootView.setKeepScreenOn(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }
}