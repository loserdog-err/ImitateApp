package com.chenantao.JdselectAddressView;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenantao.main.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ━━━━━━神兽出没━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━神兽出没━━━━━━
 * Code is far away from bug with the caonima protecting
 * 　　　　　　　　　神兽保佑,代码无bug
 * <p/>
 * author：Chenantao_gg on 2016/4/20 09:21
 * email：2313570516@qq.com
 * desc:
 */
public class SelectAddressView extends FrameLayout {

	public OnSelectFinishListener mListener;

	private TabLayout mTabLayout;
	private RecyclerView mRecyclerView;
	private MyAdapter mAdapter;
	private List<Address> mAddresss;
	private List<AddressEntity> mProvinces = new ArrayList<>();
	private List<AddressEntity> mCitys = new ArrayList<>();
	private List<AddressEntity> mAreas = new ArrayList<>();
	private String mCurrentProvince;
	private String mCurrentCity;
	private String mCurrentArea;
	private int mShowType = 0;//0 省，1 市，2区
	private RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager
		.VERTICAL, false);

	public SelectAddressView(Context context) {
		this(context, null);
	}

	public SelectAddressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SelectAddressView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.jd_select_view, this, true);
		mAddresss = AddressProvider.getAddressDatas();
		mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
		mRecyclerView = (RecyclerView) findViewById(R.id.rvData);
		initData();
		mTabLayout.setSmoothScrollingEnabled(true);
		mAdapter = new MyAdapter(mProvinces);
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				mShowType = tab.getPosition();
				switch (tab.getPosition()) {
					case 0:
						mAdapter.refreshData(mProvinces);
						break;
					case 1:
						mAdapter.refreshData(mCitys);
						break;
					case 2:
						mAdapter.refreshData(mAreas);
						break;
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			}
		});
//		mTabLayout.findViewById(R.id.tabArea).setVisibility(GONE);
	}

	private void initData() {
		for (Address address : mAddresss) {
			AddressEntity provinceEntity = new AddressEntity();
			provinceEntity.setName(address.getProvince().getName());
			provinceEntity.setSelected(address.getProvince().isSelected());
			mProvinces.add(provinceEntity);
		}
	}

	public void onItemClick(int tabPos) {
		mTabLayout.removeAllTabs();
		if (tabPos == 0) {
			for (AddressEntity entity : mProvinces) {
				entity.setSelected(false);
			}
			mCitys = AddressProvider.getCitys(mAddresss, mCurrentProvince);
			mTabLayout.addTab(mTabLayout.newTab().setText(mCurrentProvince));
			mTabLayout.addTab(mTabLayout.newTab().setText("请选择"));
		} else if (tabPos == 1) {
			for (AddressEntity entity : mCitys) {
				entity.setSelected(false);
			}
			mAreas = AddressProvider.getAreas(mAddresss, mCurrentProvince, mCurrentCity);
			mTabLayout.addTab(mTabLayout.newTab().setText(mCurrentProvince));
			mTabLayout.addTab(mTabLayout.newTab().setText(mCurrentCity));
			mTabLayout.addTab(mTabLayout.newTab().setText("请选择"));
//			mTabLayout.setScrollPosition(2, 0, false);
		} else if (tabPos == 2) {
			for (AddressEntity entity : mAreas) {
				entity.setSelected(false);
			}
			mTabLayout.addTab(mTabLayout.newTab().setText(mCurrentProvince));
			mTabLayout.addTab(mTabLayout.newTab().setText(mCurrentCity));
			mTabLayout.addTab(mTabLayout.newTab().setText(mCurrentArea));
			if (mListener != null) {
				mListener.onSelectFinish(mCurrentProvince, mCurrentCity, mCurrentArea);
			}
		}
		mTabLayout.getTabAt(mTabLayout.getTabCount() - 1).select();
	}

	public void setOnSelectFinishListener(OnSelectFinishListener listener) {
		mListener = listener;
	}

	public interface OnSelectFinishListener {
		void onSelectFinish(String province, String city, String area);
	}

	public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

		private List<AddressEntity> mDatas;


		public MyAdapter(List<AddressEntity> datas) {
			this.mDatas = datas;
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false));
		}

		@Override
		public void onBindViewHolder(final MyViewHolder holder, final int position) {
			final AddressEntity entity = mDatas.get(position);
			holder.mTv.setText(entity.getName());
			holder.mIvSelected.setVisibility(entity.isSelected() ? VISIBLE : GONE);
			holder.itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mShowType == 0) {//省
						mCurrentProvince = holder.mTv.getText().toString();
						onItemClick(0);
					} else if (mShowType == 1) {//市
						mCurrentCity = holder.mTv.getText().toString();
						onItemClick(1);
					} else {//区
						mCurrentArea = holder.mTv.getText().toString();
						onItemClick(2);
						Toast.makeText(getContext(), "省:" + mCurrentProvince + "，市：" + mCurrentCity + ",区：" + mCurrentArea, Toast
							.LENGTH_LONG).show();
					}
					entity.setSelected(true);
				}
			});
		}

		@Override
		public int getItemCount() {
			return mDatas != null ? mDatas.size() : 0;
		}

		public void refreshData(List<AddressEntity> datas) {
			if (datas != mDatas) {
				mDatas = datas;
				notifyDataSetChanged();
			}
		}


		public class MyViewHolder extends RecyclerView.ViewHolder {

			private TextView mTv;
			private ImageView mIvSelected;

			public MyViewHolder(View itemView) {
				super(itemView);
				mTv = (TextView) itemView.findViewById(R.id.tvAddress);
				mIvSelected = (ImageView) itemView.findViewById(R.id.ivSelected);
			}
		}
	}

}
