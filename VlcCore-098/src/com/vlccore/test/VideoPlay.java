package com.vlccore.test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.VLCManager;

import com.vlccore.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class VideoPlay extends Activity implements IVideoPlayer {
	private static final String TAG = VideoPlay.class.getSimpleName();
	private SurfaceView mSurface; // 屏幕
	private SurfaceView mSubtitlesSurface; // 字幕
	private SurfaceHolder mSurfaceHolder; // 根
	private SurfaceHolder mSubtitlesSurfaceHolder;// 根
	private FrameLayout mSurfaceFrame; // 层
	private LibVLC mLibVLC; // 库
	private String mLocation = "";
	private final static String PLAY_FROM_VIDEOGRID = "org.videolan.vlc.gui.video.PLAY_FROM_VIDEOGRID";
	private int savedIndexPosition = -1;

	// size of the video 视频大小
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;
	private int mCurrentSize = SURFACE_BEST_FIT;

	private static final int OVERLAY_TIMEOUT = 4000; // 超时
	private static final int OVERLAY_INFINITE = 3600000;
	private static final int FADE_OUT = 1; // 渐变
	private static final int SHOW_PROGRESS = 2;
	private static final int SURFACE_SIZE = 3;
	private static final int AUDIO_SERVICE_CONNECTION_SUCCESS = 5;
	private static final int AUDIO_SERVICE_CONNECTION_FAILED = 6;
	private static final int FADE_OUT_INFO = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player2);

		try {
			mLibVLC = VLCManager.getInstance().getLibVLC();
		} catch (Exception e) {
			Log.d(TAG, "LibVLC initialisation failed");
			return;
		}

		mSurface = (SurfaceView) findViewById(R.id.player_surface);
		mSurfaceHolder = mSurface.getHolder();
		mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);

		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		mSurfaceHolder.addCallback(mSurfaceCallback);

		mSubtitlesSurface = (SurfaceView) findViewById(R.id.subtitles_surface);
		mSubtitlesSurfaceHolder = mSubtitlesSurface.getHolder();
		mSubtitlesSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
		mSubtitlesSurface.setZOrderMediaOverlay(true);
		mSubtitlesSurfaceHolder.addCallback(mSubtitlesSurfaceCallback);

		Log.d(TAG,
				"Hardware Acceleration mode : "
						+ Integer.toString(mLibVLC.getHardwareAcceleration()));

		/* Only show the subtitles surface when using "Full Acceleration" mode */
		if (mLibVLC.getHardwareAcceleration() == LibVLC.HW_ACCELERATION_FULL)
			mSubtitlesSurface.setVisibility(View.VISIBLE);
		// Signal to LibVLC that the videoPlayerActivity was created, thus the
		// SurfaceView is now available for MediaCodec direct rendering.
		mLibVLC.eventVideoPlayerActivityCreated(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		loadMedia();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// MediaCodec opaque direct rendering should not be used anymore since
		// there is no surface to attach.
		mLibVLC.eventVideoPlayerActivityCreated(false);
	}

	/**
	 * attach and disattach surface to the lib
	 */
	private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (format == PixelFormat.RGBX_8888)
				Log.d(TAG, "Pixel format is RGBX_8888");
			else if (format == PixelFormat.RGB_565)
				Log.d(TAG, "Pixel format is RGB_565");
			else if (format == ImageFormat.YV12)
				Log.d(TAG, "Pixel format is YV12");
			else
				Log.d(TAG, "Pixel format is other/unknown");
			if (mLibVLC != null)
				mLibVLC.attachSurface(holder.getSurface(), VideoPlay.this);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mLibVLC != null)
				mLibVLC.detachSurface();
		}
	};

	private final SurfaceHolder.Callback mSubtitlesSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (mLibVLC != null)
				mLibVLC.attachSubtitlesSurface(holder.getSurface());
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mLibVLC != null)
				mLibVLC.detachSubtitlesSurface();
		}
	};

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

		mHandler.sendEmptyMessage(SURFACE_SIZE);
	}

	public static void start(Context context, String location) {
		start(context, location, null, -1, false, false);
	}

	public static void start(Context context, String location, Boolean fromStart) {
		start(context, location, null, -1, false, fromStart);
	}

	public static void start(Context context, String location, String title,
			Boolean dontParse) {
		start(context, location, title, -1, dontParse, false);
	}

	public static void start(Context context, String location, String title,
			int position, Boolean dontParse) {
		start(context, location, title, position, dontParse, false);
	}

	public static void start(Context context, String location, String title,
			int position, Boolean dontParse, Boolean fromStart) {
		Intent intent = new Intent(context, VideoPlay.class);
		intent.setAction(PLAY_FROM_VIDEOGRID);
		intent.putExtra("itemLocation", location);
		intent.putExtra("itemTitle", title);
		intent.putExtra("dontParse", dontParse);
		intent.putExtra("fromStart", fromStart);
		intent.putExtra("itemPosition", position);

		if (dontParse)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

		context.startActivity(intent);
	}

	/**
	 * External extras: - position (long) - position of the video to start with
	 * (in ms)
	 */
	@SuppressWarnings({ "unchecked" })
	private void loadMedia() {
		mLocation = null;
		String title = getResources().getString(R.string.title);
		boolean dontParse = false;
		boolean fromStart = false;
		String itemTitle = null;
		int itemPosition = -1; // Index in the media list as passed by
								// AudioServer (used only for vout transition
								// internally)
		long intentPosition = -1; // position passed in by intent (ms)

		if (getIntent().getAction() != null
				&& getIntent().getAction().equals(Intent.ACTION_VIEW)) {
			/* Started from external application 'content' */
			if (getIntent().getData() != null
					&& getIntent().getData().getScheme() != null
					&& getIntent().getData().getScheme().equals("content")) {

				// Media or MMS URI
				if (getIntent().getData().getHost().equals("media")
						|| getIntent().getData().getHost().equals("mms")) {
					try {
						Cursor cursor = getContentResolver().query(
								getIntent().getData(),
								new String[] { MediaStore.Video.Media.DATA },
								null, null, null);
						if (cursor != null) {
							int column_index = cursor
									.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
							if (cursor.moveToFirst())
								mLocation = LibVLC.PathToURI(cursor
										.getString(column_index));
							cursor.close();
						}
					} catch (Exception e) {
						Log.e(TAG, "Couldn't read the file from media or MMS");
						// encounteredError();
					}
				}

				// Mail-based apps - download the stream to a temporary file and
				// play it
				else if (getIntent().getData().getHost()
						.equals("com.fsck.k9.attachmentprovider")
						|| getIntent().getData().getHost().equals("gmail-ls")) {
					try {
						Cursor cursor = getContentResolver()
								.query(getIntent().getData(),
										new String[] { MediaStore.MediaColumns.DISPLAY_NAME },
										null, null, null);
						if (cursor != null) {
							cursor.moveToFirst();
							String filename = cursor
									.getString(cursor
											.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
							cursor.close();
							Log.i(TAG, "Getting file " + filename
									+ " from content:// URI");

							InputStream is = getContentResolver()
									.openInputStream(getIntent().getData());
							OutputStream os = new FileOutputStream(Environment
									.getExternalStorageDirectory().getPath()
									+ "/Download/" + filename);
							byte[] buffer = new byte[1024];
							int bytesRead = 0;
							while ((bytesRead = is.read(buffer)) >= 0) {
								os.write(buffer, 0, bytesRead);
							}
							os.close();
							is.close();
							mLocation = LibVLC.PathToURI(Environment
									.getExternalStorageDirectory().getPath()
									+ "/Download/" + filename);
						}
					} catch (Exception e) {
						Log.e(TAG, "Couldn't download file from mail URI");
						// encounteredError();
					}
				}

				// other content-based URI (probably file pickers)
				else {
					mLocation = getIntent().getData().getPath();
				}
			} /* External application */
			else if (getIntent().getDataString() != null) {
				// Plain URI
				mLocation = getIntent().getDataString();
				// Remove VLC prefix if needed
				if (mLocation.startsWith("vlc://")) {
					mLocation = mLocation.substring(6);
				}
				// Decode URI
				if (!mLocation.contains("/")) {
					try {
						mLocation = URLDecoder.decode(mLocation, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			} else {
				Log.e(TAG, "Couldn't understand the intent");
				// encounteredError();
			}

			// Try to get the position
			if (getIntent().getExtras() != null)
				intentPosition = getIntent().getExtras()
						.getLong("position", -1);
		} /* ACTION_VIEW */
		/* Started from VideoListActivity */
		else if (getIntent().getAction() != null
				&& getIntent().getAction().equals(PLAY_FROM_VIDEOGRID)
				&& getIntent().getExtras() != null) {
			mLocation = getIntent().getExtras().getString("itemLocation");
			itemTitle = getIntent().getExtras().getString("itemTitle");
			dontParse = getIntent().getExtras().getBoolean("dontParse");
			fromStart = getIntent().getExtras().getBoolean("fromStart");
			itemPosition = getIntent().getExtras().getInt("itemPosition", -1);
		}

		mSurface.setKeepScreenOn(true);

		if (mLibVLC == null)
			return;

		/*
		 * WARNING: hack to avoid a crash in mediacodec on KitKat. Disable the
		 * hardware acceleration the media has a ts extension.
		 */
		if (mLocation != null && LibVlcUtil.isKitKatOrLater()) {
			String locationLC = mLocation.toLowerCase(Locale.ENGLISH);
			if (locationLC.endsWith(".ts") || locationLC.endsWith(".tts")
					|| locationLC.endsWith(".m2t")
					|| locationLC.endsWith(".mts")
					|| locationLC.endsWith(".m2ts")) {
				// mDisabledHardwareAcceleration = true;
				// mPreviousHardwareAccelerationMode = mLibVLC
				// .getHardwareAcceleration();
				mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
			}
		}

		/* Start / resume playback */
		if (dontParse && itemPosition >= 0) {
			// Provided externally from AudioService
			Log.d(TAG, "Continuing playback from AudioService at index "
					+ itemPosition);
			// savedIndexPosition = itemPosition;
			if (!mLibVLC.isPlaying()) {
				// AudioService-transitioned playback for item after sleep and
				// resume
				// mLibVLC.playIndex(savedIndexPosition);
				dontParse = false;
			} else {
				// stopLoadingAnimation();
				// showOverlay();
			}
			// updateNavStatus();
		} else if (savedIndexPosition > -1) {
			// AudioServiceController.getInstance().stop(); // Stop the previous
			// playback.
			mLibVLC.setMediaList();
			mLibVLC.playIndex(savedIndexPosition);
		} else if (mLocation != null && mLocation.length() > 0 && !dontParse) {
			// AudioServiceController.getInstance().stop(); // Stop the previous
			// playback.
			mLibVLC.setMediaList();
			mLibVLC.getMediaList().add(new Media(mLibVLC, mLocation));
			savedIndexPosition = mLibVLC.getMediaList().size() - 1;
			mLibVLC.playIndex(savedIndexPosition);
		}
		// mCanSeek = false;

		if (mLocation != null && mLocation.length() > 0 && !dontParse) {
			// restore last position
			// Media media =
			// MediaDatabase.getInstance(this).getMedia(mLocation);
			// if (media != null) {
			// // in media library
			// if (media.getTime() > 0 && !fromStart)
			// mLibVLC.setTime(media.getTime());
			//
			// mLastAudioTrack = media.getAudioTrack();
			// mLastSpuTrack = media.getSpuTrack();
			// } else {
			// not in media library
			// long rTime = mSettings.getLong(
			// PreferencesActivity.VIDEO_RESUME_TIME, -1);
			// Editor editor = mSettings.edit();
			// editor.putLong(PreferencesActivity.VIDEO_RESUME_TIME, -1);
			// editor.commit();
			// if (rTime > 0)
			// mLibVLC.setTime(rTime);
			//
			// if (intentPosition > 0)
			// mLibVLC.setTime(intentPosition);
			// }

			// Get possible subtitles
			// String subtitleList_serialized = mSettings.getString(
			// PreferencesActivity.VIDEO_SUBTITLE_FILES, null);
			// ArrayList<String> prefsList = new ArrayList<String>();
			// if (subtitleList_serialized != null) {
			// ByteArrayInputStream bis = new ByteArrayInputStream(
			// subtitleList_serialized.getBytes());
			// try {
			// ObjectInputStream ois = new ObjectInputStream(bis);
			// prefsList = (ArrayList<String>) ois.readObject();
			// } catch (ClassNotFoundException e) {
			// } catch (StreamCorruptedException e) {
			// } catch (IOException e) {
			// }
			// }
			// for (String x : prefsList) {
			// if (!mSubtitleSelectedFiles.contains(x))
			// mSubtitleSelectedFiles.add(x);
			// }
			//
			// // Get the title
			// try {
			// title = URLDecoder.decode(mLocation, "UTF-8");
			// } catch (UnsupportedEncodingException e) {
			// } catch (IllegalArgumentException e) {
			// }
			// if (title.startsWith("file:")) {
			// title = new File(title).getName();
			// int dotIndex = title.lastIndexOf('.');
			// if (dotIndex != -1)
			// title = title.substring(0, dotIndex);
			// }
		} else if (itemTitle != null) {
			title = itemTitle;
		}
		// mTitle.setText(title);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void changeSurfaceSize() {
		int sw;
		int sh;

		// get screen size
		sw = getWindow().getDecorView().getWidth();
		sh = getWindow().getDecorView().getHeight();

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

		surface = mSurface;
		subtitlesSurface = mSubtitlesSurface;
		surfaceHolder = mSurfaceHolder;
		subtitlesSurfaceHolder = mSubtitlesSurfaceHolder;
		surfaceFrame = mSurfaceFrame;

		// force surface buffer size
		surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		subtitlesSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		LayoutParams lp = surface.getLayoutParams();
		lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
		lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
		surface.setLayoutParams(lp);
		subtitlesSurface.setLayoutParams(lp);

		// set frame size (crop if necessary)
		lp = surfaceFrame.getLayoutParams();
		lp.width = (int) Math.floor(dw);
		lp.height = (int) Math.floor(dh);
		surfaceFrame.setLayoutParams(lp);

		surface.invalidate();
		subtitlesSurface.invalidate();
	}

	/**
	 * Handle resize of the surface and the overlay
	 */
	private final Handler mHandler = new VideoPlayerHandler(this);

	private static class VideoPlayerHandler extends WeakHandler<VideoPlay> {
		public VideoPlayerHandler(VideoPlay owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			VideoPlay activity = getOwner();
			if (activity == null) // WeakReference could be GC'ed early
				return;

			switch (msg.what) {
			case FADE_OUT:
				// activity.hideOverlay(false);
				break;
			case SHOW_PROGRESS:
				// int pos = activity.setOverlayProgress();
				// if (activity.canShowProgress()) {
				// msg = obtainMessage(SHOW_PROGRESS);
				// sendMessageDelayed(msg, 1000 - (pos % 1000));
				// }
				break;
			case SURFACE_SIZE:
				activity.changeSurfaceSize();
				break;
			case FADE_OUT_INFO:
				// activity.fadeOutInfo();
				break;
			case AUDIO_SERVICE_CONNECTION_SUCCESS:
				// activity.startPlayback();
				break;
			case AUDIO_SERVICE_CONNECTION_FAILED:
				activity.finish();
				break;
			}
		}
	};

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void dimStatusBar(boolean dim) {
		if (!LibVlcUtil.isHoneycombOrLater()
				|| !VLCManager.getInstance().hasNavBar())
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
		mSurface.setSystemUiVisibility(visibility);
		mSubtitlesSurface.setSystemUiVisibility(visibility);
	}
}
