package com.avj.stratop.longstraddle;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LongStraddleRow {

	private BuyCall call;
	private BuyPut put;
	private Double spotprice;
	private Double pandl;
	private Double plPerLot;
	private Double roi;



}
