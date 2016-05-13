package org.videolan.libvlc;

import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;
import android.telephony.TelephonyManager;

public class VLCManager {
	private static String TAG = VLCManager.class.getSimpleName();

	public static final int CURRENT_ITEM = 1;
	public static final int PREVIOUS_ITEM = 2;
	public static final int NEXT_ITEM = 3;

	private static VLCManager mThis = null;
	private LibVLC mLibVLC;

	private Context mContext;
	private OnAudioFocusChangeListener audioFocusListener; // 音频状态

	private final static boolean mHasNavBar;

	static {
		LibVLC.loadLibrary();

		HashSet<String> devicesWithoutNavBar = new HashSet<String>();
		devicesWithoutNavBar.add("HTC One V");
		devicesWithoutNavBar.add("HTC One S");
		devicesWithoutNavBar.add("HTC One X");
		devicesWithoutNavBar.add("HTC One XL");
		mHasNavBar = LibVlcUtil.isICSOrLater()
				&& !devicesWithoutNavBar.contains(android.os.Build.MODEL);
	}

	public VLCManager() {
	}

	public static VLCManager getInstance() {
		if (mThis == null)
			mThis = new VLCManager();
		return mThis;
	}

	public void initConfig(Context context) throws Exception {
		mContext = context;
		mLibVLC = new LibVLC();
		mLibVLC.init(context);
	}

	public void release() throws Exception {
		if (mLibVLC != null) {
			if (mLibVLC.isPlaying())
				mLibVLC.stop();

			mLibVLC.closeAout();
			mLibVLC.clearBuffer();
			mLibVLC.destroy();
		}
		mLibVLC = null;
		mContext = null;
	}

	public LibVLC getLibVLC() {
		return mLibVLC;
	}

	public Context getContext() {
		return mContext;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private void changeAudioFocus(boolean gain) { // 音频变化
		if (!LibVlcUtil.isFroyoOrLater()) // NOP if not supported
			return;

		if (audioFocusListener == null) {
			audioFocusListener = new OnAudioFocusChangeListener() {
				@Override
				public void onAudioFocusChange(int focusChange) {

					switch (focusChange) {
					case AudioManager.AUDIOFOCUS_LOSS:
						if (mLibVLC.isPlaying())
							mLibVLC.pause();
						break;
					case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
						/*
						 * Lower the volume to 36% to "duck" when an alert or
						 * something needs to be played.
						 */
						mLibVLC.setVolume(36);
						break;
					case AudioManager.AUDIOFOCUS_GAIN:
					case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
					case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
						mLibVLC.setVolume(100);
						break;
					}
				}
			};
		}
		AudioManager am = (AudioManager) (mContext
				.getSystemService(Context.AUDIO_SERVICE));
		if (gain)
			am.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN);
		else
			am.abandonAudioFocus(audioFocusListener);
	}

	public void updateLibVlcSettings(SharedPreferences pref) {
		if (mLibVLC == null)
			return;

		mLibVLC.setSubtitlesEncoding(pref.getString("subtitle_text_encoding",
				""));
		mLibVLC.setTimeStretching(pref.getBoolean(
				"enable_time_stretching_audio", false));
		mLibVLC.setFrameSkip(pref.getBoolean("enable_frame_skip", false));
		mLibVLC.setChroma(pref.getString("chroma_format", ""));
		mLibVLC.setVerboseMode(pref.getBoolean("enable_verbose_mode", true));

		if (pref.getBoolean("equalizer_enabled", false))
			mLibVLC.setEqualizer(getFloatArray(pref, "equalizer_values"));

		int aout;
		try {
			aout = Integer.parseInt(pref.getString("aout", "-1"));
		} catch (NumberFormatException nfe) {
			aout = -1;
		}
		int vout;
		try {
			vout = Integer.parseInt(pref.getString("vout", "-1"));
		} catch (NumberFormatException nfe) {
			vout = -1;
		}
		int deblocking;
		try {
			deblocking = Integer.parseInt(pref.getString("deblocking", "-1"));
		} catch (NumberFormatException nfe) {
			deblocking = -1;
		}
		int hardwareAcceleration;
		try {
			hardwareAcceleration = Integer.parseInt(pref.getString(
					"hardware_acceleration", "-1"));
		} catch (NumberFormatException nfe) {
			hardwareAcceleration = -1;
		}
		int networkCaching = pref.getInt("network_caching_value", 0);
		if (networkCaching > 0)
			networkCaching = 0;
		else if (networkCaching < 0)
			networkCaching = 0;
		mLibVLC.setAout(aout);
		mLibVLC.setVout(vout);
		mLibVLC.setDeblocking(deblocking);
		mLibVLC.setNetworkCaching(networkCaching);
		mLibVLC.setHardwareAcceleration(hardwareAcceleration);
	}

	public static float[] getFloatArray(SharedPreferences pref, String key) {
		float[] array = null;
		String s = pref.getString(key, null);
		if (s != null) {
			try {
				JSONArray json = new JSONArray(s);
				array = new float[json.length()];
				for (int i = 0; i < array.length; i++)
					array[i] = (float) json.getDouble(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return array;
	}

	public boolean hasNavBar() {
		return mHasNavBar;
	}

	public boolean hasCombBar() {
		return (!isPhone() && ((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) && (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN)));
	}

	public boolean isPhone() {
		TelephonyManager manager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
			return false;
		} else {
			return true;
		}
	}

}
