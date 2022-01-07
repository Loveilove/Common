package com.ht.orm;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 *
 */
public final class DBManager {

    @SuppressLint("StaticFieldLeak")
    private static DBHelperManager dbHelperManager;

    /**
     * Initialize NoHttp, should invoke on {@link android.app.Application#onCreate()}.
     *
     * @param context {@link Context}.
     */
    public static void initialize(Context context) {
        dbHelperManager = new DBHelperManager(context);
    }

    public static void remove(String name) {
        testInitialize();
        dbHelperManager.remove(name);
    }

    public static void clearAndClose() {
        testInitialize();
        dbHelperManager.clear();
    }

    /**
     * Test initialized.
     */
    private static void testInitialize() {
        if (dbHelperManager == null) {
            throw new ExceptionInInitializerError("Please invoke initialize(Context) Applicaton onCreate() first");
        }
    }

    public static DBHelperManager getHelpManaer() {
        testInitialize();
        return dbHelperManager;
    }

    public static void setCurrent(DBHelperManager.Config config) {
        testInitialize();
        dbHelperManager.setCurrent(config);
    }

    public static void setCurrent(String configId) {
        testInitialize();
        dbHelperManager.setCurrent(configId);
    }


}
