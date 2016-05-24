package com.chenantao.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chenantao.JdselectAddressView.JDSelectAddressActivity;
import com.chenantao.qqRocket.QQRocketService;
import com.chenantao.stackLayout.StackLayoutActivity;

public class MainActivity extends AppCompatActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	//qq 安全管家小火箭
	public void qqRocket(View view) {
		Intent intent = new Intent(this, QQRocketService.class);
		startService(intent);
		finish();
	}

	//京东选择地址
	public void selectAddress(View view) {
		Intent intent = new Intent(this, JDSelectAddressActivity.class);
		startActivity(intent);
	}

	//大街堆叠布局
	public void stackLayout(View view) {
		Intent intent = new Intent(this, StackLayoutActivity.class);
		startActivity(intent);
	}

}
