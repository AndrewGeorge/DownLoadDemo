package com.example.downloaddemo.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	private int threadCount = 1;// �߳�����
	private List<DownLoad> mThreadList = null;// �̼߳��Ϸ������ֶ������߳�
	//ʹ�ô������
	public static ExecutorService sExecutorService=Executors.newCachedThreadPool();

	public DownLoadTask(Context mContext, FileInfo mFileInfo, int threadCount) {
		super();
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		mDao = new ThreadDAOImpl(mContext);
	}

	public void downLoad() {
		// ��ȡ���ݿ���߳���Ϣ
		List<ThreadInfo> mThreadInfos = mDao.getThreads(mFileInfo.getUrl());
		if (mThreadInfos.size() == 0) {
			// ��ȡÿ���߳����صĳ���
			int length = mFileInfo.getLength() / threadCount;
			// �����߳�������Ϣ
			for (int i = 0; i < threadCount; i++) {
				ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), i
						* length, (i + 1) * length, 0);
				if (i == threadCount - 1) {
					threadInfo.setEnd(mFileInfo.getLength());
				}
				// ��ӵ��߳���Ϣ������
				mThreadInfos.add(threadInfo);
				// �����ݿ��в����߳���Ϣ
				mDao.insertThread(threadInfo);
			}
		}

		mThreadList = new ArrayList<DownLoadTask.DownLoad>();

		// ��������߳�������
		for (ThreadInfo info : mThreadInfos) {
			DownLoad download = new DownLoad(info);
//			download.start();
			DownLoadTask.sExecutorService.execute(download);
			mThreadList.add(download);

		}
	}

	/**
	 * �ж������߳��Ƿ��������
	 */
	private synchronized void checkAllThreadsFinished() {
		boolean allFinished = true;
		for (DownLoad download : mThreadList) {
			if (!download.isfinished) {
				allFinished = false;
				break;
			}
		}

		if (allFinished) {
			// �������ɾ���߳���Ϣ
			mDao.deleteThread(mFileInfo.getUrl());
			// ���͹㲥��Activity
			Intent intent = new Intent(DownLoadService.ACTION_FINISH);
			intent.putExtra("fileInfo", mFileInfo);
			mContext.sendBroadcast(intent);
		}

	}

	class DownLoad extends Thread {
		private ThreadInfo mThreadInfo = null;
		public boolean isfinished = false;// ��ʾ�߳��Ƿ��������

		public DownLoad(ThreadInfo mThreadInfo) {
			super();
			this.mThreadInfo = mThreadInfo;
		}

		@Override
		public void run() {

			// ������
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
						// �ۼ����ļ���ɽ���
						mfinished += length;
						// �ۼ�ÿ���߳���ɵĽ���
						mThreadInfo.setFinished(mThreadInfo.getFinished()
								+ length);
						if (System.currentTimeMillis() - time > 1000) {
							time = System.currentTimeMillis();
							intent.putExtra("finished", mfinished * 100
									/ mFileInfo.getLength());
							intent.putExtra("id", mFileInfo.getId());
							mContext.sendBroadcast(intent);
						}
						// ������ͣ�������
						if (isPause) {
							mDao.updateThread(mThreadInfo.getUrl(),
									mThreadInfo.getId(),
									mThreadInfo.getFinished());
							return;
						}
					}
					isfinished = true;

					// ������������Ƿ����
					checkAllThreadsFinished();
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
