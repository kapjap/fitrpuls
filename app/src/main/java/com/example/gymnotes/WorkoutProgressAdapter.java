package com.example.gymnotes;

import android.view.*;
import android.widget.*;
import android.text.InputType;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class WorkoutProgressAdapter extends RecyclerView.Adapter<WorkoutProgressAdapter.Holder> {
    public interface OnSetClickListener { void onSetClicked(int position, int setIndex); }
    private final List<SessionSnapshot.Entry> items;
    private final OnSetClickListener listener;
    public WorkoutProgressAdapter(List<SessionSnapshot.Entry> items, OnSetClickListener listener) { this.items=items; this.listener=listener; }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_progress,parent,false));
    }
    @Override public void onBindViewHolder(@NonNull Holder h,int position) {
        SessionSnapshot.Entry item=items.get(position);
        h.name.setText(item.name); h.info.setText("Выполнено "+item.doneCount()+" из "+item.sets.size()+" · нажмите вес для изменения");
        h.buttons.removeAllViews();
        for(int i=0;i<item.sets.size();i++) {
            final int index=i; SessionSnapshot.SetEntry set=item.sets.get(i);
            LinearLayout row=new LinearLayout(h.itemView.getContext());row.setGravity(Gravity.CENTER_VERTICAL);
            CheckBox check=new CheckBox(h.itemView.getContext());check.setText(""+(i+1));check.setChecked(set.done);
            check.setMinHeight(dp(h.itemView,48));
            check.setContentDescription("Подход "+(i+1)+(set.done?", выполнен":", не выполнен"));
            check.setOnClickListener(v->{int pos=h.getBindingAdapterPosition();if(pos!=RecyclerView.NO_POSITION)listener.onSetClicked(pos,index);});
            row.addView(check);
            MaterialButton edit=new MaterialButton(h.itemView.getContext(),null,com.google.android.material.R.attr.materialButtonOutlinedStyle);
            edit.setText(set.weight+" кг × "+set.reps+(set.warmup?" · разминка":""));
            edit.setOnClickListener(v->editSet(h,index));
            row.addView(edit,new LinearLayout.LayoutParams(0,-2,1));h.buttons.addView(row);
        }
        MaterialButton add=new MaterialButton(h.itemView.getContext(),null,com.google.android.material.R.attr.materialButtonOutlinedStyle);
        add.setText("+ Подход"); add.setOnClickListener(v->{
            int pos=h.getBindingAdapterPosition();if(pos==RecyclerView.NO_POSITION)return;
            SessionSnapshot.Entry e=items.get(pos); SessionSnapshot.SetEntry set=new SessionSnapshot.SetEntry();
            if(!e.sets.isEmpty()){SessionSnapshot.SetEntry last=e.sets.get(e.sets.size()-1);set.weight=last.weight;set.reps=last.reps;}
            e.sets.add(set);listener.onSetClicked(pos,-1);notifyItemChanged(pos);
        });h.buttons.addView(add);
    }
    private void editSet(Holder h,int index) {
        int pos=h.getBindingAdapterPosition();if(pos==RecyclerView.NO_POSITION)return;
        SessionSnapshot.SetEntry set=items.get(pos).sets.get(index);
        LinearLayout fields=new LinearLayout(h.itemView.getContext());fields.setOrientation(LinearLayout.VERTICAL);fields.setPadding(32,16,32,16);
        EditText weight=new EditText(fields.getContext());weight.setHint("Вес, кг");weight.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);weight.setText(String.valueOf(set.weight));
        EditText reps=new EditText(fields.getContext());reps.setHint("Повторения");reps.setInputType(InputType.TYPE_CLASS_NUMBER);reps.setText(String.valueOf(set.reps));
        CheckBox warmup=new CheckBox(fields.getContext());warmup.setText("Разминочный подход");warmup.setChecked(set.warmup);
        fields.addView(weight);fields.addView(reps);fields.addView(warmup);
        AlertDialog dialog=new AlertDialog.Builder(fields.getContext()).setTitle("Подход "+(index+1)).setView(fields)
                .setNegativeButton(android.R.string.cancel,null).setPositiveButton("Сохранить",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            try {
                double w=Double.parseDouble(weight.getText().toString().replace(',','.'));int n=Integer.parseInt(reps.getText().toString());
                if(!Double.isFinite(w)||w<0||w>10000){weight.setError("Введите вес от 0 до 10000 кг");return;}
                if(n<1||n>10000){reps.setError("Введите от 1 до 10000 повторений");return;}
                int current=h.getBindingAdapterPosition();if(current==RecyclerView.NO_POSITION){dialog.dismiss();return;}
                set.weight=w;set.reps=n;set.warmup=warmup.isChecked();listener.onSetClicked(current,-1);notifyItemChanged(current);dialog.dismiss();
            }catch(NumberFormatException e){reps.setError("Проверьте вес и повторения");}
        }));dialog.show();
    }
    private int dp(View v,int value){return (int)(v.getResources().getDisplayMetrics().density*value);}
    @Override public int getItemCount(){return items.size();}
    static class Holder extends RecyclerView.ViewHolder {
        TextView name,info; LinearLayout buttons;
        Holder(View v){super(v);name=v.findViewById(R.id.textExerciseName);info=v.findViewById(R.id.textExerciseInfo);buttons=v.findViewById(R.id.setButtons);}
    }
}
