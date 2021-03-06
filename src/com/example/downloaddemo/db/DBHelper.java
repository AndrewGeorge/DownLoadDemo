package com.example.downloaddemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "download.db";
	private static final int VERSION = 1;
	private static final String SQL_CRATE = "create table thread_info(_id integer primary key autoincrement,"
			+ "thread_id integer,url text,start integer,end integer,finished integer)";
	private static final String SQL_DROP = "drop table exists thread_info";
	private static DBHelper mHelper = null;

	private DBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	/**
	 * 获取去对象
	 * 单利懒汉模式线程不安全
	 */
	public  static synchronized  DBHelper getInstence(Context context) {
		if (mHelper == null) {
			mHelper = new DBHelper(context);
		}
		return mHelper;

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CRATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {

		db.execSQL(SQL_DROP);
		db.execSQL(SQL_CRATE);
	}

}
