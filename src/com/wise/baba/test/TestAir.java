package com.wise.baba.test;

import com.wise.baba.biz.HttpAir;

import junit.framework.TestCase;

public class TestAir extends TestCase {
	public void switchClick(){
		HttpAir httpAir = new HttpAir(null,null);
		httpAir.request("56621650804", HttpAir.COMMAND_SWITCH, "{switch: 1}");
	}
}
