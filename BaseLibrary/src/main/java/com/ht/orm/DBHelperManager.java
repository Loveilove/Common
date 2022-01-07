package com.ht.orm;

import android.content.Context;
import android.os.Environment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ht.common.util.HTLog;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 数据库配置管理器
 */
public final class DBHelperManager {


    private Map<String,DBOpenHelper> helperMap;

    private DBOpenHelper currentHelper;

    private Context mContext;

    public DBHelperManager(Context mContext) {
        this.mContext = mContext;
        this.helperMap = Maps.newHashMap();
    }

//    public static Config newBuilder(Context context){
//        return new Config(context);
//    }

//    DBOpenHelper getHelper(){
//        return currentHelper;
//    }

//    public DBOpenHelper getHelper(String helperName){
//        return helperMap.get(helperName);
//    }

    public DBOpenHelper getCurrentHelper() {
        return currentHelper;
    }

    /*
    public void setCurrent(String key){
        if(helperMap.containsKey(key)){
            currentHelper = helperMap.get(key);
        }else{
            currentHelper = DBOpenHelper.getInstance(context,DBConfig.DB_PATH,DBConfig.DB_NAME,DBConfig.DB_VERSION);
            helperMap.put(DBConfig.DB_PATH,currentHelper);
        }
    }*/

    String setCurrent(String configId){
        if(helperMap.containsKey(configId)){
            HTLog.d("创建获取缓存数据库访问实例"+configId);
            currentHelper = helperMap.get(configId);
        }else{
            Config config = DBHelperManager.newConfig(mContext);
            if(configId.endsWith(".db")){
                String[] paths = configId.split("/");
                String path="";
                for(int i = 0 ;i<paths.length-1;i++){
                    path+=paths[i];
                    path+="/";
                }
                config.path(path);
            }else{
                config.path(configId);
            }
            currentHelper = DBOpenHelper.getInstance(config.mContext,config.mPath,config.mName,config.mVersion);
            helperMap.put(config.mPath,currentHelper);
            HTLog.d("创建数据库访问实例"+config.mPath);
        }
        return configId;
    }

    String setCurrent(Config config){
        if(config.mPath == null){
            throw new ExceptionInInitializerError("没有配置数据库文件路径，数据库文件路径为null");
        }
        String mId = config.mPath+"/"+config.mName;
        if(helperMap.containsKey(mId)){
            HTLog.d("获取缓存数据库访问实例"+mId);
            currentHelper = helperMap.get(mId);
        }else{
            currentHelper = DBOpenHelper.getInstance(mContext,config.mPath,config.mName,config.mVersion);
            if(config.updateClasses != null){
                currentHelper.setUpdateTables(config.updateClasses);
            }
            helperMap.put(mId,currentHelper);
            HTLog.d("创建数据库访问实例"+mId);
        }
        return config.mPath;
    }

    public void remove(String name){
        if(helperMap.containsKey(name)){
            HTLog.d("删除数据库访问实例"+name);
            DBOpenHelper helper = helperMap.remove(name);
            if(helper != null){
                helper.closeReadableDatabase();
                helper.closeWritableDatabase();
            }
            if(currentHelper == helper){
                currentHelper = null;
            }
        }
    }

    public void clear(){
        HTLog.d("删除全部数据库访问实例");
        for(String name:helperMap.keySet()){
            DBOpenHelper helper = helperMap.get(name);
            if(helper != null){
                helper.closeReadableDatabase();
                helper.closeWritableDatabase();
            }
        }
        helperMap.clear();
    }

    public static Config newConfig(Context context) {
        return new Config(context);
    }

    public static class Config {

        private Context mContext;
        private String mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+"db";
        /**
         * 数据库文件名，默认为data.db
         */
        private String mName = "data.db";

        /** 数据库版本号. */
        private int mVersion = 1;

        private boolean mDebug = false;

        private List<Class> updateClasses;

        public Config(Context context) {
            this.mContext = context.getApplicationContext();
        }

        public Config path(String path) {
            this.mPath = path;
            return this;
        }

        public Config name(String name) {
            this.mName = name;
            return this;
        }

        public Config version(int version) {
            this.mVersion = version;
            return this;
        }

        public Config debug(boolean debug) {
            this.mDebug = debug;
            return this;
        }

        public Config addUpdateClass(Class clazz){
            if(this.updateClasses == null){
                this.updateClasses = Lists.newArrayList();
            }
            this.updateClasses.add(clazz);
            return this;
        }

//        public DBHelperManager build() {
//            return new DBHelperManager(this);
//        }
    }

}
