package com.race604.picgallery.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.race604.picgallery.provider.ImageMeta;

public class CollectionDao extends BaseDao {

	public static final String TABLE_NAME = "collections";
	private static final String CREATE_TABLE = "create table " + TABLE_NAME
			+ " (_id  integer primary key autoincrement, "
			+ "url text, add_time long);";
	
	private static CollectionDao mInstance;
	
	private CollectionDao(){}
	
	public static CollectionDao getInstance() {
		if (mInstance == null) {
			mInstance = new CollectionDao();
		}
		return mInstance;
	}

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	public long addCollection(ImageMeta img) {
		ContentValues cv = new ContentValues();
		cv.put("url", img.url);
		cv.put("add_time", System.currentTimeMillis());
		return getDataBase().insert(TABLE_NAME, null, cv);
	}

	public long updateCollection(ImageMeta img) {
		ContentValues cv = new ContentValues();
		cv.put("url", img.url);
		cv.put("add_time", System.currentTimeMillis());
		return getDataBase().update(TABLE_NAME, cv, "url" + "=?",
				new String[] { img.url });
	}

	public void addOrUpdateCollection(ImageMeta img) {
		Cursor c = getDataBase().query(TABLE_NAME, null, "url" + "=?",
				new String[] { img.url }, null, null, null, null);
		if (c.moveToNext()) {
			updateCollection(img);
		} else {
			addCollection(img);
		}
		c.close();
	}

	public boolean addIfNotExist(ImageMeta img) {
		Cursor c = getDataBase().query(TABLE_NAME, null, "url" + "=?",
				new String[] { img.url }, null, null, null, null);
		if (!c.moveToNext()) {
			c.close();
			addCollection(img);
			return true;
		} else {
			c.close();
			return false;
		}
	}

}
