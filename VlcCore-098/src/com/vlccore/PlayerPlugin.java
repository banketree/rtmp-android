package com.vlccore;

import java.util.Date;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.VLCManager;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.task.TITaskListener;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayerPlugin extends PlayerView implements TITaskListener {
	private static String TAG = PlayerPlugin.class.getSimpleName();

	private String mLastUrl = "";
	private TTask mReleaseTask;

	public PlayerPlugin(Context context) {
		super(context);
	}

	public PlayerPlugin(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onClick(View v) {
		if (VLCManager.getInstance().getLibVLC() == null)
			return;

		try {
			if (v == mPlayStopImageButton) {
				if (VLCManager.getInstance().getLibVLC().isPlaying()) {
					pause();
				} else {
					play();
				}
			} else if (v == mAudioImageButton) {// 声音

			} else if (v == mNavMenuImageButton) { // 亮度

			}
		} catch (Exception e) {
			if (e.getMessage() != null)
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
		}

		super.onClick(v);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_MOVE) {
			showOverlay();
		}

		return false;
	}

	public void init() throws Exception {
		VLCManager.getInstance().initConfig(mContext);

		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		getPlayerSurfaceView().getHolder().setFormat(PixelFormat.RGBX_8888);
		getPlayerSurfaceView().getHolder().addCallback(mSurfaceCallback);

		VLCManager.getInstance().getLibVLC()
				.eventVideoPlayerActivityCreated(true);

		EventHandler.getInstance().addHandler(mViewHandler);

		mPlayerSeekBar.setOnSeekBarChangeListener(mSeekListener);
	}

	public void release() throws Exception {// 释放资源
		// mReleaseTask = new TTask();
		// mReleaseTask.setIXTaskListener(this);
		// mReleaseTask.startTask(100);
		if (VLCManager.getInstance().getLibVLC() != null) {
			VLCManager.getInstance().getLibVLC()
					.eventVideoPlayerActivityCreated(false);
			VLCManager.getInstance().release();
		}

		EventHandler.getInstance().removeHandler(mViewHandler);
	}

	public void playUrl(String url) throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		startPlayLoading();
		mLastUrl = url;
		VLCManager.getInstance().getLibVLC().playMRL(mLastUrl);
	}

	public void play() throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");
		if (TStringUtils.isEmpty(mLastUrl))
			throw new Exception("播放地址为空");

		startPlayLoading();
		VLCManager.getInstance().getLibVLC().play();
	}

	public void pause() throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		VLCManager.getInstance().getLibVLC().pause();
		stopPlayLoading();
	}

	public void stop() throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		VLCManager.getInstance().getLibVLC().stop();
		stopPlayLoading();
	}

	public boolean takeSnapShot(String filePath, int width, int height)
			throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		return VLCManager.getInstance().getLibVLC()
				.takeSnapShot(filePath, width, height);
	}

	public boolean startVideoRecord(String filePath) throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		return VLCManager.getInstance().getLibVLC().videoRecordStart(filePath);
	}

	public boolean stopVideoRecord() throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		return VLCManager.getInstance().getLibVLC().videoRecordStop();
	}

	public boolean isVideoRecordable() throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		return VLCManager.getInstance().getLibVLC().videoIsRecordable();
	}

	public boolean isVideoRecording() throws Exception {
		if (VLCManager.getInstance().getLibVLC() == null)
			throw new Exception("播放器异常");

		return VLCManager.getInstance().getLibVLC().videoIsRecording();
	}

	private void updatePlayStatus(boolean show) {
		if (isLock()) {
			mPlayStopImageButton.setVisibility(INVISIBLE);
			return;
		}

		if (VLCManager.getInstance().getLibVLC() != null
				&& VLCManager.getInstance().getLibVLC().isPlaying()) {
			mPlayStopImageButton
					.setBackgroundResource(R.drawable.ic_pause_circle);
		} else {
			mPlayStopImageButton
					.setBackgroundResource(R.drawable.ic_play_circle);
			show = true;
		}

		// if (!show) {
		// mPlayStopImageButton.startAnimation(AnimationUtils.loadAnimation(
		// mContext, android.R.anim.fade_out));
		// }
		mPlayStopImageButton.setVisibility(show ? VISIBLE : INVISIBLE);
	}

	private int updatePlayerProgress() {
		if (VLCManager.getInstance().getLibVLC() == null)
			return 0;

		int time = (int) VLCManager.getInstance().getLibVLC().getTime();
		int length = (int) VLCManager.getInstance().getLibVLC().getLength();
		if (length == 0) {
			// Media media = MediaDatabase.getInstance().getMedia(mLocation);
			// if (media != null)
			// length = (int) media.getLength();
		}

		mPlayerSeekBar.setMax(length);
		mPlayerSeekBar.setProgress(time);
		mSystimeTextView.setText(DateFormat.getTimeFormat(mContext).format(
				new Date(System.currentTimeMillis())));
		if (time >= 0)
			mTimeTextView.setText(TStringUtils.millisToString(time));
		if (length >= 0)
			mLengthTextView.setText(length > 0 ? "- "
					+ TStringUtils.millisToString(length - time) : TStringUtils
					.millisToString(length));

		return time;
	}

	@Override
	protected void showOverlay(int timeout) {
		if (VLCManager.getInstance().getLibVLC() == null)
			return;

		super.showOverlay(timeout);
		updatePlayStatus(true);
	}

	@Override
	protected void hideOverlay(boolean fromUser) {
		super.hideOverlay(fromUser);
		updatePlayStatus(false);
	}

	private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			showOverlay(OVERLAY_INFINITE);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			showOverlay();
			// hideInfo();
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser && VLCManager.getInstance().getLibVLC() != null) {// &&mCanSeek
				VLCManager.getInstance().getLibVLC().setTime(progress);
				updatePlayerProgress();
				mTimeTextView.setText(TStringUtils.millisToString(progress));
				// showInfo(TStringUtils.millisToString(progress));
			}
		}
	};

	private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (format == PixelFormat.RGBX_8888) {
				Log.d(TAG, "Pixel format is RGBX_8888");
			} else if (format == PixelFormat.RGB_565) {
				Log.d(TAG, "Pixel format is RGB_565");
			} else if (format == ImageFormat.YV12) {
				Log.d(TAG, "Pixel format is YV12");
			} else {
				Log.d(TAG, "Pixel format is other/unknown");
			}

			if (VLCManager.getInstance().getLibVLC() != null) {
				VLCManager.getInstance().getLibVLC()
						.attachSurface(holder.getSurface(), PlayerPlugin.this);
				updatePlayStatus(false);
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (VLCManager.getInstance().getLibVLC() != null)
				VLCManager.getInstance().getLibVLC().detachSurface();
		}
	};

	@Override
	protected void handleViewMessage(Message msg) {
		super.handleViewMessage(msg);

		if (msg.what == SHOW_PROGRESS) { // 进度栏
			int pos = updatePlayerProgress();

			if (mPlayerSeekBar.isShown()) {
				msg = mViewHandler.obtainMessage(SHOW_PROGRESS);
				mViewHandler.sendMessageDelayed(msg, 1000 - (pos % 1000));
			}
			return;
		}

		if (!msg.getData().containsKey("event"))
			return;
		switch (msg.getData().getInt("event")) {
		case EventHandler.MediaParsedChanged:
			Log.i(TAG, "MediaParsedChanged");
			break;
		case EventHandler.MediaPlayerPlaying:
			mBShow = true;
			Log.i(TAG, "MediaPlayerPlaying");
			// service.changeAudioFocus(true);
			// if (!service.mWakeLock.isHeld())
			// service.mWakeLock.acquire();
			mTitleTextView.setText(getTitle());
			stopPlayLoading();
			showOverlay();
			break;
		case EventHandler.MediaPlayerPaused:
			Log.i(TAG, "MediaPlayerPaused");
			mBShow = true;
			// if (service.mWakeLock.isHeld())
			// service.mWakeLock.release();
			updatePlayStatus(true);
			break;
		case EventHandler.MediaPlayerStopped:
			Log.i(TAG, "MediaPlayerStopped");
			mLastUrl = "";
			mBShow = false;
			// if (service.mWakeLock.isHeld())
			// service.mWakeLock.release();
			updatePlayStatus(true);
			break;
		case EventHandler.MediaPlayerEndReached:
			Log.i(TAG, "MediaPlayerEndReached");
			// if (service.mWakeLock.isHeld())
			// service.mWakeLock.release();
			break;
		case EventHandler.MediaPlayerVout:
			Log.i(TAG, "MediaPlayerVout");
			if (msg.getData().getInt("data") > 0) {
			}
			break;
		case EventHandler.MediaPlayerPositionChanged:
			Log.i(TAG, "MediaPlayerPositionChanged");
			mBShow = true;
			updatePlayerProgress();
			break;
		case EventHandler.MediaPlayerEncounteredError:
			Log.i(TAG, "MediaPlayerEncounteredError");
			mBShow = false;
			mLastUrl = "";
			stopPlayLoading();
			updatePlayStatus(true);
			Toast.makeText(mContext, "播放出错", Toast.LENGTH_SHORT).show();
			break;
		default:
			Log.e(TAG, "Event not handled");
			break;
		}
	}

	private String getTitle() {
		if (TStringUtils.isEmpty(mLastUrl))
			return "";
		return mLastUrl.substring(mLastUrl.lastIndexOf("/") + 1,
				mLastUrl.length());
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		if (mReleaseTask != null && mReleaseTask.equalTask(task)) {
			if (event == TaskEvent.Work) {
				// if (VLCManager.getInstance().getLibVLC() != null)
				// VLCManager.getInstance().release();
			}
		}
	}
}
