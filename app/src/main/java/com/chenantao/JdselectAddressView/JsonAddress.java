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
 * author：Chenantao_gg on 2016/4/20 09:40
 * email：2313570516@qq.com
 * desc:
 */
public class JsonAddress {


	/**
	 * name : 北京
	 * city : [{"name":"北京","area":["东城区","西城区","崇文区","宣武区","朝阳区","丰台区","石景山区","海淀区","门头沟区","房山区","通州区","顺义区","昌平区",
	 * "大兴区","平谷区","怀柔区","密云县","延庆县"]}]
	 */

	private String name;
	/**
	 * name : 北京
	 * area : ["东城区","西城区","崇文区","宣武区","朝阳区","丰台区","石景山区","海淀区","门头沟区","房山区","通州区","顺义区","昌平区","大兴区","平谷区","怀柔区","密云县",
	 * "延庆县"]
	 */

	private List<CityEntity> city;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CityEntity> getCity() {
		return city;
	}

	public void setCity(List<CityEntity> city) {
		this.city = city;
	}

	public static class CityEntity {
		private String name;
		private List<String> area;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getArea() {
			return area;
		}

		public void setArea(List<String> area) {
			this.area = area;
		}
	}
}
