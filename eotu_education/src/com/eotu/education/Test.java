package com.eotu.education;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.eotu.core.CoreFragmentActivity;
import com.eotu_education.R;
import com.fragment.SettingFragment;
import com.fragment.buyedCourse;
import com.fragment.nobuyedCourse;
import com.fragment.publicVideo;
import com.treecore.activity.fragment.TFragment;

public class Test extends CoreFragmentActivity implements OnClickListener {
	private TFragment mBuyedFragment, mNobuyFragment, mFreeFragment,
			mSettingFragment, mCurFragment;

	private ImageView mFreeImageView, mBuyedImageView, mNobuyImageView,
			mSettingImageView, mCurImageView;

	private TextView mTitleTextView;
	private int mCurIndex = 0;

	@Override
	public void processEventByInner(Intent intent) {
		super.processEventByInner(intent);
	}

	@Override
	public void processEventByProcess(Intent intent) {
		super.processEventByProcess(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		onInitView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mFreeImageView || v == mBuyedImageView || v == mNobuyImageView
				|| v == mSettingImageView) {
			changeStatus(v.getId());
		}
	}

	private void onInitView() {
		mFreeImageView = (ImageView) findViewById(R.id.ImageView_free);
		mBuyedImageView = (ImageView) findViewById(R.id.ImageView_buyed);
		mNobuyImageView = (ImageView) findViewById(R.id.ImageView_nobuy);
		mSettingImageView = (ImageView) findViewById(R.id.ImageView_setting);
		mCurImageView = (ImageView) findViewById(R.id.ImageView_current);
		mTitleTextView = (TextView) findViewById(R.id.TextView_title);

		mFreeImageView.setOnClickListener(this);
		mBuyedImageView.setOnClickListener(this);
		mNobuyImageView.setOnClickListener(this);
		mSettingImageView.setOnClickListener(this);

		changeStatus(mFreeImageView.getId());
	}

	@Override
	public void switchContent(int layId, TFragment from, TFragment to) {
		if (mCurFragment == to)
			return;
		mCurFragment = to;
		super.switchContent(layId, from, to);
	}

	@Override
	public void switchContent(int layId, TFragment fragment) {
		if (mCurFragment == fragment)
			return;

		mCurFragment = fragment;
		super.switchContent(layId, fragment);
	}

	private void changeStatus(int id) {
		Animation animation = null;

		int curIndex = mCurIndex;
		if (id == R.id.ImageView_free) {
			mFreeImageView.setSelected(true);
			mBuyedImageView.setSelected(false);
			mNobuyImageView.setSelected(false);
			mSettingImageView.setSelected(false);
			mCurIndex = 0;
			mTitleTextView.setText("免费教程");

			if (mFreeFragment == null)
				mFreeFragment = new publicVideo();

			if (mCurFragment == null)
				switchContent(R.id.FrameLayout_content, mFreeFragment);
			else
				switchContent(R.id.FrameLayout_content, mCurFragment,
						mFreeFragment);
		} else if (id == R.id.ImageView_buyed) {
			mFreeImageView.setSelected(false);
			mBuyedImageView.setSelected(true);
			mNobuyImageView.setSelected(false);
			mSettingImageView.setSelected(false);
			mCurIndex = 1;
			mTitleTextView.setText("已购教程");

			if (mBuyedFragment == null)
				mBuyedFragment = new buyedCourse();

			switchContent(R.id.FrameLayout_content, mCurFragment,
					mBuyedFragment);
		} else if (id == R.id.ImageView_nobuy) {
			mFreeImageView.setSelected(false);
			mBuyedImageView.setSelected(false);
			mNobuyImageView.setSelected(true);
			mSettingImageView.setSelected(false);
			mCurIndex = 2;
			mTitleTextView.setText("未购教程");

			if (mNobuyFragment == null)
				mNobuyFragment = new nobuyedCourse();

			switchContent(R.id.FrameLayout_content, mCurFragment,
					mNobuyFragment);
		} else if (id == R.id.ImageView_setting) {
			mFreeImageView.setSelected(false);
			mBuyedImageView.setSelected(false);
			mNobuyImageView.setSelected(false);
			mSettingImageView.setSelected(true);
			mCurIndex = 3;
			mTitleTextView.setText("设置");

			if (mSettingFragment == null)
				mSettingFragment = new SettingFragment();

			switchContent(R.id.FrameLayout_content, mCurFragment,
					mSettingFragment);
		}

		animation = new TranslateAnimation(
				curIndex
						* (findViewById(R.id.RelativeLayout_bottom).getWidth() / 4),
				mCurIndex
						* (findViewById(R.id.RelativeLayout_bottom).getWidth() / 4),
				0, 0);
		animation.setFillAfter(true);// True:图片停在动画结束位置
		animation.setDuration(150);
		mCurImageView.startAnimation(animation);
	}
}
