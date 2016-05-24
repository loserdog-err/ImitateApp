package com.chenantao.qqRocket;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.chenantao.main.R;
import com.chenantao.utils.ScreenUtils;

/**
 * Created by chenantao on 2016/5/17 15:26.
 * update:
 * desc:
 */
public class QQRocketService extends Service {
	private static final String TAG = "QQRocketService";
	WindowManager mWindowManager;
	View mBottomView;
	WindowManager.LayoutParams mBottomLayoutParams;

	private View mCircleView;
	private WindowManager.LayoutParams mCircleViewLayoutParams;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				addCircleView();
			}
		}, 1000);
	}

	public void addCircleView() {
		mCircleViewLayoutParams = new WindowManager.LayoutParams();
		mCircleViewLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		mCircleViewLayoutParams.format = PixelFormat.RGBA_8888;
		mCircleViewLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		mCircleViewLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		mCircleViewLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mCircleViewLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mCircleViewLayoutParams.x = ScreenUtils.getScreenWidth(getApplicationContext()) / 2;
		mCircleViewLayoutParams.y = ScreenUtils.getScreenHeight(getApplicationContext()) / 2;
		mCircleView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.widget_circle, null);
		mWindowManager.addView(mCircleView, mCircleViewLayoutParams);
		mCircleView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
					case MotionEvent.ACTION_DOWN:
						ImageView iv = (ImageView) mCircleView.findViewById(R.id.ivCircle);
						iv.setImageResource(R.mipmap.desktop_rocket_launch_1);
						break;
					case MotionEvent.ACTION_MOVE:
						int rawX = (int) event.getRawX();
						int rawY = (int) event.getRawY() - ScreenUtils.getStatusHeight(getApplicationContext());
//						Log.d(TAG, "onTouchX: " + rawX + ",onTouchY: " + rawY);
						mCircleViewLayoutParams.x = rawX - mCircleView.getMeasuredWidth() / 2;
						mCircleViewLayoutParams.y = rawY - mCircleView.getMeasuredHeight() / 2;
						mWindowManager.updateViewLayout(mCircleView, mCircleViewLayoutParams);
						if (isBottom(mCircleViewLayoutParams.x, mCircleViewLayoutParams.y)) {
							showBottomView();
						} else {
							hideBottomView();
						}
						break;
					case MotionEvent.ACTION_UP:
						int y = (int) event.getRawY() - ScreenUtils.getStatusHeight(getApplicationContext())
							- mCircleView.getMeasuredHeight() / 2;
						if (isBottom(0, y)) {
							ValueAnimator animator = ValueAnimator.ofInt(y, 0 - mCircleView.getMeasuredHeight());
							animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
								@Override
								public void onAnimationUpdate(ValueAnimator animation) {
									int y = (int) animation.getAnimatedValue();
									mCircleViewLayoutParams.y = y;
									mWindowManager.updateViewLayout(mCircleView, mCircleViewLayoutParams);
								}
							});
							animator.addListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									hideBottomView();
									ImageView iv = (ImageView) mCircleView.findViewById(R.id.ivCircle);
									iv.setVisibility(View.GONE);
									new Handler().postDelayed(new Runnable() {
										@Override
										public void run() {
											reset();
										}
									}, 2000);
								}

								@Override
								public void onAnimationCancel(Animator animation) {
								}

								@Override
								public void onAnimationRepeat(Animator animation) {
								}
							});
							animator.setInterpolator(new AccelerateInterpolator());
							animator.setDuration(1000);
							animator.start();
						}else{
							reset();
						}
						break;
				}
				return false;
			}
		});
	}

	public void reset() {
		ImageView iv = (ImageView) mCircleView.findViewById(R.id.ivCircle);
		iv.setVisibility(View.VISIBLE);
		iv.setImageResource(R.mipmap.ic_launcher);
		mCircleViewLayoutParams.x = ScreenUtils.getScreenWidth(getApplicationContext()) / 2;
		mCircleViewLayoutParams.y = ScreenUtils.getScreenHeight(getApplicationContext()) / 2;
		mWindowManager.updateViewLayout(mCircleView, mCircleViewLayoutParams);
	}
	public boolean isBottom(int x, int y) {
		return y > 700;
	}

	public void showBottomView() {
		if (mBottomView == null) {
			mBottomLayoutParams = new WindowManager.LayoutParams();
			mBottomLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
			mBottomLayoutParams.format = PixelFormat.RGBA_8888;
			mBottomLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			mBottomLayoutParams.gravity = Gravity.BOTTOM;
			mBottomLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
			mBottomLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
			mBottomView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.widget_bottom, null);
			ImageView iv = (ImageView) mBottomView.findViewById(R.id.iv);
			iv.setBackgroundResource(R.drawable.bottom_anim);
		}
		if (mBottomView.getParent() == null) {
			mWindowManager.addView(mBottomView, mBottomLayoutParams);
			ImageView iv = (ImageView) mBottomView.findViewById(R.id.iv);
			AnimationDrawable animationDrawable = (AnimationDrawable) iv.getBackground();
			animationDrawable.start();
		}
	}

	public void hideBottomView() {
		if (mWindowManager != null && mBottomView != null && mBottomView.getParent() != null) {
			mWindowManager.removeView(mBottomView);
			AnimationDrawable animationDrawable = mBottomView.findViewById(R.id.iv).getBackground() == null ?
				null : (AnimationDrawable) mBottomView.findViewById(R.id.iv).getBackground();
			if (animationDrawable != null) {
				animationDrawable.stop();
			}
		}
	}
}
