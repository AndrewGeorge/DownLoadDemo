package com.example.downloaddemo;

import java.util.List;

import com.example.downloaddemo.enties.FileInfo;
import com.example.downloaddemo.services.DownLoadService;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {

	private List<FileInfo> mList = null;
	private Context mContext = null;

	public ListViewAdapter(Context mContext, List<FileInfo> mList) {
		this.mContext = mContext;
		this.mList = mList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	/**
	 * ¸úÐÂprogress
	 */
	public void uptataProgress(int id, int progress) {

		FileInfo fileInfo = mList.get(id);
		fileInfo.setFinished(progress);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int arg0, View view, ViewGroup arg2) {
		final FileInfo fileInfo = mList.get(arg0);
		ViewHolder viewHolder = null;
		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.item_list,
					null);
			viewHolder = new ViewHolder();
			viewHolder.title_tv = (TextView) view
					.findViewById(R.id.id_filename);
			viewHolder.start_btn = (Button) view.findViewById(R.id.id_btnStart);
			viewHolder.stop_btn = (Button) view.findViewById(R.id.id_btnStop);
			viewHolder.progress = (ProgressBar) view
					.findViewById(R.id.progressBar1);

			viewHolder.title_tv.setText(fileInfo.getFileName());
			viewHolder.progress.setMax(100);

			viewHolder.start_btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(mContext, DownLoadService.class);
					intent.setAction(DownLoadService.ACTION_START);
					intent.putExtra("fileinfo", fileInfo);
					mContext.startService(intent);
				}
			});
			viewHolder.stop_btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent1 = new Intent(mContext, DownLoadService.class);
					intent1.setAction(DownLoadService.ACTION_STOP);
					intent1.putExtra("fileinfo", fileInfo);
					mContext.startService(intent1);
				}
			});

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		viewHolder.progress.setProgress(fileInfo.getFinished());

		return view;
	}

	static class ViewHolder {
		TextView title_tv;
		ProgressBar progress;
		Button start_btn;
		Button stop_btn;
	}

}
