package com.example.testrtmp;

import com.eotu.core.CoreActivity;
import com.example.core.CoreRtmpClient;
import com.example.testrtmp.R;
import com.treecore.activity.TActivityHandler;
import com.treecore.utils.TIHandler;
import com.treecore.utils.TStringUtils;
import com.vlccore.PlayerPlugin;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

public class Player extends CoreActivity implements OnClickListener, TIHandler {
	private String mUrl = "";
	private PlayerPlugin mPlayerPlugin;
	private TActivityHandler mActivityHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.videoview);
		String name = getActivityParameter().get(0);

		if (TStringUtils.isEmpty(name)) {
			makeText("地址为空");
			finish();
			return;
		}

		mUrl = CoreRtmpClient.getInstance().getServerUrl() + "/" + name
				+ ".flv";

		try {
			mPlayerPlugin = (PlayerPlugin) findViewById(R.id.PlayerPlugin_player);
			mPlayerPlugin.init();
		} catch (Exception e) {
			makeText("播放器异常");
			finish();
			return;
		}

		findViewById(R.id.Button_playurl).setOnClickListener(this);
		findViewById(R.id.Button_stop).setOnClickListener(this);

		mActivityHandler = new TActivityHandler(this);
		mActivityHandler.setIHandler(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mActivityHandler.sendEmptyMessage(100);
	}

	@Override
	protected void onPause() {
		try {
			mPlayerPlugin.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		try {
			mPlayerPlugin.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
	}

	@Override
	public void handleMessage(Message msg) {
		try {
			mPlayerPlugin.playUrl(mUrl);
		} catch (Exception e) {
			makeText("播放器异常");
		}
	}
}