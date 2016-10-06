package com.inlook.android_clean_bluetooth.base;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

/**
 * com.inlook.android_clean_bluetooth.base
 *
 * @auth or
 * @sinced on 2016/10/5.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        setContentView(getLayoutResId());
    }

  public  abstract int getLayoutResId();
}
