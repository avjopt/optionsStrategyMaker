package com.avj.stratop.shortstrangle;
import com.avj.stratop.common.StOptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SellCall extends StOptions {

	private Double strike;
	private Double individulBEP;
	private Double combinedBEP;
	private Double netcredit;
	private Double aya;
	private Double diya;
	private Double pandl;

	public void genNetCredit(Double otherprice) {
		netcredit = aya + otherprice;
	}

	public void genBEP() {
		individulBEP = strike + aya;
		combinedBEP = strike + netcredit;
		System.out.println("For " + strike + " CE, Individual BEP: " + individulBEP);
		System.out.println("For " + strike + " CE, Combined BEP: " + combinedBEP);
	}


}
