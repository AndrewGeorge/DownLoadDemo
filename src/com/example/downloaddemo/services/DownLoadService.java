package com.example.downloaddemo.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.example.downloaddemo.enties.FileInfo;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DownLoadService extends Service {

	// 设置存储路劲
	public static final String DOWN_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/downloads";
	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final int MSG_INIT = 0;
	private  DownLoadTask mDownLoadTask=null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 获取Activity传来的数据
		if (ACTION_START.equals(intent.getAction())) {
			FileInfo fileinfo = (FileInfo) intent
					.getSerializableExtra("fileinfo");
			new InitThread(fileinfo).start();
			Log.i("test", "start:" + fileinfo.toString());
		} else if (ACTION_STOP.equals(intent.getAction())) {
			FileInfo fileinfo = (FileInfo) intent
					.getSerializableExtra("fileinfo");
			if (mDownLoadTask!=null) {
				
				mDownLoadTask.isPause=true;
			}
			Log.i("test", "stop:" + fileinfo.toString());
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case MSG_INIT:
				FileInfo info = (FileInfo) msg.obj;
				Log.i("test", "init:"+info.toString());
				//开启下载任务
				mDownLoadTask=new DownLoadTask(DownLoadService.this, info);
				mDownLoadTask.downLoad();
				break;

			default:
				break;
			}
		}
	};

	/**
	 * 
	 * 初始化子线程
	 */
	class InitThread extends Thread {

		private FileInfo mfileInfo = null;

		public InitThread(FileInfo mfileInfo) {
			super();
			this.mfileInfo = mfileInfo;
		}

		@Override
		public void run() {

			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			try {
				// 连接网络文件
				URL url = new URL(mfileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				int length = -1;
				// 获取文件长度
				if (conn.getResponseCode() == HttpStatus.SC_OK) {
					length = conn.getContentLength();
				}
				if (length <= 0) {
					return;
				}
				File dir = new File(DOWN_PATH);
				if (!dir.exists()) {
					dir.mkdir();
				}
				// 在本地文件并设置长度
				File file = new File(dir, mfileInfo.getFileName());
				// 特殊的输出流能够 任任意位置写入
				raf = new RandomAccessFile(file, "rwd");
				// 设置本地文件的长度
				raf.setLength(length);
				mfileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT, mfileInfo).sendToTarget();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				try {
					// 关闭流操作和网络连接操作
					raf.close();
					conn.disconnect();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			super.run();
		}

	}

}
