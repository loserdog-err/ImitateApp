package com.chenantao.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.chenantao.main.R;
import com.chenantao.utils.ScreenUtils;
import com.chenantao.view.adapter.StackLayoutAdapter;

import java.util.LinkedList;

/**
 * Created by chenantao on 2015/12/28.
 */
public class StackLayout extends FrameLayout
{
	private StackLayoutAdapter mAdapter;

	private int mContentWidth = 350;//内容区域的宽度 dp
	private int mContentHeight = 470;//内容区域的高度 dp

	private float mRotateFactor;//控制item旋转范围
	private double mItemAlphaFactor;//控制item透明度变化范围

	private int mLimitTranslateX = 100;//限制移动距离，当超过这个距离的时候，删除该item

	private LinkedList<View> mScrapViews = new LinkedList<>();

	private onTouchEffectListener mOnTouchEffectListener;

	public StackLayout(Context context)
	{
		this(context, null);
	}

	public StackLayout(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public StackLayout(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.StackLayout);
		mContentWidth = t.getDimensionPixelSize(R.styleable.StackLayout_contentWidth, ScreenUtils
				.dp2px(mContentWidth, getContext()));
		mContentHeight = t.getDimensionPixelSize(R.styleable.StackLayout_contentHeight,
				ScreenUtils.dp2px(mContentHeight, getContext()));
		int screenWidth = ScreenUtils.getScreenWidth(getContext());
		mRotateFactor = 60 * 1.0f / screenWidth;
		//左滑，透明度最少到0.1f
		mItemAlphaFactor = 0.9 * 1.0f / screenWidth / 2;
	}

	public void setAdapter(StackLayoutAdapter adapter)
	{
		this.mAdapter = adapter;
		//最多加载两条数据
		int itemCount = adapter.getCount();
		int loadCount = itemCount > 2 ? 2 : itemCount;
		for (int i = 0; i < loadCount; i++)
		{
			addViewToFirst();
		}
	}

	/**
	 * 将item添加到最后的位置
	 */
	public void addViewToFirst()
	{
		makeAndAddView(0);
	}

	/**
	 * 带动画效果删除指定下标的view
	 *
	 * @param view    要删除的view
	 * @param isLeft 是否为左滑
	 * @return
	 */
	public View removeViewWithAnim(final View view, boolean isLeft)
	{
//		final View view = getChildAt(pos);
		view.animate()
				.alpha(0)
				.rotation(isLeft ? -90 : 90)
				.setDuration(400).setListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				removeView(view);
				//移除view后将view添加到我们的废弃view的list中
				resetItem(view);//记得重置状态，否则复用的时候会看不到view
				mScrapViews.add(view);
				if (getChildCount() == 0)//如果只剩一条item的时候
				{
					Toast.makeText(getContext(), "已是最后一页...", Toast.LENGTH_SHORT).show();
				}
			}
		});
		return view;
	}


	/**
	 * 获得一个view并且添加到布局的指定位置
	 *
	 * @param pos 要添加到布局的下标
	 */
	private void makeAndAddView(int pos)
	{
		if (mAdapter.getCurrentIndex() == mAdapter.getCount())
		{
			return;//没有更多数据
		}
		View item = obtainView(mAdapter.getCurrentIndex());
		addView(item, pos);
		//增加数据集的下标
		mAdapter.setCurrentIndex(mAdapter.getCurrentIndex() + 1);
	}

	/**
	 * 得到一个item布局
	 *
	 * @return
	 */
	private View obtainView(int pos)
	{
		//先尝试从废弃缓存中取出view
		View scrapView = mScrapViews.size() > 0 ? mScrapViews.removeLast() : null;
		View item = mAdapter.getView(pos, scrapView, this);
		if (item != scrapView)
		{
			//代表view布局变化了，inflate了新的布局
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mContentWidth,
					mContentHeight, Gravity.CENTER_HORIZONTAL);
			item.setLayoutParams(lp);
			//初始化事件
			initEvent(item);
		}
		return item;
	}

	/**
	 * 初始化StackLayout的默认事件
	 *
	 * @param item
	 */
	private void initEvent(final View item)
	{
		//设置item的重心，主要是旋转的中心
		item.setPivotX(item.getLayoutParams().width / 2);
		item.setPivotY(item.getLayoutParams().height * 2);
		item.setOnTouchListener(new View.OnTouchListener()
		{
			float touchX, distanceX;//手指按下时的坐标以及手指在屏幕移动的距离

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
						if (mOnTouchEffectListener != null)
							mOnTouchEffectListener.onTouchEffect(item, event, distanceX);
						item.setRotation(distanceX * mRotateFactor);
						//alpha scale 1~0.1
						//item的透明度为从1到0.1
						item.setAlpha(1 - (float) Math.abs(mItemAlphaFactor * distanceX));
						break;
					case MotionEvent.ACTION_UP:
						if (mOnTouchEffectListener != null)
							mOnTouchEffectListener.onTouchEffect(item, event, distanceX);
						if (Math.abs(distanceX) > mLimitTranslateX)
						{
							//移除view
							removeViewWithAnim(item, distanceX < 0);
							addViewToFirst();
						} else
						{
							//复位
							resetItem(item);
						}
						break;
				}
				return true;
			}
		});
	}

	private void resetItem(View item)
	{
		item.setRotation(0);
		item.setAlpha(1);
	}
	public int getLimitTranslateX()
	{
		return mLimitTranslateX;
	}

	public void setOnTouchEffectListener(onTouchEffectListener listener)
	{
		this.mOnTouchEffectListener = listener;
	}

	/**
	 * 触摸item的回调事件
	 */
	public interface onTouchEffectListener
	{
		void onTouchEffect(View item, MotionEvent event, float distanceX);
	}
}
