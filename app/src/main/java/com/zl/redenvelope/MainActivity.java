package com.zl.redenvelope;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.spot.SpotManager;

public class MainActivity extends Activity {

	private int width;
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 应用运行时，保持屏幕高亮，不锁屏

		showBanner();

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		width = metric.widthPixels;

		TextView textView = (TextView) findViewById(R.id.textView);

		ImageView imageView = (ImageView) findViewById(R.id.image);
		FrameLayout.LayoutParams rlp = new FrameLayout.LayoutParams(
				width / 10 * 8, LinearLayout.LayoutParams.MATCH_PARENT);
		rlp.setMargins(width / 10, 0, width / 10, 0);
		imageView.setLayoutParams(rlp);
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.baidu.com");
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, getResources()
						.getText(R.string.app_name)));

			}
		});
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						HelpActivity.class);
				startActivity(intent);

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
	@Override
	protected void onResume() {

		boolean flag = isAccessibilitySettingsOn(this);
		if (!flag) {
			if (dialog == null || !dialog.isShowing()) {
				showDialog();
			}
		}
		super.onResume();
	}

	private void showBanner() {
		// 加载插播资源
		SpotManager.getInstance(this).loadSpotAds();
		// 实例化LayoutParams(重要)
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		// 设置广告条的悬浮位置
		layoutParams.gravity = Gravity.TOP | Gravity.RIGHT; // 这里示例为右下角
		// 实例化广告条
		AdView adView = new AdView(MainActivity.this, AdSize.FIT_SCREEN);
		// 调用Activity的addContentView函数

		((Activity) MainActivity.this).addContentView(adView, layoutParams);
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
			SpotManager.getInstance(MainActivity.this).showSpotAds(MainActivity.this);

			super.handleMessage(msg);
		}
	};



	private Dialog dialog;

	@SuppressLint("InflateParams")
	private void showDialog() {
		final View view = this.getLayoutInflater().inflate(
				R.layout.dialog_setting, null);
		dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
		dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wl = window.getAttributes();
		DisplayMetrics metric = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metric);
		wl.width = width / 10 * 7;
		wl.height = LayoutParams.WRAP_CONTENT;
		ImageView open = (ImageView) view.findViewById(R.id.open);
		open.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					// 打开系统设置中辅助功能
					Intent intent = new Intent(
							Settings.ACTION_ACCESSIBILITY_SETTINGS);
					startActivity(intent);
					dialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围不解散
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

	}

	/**
	 * 
	 * 判断辅助服务是否开启
	 * 
	 * */
	private boolean isAccessibilitySettingsOn(Context mContext) {
		int accessibilityEnabled = 0;
		final String service = "com.zl.redenvelope/com.zl.redenvelope.RedEnvelopeService";// 包名+"/"+服务名
		boolean accessibilityFound = false;
		try {
			accessibilityEnabled = Settings.Secure.getInt(mContext
					.getApplicationContext().getContentResolver(),
					Settings.Secure.ACCESSIBILITY_ENABLED);
		} catch (SettingNotFoundException e) {
			Log.e(TAG,
					"Error finding setting, default accessibility to not found: "
							+ e.getMessage());
		}
		TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(
				':');

		if (accessibilityEnabled == 1) {
			Log.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
			String settingValue = Settings.Secure.getString(mContext
					.getApplicationContext().getContentResolver(),
					Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			if (settingValue != null) {
				TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
				splitter.setString(settingValue);
				while (splitter.hasNext()) {
					String accessabilityService = splitter.next();

					if (accessabilityService.equalsIgnoreCase(service)) {
						Log.v(TAG,
								"We've found the correct setting - accessibility is switched on!");
						return true;
					}
				}
			}
		} else {
			Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
		}

		return accessibilityFound;
	}

}
