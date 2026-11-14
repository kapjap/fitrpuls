package com.example.gymnotes;

import android.app.Activity;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/** Keeps existing feature Activities reachable without duplicate tab stacks. */
public final class MainNavigation {
    public static void attach(Activity activity,int selected) {
        ViewGroup content=activity.findViewById(android.R.id.content);
        View screen=content.getChildAt(0);content.removeView(screen);
        LinearLayout shell=new LinearLayout(activity);shell.setOrientation(LinearLayout.VERTICAL);
        shell.addView(screen,new LinearLayout.LayoutParams(-1,0,1));
        BottomNavigationView nav=new BottomNavigationView(activity);
        nav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        String[] labels={"Главная","Тренировки","Прогресс","Залы","Профиль"};
        int[] icons={R.drawable.nav_home,R.drawable.nav_workout,R.drawable.nav_progress,R.drawable.nav_map,R.drawable.ic_profile_person};
        Class<?>[] targets={HomeMenuActivity.class,WorkoutsActivity.class,ProgressActivity.class,GymsNearbyActivity.class,ProfileActivity.class};
        for(int i=0;i<labels.length;i++)nav.getMenu().add(0,i+1,i,labels[i]).setIcon(icons[i]);
        nav.setSelectedItemId(selected+1);
        nav.setOnItemSelectedListener(item->{
            int index=item.getItemId()-1;if(index==selected)return true;
            Intent intent=new Intent(activity,targets[index]);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            if(selected!=0)activity.finish();
            return true;
        });
        shell.addView(nav,new LinearLayout.LayoutParams(-1,-2));content.addView(shell,new ViewGroup.LayoutParams(-1,-1));
    }
    private MainNavigation(){}
}
