package com.avj.stratop.shortcollar;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author avjoshi
 *
 */
@Getter
@Setter
@Builder
public class ShortCollarRow {

	private BuyCall buycall;
	private SellPut sellput;
	private Futures futures;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;

}
