package com.vlccore;

import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.VLCManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.treecore.utils.THandler;
import com.vlccore.R;

public class PlayerView extends LinearLayout implements OnClickListener,
		IVideoPlayer {
	public static String TAG = PlayerView.class.getSimpleName();

	public static final int OVERLAY_TIMEOUT = 4000;
	public static final int OVERLAY_INFINITE = 3600000;
	public static final int FADE_OUT = -1;
	public static final int SHOW_PROGRESS = -2;
	public static final int SURFACE_SIZE = -3;
	public static final int FADE_OUT_INFO = 4;

	public static final int SURFACE_BEST_FIT = 0;
	public static final int SURFACE_FIT_HORIZONTAL = 1;
	public static final int SURFACE_FIT_VERTICAL = 2;
	public static final int SURFACE_FILL = 3;
	public static final int SURFACE_16_9 = 4;
	public static final int SURFACE_4_3 = 5;
	public static final int SURFACE_ORIGINAL = 6;
	protected int mCurrentSize = SURFACE_BEST_FIT;

	protected Context mContext;
	protected FrameLayout mPlayerFrameLayout;
	protected SurfaceView mPlayerSurfaceView, mSubtitleSurfaceView;
	protected ImageButton mPlayStopImageButton, mSubtitleImageButton,
			mAudioImageButton, mNavMenuImageButton, mSizeImageButton,
			mLockImageButton, mBackWardImageButton, mForwardImageButton,
			mAdvFunction;
	protected ImageView mLoadingImageView;
	protected TextView mLoadingTextView, mInfoTextView, mTitleTextView,
			mBatteryTextView, mSystimeTextView, mTimeTextView, mLengthTextView;
	protected LinearLayout mOptionLayout;
	protected RelativeLayout mProgressLayout, mHeaderLayout;
	protected SeekBar mPlayerSeekBar;

	protected ViewHandle mViewHandler;

	// size of the video 视频大小
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;

	private boolean mBLock = false;
	protected boolean mBShow = false;

	public PlayerView(Context context) {
		super(context);
		mContext = context;
		onInit();
	}

	public PlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		onInit();
	}

	private void onInit() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.playerview,
				null);
		addView(view, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		mPlayerFrameLayout = (FrameLayout) findViewById(R.id.FrameLayout_player);

		mPlayerSurfaceView = (SurfaceView) findViewById(R.id.SurfaceView_player);
		mSubtitleSurfaceView = (SurfaceView) findViewById(R.id.SurfaceView_subtitles);

		mPlayStopImageButton = (ImageButton) findViewById(R.id.ImageButton_play_stop);
		mSubtitleImageButton = (ImageButton) findViewById(R.id.ImageButton_player_subtitle);
		mAudioImageButton = (ImageButton) findViewById(R.id.ImageButton_player_audio);
		mNavMenuImageButton = (ImageButton) findViewById(R.id.ImageButton_player_navmenu);
		mSizeImageButton = (ImageButton) findViewById(R.id.ImageButton_player_size);
		mLockImageButton = (ImageButton) findViewById(R.id.ImageButton_player_lock);
		mBackWardImageButton = (ImageButton) findViewById(R.id.ImageButton_player_backward);
		mForwardImageButton = (ImageButton) findViewById(R.id.ImageButton_player_forward);
		mAdvFunction = (ImageButton) findViewById(R.id.ImageButton_player_adv_function);

		mLoadingImageView = (ImageView) findViewById(R.id.ImageView_loading);

		mLoadingTextView = (TextView) findViewById(R.id.TextView_loading_text);
		mInfoTextView = (TextView) findViewById(R.id.TextView_player_info);
		mTitleTextView = (TextView) findViewById(R.id.TextView_player_title);
		mBatteryTextView = (TextView) findViewById(R.id.TextView_player_battery);
		mSystimeTextView = (TextView) findViewById(R.id.TextView_player_systime);
		mTimeTextView = (TextView) findViewById(R.id.TextView_player_time);
		mLengthTextView = (TextView) findViewById(R.id.TextView_player_length);

		mHeaderLayout = (RelativeLayout) findViewById(R.id.RelativeLayout_player_header);
		mOptionLayout = (LinearLayout) findViewById(R.id.LinearLayout_player_option);
		mProgressLayout = (RelativeLayout) findViewById(R.id.RelativeLayout_player_progress);
		mPlayerSeekBar = (SeekBar) findViewById(R.id.SeekBar_player);

		mPlayStopImageButton.setOnClickListener(this);
		mAudioImageButton.setOnClickListener(this);
		mNavMenuImageButton.setOnClickListener(this);
		mSizeImageButton.setOnClickListener(this);
		mLockImageButton.setOnClickListener(this);

		mViewHandler = new ViewHandle(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mSizeImageButton) { // 视频适配比率
			if (mCurrentSize < SURFACE_ORIGINAL) {
				mCurrentSize++;
			} else {
				mCurrentSize = 0;
			}

			changeSurfaceSize();
			switch (mCurrentSize) {
			case SURFACE_BEST_FIT:
				showInfo(R.string.surface_best_fit, 1000);
				break;
			case SURFACE_FIT_HORIZONTAL:
				showInfo(R.string.surface_fit_horizontal, 1000);
				break;
			case SURFACE_FIT_VERTICAL:
				showInfo(R.string.surface_fit_vertical, 1000);
				break;
			case SURFACE_FILL:
				showInfo(R.string.surface_fill, 1000);
				break;
			case SURFACE_16_9:
				showInfo("16:9", 1000);
				break;
			case SURFACE_4_3:
				showInfo("4:3", 1000);
				break;
			case SURFACE_ORIGINAL:
				showInfo(R.string.surface_original, 1000);
				break;
			}

			showOverlay();
		} else if (v == mLockImageButton) {
			mBLock = !mBLock;

			if (mBLock) {
				lockScreen();
			} else {
				unlockScreen();
			}
		}
	}

	@Override
	public void setSurfaceSize(int width, int height, int visible_width,
			int visible_height, int sar_num, int sar_den) {
		if (width * height == 0)
			return;

		// store video size
		mVideoHeight = height;
		mVideoWidth = width;
		mVideoVisibleHeight = visible_height;
		mVideoVisibleWidth = visible_width;
		mSarNum = sar_num;
		mSarDen = sar_den;

		mViewHandler.sendEmptyMessage(SURFACE_SIZE);
	}

	public boolean isLock() {
		return mBLock;
	}

	public FrameLayout getPlayerFrameLayout() {
		return mPlayerFrameLayout;
	}

	public SurfaceView getPlayerSurfaceView() {
		return mPlayerSurfaceView;
	}

	public SurfaceView getSubtitleSurfaceView() {
		return mSubtitleSurfaceView;
	}

	protected void startPlayLoading() {
		AnimationSet anim = new AnimationSet(true);
		RotateAnimation rotate = new RotateAnimation(0f, 360f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate.setDuration(800);
		rotate.setInterpolator(new DecelerateInterpolator());
		rotate.setRepeatCount(RotateAnimation.INFINITE);
		anim.addAnimation(rotate);

		mLoadingImageView.setVisibility(View.VISIBLE);
		mLoadingImageView.startAnimation(anim);
		mLoadingTextView.setVisibility(View.VISIBLE);

		mPlayStopImageButton.setVisibility(View.INVISIBLE);
	}

	protected void stopPlayLoading() {
		mLoadingImageView.setVisibility(View.INVISIBLE);
		mLoadingImageView.clearAnimation();
		mLoadingTextView.setVisibility(View.INVISIBLE);
	}

	protected void handleViewMessage(Message msg) {
		if (msg.what == SURFACE_SIZE) {
			changeSurfaceSize();
		} else if (msg.what == FADE_OUT) { // 消失
			hideOverlay(false);
		} else if (msg.what == FADE_OUT_INFO) {// 信息消失
			fadeOutInfo();
		}
	}

	protected void lockScreen() {
		showInfo(R.string.locked, 1000);

		mLockImageButton.setBackgroundResource(R.drawable.ic_locked);
		mPlayerSeekBar.setEnabled(false);
		mPlayStopImageButton.setVisibility(INVISIBLE);
		mAudioImageButton.setVisibility(INVISIBLE);
		mNavMenuImageButton.setVisibility(INVISIBLE);
		mSizeImageButton.setVisibility(INVISIBLE);

		hideOverlay(true);

		// if (Build.VERSION.SDK_INT >= 18)// Build.VERSION_CODES.JELLY_BEAN_MR2
		// ((Activity) mContext)
		// .setRequestedOrientation(14 /* SCREEN_ORIENTATION_LOCKED */);
		// else
		// ((Activity) mContext).setRequestedOrientation(((Activity) mContext)
		// .getRequestedOrientation());
	}

	protected void unlockScreen() {
		showInfo(R.string.unlocked, 1000);

		mLockImageButton.setBackgroundResource(R.drawable.ic_lock);
		mPlayerSeekBar.setEnabled(true);
		mPlayStopImageButton.setVisibility(VISIBLE);
		mAudioImageButton.setVisibility(VISIBLE);
		mNavMenuImageButton.setVisibility(VISIBLE);
		mSizeImageButton.setVisibility(VISIBLE);

		showOverlay();
		// if (((Activity) mContext).getRequestedOrientation() ==
		// ActivityInfo.SCREEN_ORIENTATION_SENSOR)
		// ((Activity) mContext)
		// .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	protected void showInfo(String text, int duration) {
		mInfoTextView.setVisibility(View.VISIBLE);
		mInfoTextView.setText(text);
		mViewHandler.removeMessages(FADE_OUT_INFO);
		mViewHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
	}

	protected void showInfo(int textid, int duration) {
		mInfoTextView.setVisibility(View.VISIBLE);
		mInfoTextView.setText(textid);
		mViewHandler.removeMessages(FADE_OUT_INFO);
		mViewHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
	}

	/**
	 * Show text in the info view
	 * 
	 * @param text
	 */
	protected void showInfo(String text) {
		mInfoTextView.setVisibility(View.VISIBLE);
		mInfoTextView.setText(text);
		mViewHandler.removeMessages(FADE_OUT_INFO);
	}

	/**
	 * hide the info view with "delay" milliseconds delay
	 * 
	 * @param delay
	 */
	protected void hideInfo(int delay) {
		mViewHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, delay);
	}

	/**
	 * hide the info view
	 */
	protected void hideInfo() {
		hideInfo(0);
	}

	private void fadeOutInfo() {
		if (mInfoTextView.getVisibility() == View.VISIBLE)
			mInfoTextView.startAnimation(AnimationUtils.loadAnimation(mContext,
					android.R.anim.fade_out));
		mInfoTextView.setVisibility(View.INVISIBLE);
	}

	protected void showOverlay() { // 显示默认的时间
		showOverlay(OVERLAY_TIMEOUT);
	}

	protected void showOverlay(int timeout) { // 显示重叠
		if (!mBShow)
			return;
		mViewHandler.sendEmptyMessage(SHOW_PROGRESS);
		Message msg = mViewHandler.obtainMessage(FADE_OUT);
		if (timeout != 0) {
			mViewHandler.removeMessages(FADE_OUT);
			mViewHandler.sendMessageDelayed(msg, timeout); // 定义消失时间
		}

		mPlayStopImageButton.setVisibility(VISIBLE);
		mHeaderLayout.setVisibility(View.VISIBLE);
		mOptionLayout.setVisibility(View.VISIBLE);
		mProgressLayout.setVisibility(View.VISIBLE);

		dimStatusBar(false);
	}

	protected void hideOverlay(boolean fromUser) {
		mHeaderLayout.startAnimation(AnimationUtils.loadAnimation(mContext,
				android.R.anim.fade_out));
		mOptionLayout.startAnimation(AnimationUtils.loadAnimation(mContext,
				android.R.anim.fade_out));
		mProgressLayout.startAnimation(AnimationUtils.loadAnimation(mContext,
				android.R.anim.fade_out));

		mHeaderLayout.setVisibility(View.INVISIBLE);
		mOptionLayout.setVisibility(View.INVISIBLE);
		mProgressLayout.setVisibility(View.INVISIBLE);

		dimStatusBar(true);

		mViewHandler.removeMessages(SHOW_PROGRESS);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void dimStatusBar(boolean dim) {// Android3.x时朦胧的状态栏和导航图标
		if (!LibVlcUtil.isHoneycombOrLater()
				|| !VLCManager.getInstance().hasNavBar())// ||mIsNavMenu)
			return;
		int layout = 0;
		if (!VLCManager.getInstance().hasCombBar()
				&& LibVlcUtil.isJellyBeanOrLater())
			layout = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		int visibility = (dim ? (VLCManager.getInstance().hasCombBar() ? View.SYSTEM_UI_FLAG_LOW_PROFILE
				: View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
				: View.SYSTEM_UI_FLAG_VISIBLE)
				| layout;
		getPlayerSurfaceView().setSystemUiVisibility(visibility);
		// mSubtitlesSurface.setSystemUiVisibility(visibility);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	protected void changeSurfaceSize() { // 播放器画面刷新
		int sw;
		int sh;

		// get screen size
		sw = ((Activity) mContext).getWindow().getDecorView().getWidth();
		sh = ((Activity) mContext).getWindow().getDecorView().getHeight();

		double dw = sw, dh = sh;
		boolean isPortrait;

		// getWindow().getDecorView() doesn't always take orientation into
		// account, we have to correct the values
		isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

		if (sw > sh && isPortrait || sw < sh && !isPortrait) {
			dw = sh;
			dh = sw;
		}

		// sanity check
		if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
			Log.e(TAG, "Invalid surface size");
			return;
		}

		// compute the aspect ratio
		double ar, vw;
		if (mSarDen == mSarNum) {
			/* No indication about the density, assuming 1:1 */
			vw = mVideoVisibleWidth;
			ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
		} else {
			/* Use the specified aspect ratio */
			vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
			ar = vw / mVideoVisibleHeight;
		}

		// compute the display aspect ratio
		double dar = dw / dh;

		switch (mCurrentSize) {
		case SURFACE_BEST_FIT:
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_FIT_HORIZONTAL:
			dh = dw / ar;
			break;
		case SURFACE_FIT_VERTICAL:
			dw = dh * ar;
			break;
		case SURFACE_FILL:
			break;
		case SURFACE_16_9:
			ar = 16.0 / 9.0;
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_4_3:
			ar = 4.0 / 3.0;
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_ORIGINAL:
			dh = mVideoVisibleHeight;
			dw = vw;
			break;
		}

		SurfaceView surface;
		SurfaceView subtitlesSurface;
		SurfaceHolder surfaceHolder;
		SurfaceHolder subtitlesSurfaceHolder;
		FrameLayout surfaceFrame;

		surface = getPlayerSurfaceView();
		// subtitlesSurface = mSubtitlesSurface;
		surfaceHolder = getPlayerSurfaceView().getHolder();
		// subtitlesSurfaceHolder = mSubtitlesSurfaceHolder;
		surfaceFrame = getPlayerFrameLayout();

		// force surface buffer size
		surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		// subtitlesSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		ViewGroup.LayoutParams lp = surface.getLayoutParams();
		lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
		lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
		surface.setLayoutParams(lp);
		// subtitlesSurface.setLayoutParams(lp);

		// set frame size (crop if necessary)
		lp = surfaceFrame.getLayoutParams();
		lp.width = (int) Math.floor(dw);
		lp.height = (int) Math.floor(dh);
		surfaceFrame.setLayoutParams(lp);

		surface.invalidate();
		// subtitlesSurface.invalidate();
	}

	class ViewHandle extends THandler<View> {

		public ViewHandle(View owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (getOwner() == null)
				return;

			handleViewMessage(msg);
		}
	}

}
