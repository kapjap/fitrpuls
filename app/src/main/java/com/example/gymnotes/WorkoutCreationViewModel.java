package com.example.gymnotes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

/** Keeps one in-flight save across Activity recreation; no Activity or View references. */
public final class WorkoutCreationViewModel extends ViewModel {
    enum State { IDLE, SAVING, SAVED, ERROR }
    private final MutableLiveData<State> state=new MutableLiveData<>(State.IDLE);
    LiveData<State> state(){return state;}
    void save(AppDatabase db,String uid,String title,int minutes,int rest,List<ExerciseEntity> exercises){
        if(state.getValue()==State.SAVING||state.getValue()==State.SAVED)return;
        ArrayList<ExerciseEntity> snapshot=new ArrayList<>();
        for(ExerciseEntity e:exercises)snapshot.add(new ExerciseEntity(0,e.name,e.sets,e.reps,e.weight));
        state.setValue(State.SAVING);
        SessionRepository.IO.execute(()->{
            try{
                db.runInTransaction(()->{
                    long id=db.workoutDao().insertWorkout(new WorkoutEntity(title,snapshot.size(),minutes,rest,uid));
                    for(ExerciseEntity e:snapshot){e.workoutId=(int)id;db.exerciseDao().insert(e);}
                });
                state.postValue(State.SAVED);
            }catch(Exception error){state.postValue(State.ERROR);}
        });
    }
}
