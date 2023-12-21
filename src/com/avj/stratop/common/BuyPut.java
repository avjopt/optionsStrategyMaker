package com.avj.stratop.common;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyPut extends StOptions {
	private Double strike;
	private Double aya;
	private Double diya;
	private Double pandl;
}
