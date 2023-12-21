package com.avj.stratop.common;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StOptions {

	private String des;
	private int whereami;
	
	public void classify() {
		switch (whereami) {
		case 0:
			des = Constants.ATM_STR;
			break;
		case -1:
			des = Constants.ITM_STR;
			break;
		case 1:
			des = Constants.OTM_STR;
			break;
		default:
			des = "null";
			break;
		}
	}
}
