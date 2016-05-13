package com.vlccore.test;

import java.io.File;

import com.treecore.filepath.TFilePathManager;
import com.vlccore.PlayerPlugin;
import com.vlccore.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TestPlayerView extends Activity implements OnClickListener {
	private static String TAG = TestPlayerView.class.getSimpleName();
	private PlayerPlugin mPlayerPlugin;
	// private PlayerPlugin2 mPlayerPlugin2;
	private int mScreenOrientation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? R.layout.player_actvity
				: R.layout.player_actvity2);
		mPlayerPlugin = (PlayerPlugin) findViewById(R.id.PlayerPlugin_player);
		// mPlayerPlugin2 = (PlayerPlugin2)
		// findViewById(R.id.PlayerPlugin_player2);
		findViewById(R.id.Button_stop).setOnClickListener(this);
		findViewById(R.id.Button_screen).setOnClickListener(this);
		findViewById(R.id.Button_playurl).setOnClickListener(this);
		findViewById(R.id.Button_jietu).setOnClickListener(this);
		findViewById(R.id.Button_luxiang).setOnClickListener(this);
		try {
			mPlayerPlugin.init();
			// mPlayerPlugin2.init();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onClick(View arg0) {
		try {
			if (arg0.getId() == R.id.Button_playurl) {
				String filePath = "file:///storage/sdcard0/123"
						+ "/banketree2.mkv";
				mPlayerPlugin.playUrl(filePath);
				// mPlayerPlugin
				// .playUrl("rtmp://178.73.10.66:1935/live/mpegts.stream.flv");
				// mPlayerPlugin.playUrl("rtmp://192.168.1.126/vod/xiaoting.flv");
				// String test = TFilePathManager.getInstance().getAudioPath();
				// File file = new File("/storage/sdcard0/123/EduMain.swf");
				// if (file.exists()) {
				// mPlayerPlugin
				// .playUrl("/storage/sdcard0/123/EduMain.swf");
				// }
				// mPlayerPlugin2.playUrl(filePath);
			} else if (arg0.getId() == R.id.Button_screen) {
				mScreenOrientation = this.getResources().getConfiguration().orientation;
				if (mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				} else {// 设置为置屏幕为竖屏
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			} else if (arg0.getId() == R.id.Button_stop) {
				mPlayerPlugin.stop();
			} else if (arg0.getId() == R.id.Button_jietu) {
				File file = new File(TFilePathManager.getInstance()
						.getImagePath()
						+ File.separator
						+ System.currentTimeMillis());
				boolean result = mPlayerPlugin.takeSnapShot(file.getPath(),
						280, 280);

				Toast.makeText(
						this,
						(result ? "截图成功：" : "截图失败：")
								+ TFilePathManager.getInstance().getImagePath(),
						Toast.LENGTH_SHORT).show();
			} else if (arg0.getId() == R.id.Button_luxiang) {
				try {
					// if (!mPlayerPlugin.isVideoRecordable()) {
					// Toast.makeText(this, "不支持录像", Toast.LENGTH_SHORT)
					// .show();
					// return;
					// }

					if (mPlayerPlugin.isVideoRecording()) {
						mPlayerPlugin.stopVideoRecord();
					} else {
						mPlayerPlugin.startVideoRecord(TFilePathManager
								.getInstance().getVideoPath());
					}

					Toast.makeText(
							this,
							"录像地址："
									+ TFilePathManager.getInstance()
											.getVideoPath(), Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					e.printStackTrace();
				}

				((Button) findViewById(R.id.Button_luxiang))
						.setText(mPlayerPlugin.isVideoRecording() ? "停止录像"
								: "开始录像");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		// loadMedia();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {
			// mPlayerPlugin.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	// 监听系统设置的更改
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		String message = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "屏幕设置为：横屏"
				: "屏幕设置为：竖屏";
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
