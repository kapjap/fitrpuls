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
    private FirstLevelEffect firstLevelEffect;
    private SessionDraft latestDraft;
    private String progressExplanation;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_home_menu);
        MainNavigation.attach(this, 0);
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){startActivity(new Intent(this,MainActivity.class));finish();return;}
        String createdUid = getIntent().getStringExtra(FirstLevelEffect.CREATED_UID);
        getIntent().removeExtra(FirstLevelEffect.CREATED_UID);
        if (state == null && user.getUid().equals(createdUid)) firstLevelEffect = FirstLevelEffect.show(this);
        CharacterPreferences character=new CharacterPreferences(this);
        String line=character.greeting(System.currentTimeMillis());
        if(line!=null){((TextView)findViewById(R.id.subtitle)).setText(line);findViewById(R.id.subtitle).setVisibility(android.view.View.VISIBLE);}
        String name=user.getDisplayName();
        if(name!=null&&!name.trim().isEmpty()) ((TextView)findViewById(R.id.greeting)).setText(name.trim());
        findViewById(R.id.startWorkout).setOnClickListener(v->{
            if(latestDraft==null){open(WorkoutsActivity.class);return;}
            startActivity(new Intent(this,WorkoutSessionActivity.class).putExtra("workout_id",latestDraft.workoutId));
        });
        findViewById(R.id.openProgress).setOnClickListener(v->open(ProgressActivity.class));
        findViewById(R.id.openNotes).setOnClickListener(v->open(NotesMain.class));
        findViewById(R.id.openNews).setOnClickListener(v->open(News.class));
        findViewById(R.id.openSettings).setOnClickListener(v->open(SettingsActivity.class));
        DashboardViewModel model=new ViewModelProvider(this).get(DashboardViewModel.class);
        model.latestDraft(user.getUid()).observe(this,draft->{
            latestDraft=draft;
            ((TextView)findViewById(R.id.startWorkout)).setText(draft==null?R.string.ag_choose:R.string.ag_resume);
            TextView detail=findViewById(R.id.resumeDetail);detail.setVisibility(draft==null?android.view.View.GONE:android.view.View.VISIBLE);
            if(draft!=null){
                detail.setText(R.string.ag_draft);
                try {SessionSnapshot snapshot=new com.google.gson.Gson().fromJson(draft.payload,SessionSnapshot.class);if(snapshot!=null && snapshot.title!=null)detail.setText(snapshot.title);}catch(RuntimeException ignored){}
            }
        });
        findViewById(R.id.explainBody).setEnabled(false);
        findViewById(R.id.explainBody).setOnClickListener(v -> new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.home_insight_title).setMessage(progressExplanation)
                .setPositiveButton(android.R.string.ok, null).show());
        model.insight(user.getUid()).observe(this,value->{progressExplanation=value;findViewById(R.id.explainBody).setEnabled(value!=null);});
        model.history(user.getUid()).observe(this,history->{
            long xp=TrainingXp.total(history);int level=TrainingXp.level(xp);
            TextView xpStatus=findViewById(R.id.xpStatus);
            xpStatus.setText(getString(R.string.home_xp,level,xp));
            if(character.consumeLevelUp(level)) {
                xpStatus.setText(getString(R.string.home_level_up,level));
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
            ((TextView)findViewById(R.id.weeklyStats)).setText(String.valueOf(count));
            ((TextView)findViewById(R.id.weeklyMinutes)).setText(String.valueOf(minutes));
            ((TextView)findViewById(R.id.weeklyDetail)).setText(String.format(Locale.getDefault(),"%,.0f",volume));
            if(history.isEmpty()){
                ((TextView)findViewById(R.id.lastWorkout)).setText(R.string.home_empty_title);
                ((TextView)findViewById(R.id.lastDetail)).setText(R.string.home_empty_detail);
            }else{
                CompletedWorkoutEntity last=history.get(0);
                ((TextView)findViewById(R.id.lastWorkout)).setText(last.getTitle());
                ((TextView)findViewById(R.id.lastDetail)).setText(DateFormat.getDateInstance().format(new Date(last.getCompletedAt()))+" · "+last.getDurationMinutes()+" мин · "+last.getExerciseCount()+" упражнений");
            }
        });
    }
    @Override protected void onStop() {
        if (firstLevelEffect != null) firstLevelEffect.dismiss();
        super.onStop();
    }
    @Override protected void onResume() {
        super.onResume();
        CharacterPreferences preferences=new CharacterPreferences(this);
        if(!preferences.humor() || CharacterPreferences.NORMAL.equals(preferences.mode()))
            findViewById(R.id.subtitle).setVisibility(android.view.View.GONE);
    }
    private void open(Class<?> target){startActivity(new Intent(this,target));}
}
