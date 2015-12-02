package com.zl.redenvelope;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SplashView;
import net.youmi.android.spot.SpotDialogListener;
import net.youmi.android.spot.SpotManager;

public class SplashActivity extends Activity {

	private SplashView splashView;
	private View splash;
	private RelativeLayout splashLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AdManager.getInstance(this).init("3f61afdbb758ca22", "0caef1e42fd1c6fe", true);
		// 第二个参数传入目标activity，或者传入null，改为setIntent传入跳转的intent
		splashView = new SplashView(this, null);
		// 设置是否显示倒数
		splashView.setShowReciprocal(true);
		// 隐藏关闭按钮
		splashView.hideCloseBtn(true);

		Intent intent = new Intent(this, MainActivity.class);
		splashView.setIntent(intent);
		splashView.setIsJumpTargetWhenFail(true);

		splash = splashView.getSplashView();
		setContentView(R.layout.activity_splash);
		splashLayout = ((RelativeLayout) findViewById(R.id.splashview));
		splashLayout.setVisibility(View.GONE);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
		params.addRule(RelativeLayout.ABOVE, R.id.cutline);

		splashLayout.addView(splash, params);
		SpotManager.getInstance(this).showSplashSpotAds(this, splashView,
				new SpotDialogListener() {

					@Override
					public void onShowSuccess() {
						splashLayout.setVisibility(View.VISIBLE);
						splashLayout.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.pic_enter_anim_alpha));
						Log.d("youmisdk", "展示成功");
					}

					@Override
					public void onShowFailed() {
						Log.d("youmisdk", "展示失败");
					}

					@Override
					public void onSpotClosed() {
						Log.d("youmisdk", "展示关闭");
					}

					@Override
					public void onSpotClick() {
						Log.i("YoumiAdDemo", "插屏点击");
					}
				});
	}

}
