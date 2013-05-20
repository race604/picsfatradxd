
package com.race604.picgallery.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PicDatabase extends SQLiteOpenHelper {
	
	private static final String TAG = "PicDatabase";

    private static final int DB_VERSION = 2;

    private final static String DB_NAME = "pictures.db";

    private SQLiteDatabase mSqldb = null;

    private static PicDatabase instance = null;

    static PicDatabase getInstance() {
        return instance;
    }

    private Context mContext;

    private PicDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context.getApplicationContext();
        mSqldb = this.getWritableDatabase();
    }

    public static void createInstance(Context context) {
        if (instance == null) {
            instance = new PicDatabase(context);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    	CollectionDao.onCreate(db);
        
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	CollectionDao.onUpgrade(db, oldVersion, newVersion);
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static boolean clearCache() {
        return false;
    }

    /**
     * this method won't close db
     * 
     * @param db
     * @param engine
     * @return
     */

    void runInTrans(Runnable r) {
        try {
            mSqldb.beginTransaction();
            r.run();
            mSqldb.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                mSqldb.endTransaction();
            } catch (Exception e) {
            }
        }
    }
}
