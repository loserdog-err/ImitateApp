package com.chenantao.stackLayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenantao.main.R;
import com.chenantao.utils.ScreenUtils;
import com.squareup.picasso.Picasso;

/**
 * Created by chenantao on 2016/5/24 12:34.
 * update:
 * desc:
 */
public class StackLayoutActivity extends AppCompatActivity {

	private StackLayout mContainer;

	private double mItemIvAlphaFactor;//控制item上面的图片的透明度变化范围

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stack_layout);
		//item上面图标透明度的变化，滑动4分之一屏幕的距离便使其完全显示
		mItemIvAlphaFactor = 4.0f / ScreenUtils.getScreenWidth(this);
		mContainer = (StackLayout) findViewById(R.id.flContainer);
		mContainer.setAdapter(new StackLayoutAdapter<User>(this, GenerateData.getDatas()) {
			@Override
			public View getView(int pos, View convertView, ViewGroup parent) {
				ViewHolder viewHolder;
				User user = getItem(pos);
				if (convertView == null) {
					Log.e("cat", "inflate new layout");
					convertView = LayoutInflater.from(StackLayoutActivity.this).inflate(R.layout
						.stack_item, null);
					ImageView roundAvatar = (ImageView) convertView.findViewById(R.id.roundAvatar);
					ImageView blurAvatar = (ImageView) convertView.findViewById(R.id.blurAvatar);
					TextView tvUsername = (TextView) convertView.findViewById(R.id.tvUsername);
					TextView tvSchool = (TextView) convertView.findViewById(R.id.tvSchool);
					TextView tvMajor = (TextView) convertView.findViewById(R.id.tvMajor);
					TextView tvEntranceTime = (TextView) convertView.findViewById(R.id
						.tvEntranceTime);
					TextView tvSkill = (TextView) convertView.findViewById(R.id.tvSkill);
					ImageView ivIgnore = (ImageView) convertView.findViewById(R.id.ivIgnore);
					ImageView ivInterested = (ImageView) convertView.findViewById(R.id
						.ivInterested);
					viewHolder = new ViewHolder(roundAvatar, blurAvatar, tvUsername, tvSchool,
						tvMajor, tvEntranceTime, tvSkill, ivIgnore, ivInterested);
					convertView.setTag(viewHolder);
				} else {
					Log.e("cat", "convert view");
					viewHolder = (ViewHolder) convertView.getTag();
				}
				viewHolder.ivIgnore.setVisibility(View.GONE);
				viewHolder.ivInterested.setVisibility(View.GONE);
				Picasso.with(getApplicationContext()).load(user.getAvater())
					.placeholder(R.mipmap.erha).into(viewHolder.blurAvatar);
				Picasso.with(getApplicationContext()).load(user.getAvater()).placeholder(R.mipmap.erha).into(viewHolder.roundAvatar);
				viewHolder.tvUsername.setText(user.getName());
				viewHolder.tvSchool.setText(user.getSchool());
				viewHolder.tvMajor.setText(user.getMajor() + " | " + user.getSchoolLevel());
				viewHolder.tvEntranceTime.setText(user.getEntranceTime());
				viewHolder.tvSkill.setText("装逼 吹牛逼");
				return convertView;
			}
		});
		mContainer.setOnTouchEffectListener(new StackLayout.onTouchEffectListener() {
			@Override
			public void onTouchEffect(View item, MotionEvent event, float distanceX) {
				ImageView ivIgnore = (ImageView) item.findViewById(R.id.ivIgnore);
				ImageView ivInterested = (ImageView) item.findViewById(R.id
					.ivInterested);
				switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
						if (distanceX < 0)//如果为左滑
						{
							//显示忽略图标,隐藏感兴趣图标
							ivIgnore.setVisibility(View.VISIBLE);
							ivInterested.setVisibility(View.GONE);
							ivIgnore.setAlpha((float) (Math.abs(distanceX) * mItemIvAlphaFactor));
						} else//如果为右滑
						{
							//显示感兴趣图标,隐藏忽略图标
							ivIgnore.setVisibility(View.GONE);
							ivInterested.setVisibility(View.VISIBLE);
							ivInterested.setAlpha((float) (distanceX * mItemIvAlphaFactor));
						}
						break;
					case MotionEvent.ACTION_UP:
						if (Math.abs(distanceX) < mContainer.getLimitTranslateX()) {
							//复位
							ivIgnore.setAlpha(1.0f);
							ivInterested.setAlpha(1.0f);
							ivIgnore.setVisibility(View.GONE);
							ivInterested.setVisibility(View.GONE);
						}
						break;
				}
			}
		});
	}
}
