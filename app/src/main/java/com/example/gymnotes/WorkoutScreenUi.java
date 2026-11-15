package com.example.gymnotes;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** Insets owner for the workout list shell and scrollable creation forms only. */
final class WorkoutScreenUi {
    static void install(Activity activity, View root) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        boolean light = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat bars = new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView());
        bars.setAppearanceLightStatusBars(light); bars.setAppearanceLightNavigationBars(light);
        int left=root.getPaddingLeft(),top=root.getPaddingTop(),right=root.getPaddingRight(),bottom=root.getPaddingBottom();
        int[] lastIme={-1};
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets safe=insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            v.setPadding(left+safe.left,top+safe.top,right+safe.right,bottom+safe.bottom);
            int ime=insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            if(ime>0&&ime!=lastIme[0])v.post(()->{
                View focus=activity.getCurrentFocus();
                if(focus instanceof EditText){Rect rect=new Rect();focus.getDrawingRect(rect);focus.requestRectangleOnScreen(rect,false);}
            });
            lastIme[0]=ime;
            // Shell owns bottom padding; don't add a second inset in BottomNavigationView.
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
