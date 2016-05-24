package com.chenantao.JdselectAddressView;

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
 * author：Chenantao_gg on 2016/4/20 10:29
 * email：2313570516@qq.com
 * desc:
 */
public class Address {
	private AddressEntity province;
	private List<CityEntity> citys;

	public AddressEntity getProvince() {
		return province;
	}

	public void setProvince(AddressEntity province) {
		this.province = province;
	}

	public List<CityEntity> getCitys() {
		return citys;
	}

	public void setCitys(List<CityEntity> citys) {
		this.citys = citys;
	}

	public static class CityEntity {
		AddressEntity city;
		private List<AreaEntity> areas;

		public AddressEntity getCity() {
			return city;
		}

		public void setCity(AddressEntity city) {
			this.city = city;
		}

		public List<AreaEntity> getAreas() {
			return areas;
		}

		public void setAreas(List<AreaEntity> areas) {
			this.areas = areas;
		}

		public static class AreaEntity {
			AddressEntity area;

			public AddressEntity getArea() {
				return area;
			}

			public void setArea(AddressEntity area) {
				this.area = area;
			}
		}
	}
}
