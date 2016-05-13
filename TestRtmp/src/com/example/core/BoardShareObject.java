package com.example.core;

import java.util.List;
import java.util.Map;

import org.red5.server.IAttributeStore;
import org.red5.server.so.IClientSharedObject;
import org.red5.server.so.ISharedObjectBase;
import org.red5.server.so.ISharedObjectListener;

import android.util.Log;

import com.treecore.TIGlobalInterface;

public class BoardShareObject implements ISharedObjectListener,
		TIGlobalInterface {// 电子白板共享对象
	private String NameString = "boards";
	private IClientSharedObject mClientSharedObject;

	@Override
	public void initConfig() {
		mClientSharedObject = CoreRtmpClient.getInstance().getRtmpClient()
				.getSharedObject(NameString, false);

		mClientSharedObject.addSharedObjectListener(this);
		if (!mClientSharedObject.isConnected())
			mClientSharedObject.connect(CoreRtmpClient.getInstance()
					.getRtmpClient().getConnection());
	}

	@Override
	public void release() {
		if (mClientSharedObject == null)
			return;

		if (mClientSharedObject.isConnected())
			mClientSharedObject.disconnect();
		mClientSharedObject.removeSharedObjectListener(this);
		mClientSharedObject = null;
	}

	@Override
	public void onSharedObjectConnect(ISharedObjectBase so) {
		Log.i("", so.toString());
	}

	@Override
	public void onSharedObjectDisconnect(ISharedObjectBase so) {
		Log.i("", so.toString());
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, String key,
			Object value) {
		Log.i("", so.toString());
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			IAttributeStore values) {
		Log.i("", so.toString());
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			Map<String, Object> values) {
		Log.i("", so.toString());
	}

	@Override
	public void onSharedObjectDelete(ISharedObjectBase so, String key) {
		Log.i("", so.toString());
	}

	@Override
	public void onSharedObjectClear(ISharedObjectBase so) {
		Log.i("", so.toString());
	}

	@Override
	public void onSharedObjectSend(ISharedObjectBase so, String method,
			List<?> params) {
		Log.i("", so.toString());
	}

}
