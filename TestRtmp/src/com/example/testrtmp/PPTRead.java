package com.example.testrtmp;

import java.io.File;

import com.eotu.core.CoreActivity;
import com.example.testrtmp.R;
import com.example.whiteboard.BoardView;
import com.example.whiteboard.Point;
import com.example.whiteboard.PointData;
import com.itsrts.pptviewer.PPTViewer;

import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class PPTRead extends CoreActivity implements OnClickListener {

	PPTViewer pptViewer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ppt_view);
		pptViewer = (PPTViewer) findViewById(R.id.pptviewer);
		String path = Environment.getExternalStorageDirectory().getPath()
				+ "/123/test2.ppt";
		pptViewer.setNext_img(R.drawable.next).setPrev_img(R.drawable.prev)
				.setSettings_img(R.drawable.settings)
				.setZoomin_img(R.drawable.zoomin)
				.setZoomout_img(R.drawable.zoomout);
		File file = new File(path);
		if (!file.exists())
			return;

		try {
			pptViewer.loadPPT(this, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onClick(View v) {

	}
}
