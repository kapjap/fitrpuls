package com.example.gymnotes;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Random;

/** Presentation only: never referenced by statistics or training rules. */
public final class CharacterPreferences {
    public static final String NORMAL="Normal", BRO="Bro", CHAOS="Chaos";
    private final SharedPreferences prefs;
    public CharacterPreferences(Context context) {
        String uid=FirebaseAuth.getInstance().getUid();
        prefs=context.getSharedPreferences("anti_gym_character_"+(uid==null?"guest":uid),Context.MODE_PRIVATE);
    }
    public String mode(){return prefs.getString("mode",BRO);}
    public boolean humor(){return prefs.getBoolean("humor",true);}
    public boolean effects(){return prefs.getBoolean("effects",true);}
    public void save(String mode,boolean humor,boolean effects){prefs.edit().putString("mode",mode).putBoolean("humor",humor).putBoolean("effects",effects).apply();}
    public String greeting(long now){
        if(!humor()||NORMAL.equals(mode()))return null;
        long last=prefs.getLong("last_line",0);
        if(now-last<3L*24*60*60*1000 || new Random().nextInt(5)!=0)return null;
        prefs.edit().putLong("last_line",now).apply();
        return CHAOS.equals(mode())?"Чё, опять пришёл страдать? План всё равно твой.":"Без пафоса. Просто твоя следующая тренировка.";
    }
    public boolean consumeLevelUp(int level){
        int previous=prefs.getInt("seen_level",level);
        prefs.edit().putInt("seen_level",Math.max(previous,level)).apply();
        return effects()&&level>previous;
    }
}
