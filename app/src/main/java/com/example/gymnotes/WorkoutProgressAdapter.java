package com.example.gymnotes;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class WorkoutProgressAdapter extends RecyclerView.Adapter<WorkoutProgressAdapter.Holder> {
    public interface OnSetClickListener { void onSetClicked(int position, int setIndex); }
    private final List<SessionSnapshot.Entry> items;
    private final OnSetClickListener listener;
    // Retain partially typed values while RecyclerView recycles an exercise.
    private final Map<SessionSnapshot.SetEntry, String[]> input = new IdentityHashMap<>();
    private boolean locked;

    public WorkoutProgressAdapter(List<SessionSnapshot.Entry> items, OnSetClickListener listener) {
        this.items = items; this.listener = listener;
    }
    public boolean hasInvalidInput() {
        for (String[] value : input.values())
            if (SetInput.weight(value[0]) == null || SetInput.reps(value[1]) == null) return true;
        return false;
    }
    public void setLocked(boolean value) { locked = value; notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_progress, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        SessionSnapshot.Entry item = items.get(position);
        h.name.setText(item.name);
        updateInfo(h, item);
        h.buttons.removeAllViews();
        for (int i = 0; i < item.sets.size(); i++) {
            final int index = i;
            SessionSnapshot.SetEntry set = item.sets.get(i);
            String[] raw = input.get(set);
            if (raw == null) {
                raw = new String[]{String.valueOf(set.weight), String.valueOf(set.reps)};
                input.put(set, raw);
            }
            final String[] values = raw;
            LinearLayout row = new LinearLayout(h.itemView.getContext());
            row.setGravity(Gravity.CENTER_VERTICAL);
            MaterialButton menu = new MaterialButton(row.getContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            menu.setText(set.warmup ? "Р" : String.valueOf(i + 1));
            menu.setPadding(0, 0, 0, 0);
            menu.setContentDescription(row.getContext().getString(R.string.session_set_actions, i + 1));
            menu.setEnabled(!locked);
            menu.setOnClickListener(v -> showActions(h, index, menu));
            row.addView(menu, new LinearLayout.LayoutParams(dp(row, 48), dp(row, 56)));
            EditText weight = field(row, values[0], R.string.session_weight, true);
            EditText reps = field(row, values[1], R.string.session_reps, false);
            weight.setContentDescription(row.getContext().getString(R.string.session_weight_input, i + 1));
            reps.setContentDescription(row.getContext().getString(R.string.session_reps_input, i + 1));
            weight.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            reps.setImeOptions(EditorInfo.IME_ACTION_DONE);
            CheckBox check = new CheckBox(row.getContext());
            check.setChecked(set.done);
            check.setEnabled(!locked);
            check.setContentDescription(row.getContext().getString(R.string.session_set_done, i + 1));
            row.addView(check, new LinearLayout.LayoutParams(dp(row, 48), dp(row, 56)));
            TextWatcher watcher = new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                public void afterTextChanged(Editable s) {
                    values[0] = weight.getText().toString(); values[1] = reps.getText().toString();
                    Double w = SetInput.weight(values[0]); Integer n = SetInput.reps(values[1]);
                    weight.setError(w == null ? row.getContext().getString(R.string.session_invalid_weight) : null);
                    reps.setError(n == null ? row.getContext().getString(R.string.session_invalid_reps) : null);
                    if (w != null && n != null) { set.weight = w; set.reps = n; }
                    int pos = h.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) listener.onSetClicked(pos, -1);
                }
            };
            weight.addTextChangedListener(watcher); reps.addTextChangedListener(watcher);
            check.setOnClickListener(v -> {
                if (SetInput.weight(values[0]) == null || SetInput.reps(values[1]) == null) {
                    check.setChecked(set.done);
                    if (SetInput.weight(values[0]) == null) weight.requestFocus(); else reps.requestFocus();
                    return;
                }
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) { listener.onSetClicked(pos, index); updateInfo(h, item); }
            });
            h.buttons.addView(row);
        }
        MaterialButton add = new MaterialButton(h.itemView.getContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        add.setText(R.string.session_add); add.setEnabled(!locked);
        add.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition(); if (pos == RecyclerView.NO_POSITION) return;
            SessionSnapshot.Entry e = items.get(pos);
            SessionSnapshot.SetEntry set = new SessionSnapshot.SetEntry();
            set.reps = Math.max(1, e.reps); set.weight = e.weight;
            if (!e.sets.isEmpty()) {
                SessionSnapshot.SetEntry last = e.sets.get(e.sets.size() - 1);
                set.weight = last.weight; set.reps = last.reps;
            }
            e.sets.add(set); listener.onSetClicked(pos, -1); notifyItemChanged(pos);
        });
        h.buttons.addView(add);
    }
    private EditText field(LinearLayout row, String value, int hint, boolean decimal) {
        EditText field = new EditText(row.getContext());
        field.setSingleLine(true); field.setSelectAllOnFocus(true); field.setTextSize(18);
        field.setInputType(InputType.TYPE_CLASS_NUMBER | (decimal ? InputType.TYPE_NUMBER_FLAG_DECIMAL : 0));
        field.setHint(hint); field.setText(value); field.setEnabled(!locked);
        LinearLayout column = new LinearLayout(row.getContext()); column.setOrientation(LinearLayout.VERTICAL);
        TextView label = new TextView(row.getContext()); label.setText(hint); label.setTextSize(12);
        column.addView(label); column.addView(field, new LinearLayout.LayoutParams(-1, dp(row, 48)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        params.setMarginEnd(dp(row, 8)); row.addView(column, params);
        return field;
    }
    private void showActions(Holder h, int index, View anchor) {
        int pos = h.getBindingAdapterPosition(); if (pos == RecyclerView.NO_POSITION || locked) return;
        SessionSnapshot.SetEntry set = items.get(pos).sets.get(index);
        PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
        menu.getMenu().add(0, 1, 0, set.warmup ? R.string.session_working : R.string.session_warmup);
        menu.getMenu().add(0, 2, 1, R.string.session_remove);
        menu.setOnMenuItemClickListener(action -> {
            if (action.getItemId() == 1) {
                set.warmup = !set.warmup; listener.onSetClicked(pos, -1); notifyItemChanged(pos);
            } else new AlertDialog.Builder(anchor.getContext()).setTitle(R.string.session_remove_title)
                    .setMessage(R.string.session_remove_body).setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.session_remove, (d, w) -> {
                        if (locked) return;
                        items.get(pos).sets.remove(set); input.remove(set);
                        listener.onSetClicked(pos, -1); notifyItemChanged(pos);
                    }).show();
            return true;
        }); menu.show();
    }
    private void updateInfo(Holder h, SessionSnapshot.Entry item) {
        h.info.setText(h.itemView.getContext().getString(R.string.session_exercise_progress, item.doneCount(), item.sets.size()));
    }
    private int dp(View v, int value) { return (int) (v.getResources().getDisplayMetrics().density * value); }
    @Override public int getItemCount() { return items.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        TextView name, info; LinearLayout buttons;
        Holder(View v) { super(v); name = v.findViewById(R.id.textExerciseName); info = v.findViewById(R.id.textExerciseInfo); buttons = v.findViewById(R.id.setButtons); }
    }
}
