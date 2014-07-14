package com.wise.car;

import java.util.Comparator;
import data.BrankModel;

/**
 * 根据拼音排序
 */
public class PinyinComparator implements Comparator<BrankModel> {

	public int compare(BrankModel o1, BrankModel o2) {
		if (o1.getVehicleLetter().equals("@")
				|| o2.getVehicleLetter().equals("#")) {
			return -1;
		} else if (o1.getVehicleLetter().equals("#")
				|| o2.getVehicleLetter().equals("@")) {
			return 1;
		} else {
			return o1.getVehicleLetter().compareTo(o2.getVehicleLetter());
		}
	}
}
