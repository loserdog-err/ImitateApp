package com.chenantao.stackLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenantao on 2015/12/25.
 */
public class User
{
	private String name;
	private String school;
	private String major;//专业
	private String schoolLevel;//学校等级
	private String entranceTime;
	private String address;
	private int avater;//头像
	private List<String> skills = new ArrayList<>();

	public User(String name, int avater, String school, String major, String schoolLevel, String
			entranceTime,
	            String address, List<String> skills)
	{
		this.name = name;
		this.avater = avater;
		this.school = school;
		this.major = major;
		this.schoolLevel = schoolLevel;
		this.entranceTime = entranceTime;
		this.address = address;
		this.skills = skills;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getAvater()
	{
		return avater;
	}

	public void setAvater(int avater)
	{
		this.avater = avater;
	}

	public String getSchool()
	{
		return school;
	}

	public void setSchool(String school)
	{
		this.school = school;
	}

	public String getMajor()
	{
		return major;
	}

	public void setMajor(String major)
	{
		this.major = major;
	}

	public String getSchoolLevel()
	{
		return schoolLevel;
	}

	public void setSchoolLevel(String schoolLevel)
	{
		this.schoolLevel = schoolLevel;
	}

	public String getEntranceTime()
	{
		return entranceTime;
	}

	public void setEntranceTime(String entranceTime)
	{
		this.entranceTime = entranceTime;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public List<String> getSkills()
	{
		return skills;
	}

	public void setSkills(List<String> skills)
	{
		this.skills = skills;
	}
}
