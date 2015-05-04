package com.wise.car;

import java.util.Comparator;

import com.wise.baba.entity.BrandData;

/**
 * 根据拼音排序
 */
public class PinyinComparator implements Comparator<BrandData> {

	public int compare(BrandData o1, BrandData o2) {
		if (o1.getLetter().equals("@")
				|| o2.getLetter().equals("#")) {
			return -1;
		} else if (o1.getLetter().equals("#")
				|| o2.getLetter().equals("@")) {
			return 1;
		} else {
			return o1.getLetter().compareTo(o2.getLetter());
		}
	}
}
