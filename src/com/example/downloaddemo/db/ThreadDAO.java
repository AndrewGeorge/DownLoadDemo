package com.example.downloaddemo.db;

import java.util.List;

import android.R.bool;

import com.example.downloaddemo.enties.ThreadInfo;

/**
 *���ݷ��ʽӿ� 
 */
public interface ThreadDAO {

	/**
	 *�����߳���Ϣ 
	 */
	public void insertThread(ThreadInfo threadInfo);
	/**
	 * ɾ���߳�
	 * @param url
	 * @param thread_id
	 */
	public void deleteThread(String url);
	/***
	 * �����߳����ؽ���
	 * @param url
	 * @param thread_id
	 * @param finished
	 */
	public void updateThread(String url,int thread_id,int finished);
	/**
	 * ��ѯ�ļ����߳�
	 * @param url
	 */
	public List<ThreadInfo> getThreads(String url);
	/**
	 * �ж��߳��Ƿ���ڣ����������
	 * @param url
	 * @param thread_id
	 * @return boolean
	 */
	public boolean isExistsThread(String url,int thread_id);
	
}
