package com.avj.stratop.shortstrangle;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShortStrangleOutput {

	private Double spot;
	private String calldes;
	private Double callAya;
	private Double callDiya;
	private Double callPandL;
	private String putdes;
	private Double putAya;
	private Double putDiya;
	private Double putPandL;
	private Double spotPandL;
	private Double spotPandLPerLot;
	private Double roi;
	
}
