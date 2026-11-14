package com.example.gymnotes;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import java.util.List;
public class DashboardViewModel extends AndroidViewModel {
    public DashboardViewModel(@NonNull Application app) { super(app); }
    public LiveData<String> insight(String uid) {
        return Transformations.map(DatabaseProvider.get(getApplication()).completedExerciseDao().observeHistory(uid), ProgressInsight::explain);
    }
    public LiveData<List<CompletedWorkoutEntity>> history(String uid) {
        return DatabaseProvider.get(getApplication()).completedWorkoutDao().observeHistory(uid);
    }
}
