package com.avj.stratop.bullcallspread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BullCallSpreadRow {

	private SellCall sellcall;
	private BuyCall buycall;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;



}
