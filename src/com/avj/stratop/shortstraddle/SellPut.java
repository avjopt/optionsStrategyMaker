package com.avj.stratop.shortstraddle;
import com.avj.stratop.common.StOptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class SellPut extends StOptions {
	
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
		individulBEP = strike - aya;
		combinedBEP = strike - netcredit;
		System.out.println("For "+strike+" PE, Individual BEP: "+individulBEP);
		System.out.println("For "+strike+" PE, Combined BEP: "+combinedBEP);
	}

}
