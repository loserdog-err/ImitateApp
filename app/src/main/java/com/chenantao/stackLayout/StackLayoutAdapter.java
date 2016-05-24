package com.chenantao.stackLayout;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by chenantao on 2015/12/28.
 */
public abstract class StackLayoutAdapter<T>
{
	private Context mContext;
	private List<T> mDatas;
	private int mCurrentIndex;//目前数据集读到的下标

	public StackLayoutAdapter(Context context, List<T> datas)
	{
		this.mContext = context;
		this.mDatas = datas;
	}

	public int getCount()
	{
		return mDatas.size();
	}

	public T getItem(int pos)
	{
		return mDatas.get(pos);
	}

	public int getCurrentIndex()
	{
		return mCurrentIndex;
	}

	public void setCurrentIndex(int index)
	{
		this.mCurrentIndex = index;
	}

	public abstract View getView(int pos, View convertView, ViewGroup parent);

	public static class ViewHolder
	{
		public ImageView roundAvatar;
		public ImageView blurAvatar;
		public TextView tvUsername;
		public TextView tvSchool;
		public TextView tvMajor;
		public TextView tvEntranceTime;
		public TextView tvSkill;
		public ImageView ivIgnore;
		public ImageView ivInterested;

		public ViewHolder(ImageView roundAvatar, ImageView blurAvatar, TextView tvUsername,
		                  TextView tvSchool, TextView tvMajor, TextView tvEntranceTime, TextView
				                  tvSkill, ImageView ivIgnore, ImageView ivInterested)
		{
			this.roundAvatar = roundAvatar;
			this.blurAvatar = blurAvatar;
			this.tvUsername = tvUsername;
			this.tvSchool = tvSchool;
			this.tvMajor = tvMajor;
			this.tvEntranceTime = tvEntranceTime;
			this.tvSkill = tvSkill;
			this.ivIgnore = ivIgnore;
			this.ivInterested = ivInterested;

		}
	}

}
