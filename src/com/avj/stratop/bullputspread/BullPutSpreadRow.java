package com.avj.stratop.bullputspread;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BullPutSpreadRow {

	private BuyPut buyput;
	private SellPut sellput;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;
	private Double totalInvestment;

}
