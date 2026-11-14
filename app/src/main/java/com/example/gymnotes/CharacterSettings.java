package com.example.gymnotes;
import android.app.Activity;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
public final class CharacterSettings {
    public static void show(Activity activity){
        CharacterPreferences prefs=new CharacterPreferences(activity);
        LinearLayout layout=new LinearLayout(activity);layout.setOrientation(LinearLayout.VERTICAL);int pad=(int)(24*activity.getResources().getDisplayMetrics().density);layout.setPadding(pad,0,pad,0);
        RadioGroup modes=new RadioGroup(activity);
        String[] labels={CharacterPreferences.NORMAL,CharacterPreferences.BRO,CharacterPreferences.CHAOS};
        for(int i=0;i<labels.length;i++){RadioButton b=new RadioButton(activity);b.setId(android.view.View.generateViewId());b.setText(labels[i]);modes.addView(b);if(labels[i].equals(prefs.mode()))modes.check(b.getId());}
        CheckBox humor=new CheckBox(activity);humor.setText("Редкие реплики на главной");humor.setChecked(prefs.humor());
        CheckBox effects=new CheckBox(activity);effects.setText("Короткий эффект нового уровня");effects.setChecked(prefs.effects());
        TextView info=new TextView(activity);info.setText("Характер не меняет статистику и рекомендации. XP: 100 за день с сохранённой тренировкой (UTC), максимум 100 в день. 500 XP = следующий уровень. Вес и повторения не дают бонусов.");
        layout.addView(modes);layout.addView(humor);layout.addView(effects);layout.addView(info);
        new AlertDialog.Builder(activity).setTitle("Характер ANTI-GYM").setView(layout).setNegativeButton(android.R.string.cancel,null)
            .setPositiveButton("Сохранить",(d,w)->prefs.save(((RadioButton)modes.findViewById(modes.getCheckedRadioButtonId())).getText().toString(),humor.isChecked(),effects.isChecked())).show();
    }
    private CharacterSettings(){}
}
