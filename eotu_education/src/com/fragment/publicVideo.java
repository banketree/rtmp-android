package com.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.treecore.activity.fragment.TFragment;

public class publicVideo extends TFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Button view = new Button(getTActivity());
		view.setText("free");
		return view;
	}
}
