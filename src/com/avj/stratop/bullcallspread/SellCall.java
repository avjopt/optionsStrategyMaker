package com.avj.stratop.bullcallspread;
import com.avj.stratop.common.StOptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SellCall extends StOptions {

	private Double strike;
	private Double ibep;
	private Double cbep;
	private Double netdebit;
	private Double aya;
	private Double diya;
	private Double pandl;

	public void genNetCredit(Double otherprice) {

	}

	public void genBEP() {

		ibep = strike + aya;
		System.out.println("For " + strike + " CE, Individual BEP: " + ibep);

	}


}
