package com.eotu.whiteboard;

import java.util.ArrayList;

public class PointData {
	public float leftx;
	public float lefty;
	public float rightx;
	public float righty;
	public int drawType;
	public ArrayList<Point> point = new ArrayList<Point>();

	public PointData(float leftx, float lefty, float rightx, float righty,
			int drawType) {
		this.leftx = leftx;
		this.lefty = lefty;
		this.rightx = rightx;
		this.righty = righty;
		this.drawType = drawType;
	}

	public PointData() {

	}

	public void Init() {
		this.leftx = 0;
		this.lefty = 0;
		this.rightx = 0;
		this.righty = 0;
		this.drawType = 0;
		this.point.clear();
	}
}