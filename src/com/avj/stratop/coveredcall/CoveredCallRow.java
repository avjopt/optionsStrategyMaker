package com.avj.stratop.coveredcall;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CoveredCallRow {

	private SellCall sellcall;
	private Futures futures;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;



}
