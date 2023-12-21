package com.avj.stratop.coveredput;
import com.avj.stratop.common.StOptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SellPut extends StOptions {

	private Double strike;
	private Double ibep;
	private Double aya;
	private Double diya;
	private Double pandl;


	public void genBEP() {

		ibep = strike - aya;
		System.out.println("For " + strike + " CE, Individual BEP: " + ibep);

	}


}
