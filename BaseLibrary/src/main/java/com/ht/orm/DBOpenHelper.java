package com.ht.orm;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.ht.common.util.HTLog;
import com.ht.orm.annotation.Table;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * DBOpenHelper
 *
 * @author Wkkyo
 * @version 1.0.0
 * @date 2015-10-10
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    /**
     * 数据库版本号.
     */
    private int mNewVersion = 1;

    private int mOldVersion = 1;

    private boolean mToUpdate = false;

    /**
     * 数据库名.
     */
    private String mName;

    /**
     * 数据库文件夹全路径，不包含数据库文件
     */
    private String mPath;

//	private final Map<String,Boolean> tables = new HashMap<String, Boolean>();

//    private DBOpenHelper instance;

    /**
     * 是否已经初始化过只读数据库.
     */
    private boolean mReadIsInitializing = false;
    /**
     * 是否已经初始化过可写数据库.
     */
    private boolean mWritableIsInitializing = false;

    private SQLiteDatabase readDatabase;

    private SQLiteDatabase writeDatabase;

    private int writeOpenCount = 0;

    private int readOpenCount = 0;

    /**
     * 设置更新的表对应的类集合，只会使用一次
     *
     * @param updateTables
     */
    public void setUpdateTables(List<Class> updateTables) {
        this.updateTables = updateTables;
    }

    /**
     * 数据库版本升级时的表
     */
    private List<Class> updateTables;

    /**
     * @param context
     * @param dbPath  数据库文件全路径
     * @return
     */
    public synchronized static DBOpenHelper getInstance(Context context, String dbPath, String dbName, int version) {
//		if(dbName != null && !dbName.equals(mName)){
//			mName = dbName;
//		}
//		if(instance == null || !dbPath.equals(mPath)){
//			instance = new DBOpenHelper(context,dbPath,version);
//		}
//		return instance;
        return new DBOpenHelper(context, dbPath, dbName, version);
    }

    private DBOpenHelper(Context context, String dbPath, String dbName, int version) {
        super(new CustomPathContext(context, dbPath), dbName, null, version);
        this.mNewVersion = version;
        this.mPath = dbPath;
        this.mName = dbName;
    }

    /**
     * @param context
     * @param dbPath
     * @param dbName
     * @param version
     * @param isDebug 如果此值为true，则每次都会重新创建数据库文件。
     */
    private DBOpenHelper(Context context, String dbPath, String dbName, int version, boolean isDebug) {
        super(new CustomPathContext(context, dbPath), dbName, null, version);
        this.mNewVersion = version;
        this.mPath = dbPath;
        this.mName = dbName;
        if (isDebug) {
            File dbFile = new File(mPath + File.separator + mName);
            if (dbFile.exists()) {
                dbFile.delete();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HTLog.d("数据库升级，版本号 " + oldVersion + " -> " + newVersion);
        this.mOldVersion = oldVersion;
        mToUpdate = true;
        if (updateTables != null) {
            updateTables(db, oldVersion, newVersion, updateTables);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HTLog.d("数据库降级，版本号 " + oldVersion + " -> " + newVersion);
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (writeDatabase != null && writeDatabase.isOpen()) {
            return writeDatabase;
        }
        if (mWritableIsInitializing) {
            throw new IllegalStateException("getWritableDatabase called recursively");
        }
        // If we have a read-only database open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the database read-write would
        // fail waiting for the file lock.  To prevent that, we acquire the
        // lock on the read-only database, which shuts out other users.

        boolean success = false;
        SQLiteDatabase db = null;
        try {
            mWritableIsInitializing = true;
            if (mName == null) {
                db = SQLiteDatabase.create(null);
            } else {
                String path = mPath + File.separator + mName;
                checkOrCreateDatabaseFile();
                db = SQLiteDatabase.openOrCreateDatabase(path, null);
            }

            int version = db.getVersion();
            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        if (version > mNewVersion) {
                            onDowngrade(db, version, mNewVersion);
                        } else {
                            onUpgrade(db, version, mNewVersion);
                        }
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            onOpen(db);
            //启用事务，此处开启事务会有问题。统一在crud方法中处理事务。
//            db.beginTransaction();
            success = true;
//            writeOpenCount++;
            HTLog.d("获取可写数据库");
            return db;
        } finally {
            mWritableIsInitializing = false;
            if (success) {
                if (writeDatabase != null) {
                    try {
                        writeDatabase.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                writeDatabase = db;
            } else {
                if (db != null) {
                    db.close();
                }
            }
        }
    }

    /**
     * 获取只读数据库.
     *
     * @return 数据库对象
     */
    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (readDatabase != null && readDatabase.isOpen()) {
            //已经获取过
            return readDatabase;
        }
        if (mReadIsInitializing) {
            throw new IllegalStateException("getReadableDatabase called recursively");
        }
        SQLiteDatabase db = null;
        try {
            mReadIsInitializing = true;
            String path;
            if (mPath.endsWith("/")) {
                path = mPath + mName;
            } else {
                path = mPath + File.separator + mName;
            }

            File dbFile = new File(path);
            if (dbFile.exists()) {
                db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
                onOpen(db);
                readDatabase = db;
            } else {
                checkOrCreateDatabaseFile();
                getWritableDatabase();
                db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
                onOpen(db);
                readDatabase = db;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            mReadIsInitializing = false;
            if (db != null && db != readDatabase) {
                db.close();
            }
        }
//		readOpenCount++;
        HTLog.d("获取只读数据库");
        return readDatabase;
    }

    /**
     * 关闭可写数据库并提交事务.
     */
    public synchronized void closeWritableDatabase() {
        if (writeDatabase != null) {
            HTLog.d("关闭可写数据库");
            if (writeDatabase.inTransaction()) {
                writeDatabase.setTransactionSuccessful();
                writeDatabase.endTransaction();
            }
            if (writeDatabase.isOpen()) {
                writeDatabase.close();
                writeDatabase = null;
            }
            writeOpenCount--;
            if (writeOpenCount < 0) {
                writeOpenCount = 0;
            }
        }
    }

    public synchronized void commit() {
        if (writeDatabase != null) {
//			KKLog.d("commit数据库");
            if (writeDatabase.inTransaction()) {
                writeDatabase.setTransactionSuccessful();
                writeDatabase.endTransaction();
            }
        }
    }

    /**
     * 关闭只读数据库.
     */
    public synchronized void closeReadableDatabase() {
        if (readDatabase != null) {
            HTLog.d("关闭只读数据库");
            if (readDatabase.isOpen()) {
                readDatabase.close();
                readDatabase = null;
            }
            readOpenCount--;
            if (readOpenCount <= 0) {
                readOpenCount = 0;
            }
        }
    }

    public int getNewVersion() {
        return mNewVersion;
    }

    public int getOldVersion() {
        return mOldVersion;
    }

    public boolean needToUpdate() {
        return mToUpdate;
    }

    /**
     * 创建新的数据库
     *
     * @param db
     * @return
     */
    private void createTables(SQLiteDatabase db) {

    }

    private void checkOrCreateDatabaseFile() {
        String path = mPath + File.separator + mName;
        File dbFile = new File(path);
        if (!dbFile.exists()) {
            File file = new File(mPath);
            if (!file.exists()) {
                file.mkdirs();
            }
//			TableHelper.TABLES.clear_n();
        }
    }

//	public void cacheTable(String tableName){
//		this.tables.put(tableName, true);
//	}

    /**
     * 数据库版本更新时可以覆盖，如果只新增了列，可以选择不覆盖。
     */
    protected void updateTables(SQLiteDatabase db, int oldVersion, int newVersion, List<Class> classes) {
        try {
            for (Class<?> clazz : classes) {
                if (!clazz.isAnnotationPresent(Table.class)) {
                    continue;
                }
                String tableName = (clazz.getAnnotation(Table.class)).name();
                if (tableName == null || "".equals(tableName)) {
                    tableName = clazz.getSimpleName();
                }

                Method[] methods = clazz.getMethods();
                boolean customUpate = false;
                for (Method method : methods) {
                    if (method.getName().equals("updateTable")) {
                        customUpate = true;
                        method.invoke(clazz.newInstance(), db, oldVersion, newVersion);
                        break;
                    }
                }
                if (!customUpate) {
                    if (!TableHelper.checkTableExist(db, clazz)) {
                        continue;
                    }
                    String tempTableName = tableName + "_temp_" + oldVersion;

                    StringBuilder alterSql = new StringBuilder();
                    alterSql.append("ALTER TABLE ");
                    alterSql.append(tableName);
                    alterSql.append(" RENAME TO ");
                    alterSql.append(tempTableName);
                    try {
                        db.execSQL(alterSql.toString());
                        TableHelper.createTable(db, clazz);
//						cacheTable(tableName);
                        String[] oldColumnArr = TableHelper.getColumnNames(db, tempTableName);

                        String[] newColumnArr = TableHelper.getColumnNames(db, tableName);
                        List<String> findColumns = new ArrayList<String>();
                        for (String column : newColumnArr) {
                            for (String oldColumn : oldColumnArr) {
                                if (column.equals(oldColumn)) {
                                    findColumns.add(column);
                                    break;
                                }
                            }
                        }
                        if (findColumns.size() > 0) {
                            String[] columns = new String[findColumns.size()];
                            for (int i = 0; i < findColumns.size(); i++) {
                                columns[i] = findColumns.get(i);
                            }
                            String insertColumns = TableHelper.join(columns, ",", 0, columns.length);
                            StringBuilder insertSql = new StringBuilder();
                            insertSql.append("INSERT INTO ");
                            insertSql.append(tableName);
                            insertSql.append(" (" + insertColumns + ") ");
                            insertSql.append(" SELECT " + insertColumns + " FROM " + tempTableName);
                            db.execSQL(insertSql.toString());

                            StringBuilder dropSql = new StringBuilder();
                            dropSql.append("DROP TABLE IF EXISTS ");
                            dropSql.append(tempTableName);
                            db.execSQL(dropSql.toString());
                        }
                    } catch (SQLiteException e) {
                        HTLog.d("数据库异常:" + e.getMessage());
                        TableHelper.createTable(db, clazz);
                    } finally {
//				closeDatabase(false);
                    }
                }
            }
            classes.clear();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
//            closeDatabase(false);
        }

    }
}

final class CustomPathContext extends ContextWrapper {

    private String mDbPath;

    public CustomPathContext(Context context, String dbPath) {
        super(context);
        this.mDbPath = dbPath;
        File file = new File(mDbPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public File getDatabasePath(String name) {
        File file = new File(mDbPath + File.separator + name);
        return file;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                               CursorFactory factory) {
        return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                               CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return super.openOrCreateDatabase(getDatabasePath(name)
                .getAbsolutePath(), mode, factory, errorHandler);
    }
}