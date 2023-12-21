package com.avj.stratop.shortironbutterfly;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShortIronButterflyRow {

	private BuyCall buycall;
	private BuyPut buyput;
	private SellCall sellcall;
	private SellPut sellput;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;
	private Double totalInvestment;

}
