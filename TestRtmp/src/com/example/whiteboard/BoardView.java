package com.example.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;

//使用双缓冲画图
public class BoardView extends View implements OnTouchListener,
		OnGlobalLayoutListener {
	private Paint mPaint = null;
	private Bitmap mCacheBitmap = null; // 定义一个内存中的图片,该图片将作为缓冲区
	private Canvas mCacheCanvas = null; // 定义cacheBitmap上的canvas对象
	private Path path;

	public BoardView(Context context) {
		super(context);
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	public void onGlobalLayout() { // 布局发生改变
		if (mPaint == null) {
			mPaint = new Paint();
		}

		if (mCacheBitmap == null & getWidth() > 0 && getHeight() > 0) {
			mCacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
					Config.ARGB_8888);
		}

		if (mCacheCanvas == null) {
			mCacheCanvas = new Canvas();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Paint bmpPaint = new Paint();
		// canvas.drawBitmap(mCacheBitmap, 0, 0, bmpPaint);//
		// 将cacheBitmap绘制到该View组件上
		// canvas.drawPath(path, mPaint);// 沿着path绘制
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		return false;
	}

}

// public class CopyOfBoardView extends View {
//
// private ArrayList<PointData> list = new ArrayList<PointData>();
// public PointData moveObj = new PointData();
// public ArrayList<DashPoint> dashList = new ArrayList<DashPoint>();
// public Boolean isUp = false;
//
// public CopyOfBoardView(Context context) {
// super(context);
// }
//
// public CopyOfBoardView(Context context, AttributeSet attrs) {
// super(context, attrs);
// }
//
// protected void onDraw(Canvas canvas) {
// super.onDraw(canvas);
// if (isUp) {
// switch (moveObj.drawType) {
// case 1:// 画线
// drawLine(canvas, moveObj);
// break;
// case 2:// 画笔
// drawPen(canvas, moveObj);
// break;
// case 3:
// drawFillRect(canvas, moveObj);
// break;
// case 4:// 实心圆
// drawFillRect(canvas, moveObj);
// break;
// case 5:
// drawOval(canvas, moveObj);
// break;
// case 6:
// drawOval(canvas, moveObj);
// break;
// case 7:
// drawDashRect(canvas, moveObj);
// break;
// case 8:
// drawDashRect(canvas, moveObj);
// }
// }
// // 存在的类型绘制
// for (int i = 0; i < list.size(); i++) {
// PointData obj = list.get(i);
// switch (obj.drawType) {
// case 1:// 画线
// drawLine(canvas, obj);
// break;
// case 2:// 画笔
// drawPen(canvas, obj);
// break;
// case 3:// 空心矩形
// drawFillRect(canvas, obj);
// break;
// case 4:// 实心矩形
// drawFillRect(canvas, obj);
// break;
// case 5:// 空心圆
// drawOval(canvas, obj);
// break;
// case 6:// 实心圆
// drawOval(canvas, obj);
// break;
// case 7:// 虚线选中
// selectDrawRectDashed(canvas, obj);
// break;
// case 8:// 删除
// selectDelete(canvas, obj);
// break;
// case 9:
// // 选中的点 down和up的点相减就是我们每个物品的移动的点
// resetDrawInvalidate(canvas, obj);
// break;
// }
// }
// drawDashRectSelect(canvas, dashList);
// }
//
// protected void resetDrawInvalidate(Canvas canvas, PointData obj) {
// float x, y;
// x = obj.rightx - obj.leftx;
// y = obj.righty - obj.lefty;
//
// for (int i = 0; i < dashList.size(); i++) {
// DashPoint dashObj = dashList.get(i);
// dashList.get(i).left = dashObj.left + (x);
// dashList.get(i).top = dashObj.top + (y);
// dashList.get(i).right = dashObj.right + (x);
// dashList.get(i).bottom = dashObj.bottom + (y);
//
// // 加上了list里面的数据
// list.get(dashObj.point_index).leftx = list.get(dashObj.point_index).leftx
// + (x);
// list.get(dashObj.point_index).lefty = list.get(dashObj.point_index).lefty
// + (y);
// list.get(dashObj.point_index).rightx = list
// .get(dashObj.point_index).rightx + (x);
// list.get(dashObj.point_index).righty = list
// .get(dashObj.point_index).righty + (y);
//
// // 当是画笔的时候需要我们把移动过程的点数也加上
// if (list.get(dashObj.point_index).drawType == 2) {
// for (int listi = 0; listi < list.get(dashObj.point_index).point
// .size(); listi++) {
// Point pointvalue = list.get(dashObj.point_index).point
// .get(listi);
// pointvalue.startx = pointvalue.startx + (x);
// pointvalue.starty = pointvalue.starty + (y);
// pointvalue.x = pointvalue.x + (x);
// pointvalue.y = pointvalue.y + (y);
// }
// }
// }
//
// list.remove(obj);
// invalidate();
// }
//
// protected void drawDashRect(Canvas canvas, PointData obj) {
// Paint p = new Paint();
// PathEffect effects = new DashPathEffect(new float[] { 5, 5, 5, 5 }, 1);
// p.setPathEffect(effects);
// p.setColor(Color.GRAY);// 设置颜色
// p.setStyle(Paint.Style.STROKE);// 设置填满
// canvas.drawRect(obj.leftx, obj.lefty, obj.rightx, obj.righty, p);
// }
//
// protected void selectDelete(Canvas canvas, PointData obj) {
// Vector<PointData> myArray = new Vector<PointData>();
// for (int i = 0; i < list.size(); i++) {
// PointData objs = list.get(i);
//
// // 虚线矩形必须进行判断
// float left = obj.leftx, top = obj.lefty, right = obj.rightx, bottom =
// obj.righty;
// if (obj.rightx < obj.leftx) {
// left = obj.rightx;
// right = obj.leftx;
// }
//
// if (obj.righty < obj.lefty) {
// top = obj.righty;
// bottom = obj.lefty;
// }
// switch (objs.drawType) {
// case 1:// 画线
// Region currentRegion = new Region();
// currentRegion.set((int) left, (int) top, (int) right,
// (int) bottom);
//
// Region clip1 = new Region();
// clip1.set(0, 0, 2000, 2000);
//
// // 这条线的region
// Region compareRegion = new Region();
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
//
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
//
// Path m_path = new Path();
// m_path.moveTo(left, top);
// m_path.lineTo(left + 1, top);
// m_path.lineTo(right + 1, bottom);
// m_path.lineTo(right, bottom);
// m_path.lineTo(left, top);
// compareRegion.setPath(m_path, clip1);
// if (currentRegion.op(compareRegion, Region.Op.INTERSECT)) {
// myArray.add(objs);
// }
// break;
// case 2:// 画笔
// // 当前的这个矩形的Region
// Region R1 = new Region();
// R1.set((int) left, (int) top, (int) right, (int) bottom);
//
// // 定义的一个空的Region
// Region region = new Region();
// region.set(0, 0, 1, 1);
//
// // 为了下面的Region
// Region clip = new Region();
// clip.set(0, 0, 2000, 2000);
//
// int minleft = 0;
// int mintop = 0;
// int maxright = 0;
// int maxbottom = 0;
//
// for (int peni = 0; peni < objs.point.size(); peni++) {
// Point pointvalue = objs.point.get(peni);
// float x = pointvalue.x;
// float y = pointvalue.y;
// float startx = pointvalue.startx;
// float starty = pointvalue.starty;
//
// if (0 == peni) {
// minleft = maxright = (int) objs.leftx;
// mintop = maxbottom = (int) objs.lefty;
// }
//
// if (minleft > (int) x) {
// minleft = (int) x;
// }
//
// if (mintop > (int) y) {
// mintop = (int) y;
// }
//
// if (maxright < (int) x) {
// maxright = (int) x;
// }
//
// if (maxbottom < (int) y) {
// maxbottom = (int) y;
// }
//
// Region newRegion = new Region();
//
// left = startx;
// top = starty;
// right = x;
// bottom = y;
//
// if (x < startx) {
// left = x;
// right = startx;
// }
//
// if (y < starty) {
// top = y;
// bottom = starty;
// }
//
// Path rectRg = new Path();
// rectRg.moveTo(left, top);
// rectRg.lineTo(left + 1, top);
// rectRg.lineTo(right + 1, bottom);
// rectRg.lineTo(right, bottom);
// rectRg.lineTo(left, top);
//
// newRegion.setPath(rectRg, clip);
// region.op(newRegion, Region.Op.UNION);
// }
// // dash的矩形和当前绘制的矩形的交集
// if (R1.op(region, Region.Op.INTERSECT)) {
// myArray.add(objs);
// }
// break;
// case 3:// 空心矩形
// Rect a = new Rect((int) left, (int) top, (int) right,
// (int) bottom);
//
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
//
// Rect b = new Rect((int) left, (int) top, (int) right,
// (int) bottom);
// Boolean boolRect = Rect.intersects(a, b);
// if (boolRect) {
// myArray.add(objs);
// }
// break;
// case 4:// 实心矩形
// Rect ax = new Rect((int) left, (int) top, (int) right,
// (int) bottom);
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
//
// Rect bx = new Rect((int) left, (int) top, (int) right,
// (int) bottom);
// Boolean boolRectBoolean = Rect.intersects(ax, bx);
// if (boolRectBoolean) {
// myArray.add(objs);
// }
// break;
// case 5:// 空心圆
// Region currentRegionF = new Region();
// currentRegionF.set((int) left, (int) top, (int) right,
// (int) bottom);
//
// // 这条线的region
// Region compareCircle = new Region();
//
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
// compareCircle.set((int) left, (int) top, (int) right,
// (int) bottom);
//
// if (currentRegionF.op(compareCircle, Region.Op.INTERSECT)) {
// myArray.add(objs);
// }
// break;
// case 6:// 实心圆
// Region currentRegionFill = new Region();
// currentRegionFill.set((int) left, (int) top, (int) right,
// (int) bottom);
//
// // 这条线的region
// Region compareCircleFill = new Region();
//
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
// compareCircleFill.set((int) left, (int) top, (int) right,
// (int) bottom);
// if (currentRegionFill
// .op(compareCircleFill, Region.Op.INTERSECT)) {
// myArray.add(objs);
// }
// break;
// }
// }
//
// if (!myArray.isEmpty()) {
// for (int ai = 0; ai < myArray.size(); ai++) {
// list.remove(myArray.get(ai));
// }
// }
//
// list.remove(obj);
// invalidate();
// }
//
// protected void selectDrawRectDashed(Canvas canvas, PointData obj) {
// dashList.clear();
// for (int i = 0; i < list.size(); i++) {
// DashPoint dashObj = new DashPoint();
// PointData objs = list.get(i);
//
// // 虚线矩形必须进行判断
// float left = obj.leftx, top = obj.lefty, right = obj.rightx, bottom =
// obj.righty;
// if (obj.rightx < obj.leftx) {
// left = obj.rightx;
// right = obj.leftx;
// }
//
// if (obj.righty < obj.lefty) {
// top = obj.righty;
// bottom = obj.lefty;
// }
// switch (objs.drawType) {
// case 1:// 画线
// Region currentRegion = new Region();
// currentRegion.set((int) left, (int) top, (int) right,
// (int) bottom);
//
// Region clip1 = new Region();
// clip1.set(0, 0, 2000, 2000);
//
// // 这条线的region
// Region compareRegion = new Region();
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
//
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
//
// Path m_path = new Path();
// m_path.moveTo(left, top);
// m_path.lineTo(left + 1, top);
// m_path.lineTo(right + 1, bottom);
// m_path.lineTo(right, bottom);
// m_path.lineTo(left, top);
// compareRegion.setPath(m_path, clip1);
//
// if (currentRegion.op(compareRegion, Region.Op.INTERSECT)) {
// dashObj.left = left - 4;
// dashObj.top = top - 4;
// dashObj.right = right + 4;
// dashObj.bottom = bottom + 4;
// dashObj.point_index = i;
// dashList.add(dashObj);
// }
// break;
// case 2:// 画笔
// // 当前的这个矩形的Region
// Region R1 = new Region();
// R1.set((int) left, (int) top, (int) right, (int) bottom);
//
// // 定义的一个空的Region
// Region region = new Region();
// region.set(0, 0, 1, 1);
//
// // 为了下面的Region
// Region clip = new Region();
// clip.set(0, 0, 2000, 2000);
//
// int minleft = 0;
// int mintop = 0;
// int maxright = 0;
// int maxbottom = 0;
//
// for (int peni = 0; peni < objs.point.size(); peni++) {
// Point pointvalue = objs.point.get(peni);
// float x = pointvalue.x;
// float y = pointvalue.y;
// float startx = pointvalue.startx;
// float starty = pointvalue.starty;
//
// if (0 == peni) {
// minleft = maxright = (int) objs.leftx;
// mintop = maxbottom = (int) objs.lefty;
// }
//
// if (minleft > (int) x) {
// minleft = (int) x;
// }
//
// if (mintop > (int) y) {
// mintop = (int) y;
// }
//
// if (maxright < (int) x) {
// maxright = (int) x;
// }
//
// if (maxbottom < (int) y) {
// maxbottom = (int) y;
// }
//
// Region newRegion = new Region();
//
// left = startx;
// top = starty;
// right = x;
// bottom = y;
//
// if (x < startx) {
// left = x;
// right = startx;
// }
//
// if (y < starty) {
// top = y;
// bottom = starty;
// }
//
// Path rectRg = new Path();
// rectRg.moveTo(left, top);
// rectRg.lineTo(left + 1, top);
// rectRg.lineTo(right + 1, bottom);
// rectRg.lineTo(right, bottom);
// rectRg.lineTo(left, top);
//
// newRegion.setPath(rectRg, clip);
// region.op(newRegion, Region.Op.UNION);
// }
// // dash的矩形和当前绘制的矩形的交集
// if (R1.op(region, Region.Op.INTERSECT)) {
// dashObj.left = minleft - 4;
// dashObj.top = mintop - 4;
// dashObj.right = maxright + 4;
// dashObj.bottom = maxbottom + 4;
// dashObj.point_index = i;
// dashList.add(dashObj);
// }
// break;
// case 3:// 空心矩形
// Rect a = new Rect((int) left, (int) top, (int) right,
// (int) bottom);
//
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
//
// Rect b = new Rect((int) left, (int) top, (int) right,
// (int) bottom);
// Boolean boolRect = Rect.intersects(a, b);
// if (boolRect) {
// dashObj.left = left - 4;
// dashObj.top = top - 4;
// dashObj.right = right + 4;
// dashObj.bottom = bottom + 4;
// dashObj.point_index = i;
// dashList.add(dashObj);
// }
// break;
// case 4:// 实心矩形
// Rect ax = new Rect((int) left, (int) top, (int) right,
// (int) bottom);
//
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
//
// Rect bx = new Rect((int) left, (int) top, (int) right,
// (int) bottom);
// Boolean boolRectBoolean = Rect.intersects(ax, bx);
// if (boolRectBoolean) {
// dashObj.left = left - 4;
// dashObj.top = top - 4;
// dashObj.right = right + 4;
// dashObj.bottom = bottom + 4;
// dashObj.point_index = i;
// dashList.add(dashObj);
// }
// break;
// case 5:// 空心圆
// Region currentRegionF = new Region();
// currentRegionF.set((int) left, (int) top, (int) right,
// (int) bottom);
//
// // 这条线的region
// Region compareCircle = new Region();
//
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
// compareCircle.set((int) left, (int) top, (int) right,
// (int) bottom);
//
// if (currentRegionF.op(compareCircle, Region.Op.INTERSECT)) {
// dashObj.left = left - 4;
// dashObj.top = top - 4;
// dashObj.right = right + 4;
// dashObj.bottom = bottom + 4;
// dashObj.point_index = i;
// dashList.add(dashObj);
// }
// break;
// case 6:// 实心圆
// Region currentRegionFill = new Region();
// currentRegionFill.set((int) left, (int) top, (int) right,
// (int) bottom);
//
// // 这条线的region
// Region compareCircleFill = new Region();
//
// left = objs.leftx;
// top = objs.lefty;
// right = objs.rightx;
// bottom = objs.righty;
// if (objs.rightx < objs.leftx) {
// left = objs.rightx;
// right = objs.leftx;
// }
//
// if (objs.righty < objs.lefty) {
// top = objs.righty;
// bottom = objs.lefty;
// }
// compareCircleFill.set((int) left, (int) top, (int) right,
// (int) bottom);
// if (currentRegionFill
// .op(compareCircleFill, Region.Op.INTERSECT)) {
// dashObj.left = left - 4;
// dashObj.top = top - 4;
// dashObj.right = right + 4;
// dashObj.bottom = bottom + 4;
// dashObj.point_index = i;
// dashList.add(dashObj);
// }
// break;
// }
// }
//
// if (!dashList.isEmpty()) {
// drawDashRectSelect(canvas, dashList);
// }
//
// list.remove(obj);
// }
//
// protected void drawDashRectSelect(Canvas canvas,
// ArrayList<DashPoint> dashList) {
// Paint p = new Paint();
// PathEffect effects = new DashPathEffect(new float[] { 5, 5, 5, 5 }, 1);
// p.setPathEffect(effects);
// p.setColor(Color.GRAY);// 设置颜色
// p.setStyle(Paint.Style.STROKE);// 设置填满
// for (int i = 0; i < dashList.size(); i++) {
// DashPoint obj = dashList.get(i);
// canvas.drawRect(obj.left, obj.top, obj.right, obj.bottom, p);
// }
// }
//
// protected void drawPen(Canvas canvas, PointData obj) {
// Path mPath = new Path();// 路径对象
// Paint mPaint = new Paint();
// mPaint.setColor(Color.BLACK);// 设置颜色
// mPaint.setAntiAlias(true);// 画笔抗锯齿
// mPaint.setStyle(Paint.Style.STROKE);// 画笔的类型
// mPaint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔圆滑状
// mPaint.setStrokeWidth(2);// 设置线的宽度
//
// mPath.moveTo(obj.leftx, obj.lefty);
//
// for (int i = 0; i < obj.point.size(); i++) {
// Point pointvalue = obj.point.get(i);
// float x = pointvalue.x;
// float y = pointvalue.y;
// float startx = pointvalue.startx;
// float starty = pointvalue.starty;
// mPath.quadTo(startx, starty, x, y);
// }
//
// canvas.drawPath(mPath, mPaint);
// }
//
// protected void drawLine(Canvas canvas, PointData obj) {
// Paint p = new Paint();
// p.setColor(Color.BLACK);
// canvas.drawLine(obj.leftx, obj.lefty, obj.rightx, obj.righty, p);
// }
//
// protected void drawFillRect(Canvas canvas, PointData obj) {
// Paint p = new Paint();
// if (obj.drawType == 3) {// 空心圆
// p.setColor(Color.GRAY);// 设置颜色
// p.setStyle(Paint.Style.STROKE);// 设置填满
// canvas.drawRect(obj.leftx, obj.lefty, obj.rightx, obj.righty, p);
// } else if (obj.drawType == 4) {// 实心圆
// p.setColor(Color.GRAY);// 设置颜色
// p.setStyle(Paint.Style.FILL);// 设置填满
// canvas.drawRect(obj.leftx, obj.lefty, obj.rightx, obj.righty, p);
// }
// }
//
// protected void drawOval(Canvas canvas, PointData obj) {
// Paint p = new Paint();
// if (obj.drawType == 5) {// 空心圆
// p.setColor(Color.GRAY);// 设置颜色
// p.setStyle(Paint.Style.STROKE);// 设置空心
// canvas.drawOval(new RectF(obj.leftx, obj.lefty, obj.rightx,
// obj.righty), p);
// } else if (obj.drawType == 6) {// 实心圆
// p.setColor(Color.GRAY);// 设置颜色
// p.setStyle(Paint.Style.FILL);// 设置填满
// canvas.drawOval(new RectF(obj.leftx, obj.lefty, obj.rightx,
// obj.righty), p);
// }
// }
//
// public void setPonintCollection(PointData o) {
// list.add(o);
// }
//
// protected void setClearArrayList() {
// list.clear();
// moveObj.Init();
// invalidate();
// }
//
// protected void setDashListClear() {
// dashList.clear();
// invalidate();
// }
// }