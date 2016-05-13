package com.example.testrtmp;

import android.content.Intent;

import com.eotu.core.crash.CrashActivity;
import com.eotu.core.crash.CrashReportSender;
import com.treecore.TApplication;
import com.treecore.crash.TCrash;

public class MyApp extends TApplication {
	private static final String TAG = MyApp.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();

		// 奔溃处理
		TCrash.getInstance().removeAllReportSenders();
		TCrash.getInstance().setReportSender(
				new CrashReportSender(getApplicationContext()));
	}

	@Override
	public void onAppCrash(String crashFile) {
		super.onAppCrash(crashFile);

		Intent dialogIntent = new Intent(this, CrashActivity.class);
		dialogIntent.putExtra(TCrash.EXTRA_REPORT_FILE_NAME, crashFile);
		dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(dialogIntent);
	}
}
