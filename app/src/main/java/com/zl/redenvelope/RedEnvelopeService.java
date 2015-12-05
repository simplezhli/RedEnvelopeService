package com.zl.redenvelope;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class RedEnvelopeService extends AccessibilityService {
	static final String TAG = "RobMoney";
	static final String ENVELOPE_TEXT_KEY = "[微信红包]";
	static boolean isReceive = false;

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int eventType = event.getEventType();
		switch (eventType) {
		// 第一步：监听通知栏消息
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				for (CharSequence text : texts) {
					String content = text.toString();
					if (content.contains("[微信红包]")||content.contains("[QQ红包]")) {
						// 模拟打开通知栏消息
						if (event.getParcelableData() != null
								&& event.getParcelableData() instanceof Notification) {
							Notification notification = (Notification) event
									.getParcelableData();
							PendingIntent pendingIntent = notification.contentIntent;
							try {
								pendingIntent.send();
							} catch (CanceledException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			break;
		// 第二步：监听是否进入微信红包消息界面

		case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:// 界面内容改变调用
		case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:// 界面状态变化调用
			String className = event.getClassName().toString();
			if(className == null){
				return;
			}

			if (className.equals("com.tencent.mm.ui.LauncherUI")
					|| className.equals("com.flamy.meizu.laucher.Laucher")) {// 微信消息列表和聊天界面，开始抢红包

				getPacket();

			} else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {// 微信拆红包界面

				openPacket();// 开始打开红包

			} else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {// 微信拆完红包后看详细的纪录界面

				performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);// 正常领取，到达详细页就返回上一页面。

			}else if( className.equals("com.tencent.mobileqq.activity.SplashActivity")){// QQ消息列表和聊天界面，开始抢红包

				openPacketQQ();

			}else if(className.equals("cooperation.qwallet.plugin.QWalletPluginProxyActivity")){// QQ拆红包界面

				performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);

			}
			break;

		}
	}

	private void openPacketQQ() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo != null) {
			List<AccessibilityNodeInfo> list = nodeInfo
					.findAccessibilityNodeInfosByText("点击拆开");
			if (list.size() == 0) {
				intent();
				return;
			}
			for (AccessibilityNodeInfo n : list) {
				n.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
			}
		}
	}

	/**
	 * 到达拆红包界面，有“拆红包”字段则点击拆除，没有则返回（一般红包过期会没有此字段） 返回后又到达聊天界面，进入此界面判断，方便继续抢红包。
	 */

	private void openPacket() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo != null) {
			List<AccessibilityNodeInfo> list = nodeInfo
					.findAccessibilityNodeInfosByText("拆红包");
			if (list.size() == 0) {
				performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
				return;
			}
			for (AccessibilityNodeInfo n : list) {
				n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			}
		}

	}

	/**
	 * 两种情况
	 * 
	 * 1.（在消息列表界面或者说是主界面）获取是否有“领取红包”字段（一般在此页面为“[微信红包]”字段）
	 * 为空，就判断是否有“[微信红包]”字段，有就点击进去，否则返回。不为空则为第二种情况。
	 * 
	 * 2.（在聊天界面）获取是否有“领取红包”字段，有点击。没有就到了返回操作（即没有 “领取红包”字段，又没有“[微信红包]”字段）
	 * 
	 * 防止重复领取：点击后做一个点击标记，返回后恢复，则不再点击领取。
	 * 
	 * 此方法在是对界面进行判断，逐级返回，最终返回桌面，重新开始。
	 * 
	 * */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void getPacket() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			Log.w(TAG, "rootWindow为空");
			return;
		}
		List<AccessibilityNodeInfo> list = nodeInfo
				.findAccessibilityNodeInfosByText("领取红包");
		if (list.isEmpty()) {
			list = nodeInfo.findAccessibilityNodeInfosByText(ENVELOPE_TEXT_KEY);
			if (list.size() == 0) {
				intent();
				return;
			}
			for (AccessibilityNodeInfo n : list) {
				n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				return;
			}
		} else {
			// 从最新的红包开始领
			for (int i = list.size() - 1; i >= 0; i--) {
				AccessibilityNodeInfo parent = list.get(i).getParent();
				if (parent != null) {
					if (!isReceive) {
						parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
						isReceive = true;
						return;// 暂时支持一次领取一个，多个可能会有影响。（正常情况下足够）
					} else {
						isReceive = false;
						intent();
						return;
					}
				}
			}
		}
	}

	private void intent(){
		Intent intent = new Intent(this,MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		intent.addCategory(Intent.CATEGORY_DEFAULT);  
		startActivity(intent);
	}
	@Override
	public AccessibilityNodeInfo getRootInActiveWindow() {
		// TODO Auto-generated method stub
		return super.getRootInActiveWindow();
	}

	@Override
	public void onInterrupt() {
		Toast.makeText(this, "抢红包服务关闭", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected() {
		AccessibilityServiceInfo info = getServiceInfo();
		info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
		info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		info.notificationTimeout = 100;
		info.packageNames = new String[]{"com.tencent.mm", "com.tencent.mobileqq"};
		setServiceInfo(info);
		super.onServiceConnected();
		Toast.makeText(this, "抢红包服务开始", Toast.LENGTH_SHORT).show();

	}
}
