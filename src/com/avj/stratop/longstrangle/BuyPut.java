package com.avj.stratop.longstrangle;

import com.avj.stratop.common.StOptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class BuyPut extends StOptions {

	private Double strike;
	private Double ibep;
	private Double cbep;
	private Double netdebit;
	private Double aya;
	private Double diya;
	private Double pandl;

	public void genBEP() {
		ibep = strike - diya;
		cbep = strike - netdebit;
		System.out.println("For " + strike + " PE, Individual BEP: " + ibep);
		System.out.println("For " + strike + " PE, Combined BEP: " + cbep);
	}

}
