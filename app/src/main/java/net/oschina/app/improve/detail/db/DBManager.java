package net.oschina.app.improve.detail.db;

import android.content.Context;
import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 数据库帮助类
 * Created by haibin on 2017/5/24.
 */
@SuppressWarnings("all")
public final class DBManager {
    private static DBManager mManager;
    private DBHelper mHelper;
    private String sql;

    public static DBManager from(Context context) {
        if (mManager == null) {
            mManager = new DBManager();
            mManager.mHelper = new DBHelper(context);
        }
        return mManager;
    }

    public void create(Class<?> cls) {
        mManager.mHelper.create(cls);
    }

    public boolean alter(String tableName, String columnName, String type) {
        return mManager.mHelper.alter(tableName, columnName, type);
    }

    public boolean alter(Class<?> cls) {
        return mManager.mHelper.alter(cls);
    }

    /**
     * 选择表结构
     *
     * @param object object Annotation with table
     * @return DBManager
     */
    public DBHelper select(Object object) {
        Class<?> cls = object.getClass();
        Annotation[] annotations = cls.getDeclaredAnnotations();
        if (annotations == null || annotations.length == 0)
            return null;
        return mManager.mHelper;
    }


    public DBManager where(String where) {
        mHelper.where(where);
        return mManager;
    }

    public DBManager where(String where, String...args) {
        mHelper.where(where, args);
        return mManager;
    }

    public boolean update(Object object) {
        return mManager.mHelper.update(object);
    }

    public boolean update(String table, String column, Object object) {
        return mManager.mHelper.update(table, column, object);
    }

    public DBManager insert(Object object) {
        try {
            Class<?> cls = object.getClass();
            Annotation[] annotations = cls.getDeclaredAnnotations();
            if (annotations == null || annotations.length == 0) {
                throw new IllegalStateException("you must user Table annotation for bean");
            }
            String tableName = null;
            for (Annotation annotation : annotations) {
                if (annotation instanceof Table)
                    tableName = ((Table) annotation).tableName();
            }
            if (TextUtils.isEmpty(tableName)) {
                throw new IllegalStateException("you must user Table annotation for bean");
            }
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mManager;
    }
}
