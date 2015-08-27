package com.example.downloaddemo.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpStatus;

import com.example.downloaddemo.db.ThreadDAO;
import com.example.downloaddemo.db.ThreadDAOImpl;
import com.example.downloaddemo.enties.FileInfo;
import com.example.downloaddemo.enties.ThreadInfo;

import android.content.Context;
import android.content.Intent;

/**
 * 
 * ��������
 * 
 */
public class DownLoadTask {

	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mfinished = 0;
	public boolean isPause = false;

	public DownLoadTask(Context mContext, FileInfo mFileInfo) {
		super();
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		mDao = new ThreadDAOImpl(mContext);
	}

	public void downLoad() {
		// ��ȡ���ݿ���߳���Ϣ
		List<ThreadInfo> mThreadInfos = mDao.getThreads(mFileInfo.getUrl());
		ThreadInfo inf = null;
		if (mThreadInfos.size() == 0) {
			inf = new ThreadInfo(0, mFileInfo.getUrl(), 0,
					mFileInfo.getLength(), 0);
		} else {
			inf = mThreadInfos.get(0);
		}
		// �������߳�������
		new DownLoad(inf).start();
	}

	class DownLoad extends Thread {
		private ThreadInfo mThreadInfo = null;

		public DownLoad(ThreadInfo mThreadInfo) {
			super();
			this.mThreadInfo = mThreadInfo;
		}

		@Override
		public void run() {

			// �����ݿ��в����߳���Ϣ
			if (!mDao.isExistsThread(mThreadInfo.getUrl(), mThreadInfo.getId())) {
				mDao.insertThread(mThreadInfo);
			}

			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream ins = null;
			try {
				URL url = new URL(mThreadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				// ��������λ��
				conn.setRequestProperty("Range",
						"bytes=" + "-" + mThreadInfo.getEnd());
				// �����ļ�д��λ��
				File file = new File(DownLoadService.DOWN_PATH,
						mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);
				Intent intent = new Intent(DownLoadService.ACTION_UPDATE);
				mfinished += mThreadInfo.getFinished();
				// ��ʼ����
				if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {

					// ��ȡ����
					ins = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4];
					int length = -1;
					long time = System.currentTimeMillis();
					while ((length = ins.read(buffer)) != -1) {
						// д���ļ�
						raf.write(buffer, 0, length);
						// �����ؽ��ȷ��͹㲥����UI
						mfinished += length;
						if (System.currentTimeMillis() - time > 500) {
							time = System.currentTimeMillis();
							intent.putExtra("finished", mfinished * 100
									/ mFileInfo.getLength());
							mContext.sendBroadcast(intent);
						}
						// ������ͣ�������
						if (isPause) {
							mDao.updateThread(mThreadInfo.getUrl(),
									mThreadInfo.getId(), mfinished);
							return;
						}
					}
					// �������ɾ���߳���Ϣ
					mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					conn.disconnect();
					ins.close();
					raf.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			super.run();
		}

	}

}
