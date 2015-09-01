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
 * 下载任务
 * 
 */
public class DownLoadTask {

	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mfinished = 0;
	public boolean isPause = false;
	private int threadCount = 1;// 线程数量
	private List<DownLoad> mThreadList = null;// 线程集合方便管理分段下载线程
	//使用带缓存的
	public static ExecutorService sExecutorService=Executors.newCachedThreadPool();

	public DownLoadTask(Context mContext, FileInfo mFileInfo, int threadCount) {
		super();
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		mDao = new ThreadDAOImpl(mContext);
	}

	public void downLoad() {
		// 读取数据库的线程信息
		List<ThreadInfo> mThreadInfos = mDao.getThreads(mFileInfo.getUrl());
		if (mThreadInfos.size() == 0) {
			// 获取每个线程下载的长度
			int length = mFileInfo.getLength() / threadCount;
			// 创建线程下载信息
			for (int i = 0; i < threadCount; i++) {
				ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), i
						* length, (i + 1) * length, 0);
				if (i == threadCount - 1) {
					threadInfo.setEnd(mFileInfo.getLength());
				}
				// 添加到线程信息集合中
				mThreadInfos.add(threadInfo);
				// 向数据库中插入线程信息
				mDao.insertThread(threadInfo);
			}
		}

		mThreadList = new ArrayList<DownLoadTask.DownLoad>();

		// 启动多个线程来下载
		for (ThreadInfo info : mThreadInfos) {
			DownLoad download = new DownLoad(info);
//			download.start();
			DownLoadTask.sExecutorService.execute(download);
			mThreadList.add(download);

		}
	}

	/**
	 * 判断下载线程是否都下载完毕
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
			// 下载完毕删除线程信息
			mDao.deleteThread(mFileInfo.getUrl());
			// 发送广播到Activity
			Intent intent = new Intent(DownLoadService.ACTION_FINISH);
			intent.putExtra("fileInfo", mFileInfo);
			mContext.sendBroadcast(intent);
		}

	}

	class DownLoad extends Thread {
		private ThreadInfo mThreadInfo = null;
		public boolean isfinished = false;// 表示线程是否下载完毕

		public DownLoad(ThreadInfo mThreadInfo) {
			super();
			this.mThreadInfo = mThreadInfo;
		}

		@Override
		public void run() {

			// 打开连接
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream ins = null;
			try {
				URL url = new URL(mThreadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				// 设置下载位置
				conn.setRequestProperty("Range",
						"bytes=" + "-" + mThreadInfo.getEnd());
				// 设置文件写入位置
				File file = new File(DownLoadService.DOWN_PATH,
						mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);
				Intent intent = new Intent(DownLoadService.ACTION_UPDATE);
				mfinished += mThreadInfo.getFinished();
				// 开始下载
				if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {

					// 读取数据
					ins = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4];
					int length = -1;
					long time = System.currentTimeMillis();
					while ((length = ins.read(buffer)) != -1) {
						// 写入文件
						raf.write(buffer, 0, length);
						// 把下载进度发送广播更新UI
						// 累加整文件完成进度
						mfinished += length;
						// 累加每个线程完成的进度
						mThreadInfo.setFinished(mThreadInfo.getFinished()
								+ length);
						if (System.currentTimeMillis() - time > 1000) {
							time = System.currentTimeMillis();
							intent.putExtra("finished", mfinished * 100
									/ mFileInfo.getLength());
							intent.putExtra("id", mFileInfo.getId());
							mContext.sendBroadcast(intent);
						}
						// 下载暂停保存进度
						if (isPause) {
							mDao.updateThread(mThreadInfo.getUrl(),
									mThreadInfo.getId(),
									mThreadInfo.getFinished());
							return;
						}
					}
					isfinished = true;

					// 检查下载任务是否完成
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
