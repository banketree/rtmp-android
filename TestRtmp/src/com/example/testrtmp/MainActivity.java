package com.example.testrtmp;

import java.util.Map;

import com.eotu.core.CoreActivity;
import com.eotu.core.CoreEvent;
import com.example.core.CoreRtmpClient;
import com.example.core.RtmpEvent;
import com.treecore.TBroadcastByInner;
import com.treecore.filepath.TFilePathManager;
import com.treecore.utils.TActivityUtils;
import com.treecore.utils.TStringUtils;
import com.treecore.utils.task.TTask;
import com.treecore.utils.task.TTask.Task;
import com.treecore.utils.task.TTask.TaskEvent;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends CoreActivity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {
	private TextView mStatusTextView;
	private TTask mLoginRtmp = null, mRecorderPlayer;
	private ListView mFlvsListView;
	private FlvBaseAdapter mFlvBaseAdapter;
	private TTask mPublisherPlayer;
	private EditText mEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.Button_rtmp_loginOrExit).setOnClickListener(this);
		findViewById(R.id.Button_getAvailableFlvs).setOnClickListener(this);
		findViewById(R.id.Button_sendMessage).setOnClickListener(this);
		findViewById(R.id.Button_getMembers).setOnClickListener(this);
		findViewById(R.id.Button_whiteboard).setOnClickListener(this);
		findViewById(R.id.Button_ppt).setOnClickListener(this);

		mStatusTextView = (TextView) findViewById(R.id.TextView_rtmp_status);
		mFlvsListView = (ListView) findViewById(R.id.ListView_AvailableFlvs);
		mFlvsListView.setOnItemClickListener(this);
		mFlvsListView.setOnItemLongClickListener(this);

		CoreRtmpClient.getInstance().initConfig(this);
		CoreRtmpClient.getInstance().setAppName("oflaDemo");
		CoreRtmpClient.getInstance().setHost("192.168.1.126");
		CoreRtmpClient.getInstance().setPort(1935);
		CoreRtmpClient.getInstance().setPublishMode("live");

		String filePath = TFilePathManager.getInstance().getCachePath();
		Log.i("", filePath);//
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == R.id.Button_rtmp_loginOrExit) {
			if (CoreRtmpClient.getInstance().isRtmpConnected()) {
				CoreRtmpClient.getInstance().stopRtmp();
			} else if (CoreRtmpClient.getInstance().isRtmpDisconnected()) {
				if (mLoginRtmp == null) {
					mLoginRtmp = new TTask();
					mLoginRtmp.setIXTaskListener(this);
				}

				mLoginRtmp.startTask("");
			}
		} else if (arg0.getId() == R.id.Button_getAvailableFlvs) {
			if (!CoreRtmpClient.getInstance().isRtmpConnected()) {
				makeText("未连接");
				return;
			}

			CoreRtmpClient.getInstance().onGetAvailableFlvs();
		} else if (arg0.getId() == R.id.Button_sendMessage) {
			sentMessageToFriend();
		} else if (arg0.getId() == R.id.Button_getMembers) {
			showMembers();
		} else if (arg0.getId() == R.id.Button_whiteboard) {
			TActivityUtils.jumpToActivity(mContext, WhiteBoard.class);
		} else if (arg0.getId() == R.id.Button_ppt) {
			TActivityUtils.jumpToActivity(mContext, PPTRead.class);
		}
	}

	@Override
	public void processEventByInner(Intent intent) {
		super.processEventByInner(intent);

		int mainEvent = intent.getIntExtra(TBroadcastByInner.MAINEVENT, -1);
		if (mainEvent != CoreEvent.RtmpEvent)
			return;

		int subEvent = intent.getIntExtra(TBroadcastByInner.EVENT, -1);

		if (subEvent == RtmpEvent.Connect) {
			String contentString = "正在连接";

			if (CoreRtmpClient.getInstance().isRtmpConnecting()) {
			} else if (CoreRtmpClient.getInstance().isRtmpConnected()) {
				contentString = "已连接";
				((Button) findViewById(R.id.Button_rtmp_loginOrExit))
						.setText("rtmp退出");
			} else if (CoreRtmpClient.getInstance().isRtmpDisconnected()) {
				if (mLoginRtmp != null && mLoginRtmp.isTasking()) {
					contentString = "正在连接";
				} else {
					contentString = "未连接";
					((Button) findViewById(R.id.Button_rtmp_loginOrExit))
							.setText("rtmp登陆");
				}
			}

			mStatusTextView.setText("rtmp" + contentString);
		} else if (subEvent == RtmpEvent.Exception) {
			showDialog("服务器异常");
		} else if (subEvent == RtmpEvent.ServerShotout) {
			showDialog("你被服务器提出了");
		} else if (subEvent == RtmpEvent.SetAvailableFlvs) {
			if (mFlvBaseAdapter == null) {
				mFlvBaseAdapter = new FlvBaseAdapter(this);
				mFlvsListView.setAdapter(mFlvBaseAdapter);
			}

			mFlvBaseAdapter.notifyDataSetChanged();
		} else if (subEvent == RtmpEvent.ChatMessage) {
			String content = getBroadcastParameterByInner().get(0);
			makeText("服务器反馈消息：" + content);
		} else if (subEvent == RtmpEvent.MembersUpdate) {// 成员列表更新
			showMembers();
		}
	}

	@Override
	public void onTask(Task task, TaskEvent event, Object... params) {
		super.onTask(task, event, params);

		if (mLoginRtmp != null && mLoginRtmp.equalTask(task)) {

			if (event == TaskEvent.Work) {
				try {
					CoreRtmpClient.getInstance().startRtmp("banketree", "test");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (event == TaskEvent.Cancel) {
				TBroadcastByInner.sentEvent(mContext, CoreEvent.RtmpEvent,
						RtmpEvent.Connect);
			}
		} else if (mRecorderPlayer != null && mRecorderPlayer.equalTask(task)) {

		} else if (mPublisherPlayer != null && mPublisherPlayer.equalTask(task)) {

		}
	}

	private void showDialog(String content) {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage(content);
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { // 根据id删除短信
				dialog.dismiss();
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		showPlayDialog(CoreRtmpClient.getInstance().getAvailableFlvs()
				.get(arg2));
	}

	private void showPlayDialog(final String content) {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage("是否播放：" + content);
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { // 根据id删除短信
				dialog.dismiss();
				TActivityUtils.jumpToActivity(mContext, Player.class, content);
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	public void sentMessageToFriend() {
		mEditText = null;
		mEditText = new EditText(this);
		new AlertDialog.Builder(this).setTitle("请输入要发送的内容")
				.setIcon(android.R.drawable.ic_dialog_info).setView(mEditText)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (!CoreRtmpClient.getInstance().isRtmpConnected()) {
							makeText("未连接");
							return;
						}

						String content = mEditText.getText().toString();
						if (TStringUtils.isEmpty(content)) {
							makeText("内容为空……");
							return;
						}

						CoreRtmpClient.getInstance().sendMessage(content);
					}
				}).setNegativeButton("取消", null).show();
	}

	private void showMembers() {
		Map<String, Object> members = null;
		try {
			members = CoreRtmpClient.getInstance().getMembersShareObject()
					.getMembers();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			makeText("成员列表刷新了：" + (members == null ? "空" : members.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
