package me.hutcwp.mofo.ui;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.hutcwp.mofo.R;
import me.hutcwp.mofo.util.NavigationUtils;

public class SplashActivity extends AppCompatActivity {

    private static int DelayTime = 2 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                NavigationUtils.navToMain(SplashActivity.this);
                NavigationUtils.navToMain(SplashActivity.this);
                SplashActivity.this.finish();
            }
        }, DelayTime);
    }

}
