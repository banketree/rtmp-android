package com.eotu.rtmp;

import java.util.List;
import java.util.Map;

import org.red5.server.IAttributeStore;
import org.red5.server.so.IClientSharedObject;
import org.red5.server.so.ISharedObjectBase;
import org.red5.server.so.ISharedObjectListener;

import android.content.Context;

import com.eotu.core.CoreEvent;
import com.treecore.TBroadcastByInner;
import com.treecore.TIGlobalInterface;
import com.treecore.utils.log.TLog;
import com.treecore.utils.task.TITaskListener;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

public class MembersShareObject implements ISharedObjectListener,
		TIGlobalInterface, TITaskListener {
	private String NameString = "memberList";
	private String listString = "list";
	private TTask mUpdateTask;
	private IClientSharedObject mClientSharedObject;

	public MembersShareObject() {
		mUpdateTask = new TTask();
		mUpdateTask.setIXTaskListener(this);
	}

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
		TLog.i(this, "onSharedObjectConnect");
		mUpdateTask.startTask(100);
	}

	@Override
	public void onSharedObjectDisconnect(ISharedObjectBase so) {
		TLog.i(this, "onSharedObjectDisconnect");
		mUpdateTask.startTask(100);
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so, String key,
			Object value) {
		TLog.i(this, "onSharedObjectUpdate");
		mUpdateTask.startTask(100);
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			IAttributeStore values) {
		TLog.i(this, "onSharedObjectUpdate");
		mUpdateTask.startTask(100);
	}

	@Override
	public void onSharedObjectUpdate(ISharedObjectBase so,
			Map<String, Object> values) {
		TLog.i(this, "onSharedObjectUpdate");
		mUpdateTask.startTask(100);
	}

	@Override
	public void onSharedObjectDelete(ISharedObjectBase so, String key) {
		TLog.i(this, "onSharedObjectDelete");
		mUpdateTask.startTask(100);
	}

	@Override
	public void onSharedObjectClear(ISharedObjectBase so) {
		TLog.i(this, "onSharedObjectClear");
		mUpdateTask.startTask(100);
	}

	@Override
	public void onSharedObjectSend(ISharedObjectBase so, String method,
			List<?> params) {
		TLog.i(this, "onSharedObjectSend");
	}

	public Map<String, Object> getMembers() {
		try {
			// return (Map<String, Object>) mClientSharedObject
			// .getMapAttribute(listString);
			return mClientSharedObject.getData();
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		if (mUpdateTask != null && mUpdateTask.equalTask(task)) {
			if (event == TaskEvent.Work) {
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
				}

				if (task.isCancel())
					return;
			} else if (event == TaskEvent.Cancel) {
				if (task.isCancel())
					return;
				TBroadcastByInner.sentEvent(CoreRtmpClient.getInstance()
						.getContext(), CoreEvent.RtmpEvent,
						RtmpEvent.MembersUpdate);
			}
		}
	}
}
