package com.avj.stratop.common;

public class OptionsStrategy {

	public void evaluate() {
	}

	// roundoff till 2 decimal places
	public static double roundoff(Double dval) {
		return Math.round(dval * 100.0) / 100.0;
	}

	// roundoff till 2 decimal places
	public static double roundoff(Long dval) {
		return Math.round(dval * 100.0) / 100.0;
	}
	


}
