package com.vlccore.test;

import java.util.ArrayList;
import java.util.List;

import org.videolan.libvlc.VLCManager;

import com.treecore.utils.TActivityUtils;
import com.treecore.utils.TStringUtils;
import com.vlccore.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActvity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_actvity);

		findViewById(R.id.Button_video).setOnClickListener(this);
		findViewById(R.id.Button_video2).setOnClickListener(this);
		findViewById(R.id.Button_video3).setOnClickListener(this);
		findViewById(R.id.Button_video4).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onClick(View arg0) {

		if (arg0.getId() == R.id.Button_video) {
			VideoPlayerActivity
					.start(this,
							"file:///storage/sdcard0/com.example.testrtmp/cache/banketree1.mp4");
		} else if (arg0.getId() == R.id.Button_video2) {
			VideoPlay
					.start(this,
							"file:///storage/sdcard0/com.example.testrtmp/cache/banketree1.mp4");
		} else if (arg0.getId() == R.id.Button_video3) {
			// new Thread(new Runnable() {
			//
			// @Override
			// public void run() {
			// List<String> medias = new ArrayList<String>();
			// medias.add("rtmp://192.168.1.126/vod/xiaoting.flv");
			// // medias.add("rtmp://178.73.10.66:1935/live/mpegts.stream.flv");
			// // medias.add("rtsp://217.146.95.166:554/live/ch6bqvga.3gp");
			// // try {
			// // // String testUrl = VLCManager.getInstance().getLibVLC()
			// // // .getTestUrl("");
			// // // medias.add(testUrl);
			// // VLCManager.getInstance().load(medias, 0, false);
			// // } catch (Exception e) {
			// // e.printStackTrace();
			// // }
			// }
			// }) {
			//
			// }.start();
			copy("rtmp://192.168.1.126/vod/xiaoting.flv".toLowerCase());
		} else if (arg0.getId() == R.id.Button_video4) {
			Intent intent = new Intent(this, TestPlayerView.class);
			startActivity(intent);
		}
	}

	@SuppressWarnings("deprecation")
	public void copy(CharSequence content) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(content);
	}
}
