package com.avj.stratop.coveredput;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CoveredPutRow {

	private SellPut sellput;
	private Futures futures;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;



}
