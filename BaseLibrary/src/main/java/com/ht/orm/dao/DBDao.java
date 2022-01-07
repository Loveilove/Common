package com.ht.orm.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;


import com.ht.common.util.HTLog;
import com.ht.orm.DBManager;
import com.ht.orm.DBOpenHelper;
import com.ht.orm.Page;
import com.ht.orm.TableHelper;
import com.ht.orm.annotation.BusinessId;
import com.ht.orm.annotation.Column;
import com.ht.orm.annotation.Id;
import com.ht.orm.annotation.ManyToOne;
import com.ht.orm.annotation.OneToMany;
import com.ht.orm.annotation.Table;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 数据库交互Dao
 *
 */
public abstract class DBDao<T> implements IDBDao<T> {

    private DBOpenHelper dbHelper;

    protected Class<T> clazz;

    private String idColumn;

    private String tableName;

    private List<Field> columnFields;

    private static final ReentrantLock lock = new ReentrantLock();

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("unchecked")
    public DBDao(Context context) {
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.clazz = (Class<T>) pt.getActualTypeArguments()[0];
        this.dbHelper = DBManager.getHelpManaer().getCurrentHelper();
        if (dbHelper == null) {
            HTLog.d("dbHelper 没有初始化完成");
            return;
        }
//		this.dbHelper = DBOpenHelper.getInstance(context,DBConfig.DB_PATH,DBConfig.DB_NAME,DBConfig.DB_VERSION);
        if (!clazz.isAnnotationPresent(Table.class)) {
            HTLog.d("Model 对象未注记Table");
            return;
        }
        this.tableName = clazz.getAnnotation(Table.class).name();
        if (this.tableName.equals("")) {
            this.tableName = clazz.getSimpleName().toUpperCase();
        }
        this.columnFields = TableHelper.getColumnFields(clazz);
        for (Field field : columnFields) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(BusinessId.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name();
                if (columnName.equals("")) {
                    columnName = field.getName();
                }
                this.idColumn = columnName;
                break;
            }
        }
        SQLiteDatabase db = startWritableDatabase();
        if (db != null) {
            if (!TableHelper.checkTableExist(db, clazz)) {
                try {
                    lock.lock();
                    TableHelper.createTable(db, clazz);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } finally {
                    commit();
                    lock.unlock();
                }
            } else {
                //数据库升级
                if (dbHelper.needToUpdate()) {
//					updateTable(db,dbHelper.getOldVersion(),dbHelper.getNewVersion());
//					commit();
                }
            }
        }
    }
	
	/*public DBOpenHelper getDbHelper() {
		return dbHelper;
	}*/

