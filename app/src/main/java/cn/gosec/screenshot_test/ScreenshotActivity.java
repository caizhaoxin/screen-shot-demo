package cn.gosec.screenshot_test;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenshotActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("onCreate: ", "onCreate");
//        requestPermission();
//        requestPermission(getApplicationContext());
//        verifyStoragePermissions(this);

        Button button = (Button) findViewById(R.id.get_screenshot);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                screenshotByView(view);
                screenShotByShell("/storage/emulated/0/apple.png");
            }
        });
    }

    public void screenShotByShell(String filePath){
        String shotCmd = "screencap -p " + filePath;
        try {
            Log.i("screenShotByShell: ", shotCmd);
            Runtime.getRuntime().exec("su");
            Runtime.getRuntime().exec(shotCmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ToastUtils.toastShow(getApplicationContext(), "截图成功！");
    }

    private void screenshotByView(View view) {
        Log.i("a7888", "onClicking");
        // 获取当前activity的view
        View dView = getWindow().getDecorView();
        // 允许缓存当前view的图片（截图）
        dView.setDrawingCacheEnabled(true);
        // 缓存当前view图像
        dView.buildDrawingCache();
        // 获取缓存内的图像，然后生成一个bitmap
        Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
        // 之后就是optional的操作了..........
        if (bitmap != null) {
            try {
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
//                        // 图片文件路径
                String filePath = sdCardPath + File.separator + "screenshot.png";
                Toast.makeText(getApplicationContext(), filePath, Toast.LENGTH_SHORT).show();
                Log.i("filePath: ", filePath);
//                        File file = new File(filePath);
//                        FileOutputStream os = new FileOutputStream(file);
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
//                        os.flush();
//                        os.close();
//                        Log.i("a7888", "存储完成");
            } catch (Exception e) {
                Log.i("a7888", "存储失败 exception: " + e.toString());
            }
        } else {
            Log.i("a7888", "存储失败");
        }
    }

    private static int REQUEST_CODE = 1024;

    private void requestPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                writeFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                writeFile();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        } else {
            writeFile();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                writeFile();
            } else {
                Toast.makeText(getApplicationContext(), "存储权限获取失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                writeFile();
            } else {
                Toast.makeText(getApplicationContext(), "存储权限获取失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 模拟文件写入
     */
    private void writeFile() {
        Toast.makeText(this, "可以写入文件了", Toast.LENGTH_SHORT).show();
    }

    /**
     * 请求授权
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            Log.i("requestPermission: ", "requestPermission");
        } else {
            Log.i("startlistener: ", "startlistener");
            startlistener();
        }
        startlistener();
    }

    private void startlistener() {
        Log.i("startlistener: ", "startlistener");
        new Thread(new Runnable() {
            @Override
            public void run() {
                ScreenShotListenManager manager = new ScreenShotListenManager(getApplicationContext());
                manager.setListener(
                        imagePath -> {
                            // do something
                            Toast.makeText(getApplicationContext(), "----触发截屏imagePath:" + imagePath, Toast.LENGTH_LONG).show();
                            Log.i("screenshot data: ", "-----------触发截屏imagePath:" + imagePath);
                        }
                );
                manager.startListen();
            }
        }).start();
    }
}