package com.ht.common.util;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import es.dmoral.toasty.MyToast;

/**
 * Toast统一工具类
 */
public final class T
{

//    private static Toast shortToast;
//    private static Toast longToast;
    private static Toast currentToast;

    private T()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 是否支持Toast
     */
//    public static boolean isShow = true;

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, CharSequence message)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }else {
            MyToast.show(message);
        }
       /* if (isShow){
            if(shortToast == null){
                shortToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            }else{
                shortToast.setText(message);
            }
            shortToast.show();
        }*/
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, int message)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }else {
            MyToast.show(message);
        }
        /*if (isShow){
            if(shortToast == null){
                shortToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            }else{
                shortToast.setText(message);
            }
            shortToast.show();
        }*/
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, CharSequence message)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }else{
            MyToast.showL(message);
        }


    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, int message)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }else {
            MyToast.showL(message);
        }
        /*if (isShow){
            if(longToast == null){
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }else{
                longToast.setText(message);
            }
            longToast.show();
        }*/
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void show(Context context, CharSequence message, int duration)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            Toast.makeText(context, message, duration).show();
        }else {
            MyToast.show(message);
        }
        /*if (isShow)
            Toast.makeText(context, message, duration).show();*/
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void show(Context context, int message, int duration)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            Toast.makeText(context, message, duration).show();
        } else {
            MyToast.show(message);
        }
       /* if (isShow)
            Toast.makeText(context, message, duration).show();*/
    }

}