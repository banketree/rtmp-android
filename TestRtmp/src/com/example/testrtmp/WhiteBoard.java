package com.example.testrtmp;

import com.eotu.core.CoreActivity;
import com.example.testrtmp.R;
import com.example.whiteboard.BoardView;
import com.example.whiteboard.Point;
import com.example.whiteboard.PointData;

import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class WhiteBoard extends CoreActivity implements OnClickListener {
	private BoardView mBoardView;
	private boolean selectStatus = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.whiteboard);

		findViewById(R.id.Button_draw_circle).setOnClickListener(this);
		findViewById(R.id.Button_draw_line).setOnClickListener(this);
		findViewById(R.id.Button_draw_point).setOnClickListener(this);
		findViewById(R.id.Button_draw_rectangle).setOnClickListener(this);
		mBoardView = (BoardView) findViewById(R.id.BoardView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private class touchListener implements OnTouchListener {
		PointData obj;
		Point pointvalue;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// mBoardView.moveObj.Init();
				//
				// obj = new PointData();
				// obj.leftx = x;
				// obj.lefty = y;
				// // 当选中的图形做标记
				// if (!mBoardView.dashList.isEmpty()) {
				// for (int i1 = 0; i1 < mBoardView.dashList.size(); i1++) {
				// DashPoint objSelect = mBoardView.dashList.get(i1);
				// if ((int) x >= (int) objSelect.left
				// && (int) x <= (int) objSelect.right
				// && (int) y >= (int) objSelect.top
				// && (int) y <= (int) objSelect.bottom) {
				// selectStatus = true;
				// }
				//
				// if ((int) x <= (int) objSelect.left
				// && (int) x >= (int) objSelect.right
				// && (int) y <= (int) objSelect.top
				// && (int) y >= (int) objSelect.bottom) {
				// selectStatus = true;
				// }
				// }
				// }
				//
				// if (!selectStatus) {
				// mBoardView.moveObj.leftx = x;
				// mBoardView.moveObj.lefty = y;
				// }

				break;
			case MotionEvent.ACTION_UP:
				obj.rightx = x;
				obj.righty = y;
				// obj.drawType = m_type;

				if (selectStatus)// 选中
				{
					// resetDrawType = 9;
					// obj.drawType = resetDrawType;
					selectStatus = false;
				}

				// mBoardView.isUp = false;// 临时的那个画图就没了
				// mBoardView.setPonintCollection(obj);
				mBoardView.invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				pointvalue.x = x;
				pointvalue.y = y;
				obj.point.add(pointvalue);

				if (!selectStatus) {
					// mBoardView.isUp = true;
					// mBoardView.moveObj.rightx = x;
					// mBoardView.moveObj.righty = y;
					// mBoardView.moveObj.point.add(pointvalue);
					// mBoardView.moveObj.drawType = m_type;
				}
				mBoardView.invalidate();
				break;
			}
			pointvalue = new Point();
			pointvalue.startx = x;
			pointvalue.starty = y;

			return true;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.Button_draw_circle) {
			// mBoardView.set
		} else if (v.getId() == R.id.Button_draw_line) {

		} else if (v.getId() == R.id.Button_draw_point) {

		} else if (v.getId() == R.id.Button_draw_rectangle) {

		}
	}
}
