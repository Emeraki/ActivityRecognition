package com.uestc.zl427.newPhone2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity implements OnClickListener {

    private TextView result;
    private TextView values;
    private Button startButton;

    // 获取相关权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("MainActivity", "onCreate()被调用");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result = (TextView) findViewById(R.id.result);
        values = (TextView) findViewById(R.id.values);
        startButton = (Button) findViewById(R.id.start_button);
        //设置开始检测按钮监听事件
        startButton.setOnClickListener(this);

        //判断是否开启省电白名单
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            System.out.println("开启中....");
            if (!KeepAliveUtil.isIgnoringBatteryOptimizations(this)) {
                KeepAliveUtil.requestIgnoreBatteryOptimizations(this, 0x001);
                System.out.println("开启了");
            } else {
                Toast.makeText(this, "\"忽略电池优化功能\"已开启", Toast.LENGTH_SHORT).show();
                System.out.println("已开启");
            }
        } else {
            System.out.println("无法开启");
            Toast.makeText(this, "安卓版本太低，不支持此功能", Toast.LENGTH_SHORT).show();
        }

        //开启相关权限
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        // 检查写的权限是否开启，主要对于Android11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 适配android11读写权限
            if (Environment.isExternalStorageManager()) {
                //已获取android读写权限
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
            }
        }
        //开启后台服务，将传感器数据检测和发送任务放到后台
        Intent intent = new Intent(this, MyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent);
        } else {
            this.startService(intent);
        }
    }


    @Override
    protected void onPause() {
        Log.d("MainActivity", "onPause()被调用");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity", "onStop()被调用");
        super.onStop();
    }

    protected void onDestroy() {
        Log.d("MainActivity", "onDestory()被调用");
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        Log.d("MainActivity", "onClick()被调用");
        switch (view.getId()) {
        }
    }


}

