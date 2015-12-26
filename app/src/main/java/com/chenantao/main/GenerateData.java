package com.chenantao.main;

import com.chenantao.bean.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenantao on 2015/12/25.
 */
public class GenerateData
{
	public static List<User> getDatas()
	{
		List<User> users = new ArrayList<>();
		User u1 = new User("Cat_gg", R.mipmap.avatar, "学霸学院", "计算机应用", "专科", "2013级入学", "广州", new
				ArrayList<String>());
		User u2 = new User("Xixihehe", R.mipmap.avatar1, "哇哈哈学院", "tf专业", "本科", "2013级入学", "广州",
				new
				ArrayList<String>());
		User u3 = new User("蔡秋", R.mipmap.avatar2, "球赛学院", "文明看球专科", "专科", "2013级入学", "广州", new
				ArrayList<String>());
		User u4 = new User("陈黑", R.mipmap.avatar3, "偷渡学院", "染黑专业", "专科", "2013级入学", "广州", new
				ArrayList<String>());
		User u5 = new User("咸鱼", R.mipmap.avatar4, "海底学院", "咸鱼系", "专科", "2013级入学", "广州", new
				ArrayList<String>());
		User u6 = new User("夹夹", R.mipmap.avatar5, "牛逼学院", "夹夹", "专科", "2013级入学", "广州", new
				ArrayList<String>());
		users.add(u1);
		users.add(u2);
		users.add(u3);
		users.add(u4);
		users.add(u5);
		users.add(u6);
		return users;
	}

}
