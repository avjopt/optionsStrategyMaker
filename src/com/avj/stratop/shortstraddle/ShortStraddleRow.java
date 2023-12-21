package com.avj.stratop.shortstraddle;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShortStraddleRow {

	private SellCall call;
	private SellPut put;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;



}
