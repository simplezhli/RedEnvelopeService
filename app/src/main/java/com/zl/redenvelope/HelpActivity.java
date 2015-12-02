package com.zl.redenvelope;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.spot.SpotManager;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		showBanner();

		TextView back = (TextView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	@Override
	protected void onDestroy() {
		SpotManager.getInstance(this).onDestroy();
		super.onDestroy();
	}
	@Override
	protected void onStop() {
		// 如果不调用此方法，则按home键的时候会出现图标无法显示的情况。
		SpotManager.getInstance(this).onStop();
		super.onStop();
	}
	private void showBanner() {

		// 实例化LayoutParams(重要)
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		// 设置广告条的悬浮位置
		layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT; // 这里示例为右下角
		// 实例化广告条
		AdView adView = new AdView(HelpActivity.this, AdSize.FIT_SCREEN);
		// 调用Activity的addContentView函数

		((Activity) HelpActivity.this).addContentView(adView, layoutParams);

		setSpotAd();
	}



	private void setSpotAd() {

		// 插屏出现动画效果，0:ANIM_NONE为无动画，1:ANIM_SIMPLE为简单动画效果，2:ANIM_ADVANCE为高级动画效果
		SpotManager.getInstance(this).setAnimationType(
				SpotManager.ANIM_ADVANCE);
		// 设置插屏动画的横竖屏展示方式，如果设置了横屏，则在有广告资源的情况下会是优先使用横屏图。
		SpotManager.getInstance(this).setSpotOrientation(
				SpotManager.ORIENTATION_PORTRAIT);
		mHandler.sendEmptyMessageDelayed(0,3000);

	}
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// 展示插播广告，可以不调用loadSpot独立使用
			SpotManager.getInstance(HelpActivity.this).showSpotAds(HelpActivity.this);

			super.handleMessage(msg);
		}
	};
}
