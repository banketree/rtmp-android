package com.example.testrtmp;

import com.example.core.CoreRtmpClient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FlvBaseAdapter extends BaseAdapter {
	private Context mContext;

	public FlvBaseAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		return CoreRtmpClient.getInstance().getAvailableFlvs() == null ? 0
				: CoreRtmpClient.getInstance().getAvailableFlvs().size();
	}

	@Override
	public Object getItem(int arg0) {
		return CoreRtmpClient.getInstance().getAvailableFlvs() == null ? null
				: CoreRtmpClient.getInstance().getAvailableFlvs().get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		FlvHold flvView = null;
		if (arg1 == null) {
			arg1 = LayoutInflater.from(mContext).inflate(R.layout.flvs_item,
					null);
			flvView = new FlvHold();
			flvView.mFlvName = (TextView) arg1.findViewById(R.id.ItemTitle);
			arg1.setTag(flvView);
		} else {
			flvView = (FlvHold) arg1.getTag();
		}

		flvView.mFlvName.setText(CoreRtmpClient.getInstance()
				.getAvailableFlvs().get(arg0));
		return arg1;
	}

	static class FlvHold {
		public TextView mFlvName;
	}
}
