package cn.gosec.screenshot_test;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
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
import java.util.Objects;

public class ScreenShotByMediaProjectionServer extends Service {
    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    private int screenHeight;
    private int screenWidth;
    private int screenDensity;
    private VirtualDisplay virtualDisplay;
    private String TAG = "ScreenShotByMediaProjectionServer";

    public ScreenShotByMediaProjectionServer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent data = intent.getParcelableExtra("data");
        int resultCode = intent.getIntExtra("code", 0);
        screenWidth = intent.getIntExtra("screenWidth", 0);
        screenHeight = intent.getIntExtra("screenHeight", 0);
        screenDensity = intent.getIntExtra("screenDensity", 0);
        ToastUtils.toastShow(getApplicationContext(), String.valueOf(screenWidth));
        this.mediaProjection = ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .getMediaProjection(resultCode, Objects.requireNonNull(data));
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
        startScreenShot();
        return super.onStartCommand(intent, flags, startId);
    }
    private void startVirtual() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            return;
        }
        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenShot",
                screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                imageReader.getSurface(), null, null);
    }
    private void startScreenShot() {
        startVirtual();
        startCapture();
    }

    private void startCapture() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Image image = imageReader.acquireLatestImage();
                if (image == null) {
                    releaseResourse();
                    startScreenShot();
                    Log.i(TAG, "Capture Failed");
                    return;
                }

                int width = image.getWidth();
                int height = image.getHeight();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();

                //每个像素的间距
                int pixelStride = planes[0].getPixelStride();
                //总的间距
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                Log.i(TAG, "bitmap: "+bitmap.toString());
                image.close();
            }
        }, 100);
    }
    private void releaseResourse() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }
}