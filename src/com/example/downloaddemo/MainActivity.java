package com.example.downloaddemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.downloaddemo.enties.FileInfo;
import com.example.downloaddemo.services.DownLoadService;

public class MainActivity extends Activity implements OnClickListener {

	private Button btnStop=null;
	private Button btnStart=null;
	private TextView fileNmae=null;
	private ProgressBar prograss=null;
	FileInfo fileinfo=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initData();
		initView();
	}

	// 初始化数据
	private void initData() {
	//注册广播接收器
		IntentFilter mFilter=new IntentFilter();
		mFilter.addAction(DownLoadService.ACTION_UPDATE);
		registerReceiver(mBroadcastReceiver, mFilter);
		
	//创建文件信息对象
	fileinfo=new FileInfo(0, "http://dlsw.baidu.com/sw-search-sp/soft/3d/13172/LOLBox_V4.5.18_setup.1439349463.exe", "LOLBox_V4.5.18_setup.1439349463.exe", 0, 0);
	}

	// 初始化视图
	private void initView() {
		btnStart = (Button) findViewById(R.id.id_btnStart);
		btnStart.setOnClickListener(this);
		btnStop = (Button) findViewById(R.id.id_btnStop);
		btnStop.setOnClickListener(this);
		fileNmae = (TextView) findViewById(R.id.id_filename);
		prograss = (ProgressBar) findViewById(R.id.progressBar1);
		prograss.setMax(100);
	}

	
	BroadcastReceiver mBroadcastReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent intent) {
			
			if (DownLoadService.ACTION_UPDATE.equals(intent.getAction())) {
				int finished=intent.getIntExtra("finished", 0);
				prograss.setProgress(finished);
			}
		}
	};
	
	
	
	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.id_btnStart:
			//通过Intent给Service传递参数
			Intent intent=new Intent(MainActivity.this,DownLoadService.class);
			intent.setAction(DownLoadService.ACTION_START);
			intent.putExtra("fileinfo", fileinfo);
			startService(intent);
			break;
		case R.id.id_btnStop:
			Intent intent1=new Intent(MainActivity.this,DownLoadService.class);
			intent1.setAction(DownLoadService.ACTION_STOP);
			intent1.putExtra("fileinfo", fileinfo);
			startService(intent1);
			break;

		default:
			break;
		}

	}

	@Override
	protected void onDestroy() {
		
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}
}
