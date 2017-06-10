package com.ecar.pdalauncher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ecar.com.pdalauncher.Util.CheckUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //检测桌面
    public void check(View view) {
        CheckUtil.checkLauncher(this);
    }
}
