package com.avj.stratop.bearputspread;

import com.avj.stratop.common.BuyPut;
import com.avj.stratop.common.SellPut;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BearPutSpreadRow {

	private BuyPut buyput;
	private SellPut sellput;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;
	private Double totalInvestment;

}
