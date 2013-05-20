
package com.race604.picgallery.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDao {
    protected static final String FIELD_ID = "_id";

    private SQLiteOpenHelper mSqlHelper = PicDatabase.getInstance();

    protected SQLiteDatabase getDataBase() {
        return mSqlHelper.getWritableDatabase();
    }

    protected SQLiteDatabase getReadOnlyDataBase() {
        return mSqlHelper.getReadableDatabase();
    }
}
