package com.avj.stratop.shortstrangle;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShortStrangleRow {

	private SellCall call;
	private SellPut put;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;



}
