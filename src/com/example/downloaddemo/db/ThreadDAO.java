package com.example.downloaddemo.db;

import java.util.List;

import android.R.bool;

import com.example.downloaddemo.enties.ThreadInfo;

/**
 *数据访问接口 
 */
public interface ThreadDAO {

	/**
	 *插入线程信息 
	 */
	public void insertThread(ThreadInfo threadInfo);
	/**
	 * 删除线程
	 * @param url
	 * @param thread_id
	 */
	public void deleteThread(String url);
	/***
	 * 更新线程下载进度
	 * @param url
	 * @param thread_id
	 * @param finished
	 */
	public void updateThread(String url,int thread_id,int finished);
	/**
	 * 查询文件的线程
	 * @param url
	 */
	public List<ThreadInfo> getThreads(String url);
	/**
	 * 判断线程是否存在，存在则更新
	 * @param url
	 * @param thread_id
	 * @return boolean
	 */
	public boolean isExistsThread(String url,int thread_id);
	
}
