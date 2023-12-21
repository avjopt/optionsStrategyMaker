package com.avj.stratop.bullputspread;

import com.avj.stratop.common.StOptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyPut extends StOptions {

	private Double strike;
	private Double ibep;
	private Double aya;
	private Double diya;
	private Double pandl;




	public void genBEP() {
		ibep = strike - diya;
		System.out.println("For " + strike + " PE, Individual BEP: " + ibep);
	}

}
