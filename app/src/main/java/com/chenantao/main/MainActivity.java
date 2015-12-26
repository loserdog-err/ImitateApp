package com.chenantao.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chenantao.bean.User;
import com.chenantao.utils.CatImageLoader;
import com.chenantao.utils.ScreenUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity
{

	private RelativeLayout mRoot;
	private FrameLayout mContainer;
	private float mRotateScale = 0.3f;
	private double mItemAlphaScale = 0.5f;
	private double mItemIvAlphaScale = 0.5f;
	private float mLimitTranslateX = 100;

	private List<User> mDatas;
	private int mIndex = -1;//标识当前读取到数据的第几个下标


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDatas = GenerateData.getDatas();
		//从屏幕的最右边滑动到最左边，最多旋转60度
		int screenWidth = ScreenUtils.getScreenWidth(this);
		mRotateScale = 60 * 1.0f / screenWidth;
		//左滑，透明度最少到0.3f
		mItemAlphaScale = 0.7 * 1.0f / screenWidth / 2;
		//item上面图标透明度的变化，滑动4分之一屏幕的距离便使其完全显示
		mItemIvAlphaScale = 4.0f / screenWidth;
		mRoot = (RelativeLayout) findViewById(R.id.rlRoot);
		mContainer = (FrameLayout) findViewById(R.id.flContainer);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if (hasFocus)
		{
			addViewToBehind();
			addViewToBehind();
		}
	}

	public void addViewToBehind()
	{
		if (mIndex == mDatas.size() - 1)
		{
			return;
		}
		final View item = LayoutInflater.from(this).inflate(R.layout.stack_item, null);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp2px(350), dp2px(491), Gravity
				.CENTER_HORIZONTAL);
		item.setLayoutParams(lp);
//		tv.setText(position + "");
		mContainer.addView(item, 1);
		//初始化item的数据
		User user = mDatas.get(++mIndex);//取出一条数据，并且增加index的下标
		ImageView roundAvatar = (ImageView) item.findViewById(R.id.roundAvatar);
		ImageView blurAvatar = (ImageView) item.findViewById(R.id.blurAvatar);
//		Log.e("cat", "avatar resId:" + user.getAvater());
		CatImageLoader.getInstance().loadImage(user.getAvater(), blurAvatar);
		CatImageLoader.getInstance().loadImage(user.getAvater(), roundAvatar);
		TextView tvUsername = (TextView) item.findViewById(R.id.tvUsername);
		TextView tvSchool = (TextView) item.findViewById(R.id.tvSchool);
		TextView tvMajor = (TextView) item.findViewById(R.id.tvMajor);
		TextView tvEntranceTime = (TextView) item.findViewById(R.id.tvEntranceTime);
		TextView tvSkill = (TextView) item.findViewById(R.id.tvSkill);
		tvUsername.setText(user.getName());
		tvSchool.setText(user.getSchool());
		tvMajor.setText(user.getMajor() + " | " + user.getSchoolLevel());
		tvEntranceTime.setText(user.getEntranceTime());
		tvSkill.setText("装逼 吹牛逼");
		final ImageView ivIgnore = (ImageView) item.findViewById(R.id.ivIgnore);
		final ImageView ivInterested = (ImageView) item.findViewById(R.id.ivInterested);
//		Log.e("cat", "size:" + mContainer.getChildCount());
		item.setPivotX(item.getLayoutParams().width / 2);
		item.setPivotY(item.getLayoutParams().height * 2);
		item.setOnTouchListener(new View.OnTouchListener()
		{
			float touchX, distanceX;

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch (event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						touchX = event.getRawX();
						break;
					case MotionEvent.ACTION_MOVE:
						distanceX = event.getRawX() - touchX;
						item.setRotation(distanceX * mRotateScale);
						//alpha scale 1~0.3
						//item的透明度为从1到0.3
						item.setAlpha(1 - (float) Math.abs(mItemAlphaScale * distanceX));
						if (distanceX < 0)//如果为左滑
						{
							//显示忽略图标,隐藏感兴趣图标
							ivIgnore.setVisibility(View.VISIBLE);
							ivInterested.setVisibility(View.GONE);
							ivIgnore.setAlpha((float) (Math.abs(distanceX) * mItemIvAlphaScale));
						} else
						{
							//显示感兴趣图标,隐藏忽略图标
							ivIgnore.setVisibility(View.GONE);
							ivInterested.setVisibility(View.VISIBLE);
							ivInterested.setAlpha((float) (distanceX * mItemIvAlphaScale));
						}
						break;
					case MotionEvent.ACTION_UP:
						if (Math.abs(distanceX) > mLimitTranslateX)
						{
							View removeView = mContainer.getChildAt(mContainer.getChildCount() -
									1);
							removeView(removeView, distanceX < 0 ? true : false);
							addViewToBehind();
						} else
						{
							//复位
							item.setRotation(0);
							item.setAlpha(1);
							ivIgnore.setAlpha(1.0f);
							ivInterested.setAlpha(1.0f);
							ivIgnore.setVisibility(View.GONE);
							ivInterested.setVisibility(View.GONE);

						}
						break;
				}
				return true;
			}
		});
	}


	/**
	 * 移除view
	 *
	 * @param view
	 * @param left 是否为左滑
	 */
	public void removeView(final View view, boolean left)
	{
		view.animate()
				.alpha(0)
				.rotation(left ? -90 : 90)
				.setDuration(300).setListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				mContainer.removeView(view);
				if (mContainer.getChildCount() == 2)//如果只剩一条item和背景图片
				{
					//隐藏背景图片
					mContainer.getChildAt(0).setVisibility(View.GONE);
				} else if ((mContainer.getChildCount() == 1))
				{
					Toast.makeText(MainActivity.this, "已是最后一页...", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public int dp2px(int dp)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources()
				.getDisplayMetrics());
	}
}
