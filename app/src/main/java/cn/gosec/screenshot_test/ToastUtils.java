package cn.gosec.screenshot_test;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void toastShow(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
