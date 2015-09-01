package com.example.downloaddemo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.downloaddemo.enties.FileInfo;
import com.example.downloaddemo.services.DownLoadService;

public class MainActivity extends Activity {

	private List<FileInfo> mFilList = null;
	private ListView mListView = null;
	private ListViewAdapter mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initData();
		initView();
	}

	// 初始化数据
	private void initData() {
		// 注册广播接收器
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(DownLoadService.ACTION_UPDATE);
		mFilter.addAction(DownLoadService.ACTION_FINISH);
		registerReceiver(mBroadcastReceiver, mFilter);
		// 创建文件信息对象
		mFilList = new ArrayList<FileInfo>();
	//http://www.imooc.com/mobile/mukewang.apk

			mFilList.add(new FileInfo(
					0,
					"http://dlsw.baidu.com/sw-search-sp/soft/3d/13172/LOLBox_V4.5.18_setup.1439349463.exe",
					"LOLBox_V4.5.18_setup.1439349463.exe", 0, 0));
			mFilList.add(new FileInfo(
					1,
					"http://www.imooc.com/mobile/mukewang.apk",
					"mukewang.apk", 0, 0));
			mFilList.add(new FileInfo(
					2,
					"http://www.imooc.com/mobile/mukewang.apk",
					"mukewang.apk", 0, 0));
		

	}

	// 初始化视图
	private void initView() {

		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new ListViewAdapter(MainActivity.this, mFilList);
		mListView.setAdapter(mAdapter);
	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {

			if (DownLoadService.ACTION_UPDATE.equals(intent.getAction())) {
				int finished = intent.getIntExtra("finished", 0);
				int id = intent.getIntExtra("id", 0);
				mAdapter.uptataProgress(id, finished);
			} else if (DownLoadService.ACTION_FINISH.equals(intent.getAction())) {
				FileInfo filInfo = (FileInfo) intent
						.getSerializableExtra("fileInfo");
				mAdapter.uptataProgress(filInfo.getId(), 0);
				Toast.makeText(MainActivity.this,
						mFilList.get(filInfo.getId()).getFileName() + "下载完毕",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	protected void onDestroy() {

		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}
}
