package com.avj.stratop.longcollar;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LongCollarRow {

	private SellCall sellcall;
	private BuyPut buyput;
	private Futures futures;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;

}
