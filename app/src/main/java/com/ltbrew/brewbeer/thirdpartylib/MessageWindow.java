package com.ltbrew.brewbeer.thirdpartylib;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltbrew.brewbeer.R;

/**
 * 弹窗辅助类
 * 
 * @ClassName WindowUtils
 * 
 * 
 */
@SuppressLint({ "ClickableViewAccessibility", "InflateParams" })
public class MessageWindow {

	private final String TAG = "MessageWindow";
	private boolean isShown = false;
	private Context mContext = null;
	private View mView = null;
	private WindowManager mWindowManager = null;
	private LayoutParams params;
	private View viewMessage;
	private static AnimationDrawable adRecord = null;
	private ImageView ivPlayer;
	private ImageView ivLabelSOS;
	private TextView tvtime;
	private TextView tvMessage;
	private TextView tvTitle;
	private TextView tvLook;
	private MediaPlayer mpRecord;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			hidePopupWindow();
		}
	};
	private OnCloseWindowListener onCloseWindowListener;

	/**
	 * 显示弹出框
	 * 
	 * @param context
	 */
	public MessageWindow(Context context) {
		// 获取应用的Context
		mContext = context;
		// 获取WindowManager
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

		params = new LayoutParams();

		// 类型
//		params.type = LayoutParams.TYPE_SYSTEM_ALERT;

		// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

		// 设置flag

		int flags = LayoutParams.FLAG_ALT_FOCUSABLE_IM | LayoutParams.FLAG_NOT_FOCUSABLE
		// | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
		;
		// 如果不设置WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
		params.flags = flags;
		// 不设置这个弹出框的透明遮罩显示为黑色
//		params.format = PixelFormat.TRANSLUCENT;
		// FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
		// 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
		// 不设置这个flag的话，home页的划屏会有问题

		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.WRAP_CONTENT;

		params.gravity = Gravity.TOP;
	}

	/**
	 * 弹出对话框
	 */

	public MessageWindow showMessageWindow(String title, String message) {
		if (isShown) {
			return this;
		}
		isShown = true;
		tvTitle.setText(title);
		tvMessage.setText(message);

		mWindowManager.addView(mView, params);
		viewMessage.setVisibility(View.VISIBLE);
		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_popup_open);
		viewMessage.startAnimation(animation);
		return this;
	}

	/**
	 * 隐藏对话框
	 */
	public void hidePopupWindow() {
		if (isShown && null != mView) {
			Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_popup_close);
			viewMessage.startAnimation(animation);
			viewMessage.postDelayed(new Runnable() {

				@Override
				public void run() {
					viewMessage.setVisibility(View.GONE);
					try {
						mWindowManager.removeView(mView);
					} catch (Exception e) {
					}
					isShown = false;
				}
			}, 200);
		}
	}

	private View setUpView() {

		View view = LayoutInflater.from(mContext).inflate(R.layout.layout_message_window, null);

		viewMessage = view.findViewById(R.id.message_window);
		TextView tvClose = (TextView) view.findViewById(R.id.tv_close);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvMessage = (TextView) view.findViewById(R.id.tv_message);

		tvClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(onCloseWindowListener != null)
					onCloseWindowListener.onCloseWindow();
				hidePopupWindow();
			}
		});
		// 可滑动删除
		setTouchEvent(view);
		return view;
	}

	private void setTouchEvent(View view) {

		view.setOnTouchListener(new OnTouchListener() {

			private float viewX;
			private float rawX;
			private int left;
			private int top;
			private int right;
			private int bottom;
			private int translateX;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						viewX = event.getX();
						left = viewMessage.getLeft();
						top = viewMessage.getTop();
						right = viewMessage.getRight();
						bottom = viewMessage.getBottom();
						break;
					case MotionEvent.ACTION_MOVE:
						rawX = event.getX();
						translateX = (int) (rawX - viewX);
						viewMessage.layout(left + translateX, top, right + translateX, bottom);
						break;
					case MotionEvent.ACTION_UP:
						int translateLimitX = (right - left) / 5 * 3;
						if (translateX >= translateLimitX) {
							hidePopupWindow();
						} else if (translateX != 0) {
							TranslateAnimation anim = new TranslateAnimation(left + translateX, left, 0, 0);
							anim.setDuration(200);
							viewMessage.startAnimation(anim);
							viewMessage.layout(left, top, right, bottom);
						}
						break;
					default:
						break;
				}
				return false;
			}
		});
	}

	public MessageWindow setupWindow() {
		mView = setUpView();
		return this;
	}

	public MessageWindow setOnCloseWindowListener(OnCloseWindowListener onCloseWindowListener){
		this.onCloseWindowListener = onCloseWindowListener;
		return this;
	}


	public interface OnCloseWindowListener{
		void onCloseWindow();
	}

}