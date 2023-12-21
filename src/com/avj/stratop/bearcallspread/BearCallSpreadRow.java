package com.avj.stratop.bearcallspread;
import com.avj.stratop.common.BuyCall;
import com.avj.stratop.common.SellCall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BearCallSpreadRow {

	private SellCall sellcall;
	private BuyCall buycall;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;



}
