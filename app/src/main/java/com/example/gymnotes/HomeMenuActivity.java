package com.example.gymnotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeMenuActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_home_menu);
        MainNavigation.attach(this, 0);
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){startActivity(new Intent(this,MainActivity.class));finish();return;}
        CharacterPreferences character=new CharacterPreferences(this);
        String line=character.greeting(System.currentTimeMillis());
        if(line!=null)((TextView)findViewById(R.id.subtitle)).setText(line);
        String name=user.getDisplayName();
        if(name!=null&&!name.trim().isEmpty()) ((TextView)findViewById(R.id.greeting)).setText("С возвращением, "+name.trim());
        findViewById(R.id.startWorkout).setOnClickListener(v->open(WorkoutsActivity.class));
        findViewById(R.id.openProgress).setOnClickListener(v->open(ProgressActivity.class));
        findViewById(R.id.openNotes).setOnClickListener(v->open(NotesMain.class));
        findViewById(R.id.openNews).setOnClickListener(v->open(News.class));
        findViewById(R.id.openSettings).setOnClickListener(v->open(SettingsActivity.class));
        DashboardViewModel model=new ViewModelProvider(this).get(DashboardViewModel.class);
        model.insight(user.getUid()).observe(this,value->((TextView)findViewById(R.id.explainBody)).setText(value));
        model.history(user.getUid()).observe(this,history->{
            long xp=TrainingXp.total(history);int level=TrainingXp.level(xp);
            TextView xpStatus=findViewById(R.id.xpStatus);
            xpStatus.setText("Уровень "+level+" · "+xp+" XP · за регулярность, не за вес");
            if(character.consumeLevelUp(level)) {
                xpStatus.setText("LEVEL UP · "+level+" — твой ритм становится привычкой");
                if(android.provider.Settings.Global.getFloat(getContentResolver(),android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0){xpStatus.setAlpha(0.3f);xpStatus.animate().alpha(1f).setDuration(450).start();}
            }
            Calendar week=Calendar.getInstance();
            week.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);week.set(Calendar.HOUR_OF_DAY,0);
            week.set(Calendar.MINUTE,0);week.set(Calendar.SECOND,0);week.set(Calendar.MILLISECOND,0);
            if(week.getTimeInMillis()>System.currentTimeMillis())week.add(Calendar.WEEK_OF_YEAR,-1);
            int count=0,minutes=0;double volume=0;
            for(CompletedWorkoutEntity w:history)if(w.getCompletedAt()>=week.getTimeInMillis()){
                count++;minutes+=w.getDurationMinutes();volume+=w.getTotalWeight();
            }
            ((TextView)findViewById(R.id.weeklyStats)).setText(count+" тренировок · "+minutes+" мин");
            ((TextView)findViewById(R.id.weeklyDetail)).setText(String.format(Locale.getDefault(),"Объём: %,.0f кг · сохранено локально",volume));
            if(history.isEmpty()){
                ((TextView)findViewById(R.id.lastWorkout)).setText("Здесь начинается твоя история");
                ((TextView)findViewById(R.id.lastDetail)).setText("Создай тренировку, отметь выполненные подходы и сохрани результат.");
            }else{
                CompletedWorkoutEntity last=history.get(0);
                ((TextView)findViewById(R.id.lastWorkout)).setText(last.getTitle());
                ((TextView)findViewById(R.id.lastDetail)).setText(DateFormat.getDateInstance().format(new Date(last.getCompletedAt()))+" · "+last.getDurationMinutes()+" мин · "+last.getExerciseCount()+" упражнений");
            }
        });
    }
    @Override protected void onResume() {
        super.onResume();
        CharacterPreferences preferences=new CharacterPreferences(this);
        if(!preferences.humor() || CharacterPreferences.NORMAL.equals(preferences.mode()))
            ((TextView)findViewById(R.id.subtitle)).setText("Тренируйся. Замечай изменения. Продолжай.");
    }
    private void open(Class<?> target){startActivity(new Intent(this,target));}
}
