package com.avj.stratop.bullcallspread;
import com.avj.stratop.common.StOptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyCall extends StOptions {

	private Double strike;
	private Double ibep;
	private Double cbep;
	private Double netdebit;
	private Double aya;
	private Double diya;
	private Double pandl;

	public void genBEP() {
		ibep = strike + diya;
		
		System.out.println("For " + strike + " CE, Individual BEP: " + ibep);
	}


}
