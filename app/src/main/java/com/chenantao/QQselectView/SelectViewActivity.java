package com.chenantao.QQselectView;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chenantao.main.R;

/**
 * Created by chenantao on 2016/5/24 14:00.
 * update:
 * desc:
 */
public class SelectViewActivity extends AppCompatActivity {
	private SelectRecyclerView mRv;
	private RecyclerView.LayoutManager mLayoutManager;
	private RecyclerView.Adapter mAdapter;
	private String[] mDatas = new String[30];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qq_select_views);
		for (int i = 0; i < 10; i++) {
			mDatas[i] = "xixi:" + i;
		}
		mRv = (SelectRecyclerView) findViewById(R.id.rv);
		mLayoutManager = new SnappingLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRv.setLayoutManager(mLayoutManager);
		mAdapter = new SelectRecyclerViewAdapter(this, mDatas);
		mRv.setAdapter(mAdapter);
	}
}
