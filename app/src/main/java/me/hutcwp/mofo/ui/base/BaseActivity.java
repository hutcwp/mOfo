package me.hutcwp.mofo.ui.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by hutcwp on 2018/3/31.
 */


public class BaseActivity extends AppCompatActivity{

    private static final String TAG = "BaseActivity";
    private boolean mIsResumed = false;
    private boolean mIsStoped = false;

    public static final int EATKEYEVENT = 5;// 是否屏蔽按键的事件，控制按键的频率
    private static final int keyEventTime = 350; //最短的按键事件应该是在100ms
    private static double DOUBLE_CLICK_TIME = 0L;
    private static boolean eatKeyEvent = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EATKEYEVENT:
                    eatKeyEvent = false;
                    break;
            }
        }
    };
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttachFragment(android.app.Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    public void toast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        } else {
            ((TextView) mToast.getView()).setText(msg);
        }
        mToast.show();
    }

    public void toast(int resId) {
        String s = getString(resId);
        toast(s);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsStoped = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsStoped = false;
        if (mToast != null) {
            mToast.cancel();
        }
    }

    public boolean isPageStoped() {
        return mIsStoped;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResumed = false;
    }

    protected boolean isPageResumed() {
        return mIsResumed;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
            return super.dispatchKeyEvent(event);
        }
        if (eatKeyEvent) {
            return true;
        }
        int action = event.getAction();
        switch (action) {
            case KeyEvent.ACTION_DOWN:
                if (event.getRepeatCount() > 0) {
                    eatKeyEvent = true;
                    mHandler.removeMessages(EATKEYEVENT);
                    Message msg = mHandler.obtainMessage(EATKEYEVENT);
                    mHandler.sendMessageDelayed(msg, keyEventTime);
                }
                if (System.currentTimeMillis() - DOUBLE_CLICK_TIME < keyEventTime) {
                    eatKeyEvent = true;
                    mHandler.removeMessages(EATKEYEVENT);
                    Message msg = mHandler.obtainMessage(EATKEYEVENT);
                    mHandler.sendMessageDelayed(msg, keyEventTime);
                } else {
                    DOUBLE_CLICK_TIME = System.currentTimeMillis();
                }
                break;
            case KeyEvent.ACTION_UP:
                break;
            case KeyEvent.ACTION_MULTIPLE:
                break;
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    protected void addFragment(int containerViewId, Fragment fragment, String tag) {
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment, tag);
        fragmentTransaction.commit();
    }

    @TargetApi(17)
    protected boolean checkActivityValid() {
        if (this.isFinishing()) {
            //YLog.w(TAG, "activity is finishing");
            return false;
        }
        return !(Build.VERSION.SDK_INT >= 17 && this.isDestroyed());
    }

    protected void showDialogFragment(DialogFragment dialogFragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment oldFragment = fragmentManager.findFragmentByTag(tag);
        if (null != oldFragment && oldFragment.isAdded() && !oldFragment.isDetached()) {
            fragmentManager.beginTransaction().remove(oldFragment).commitAllowingStateLoss();
        }
        dialogFragment.show(fragmentManager, tag);
    }


}
