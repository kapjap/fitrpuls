package com.example.gymnotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddExerciseActivity extends AppCompatActivity {

    private ImageView buttonBack;
    private TextView buttonSaveExercise;

    private EditText editExerciseName;
    private EditText editSets;
    private EditText editReps;
    private EditText editWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_exercise);

        WorkoutScreenUi.install(this, findViewById(R.id.main));

        buttonBack = findViewById(R.id.buttonBack);
        buttonSaveExercise = findViewById(R.id.buttonSaveExercise);

        editExerciseName = findViewById(R.id.editExerciseName);
        editSets = findViewById(R.id.editSets);
        editReps = findViewById(R.id.editReps);
        editWeight = findViewById(R.id.editWeight);

        if(savedInstanceState==null && getIntent().hasExtra("edit_index")) {
            editExerciseName.setText(getIntent().getStringExtra("name"));
            editSets.setText(String.valueOf(getIntent().getIntExtra("sets",1)));
            editReps.setText(String.valueOf(getIntent().getIntExtra("reps",1)));
            editWeight.setText(String.valueOf(getIntent().getDoubleExtra("weight",0)));
        }
        buttonBack.setOnClickListener(v -> finish());
        buttonSaveExercise.setOnClickListener(v -> saveExercise());
    }

    private void saveExercise() {
        String name = editExerciseName.getText().toString().trim();
        String setsText = editSets.getText().toString().trim();
        String repsText = editReps.getText().toString().trim();
        String weightText = editWeight.getText().toString().trim();

        if (name.isEmpty()) {
            editExerciseName.setError("Введите название упражнения");
            return;
        }

        if (setsText.isEmpty()) {
            editSets.setError("Введите количество подходов");
            return;
        }

        if (repsText.isEmpty()) {
            editReps.setError("Введите количество повторений");
            return;
        }

        if (weightText.isEmpty()) {
            editWeight.setError("Введите вес");
            return;
        }

        int sets;
        int reps;
        double weight;

        try {
            sets = Integer.parseInt(setsText);
        } catch (NumberFormatException e) {
            editSets.setError("Введите корректное число");
            return;
        }

        try {
            reps = Integer.parseInt(repsText);
        } catch (NumberFormatException e) {
            editReps.setError("Введите корректное число");
            return;
        }

        try {
            weight = Double.parseDouble(weightText.replace(",", "."));
        } catch (NumberFormatException e) {
            editWeight.setError("Введите корректный вес");
            return;
        }

        if (sets <= 0) {
            editSets.setError("Подходов должно быть больше 0");
            return;
        }

        if (sets > 5) {
            editSets.setError("Максимум 5 подходов");
            return;
        }

        if (reps <= 0) {
            editReps.setError("Повторений должно быть больше 0");
            return;
        }

        if (!Double.isFinite(weight) || weight < 0) {
            editWeight.setError("Вес не может быть отрицательным");
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("edit_index",getIntent().getIntExtra("edit_index",-1));
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("sets", sets);
        resultIntent.putExtra("reps", reps);
        resultIntent.putExtra("weight", weight);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}