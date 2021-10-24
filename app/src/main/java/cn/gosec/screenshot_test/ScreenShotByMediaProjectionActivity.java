package cn.gosec.screenshot_test;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

public class ScreenShotByMediaProjectionActivity extends AppCompatActivity {
    Handler handler;
    private String TAG = "ScreenShotByMediaProjection ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.get_screenshot);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCapturePermission();
            }
        });
    }
    public void requestCapturePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            return;
        }
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if(mediaProjectionManager!=null)
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: //REQUEST_MEDIA_PROJECTION
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //5.0 之后才允许使用屏幕截图
                    return;
                }
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // 必须用server来启动
                    Intent service = new Intent(this, ScreenShotByMediaProjectionServer.class);
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    int screenWidth = metrics.widthPixels;
                    int screenHeight = metrics.heightPixels;
                    int screenDensity = metrics.densityDpi;

                    service.putExtra("code", resultCode);
                    service.putExtra("data", data);
                    service.putExtra("screenWidth", screenWidth);
                    service.putExtra("screenHeight", screenHeight);
                    service.putExtra("screenDensity", screenDensity);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(service);
                    }
//                    // 开始获取截屏
//                    setMediaProjection(this.mediaProjection);
//                    startScreenShot();
                } else {
                    //如果获取失败的处理，可以选择再次请求
                    requestCapturePermission();
                }
                break;
        }
    }
}