package com.example.downloaddemo.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.downloaddemo.enties.ThreadInfo;

/*****
 * classname:数据访问接口 description:数据访问操作 version 1.00 time:2015-8-27 18:24:01
 */
public class ThreadDAOImpl implements ThreadDAO {

	private DBHelper mhelper = null;

	public ThreadDAOImpl(Context context) {
		mhelper = new DBHelper(context);
	}

	@Override
	public void insertThread(ThreadInfo threadInfo) {

		SQLiteDatabase db = mhelper.getWritableDatabase();
		db.execSQL(
				"insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
				new Object[] { threadInfo.getId(), threadInfo.getUrl(),
						threadInfo.getStart(), threadInfo.getEnd(),
						threadInfo.getFinished() });
		db.close();
	}

	@Override
	public void deleteThread(String url, int thread_id) {
		SQLiteDatabase db = mhelper.getWritableDatabase();
		db.execSQL("delete from thread_info where url=? and thread_id=?",
				new Object[] { url, thread_id });
		db.close();
	}

	@Override
	public void updateThread(String url, int thread_id, int finished) {
		SQLiteDatabase db = mhelper.getWritableDatabase();
		db.execSQL(
				"update thread_info set finished=? where url=? and thread_id=?",
				new Object[] { finished, url, thread_id });
		db.close();
	}

	@Override
	public List<ThreadInfo> getThreads(String url) {

		SQLiteDatabase db = mhelper.getWritableDatabase();
		List<ThreadInfo> list = new ArrayList<ThreadInfo>();
		Cursor cursor = db.rawQuery("select * from thread_info where url=?",
				new String[] { url });

		while (cursor.moveToNext()) {
			ThreadInfo threadin = new ThreadInfo();
			threadin.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			threadin.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			threadin.setStart(cursor.getInt(cursor.getColumnIndex("start")));
			threadin.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
			threadin.setFinished(cursor.getInt(cursor
					.getColumnIndex("finished")));
			list.add(threadin);
		}
		cursor.close();
		db.close();
		return list;
	}

	@Override
	public boolean isExistsThread(String url, int thread_id) {

		SQLiteDatabase db = mhelper.getWritableDatabase();
		List<ThreadInfo> list = new ArrayList<ThreadInfo>();
		Cursor cursor = db.rawQuery(
				"select * from thread_info where url=? and thread_id=?",
				new String[] { url, thread_id + "" });
		boolean exists = cursor.moveToNext();
		cursor.close();
		db.close();
		return exists;
	}

}
