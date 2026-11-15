package com.example.gymnotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    interface Actions { void edit(int position); void remove(int position); }
    private final Actions actions;
    private List<ExerciseEntity> exercises;

    public ExerciseAdapter(List<ExerciseEntity> exercises, Actions actions) {
        this.actions=actions;
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseEntity exercise = exercises.get(position);
        holder.nameText.setText(exercise.name);

        String weight=new java.text.DecimalFormat("0.##").format(exercise.weight);
        holder.infoText.setText(holder.itemView.getContext().getString(R.string.exercise_summary,exercise.sets,exercise.reps,weight));
        holder.itemView.findViewById(R.id.exerciseMenu).setOnClickListener(v->{
            android.widget.PopupMenu menu=new android.widget.PopupMenu(v.getContext(),v);
            menu.getMenu().add(0,1,0,R.string.exercise_edit);
            menu.getMenu().add(0,2,1,R.string.exercise_remove);
            menu.setOnMenuItemClickListener(item->{
                int index=holder.getBindingAdapterPosition();if(index==RecyclerView.NO_POSITION)return false;
                if(item.getItemId()==1)actions.edit(index);else actions.remove(index);
                return true;
            });menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, infoText;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textExerciseName1);
            infoText = itemView.findViewById(R.id.textExerciseInfo1);
        }
    }
}