//	private static byte[] bmpToByteArray(Bitmap bmp) {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		try {
//			bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//			bos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return bos.toByteArray();
//	}

    @Override
    public T save(T t) {
        try {
            lock.lock();
            SQLiteDatabase db = startWritableDatabase();
            db.beginTransaction();
            mSave(t, db);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            commit();
            lock.unlock();
        }
        return t;
    }

    @Override
    public List<T> save(List<T> list) {
        try {
            lock.lock();
            SQLiteDatabase db = startWritableDatabase();
            db.beginTransaction();
            for (T t : list) {
                mSave(t, db);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            commit();
            lock.unlock();
        }
        return list;
    }

    @Override
    public T get(Serializable id) {
        String idValue;
        if (id instanceof String) {
            idValue = "'" + id + "'";
        } else {
            idValue = id.toString();
        }
        List<T> list = query(idColumn + " = " + idValue);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /**
     * @param sql
     * @return
     */
    public List<T> queryBySql(String sql) {
        List<T> list = new ArrayList<T>();
        try {
            lock.lock();
            SQLiteDatabase db = startReadableDatabase();
            if (db != null) {
//				long s = System.currentTimeMillis();
                Cursor cursor = db.rawQuery(sql.toString(), null);
//				long e = System.currentTimeMillis();
//				KKLog.d("sql查询"+sql.toString()+"耗时:  " + (e-s));
                while (cursor.moveToNext()) {
                    T t = renderBean(cursor, db);
                    list.add(t);
                }
//				e = System.currentTimeMillis();
//				KKLog.d("sql查询"+sql.toString()+"循环 "+in+" 次 ,转换"+list.size()+"条记录耗时:  " + (e-s));
                cursor.close();
            } else {
                HTLog.d("startReadableDatabase 返回Null");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
//			closeDatabase(false);
            lock.unlock();
        }
        return list;
    }

    @Override
    public List<T> query() {
        return query("", null);
    }

    public List<T> query(Page page) {
        return query(null, page);
    }

    @Override
    public List<T> query(String where, Page page) {
        List<T> list = new ArrayList<T>();
        try {
            lock.lock();
            SQLiteDatabase db = startReadableDatabase();
            if (db != null) {
                StringBuilder sql = new StringBuilder();
                sql.append("select *");
			/*for(int i = 0;i<columnFields.size();i++){
				Field field = columnFields.get(i);
//				if(!(field.getType() == byte[].class)){
					if(i > 0){
						sql.append(",");
					}
					Column column = field.getAnnotation(Column.class);
					String columnName = column.name();
					if(columnName.equals("")){
						columnName = field.getName();
					}
					sql.append(columnName);
//				}
			}*/
                sql.append(" from ");
                sql.append(tableName);
                if (where != null && !where.equals("")) {
                    sql.append(" where ");
                    sql.append(where);
                }
                if (page != null) {
                    sql.append(" limit " + page.getPageSize() + " offset " + page.getOffset());
                }

                long s = System.currentTimeMillis();
                Cursor cursor = db.rawQuery(sql.toString(), null);
                long e = System.currentTimeMillis();
//				KKLog.d("sql查询"+sql.toString()+"耗时:  " + (e-s));
                s = System.currentTimeMillis();
                while (cursor.moveToNext()) {
                    T t = renderBean(cursor, db);
                    list.add(t);
                }
                e = System.currentTimeMillis();
//				KKLog.d("sql查询"+sql.toString()+"转换"+list.size()+"条记录耗时:  " + (e-s));
                cursor.close();
            } else {
                HTLog.d("startReadableDatabase 返回Null");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
//			closeDatabase(false);
            lock.unlock();
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.wkkyo.android.orm.dao.IDBDao#query(java.lang.String)
     */
    @Override
    public List<T> query(String where) {
        return query(where, null);
    }

    @Override
    public List<T> queryOrder(String orderBy) {
        return queryOrder(orderBy, null);
    }

    @Override
    public List<T> queryOrder(String orderBy, Page page) {
        List<T> list = new ArrayList<T>();
        try {
            lock.lock();
            SQLiteDatabase db = startReadableDatabase();
            StringBuilder sql = new StringBuilder();
            sql.append("select * from ");
            sql.append(tableName);
            if (orderBy != null) {
                sql.append(" ");
                sql.append(orderBy);
            }
            if (page != null) {
                sql.append(" limit " + page.getPageSize() + " offset " + page.getOffset());
            }
            Cursor cursor = db.rawQuery(sql.toString(), null);
            while (cursor.moveToNext()) {
                T t = renderBean(cursor, db);
                list.add(t);
            }
            cursor.close();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
//			closeDatabase(false);
            lock.unlock();
        }
        return list;
    }

    @Override
    public List<T> query(String selection, String[] selectionArgs, Page page) {
        return query(selection, selectionArgs, null, null, null, page);
    }

    @Override
    public List<T> query(String selection, String[] selectionArgs,
                         String groupBy, String having, String orderBy, Page page) {
        List<T> list = new ArrayList<T>();
        try {
            lock.lock();
            SQLiteDatabase db = startReadableDatabase();
            Cursor cursor = db.query(tableName, null, selection, selectionArgs,
                    groupBy, having, orderBy);
            while (cursor.moveToNext()) {
                T t = renderBean(cursor, db);
                list.add(t);
            }
            cursor.close();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
//			closeDatabase(false);
            lock.unlock();
        }
        return list;
    }

    @Override
    public long delete(Serializable id) {
        long row = 0;
        try {
            lock.lock();
            SQLiteDatabase db = startWritableDatabase();
            db.beginTransaction();
            String where = idColumn + " = ?";
            String[] args = new String[]{String.valueOf(id)};
            row = db.delete(tableName, where, args);
        } finally {
            commit();
            lock.unlock();
        }
        return row;
    }

    @Override
    public long deleteAll() {
        long row = 0;
        try {
            lock.lock();
            SQLiteDatabase db = startWritableDatabase();
            db.beginTransaction();
            row = db.delete(this.tableName, null, null);
        } finally {
            commit();
            lock.unlock();
        }
        return row;
    }

    @Override
    public long getCount(String where) {
        long count = 0;
        try {
            lock.lock();
            SQLiteDatabase db = startReadableDatabase();
            count = getCount(where, db);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
//			closeDatabase(false);
            lock.unlock();
        }
        return count;
    }

    @Override
    public long getCount() {
        return getCount(null);
    }

    @Override
    public T getLast() {
        T t = null;
        try {
            lock.lock();
            SQLiteDatabase db = startReadableDatabase();
            StringBuilder sql = new StringBuilder();
            sql.append("select * from ");
            sql.append(tableName);
            Cursor cursor = db.rawQuery(sql.toString(), null);
            cursor.moveToLast();
            t = renderBean(cursor, db);
            cursor.close();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return t;
    }

    /**
     * 主键生成器，默认采用“前缀+随机UUID+后缀”，子类根据实际需求覆盖
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return
     */
    public String generatorBusinessId(String prefix, String suffix) {
        String businessIdValue = UUID.randomUUID().toString().replace("-", "");
        return prefix + businessIdValue + suffix;
    }

    @Override
    public void execSQL(String sql) {
        try {
            lock.lock();
            SQLiteDatabase db = startWritableDatabase();
            if (db != null) {
                db.execSQL(sql);
            } else {
                HTLog.d("startReadableDatabase 返回Null");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
//			closeDatabase(false);
            lock.unlock();
        }
    }


    /**
     * 数据库版本更新时可以覆盖，如果只新增了列，可以选择不覆盖。
     */
    protected void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            lock.lock();
            String tableName = clazz.getAnnotation(Table.class).name();
            if (tableName == null || "".equals(tableName)) {
                tableName = clazz.getSimpleName();
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
//                dbHelper.cacheTable(tableName);
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

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
//            closeDatabase(false);
            lock.unlock();
        }

    }

    private long getCount(String where, SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        sql.append(tableName);
        if (where != null) {
            sql.append(" where ");
            sql.append(where);
        }
        long count = 0;
        Cursor cursor = db.rawQuery(sql.toString(), null);
        while (cursor.moveToNext()) {
            count = cursor.getLong(0);
        }
        cursor.close();
        return count;
    }

    private void mSave(T entity, SQLiteDatabase db) throws IllegalArgumentException, IllegalAccessException {
        long row = 0;
        Field idField = null;
        List<Field> fields = TableHelper.getColumnFields(clazz);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
                break;
            }
        }

        Field businessField = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(BusinessId.class)) {
                businessField = field;
                break;
            }
        }
        if (idField == null && businessField == null) {
            HTLog.d(entity.toString() + " Model 对象未注解主键");
            mInsert(entity, db);
            return;
        }
        if (businessField == null) {
            idField.setAccessible(true);
            Object idValue = idField.get(entity);
            ContentValues contentValues = setContentValues(entity);
            boolean isUpdate = true;
            if (idValue == null || "".equals(idValue)) {
                isUpdate = false;
            } else if (Integer.TYPE == idField.getType() || idField.getType() == Integer.class) {
                int id = Integer.parseInt(idValue.toString());
                if (id == 0) {
                    isUpdate = false;
                }
            } else if (Long.TYPE == idField.getType() || idField.getType() == Long.class) {
                long id = Long.parseLong(idValue.toString());
                if (id == 0) {
                    isUpdate = false;
                }
            } else if (Short.TYPE == idField.getType() || idField.getType() == Short.class) {
                long id = Short.parseShort(idValue.toString());
                if (id == 0) {
                    isUpdate = false;
                }
            }
            if (isUpdate) {
                Column column = idField.getAnnotation(Column.class);
                String columnName = column.name();
                if (columnName.equals("")) {
                    columnName = idField.getName();
                }
                String where = columnName + " = ?";
                String[] args = new String[]{String.valueOf(idValue)};
                row = db.update(tableName, contentValues, where, args);
            } else {
                row = db.insert(tableName, null, contentValues);
                if (row > -1) {
                    if (Integer.TYPE == idField.getType() || idField.getType() == Integer.class) {
                        idField.set(entity, (int) row);
                    } else if (Long.TYPE == idField.getType() || idField.getType() == Long.class) {
                        idField.set(entity, (long) row);
                    } else if (Short.TYPE == idField.getType() || idField.getType() == Short.class) {
                        idField.set(entity, (short) row);
                    } else {
                        idField.set(entity, row);
                    }
                }
            }
        } else {
            businessField.setAccessible(true);
            Object businessIdValue = businessField.get(entity);
            BusinessId businessId = businessField.getAnnotation(BusinessId.class);
            if (businessIdValue == null && !businessId.automatic()) {
                HTLog.d("Model 对象业务主键的值不能为null");
                return;
            } else {
                boolean isUpdate = true;
                if (businessId.automatic()) {
                    if (businessIdValue == null || "".equals(businessIdValue.toString())) {
                        String prefix = businessId.prefix();
                        String suffix = businessId.suffix();
                        businessIdValue = generatorBusinessId(prefix, suffix);
                        businessField.set(entity, businessIdValue);
                    }
                }
                Column column = businessField.getAnnotation(Column.class);
                String columnName = column.name();
                if (columnName.equals("")) {
                    columnName = businessField.getName();
                }
                long count = getCount(columnName + " = '" + String.valueOf(businessIdValue) + "'", db);
                if (count == 0) {
                    isUpdate = false;
                }
                ContentValues contentValues = setContentValues(entity);
                if (isUpdate) {
                    String where = columnName + " = ?";
                    String[] args = new String[]{String.valueOf(businessIdValue)};
                    row = db.update(tableName, contentValues, where, args);
                } else {
                    row = db.insert(tableName, null, contentValues);
                }
            }
        }
    }

    /**
     * 新增记录。
     *
     * @param entity
     * @param db
     * @return
     */
    private long mInsert(T entity, SQLiteDatabase db) {
        ContentValues contentValues = setContentValues(entity);
        return db.insert(tableName, null, contentValues);
    }

    /**
     * 获取写数据库，启用事务.
     */
    private SQLiteDatabase startWritableDatabase() {
        return this.dbHelper.getWritableDatabase();
    }

    /**
     * 获取只读数据库.
     */
    private SQLiteDatabase startReadableDatabase() {
        return this.dbHelper.getReadableDatabase();
    }

    /**
     * 提交事务
     */
    private void commit() {
//		KKLog.d(clazz.getSimpleName()+"数据库提交事务");
        dbHelper.commit();
    }

    /**
     * 关闭数据库并提交事务.
     */
    @Deprecated
    private void closeDatabase(boolean writable) {
        HTLog.d(clazz.getSimpleName() + "关闭数据库");
        if (writable) {
            dbHelper.commit();
//			dbHelper.closeWritableDatabase();
        } else {
//			dbHelper.closeReadableDatabase();
        }
    }

    private final static Object getCursorValue(Cursor cursor, Field field) {
        String name = "";
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            name = column.name();
            if (field.isAnnotationPresent(ManyToOne.class)) {
                field = TableHelper.getIdColumnField(field.getType());
            }
        }
        if (name.equals("")) {
            name = field.getName();
        }
        int index = cursor.getColumnIndex(name);
        if (index == -1) {
            name = name.toUpperCase();
            index = cursor.getColumnIndex(name);
        }
        Object value = null;
        if (index > -1) {
            if (field.getType() == String.class) {
                value = cursor.getString(index);
            } else if (Integer.TYPE == field.getType() || field.getType() == Integer.class) {
                value = cursor.getInt(index);
            } else if (Short.TYPE == field.getType() || field.getType() == Short.class) {
                value = cursor.getShort(index);
            } else if (Long.TYPE == field.getType() || field.getType() == Long.class) {
                value = cursor.getLong(index);
            } else if (Double.TYPE == field.getType() || field.getType() == Double.class) {
                value = cursor.getDouble(index);
            } else if (Float.TYPE == field.getType() || field.getType() == Float.class) {
                value = cursor.getFloat(index);
            } else if (Date.class == field.getType()) {
                try {
                    value = dateFormat.parse(cursor.getString(index));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (byte[].class == field.getType()) {
                try {
                    value = cursor.getBlob(index);
                } catch (SQLiteException e) {
                    Log.d("DAO", "getBlob 异常");
//					e.printStackTrace();
                }

            } else {
                value = cursor.getString(index);
            }
        }else{
            if (Integer.TYPE == field.getType()
                    || Short.TYPE == field.getType()
                    || Long.TYPE == field.getType()
                    || Double.TYPE == field.getType()
                    || Float.TYPE == field.getType()) {
               value = 0;
           }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private T renderBean(Cursor cursor, SQLiteDatabase db) throws IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        long e1 = System.currentTimeMillis();

        T t = this.clazz.newInstance();
//		KKLog.d("sql查询转换render bean开始:  "+t.getClass());
        List<Field> fields = TableHelper.getColumnFields(clazz);
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                boolean lazyLoad = manyToOne.lazy();
                if (!lazyLoad) {
                    if (field.getType() instanceof Class) {
                        String foreignValue = String.valueOf(getCursorValue(cursor, field));
                        String name = "";
                        if (name.equals("")) {
                            name = field.getName();
                        }
                        int index = cursor.getColumnIndex(name);
                        if (index == -1) {
                            name = name.toUpperCase();
                            index = cursor.getColumnIndex(name);
                        }
                        if (field.getType().isAnnotationPresent(Table.class)) {
                            if (TableHelper.checkTableExist(db, field.getType())) {
                                String relationsTableName = field.getType().getAnnotation(Table.class).name();
                                if (relationsTableName.equals("")) {
                                    relationsTableName = field.getType().getSimpleName().toUpperCase();
                                }

                                Field relationsIdField = TableHelper.getIdColumnField(field.getType());
                                String foreignKey = relationsIdField.getAnnotation(Column.class).name();
                                if (foreignKey.equals("")) {
                                    foreignKey = relationsIdField.getName();
                                }
                                StringBuilder sql = new StringBuilder();
                                sql.append("select * from ");
                                sql.append(relationsTableName);
                                sql.append(" where ");
                                sql.append(foreignKey);
                                sql.append(" = '" + foreignValue);
                                sql.append("'");
                                Cursor foreignCursor = db.rawQuery(sql.toString(), null);
                                while (foreignCursor.moveToNext()) {
                                    Object foreign = renderBean(field.getType(), foreignCursor);
                                    field.set(t, foreign);
                                }
                                foreignCursor.close();
                            }
                        }
                    }
                }
            } else if (field.isAnnotationPresent(OneToMany.class)) {
                OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                boolean lazyLoad = oneToMany.lazy();
                if (!lazyLoad) {
                    if (field.getType().isAssignableFrom(List.class)) {
                        Class<?> listEntityClazz = null;
                        Type fc = field.getGenericType();
                        if (fc == null) {
                            continue;
                        }
                        if (fc instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) fc;
                            listEntityClazz = (Class<?>) pt.getActualTypeArguments()[0];
                        }
                        if (listEntityClazz != null && TableHelper.checkTableExist(db, listEntityClazz)) {
                            String relationsTableName = listEntityClazz.getAnnotation(Table.class).name();
                            if (relationsTableName.equals("")) {
                                relationsTableName = field.getType().getSimpleName().toUpperCase();
                            }
                            String foreignKey;
                            Field relationsIdField = TableHelper.getIdColumnField(clazz);
                            if (oneToMany.joinColumn().equals("")) {
                                foreignKey = relationsIdField.getAnnotation(Column.class).name();
                                if (foreignKey.equals("")) {
                                    foreignKey = relationsIdField.getName();
                                }
                            } else {
                                foreignKey = oneToMany.joinColumn();
                            }
                            String foreignValue = String.valueOf(getCursorValue(cursor, relationsIdField));
                            StringBuilder sql = new StringBuilder();
                            sql.append("select * from ");
                            sql.append(relationsTableName);
                            sql.append(" where ");
                            sql.append(foreignKey);
                            sql.append(" = '" + foreignValue);
                            sql.append("'");
                            Cursor foreignCursor = db.rawQuery(sql.toString(), null);
                            List<T> list = new ArrayList<T>();
                            while (foreignCursor.moveToNext()) {
                                Object foreign = renderBean(listEntityClazz, foreignCursor);
                                list.add((T) foreign);
                            }
                            foreignCursor.close();
                            if (list.size() > 0) {
                                field.set(t, list);
                            }
                        }
                    }


//					String relationsTableName = field.getType().getAnnotation(Table.class).name();
//					if(relationsTableName.equals("")){
//						relationsTableName = field.getType().getSimpleName().toUpperCase();
//					}
//					Field relationsIdField = TableHelper.getIdColumnField(field.getType());
//					String foreignKey = relationsIdField.getAnnotation(Column.class).name();
//					if(foreignKey.equals("")){
//						foreignKey = relationsIdField.getName();
//					}
//					StringBuilder sql = new StringBuilder();
//					sql.append("select * from ");
//					sql.append(relationsTableName);
//					sql.append(" where ");
//					sql.append(foreignKey);
//					sql.append(" = '"+foreignValue);
//					sql.append("'");
//					Cursor foreignCursor = db.rawQuery(sql.toString(),null);
//					while (foreignCursor.moveToNext()) {
//						Object foreign = renderBean(field.getType(), foreignCursor);
//						field.set(t,foreign);
//					}
//					foreignCursor.close();

                }
            } else {
                field.set(t, getCursorValue(cursor, field));
            }
        }
        long e = System.currentTimeMillis();
//		KKLog.d("sql查询转换render bean耗时:  "+t.getClass() +" --- "+ (e-e1));
        return t;
    }

    private <K> K renderBean(Class<K> entity, Cursor cursor) throws InstantiationException, IllegalAccessException {
        K k = entity.newInstance();
        List<Field> fields = TableHelper.getColumnFields(entity);
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(k, getCursorValue(cursor, field));
        }
        return k;
    }

    private ContentValues setContentValues(T t) {
        try {
            ContentValues contentValues = new ContentValues();
            List<Field> fields = TableHelper.getColumnFields(clazz);
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    if (!field.isAnnotationPresent(Id.class)) {
                        field.setAccessible(true);
                        Object fieldValue = field.get(t);
                        if (fieldValue != null) {
                            Column column = field.getAnnotation(Column.class);
                            String columnName = column.name();
                            if (field.isAnnotationPresent(ManyToOne.class)) {
                                Field foreignField = TableHelper.getIdColumnField(fieldValue.getClass());
                                foreignField.setAccessible(true);
                                fieldValue = foreignField.get(fieldValue);
                                if (columnName.equals("")) {
                                    column = foreignField.getAnnotation(Column.class);
                                    columnName = column.name();
                                }
                                if (columnName.equals("")) {
                                    columnName = foreignField.getName();
                                }
                            } else if (columnName.equals("")) {
                                columnName = field.getName();
                            }
                            if (field.getType() == Date.class) {
                                String dateValue = dateFormat.format(fieldValue);
                                contentValues.put(columnName, dateValue);
                            } else if (byte[].class == field.getType()) {
                                byte[] byteData = (byte[]) fieldValue;
                                if (byteData.length <= 1024 * 1024 * 3) {
                                    contentValues.put(columnName, (byte[]) fieldValue);
                                } else {
                                    HTLog.d("二进制数据超过3M，无法存储");
                                }
                            } else if (column.type().toUpperCase().startsWith("DATE")) {
                                if (!TextUtils.isEmpty(fieldValue.toString())) {
                                    //TODO 没有做验证，考虑优化
                                    contentValues.put(columnName, fieldValue.toString());
                                }
                            } else {
                                contentValues.put(columnName, fieldValue.toString());
                            }
                        }
                    }
                }
            }
            return contentValues;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
