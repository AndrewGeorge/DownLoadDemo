package com.example.downloaddemo.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

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

	// ���ô洢·��
	public static final String DOWN_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/downloads";
	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_FINISH = "ACTION_FINISH";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final int MSG_INIT = 0;
	// private DownLoadTask mDownLoadTask=null;
	private Map<Integer, DownLoadTask> mTasks = new LinkedHashMap<Integer, DownLoadTask>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// ��ȡActivity����������
		if (ACTION_START.equals(intent.getAction())) {
			FileInfo fileinfo = (FileInfo) intent
					.getSerializableExtra("fileinfo");
			InitThread minitThread=new InitThread(fileinfo);
			DownLoadTask.sExecutorService.execute(minitThread);
			
			
			Log.i("test", "start:" + fileinfo.toString());
		} else if (ACTION_STOP.equals(intent.getAction())) {
			FileInfo fileinfo = (FileInfo) intent
					.getSerializableExtra("fileinfo");
			// �Ӽ����л�ȡ��������
			DownLoadTask task = mTasks.get(fileinfo.getId());
			if (task != null) {
				// ֹͣ��������
				task.isPause = true;
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
				Log.i("test", "init:" + info.toString());
				// ������������,Ĭ��Ϊ�����߳�����
				DownLoadTask task = new DownLoadTask(DownLoadService.this,
						info, 3);
				task.downLoad();
				// ������������ӵ�������
				mTasks.put(info.getId(), task);
				break;

			default:
				break;
			}
		}
	};

	/**
	 * 
	 * ��ʼ�����߳�
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
				// ���������ļ�
				URL url = new URL(mfileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				int length = -1;
				// ��ȡ�ļ�����
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
				// �ڱ����ļ������ó���
				File file = new File(dir, mfileInfo.getFileName());
				// �����������ܹ� ������λ��д��
				raf = new RandomAccessFile(file, "rwd");
				// ���ñ����ļ��ĳ���
				raf.setLength(length);
				mfileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT, mfileInfo).sendToTarget();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				try {
					// �ر����������������Ӳ���
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
