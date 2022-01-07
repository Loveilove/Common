package com.ht.common.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.io.File;

import androidx.core.content.FileProvider;


/**
 * 文件打开工具类
 */
public class FileOpen {

    public static final int INSTALL_UNKNOWN_APP_SOURCES = 4096;

    /**
     * 打开文件
     * @param activity
     * @param filePath 文件全路径
     */
    public static void openFile(Activity activity,String filePath){
        if(filePath.endsWith(".apk")){
            //apk安装需要特殊处理
            checkIsAndroidO(activity,filePath);
        }else{
            String authority = activity.getPackageName()+".fileProvider";
            Intent intent = getOpenIntent(filePath,activity,authority);
            if(intent == null){
                T.showShort(activity,"不支持打开此文件格式，请安装合适的程序再打开: "+filePath);
                return;
            }
            if (activity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    HTLog.d("activity not found for mimeType over " + Uri.parse(filePath));
                }
            }
        }

    }

    /**
     * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
     */
    private static void checkIsAndroidO(Activity activity,String filePath) {
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = activity.getPackageManager().canRequestPackageInstalls();
            if (b) {
                installAPK(activity,filePath);
            } else {
                //请求安装未知应用来源的权限
                Uri packageURI = Uri.parse("package:"+activity.getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
                activity.startActivityForResult(intent, INSTALL_UNKNOWN_APP_SOURCES);
            }
        } else {
            installAPK(activity,filePath);
        }
    }


    /**
     * 获取一个文件打开Intent。自动识别文件扩展名。
     * @param filePath
     * @return
     */
    public static Intent getOpenIntent(String filePath,Context context,String authority){
        File file = new File(filePath);
        if(!file.exists()) {
            HTLog.d("找不到要打开的文件: "+filePath);
            return null;
        }
            /* 取得扩展名 */
        String end=file.getName().substring(file.getName().lastIndexOf(".") + 1,file.getName().length()).toLowerCase();
            /* 依扩展名的类型决定MimeType */
        if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||
                end.equals("xmf")||end.equals("ogg")||end.equals("wav")){
            return getAudioFileIntent(file,context,authority);
        }else if(end.equals("3gp")||end.equals("mp4")){
            return getVideoFileIntent(file,context,authority);
        }else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||
                end.equals("jpeg")||end.equals("bmp")){
            return getImageFileIntent(file,context,authority);
        }else if(end.equals("apk")){
            return getApkFileIntent(file,context,authority);
        }else if(end.equals("ppt")){
            return getPptFileIntent(file,context,authority);
        }else if(end.equals("xls")){
            return getExcelFileIntent(file,context,authority);
        }else if(end.equals("doc")){
            return getWordFileIntent(file,context,authority);
        }else if(end.equals("pdf")){
            return getPdfFileIntent(file,context,authority);
        }else if(end.equals("chm")){
            return getChmFileIntent(file,context,authority);
        }else if(end.equals("txt")){
            return getTextFileIntent(file,context,authority);
        }else{
            return getAllIntent(file,context,authority);
        }
    }

    /**
     * 获取一个用于打开文件的Intent
     * @param file
     * @param context
     * @return
     */
    public static Intent getAllIntent(File file,Context context,String authority) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, authority, file);
            intent.setDataAndType(contentUri, "*/*");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "*/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setAction(Intent.ACTION_VIEW);
        return intent;
    }

    //Android获取一个用于打开APK文件的intent
    public static Intent getApkFileIntent(File file,Context context,String authority) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, authority, file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    //Android获取一个用于打开VIDEO文件的intent
    public static Intent getVideoFileIntent(File file,Context context,String authority) {
        Intent intent = new Intent("android.intent.action.VIEW");
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "video/*");
        return intent;
    }

    //Android获取一个用于打开AUDIO文件的intent
    public static Intent getAudioFileIntent(File file,Context context,String authority){
        Intent intent = new Intent("android.intent.action.VIEW");
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            contentUri = Uri.fromFile(file);
        }
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        intent.setDataAndType(contentUri, "audio/*");
        return intent;
    }

    //Android获取一个用于打开Html文件的intent
    public static Intent getHtmlFileIntent(String param,Context context,String authority){
        Intent intent = createViewIntent();
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = Uri.parse(param ).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param ).build();
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentUri = Uri.parse(param ).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param ).build();
        }
        intent.setDataAndType(contentUri, "text/html");
        return intent;
    }

    //Android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(File file,Context context,String authority) {
        Intent intent = createViewIntent();
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "image/*");
        return intent;
    }

    //Android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent(File file,Context context,String authority){
        Intent intent = createViewIntent();
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "application/vnd.ms-powerpoint");
        return intent;
    }

    //Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(File file,Context context,String authority){
        Intent intent = createViewIntent();
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "application/vnd.ms-excel");
        return intent;
    }

    //Android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent(File file,Context context,String authority){
        Intent intent = createViewIntent();
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "application/msword");
        return intent;
    }

    //Android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent(File file,Context context,String authority){
        Intent intent = createViewIntent();
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "application/x-chm");
        return intent;
    }

    //Android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent(File file,Context context,String authority){
        Intent intent = createViewIntent();
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "text/plain");
        return intent;
    }
    //Android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent(File file,Context context,String authority){
        Intent intent = createViewIntent();
        //判断是否是AndroidN以及更高的版本
        Uri contentUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contentUri = FileProvider.getUriForFile(context,authority, file);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentUri = Uri.fromFile(file);
        }
        intent.setDataAndType(contentUri, "application/pdf");
        return intent;
    }

    private static Intent createViewIntent(){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        return intent;
    }

    public static void installAPK(Activity activity,String filePath){
        String authority = activity.getPackageName()+".fileProvider";
        Intent intent = getOpenIntent(filePath,activity,authority);
        if (activity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                HTLog.d("activity not found for mimeType over " + Uri.parse(filePath));
            }
        }
    }
}
