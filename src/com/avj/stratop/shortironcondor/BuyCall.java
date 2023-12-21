package com.avj.stratop.shortironcondor;

import com.avj.stratop.common.StOptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyCall extends StOptions{

	private Double strike;
	private Double aya;
	private Double diya;
	private Double pandl;
}
