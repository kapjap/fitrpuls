package com.example.gymnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Plans and unfinished sessions, without changing their stored data. */
public class WorkoutsActivity extends AppCompatActivity {
    private WorkoutAdapter adapter;
    private AppDatabase database;
    private int requestVersion;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workouts);
        MainNavigation.attach(this,1);
        WorkoutScreenUi.install(this, ((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0));
        database=DatabaseProvider.get(this);
        RecyclerView list=findViewById(R.id.recyclerWorkouts);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter=new WorkoutAdapter(new ArrayList<>(),new WorkoutAdapter.OnWorkoutClickListener(){
            @Override public void onStartClick(WorkoutEntity workout){
                startActivity(new Intent(WorkoutsActivity.this,WorkoutSessionActivity.class)
                        .putExtra("workout_id",workout.getId()).putExtra("workout_title",workout.getTitle())
                        .putExtra("workout_time",workout.getEstimatedTime()));
            }
            @Override public void onItemClick(WorkoutEntity workout){onStartClick(workout);}
            @Override public void onItemLongClick(WorkoutEntity workout){confirmDelete(workout);}
        });
        list.setAdapter(adapter);
        findViewById(R.id.buttonAddWorkout).setOnClickListener(v->createWorkout());
    }

    @Override protected void onResume(){super.onResume();loadWorkouts();}
    private void createWorkout(){startActivity(new Intent(this,CreateWorkoutActivity.class));}

    private void loadWorkouts(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){startActivity(new Intent(this,MainActivity.class));finish();return;}
        String uid=user.getUid();int version=++requestVersion;
        showState(R.string.ag_loading,0,0,null);
        SessionRepository.IO.execute(()->{
            try {
                List<WorkoutEntity> workouts=database.workoutDao().getAllWorkoutsByUser(uid);
                Set<Integer> drafts=new HashSet<>(database.sessionDraftDao().getWorkoutIds(uid));
                workouts.sort((a,b)->Boolean.compare(drafts.contains(b.getId()),drafts.contains(a.getId())));
                runOnUiThread(()->{
                    if(isDestroyed()||version!=requestVersion)return;
                    adapter.setDraftIds(drafts);adapter.updateList(new ArrayList<>(workouts));
                    ((TextView)findViewById(R.id.workoutCount)).setText(getString(R.string.ag_count,workouts.size()));
                    findViewById(R.id.recyclerWorkouts).setVisibility(workouts.isEmpty()?View.GONE:View.VISIBLE);
                    if(workouts.isEmpty())showState(R.string.ag_empty,R.string.ag_empty_body,0,null);
                    else findViewById(R.id.workoutState).setVisibility(View.GONE);
                });
            }catch(Exception e){runOnUiThread(()->{
                if(!isDestroyed()&&version==requestVersion)showState(R.string.ag_error,R.string.ag_error_body,R.string.ag_retry,this::loadWorkouts);
            });}
        });
    }

    private void showState(int title,int body,int action,Runnable callback){
        findViewById(R.id.workoutState).setVisibility(View.VISIBLE);
        findViewById(R.id.recyclerWorkouts).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.stateTitle)).setText(title);
        TextView detail=findViewById(R.id.stateBody);
        detail.setVisibility(body==0?View.GONE:View.VISIBLE);if(body!=0)detail.setText(body);
        TextView button=findViewById(R.id.stateAction);
        button.setVisibility(action==0?View.GONE:View.VISIBLE);
        if(action!=0){button.setText(action);button.setOnClickListener(v->callback.run());}
    }

    private void confirmDelete(WorkoutEntity workout){
        new AlertDialog.Builder(this).setTitle("Удаление тренировки")
                .setMessage("Удалить план «"+workout.getTitle()+"»? История выполненных тренировок сохранится.")
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton("Удалить",(dialog,which)->deleteWorkout(workout)).show();
    }

    private void deleteWorkout(WorkoutEntity workout){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user==null||!user.getUid().equals(workout.getUserId()))return;
        SessionRepository.IO.execute(()->{
            try {
                // Use the same ordered queue as session saves so a pending draft is visible.
                if(database.sessionDraftDao().get(user.getUid(),workout.getId())!=null){
                    runOnUiThread(()->Toast.makeText(this,"Сначала завершите сохранённую тренировку",Toast.LENGTH_LONG).show());return;
                }
                database.runInTransaction(()->{
                    database.exerciseDao().deleteByWorkout(workout.getId());
                    database.workoutDao().deleteWorkout(workout);
                });
                runOnUiThread(()->{if(!isDestroyed()){Toast.makeText(this,R.string.ag_workout_removed,Toast.LENGTH_SHORT).show();loadWorkouts();}});
            }catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,R.string.ag_delete_error,Toast.LENGTH_LONG).show();});}
        });
    }
}
