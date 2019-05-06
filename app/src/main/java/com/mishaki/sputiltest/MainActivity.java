package com.mishaki.sputiltest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mishaki.sputil.BaseSharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseSharedPreferencesUtil util = new BaseSharedPreferencesUtil() {
            @Override
            protected String getSpName() {
                return "name";
            }
        };
    }
}
