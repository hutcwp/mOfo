package me.hutcwp.mofo.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.elvishew.xlog.XLog;

import me.hutcwp.mofo.ui.MainActivity;
import me.hutcwp.mofo.ui.map.CustomLocationModeMapActivity;
import me.hutcwp.mofo.ui.map.WalkRouteActivity;

/**
 * Created by hutcwp on 2018/3/31.
 */


public class NavigationUtils {

    private static final String TAG = "NavigationUtils";

    public static void navToMain(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void navToCustomLocationModeMapActivity(Context context) {
        Intent intent = new Intent(context, CustomLocationModeMapActivity.class);
        context.startActivity(intent);
    }

    public static void navToWalkRouteActivity(Context context) {
        Intent intent = new Intent(context, WalkRouteActivity.class);
        context.startActivity(intent);
    }


}
