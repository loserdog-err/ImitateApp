package com.chenantao.JdselectAddressView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.chenantao.main.R;

/**
 * Created by chenantao on 2016/5/24 14:09.
 * update:
 * desc:
 */
public class JDSelectAddressActivity extends AppCompatActivity {
	private SelectAddressView mSelectAddressView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jd_select_view);
		mSelectAddressView = (SelectAddressView) findViewById(R.id.selectAddressView);
		mSelectAddressView.setOnSelectFinishListener(new SelectAddressView.OnSelectFinishListener() {
			@Override
			public void onSelectFinish(String province, String city, String area) {
				Toast.makeText(JDSelectAddressActivity.this, "省：" + province + ",市：" + city + ",区：" + area, Toast
					.LENGTH_SHORT).show();
			}
		});
	}
}
