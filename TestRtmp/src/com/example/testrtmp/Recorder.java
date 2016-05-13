/*
 * Copyright (C) 2012 YIXIA.COM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.testrtmp;

import com.eotu.core.CoreActivity;
import com.example.core.CoreRtmpClient;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.task.TITaskListener;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

public class Recorder extends CoreActivity {
	// private VideoView mVideoView;
	private String mPath = "";
	private TTask mVideoTask;
	private boolean isPlayer = false;
	private long mPos = 0;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		try {
			// if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
			// return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		mPath = getActivityParameter().get(0);
		if (TStringUtils.isEmpty(mPath)) {
			makeText("路径为空");
			finish();
			return;
		}
		setContentView(R.layout.videoview);
		// mVideoView = (VideoView) findViewById(R.id.surface_view);
		// mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
		// mVideoView.setMediaController(new MediaController(this));
		// mVideoView.setVideoPath(mPath);

		mVideoTask = new TTask();
		mVideoTask.setIXTaskListener(this);
		mVideoTask.startTask("");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mVideoTask.stopTask();
		mVideoTask = null;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
//		if (mVideoView != null)
//			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);

		if (mVideoTask != null && mVideoTask.equalTask(task)) {
			if (event == TaskEvent.Work) {
				do {
					if (task.isCancel())
						return;

					// try {
					// Thread.sleep(1000);
					// } catch (Exception e) {
					// // TODO: handle exception
					// }
					//
					// long pos = mVideoView.getCurrentPosition();
					// long totalLen = mVideoView.getDuration();
					// int buffer = mVideoView.getBufferPercentage();
					//
					// if (buffer > 0) {
					// if (!mVideoView.isPlaying())
					// mVideoView.start();
					// } else {
					// if (mVideoView.isPlaying())
					// mVideoView.pause();
					// }

				} while (true);
			}
		}
	}
}
