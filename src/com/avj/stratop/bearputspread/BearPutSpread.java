package com.avj.stratop.bearputspread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.avj.stratop.common.BuyPut;
import com.avj.stratop.common.Constants;
import com.avj.stratop.common.OptionsStrategy;
import com.avj.stratop.common.SellPut;

public class BearPutSpread extends OptionsStrategy {

	private Double bputStrike;
	private Double bputPrice;
	private Double bputibep;
	private Double bputcbep;
	
	private Double sputStrike;
	private Double sputPrice;
	private Double sputibep;
	private Double sputcbep;
	
	private Double netdebit;
	
	private Double strikediff;
	private Double lotsize;
	private Double netinvested;
	private List<BearPutSpreadRow> spotlist = new ArrayList<>();
	private String disp = null;

	public BearPutSpread(String str) {
		this.disp = str;
	}

	private void generate() {

		List<Double> spotdl = new ArrayList<>();
		// First add individual BEPs
		spotdl.add(bputibep);
		spotdl.add(bputcbep);
		spotdl.add(sputibep);
		spotdl.add(sputcbep);

		Double lowestBEP = sputibep;
		Double start = sputStrike;

		while (start > lowestBEP) {
			start = start - strikediff;
			spotdl.add(start);
		}
		// go 6 strikes below lowest BEP
		int i = 0;
		while (i < 6) {
			start = start - strikediff;
			spotdl.add(start);
			i++;
		}
		// Cover range between buy and sell
		Double midrangestart = sputStrike;
		while (midrangestart < bputStrike) {
			spotdl.add(midrangestart);
			midrangestart += strikediff;
		}
		// Add 6 strikes above bought PE
		Double buyputstrike = bputStrike;
		i = 0;
		while (i < 6) {
			spotdl.add(buyputstrike);
			buyputstrike += strikediff;
			i++;
		}
		Collections.sort(spotdl);
		spotdl.forEach(s -> System.out.println("spot: " + s));
		Double bputstrike = bputStrike;
		Double sputstrike = sputStrike;
		for (Double spotat : spotdl) {
			BuyPut buyPutAtSpot = BuyPut.builder().aya(0.0).diya(bputPrice).strike(bputstrike)
					.build();
			if (spotat > bputstrike) {
				buyPutAtSpot.setWhereami(Constants.OTM);
				buyPutAtSpot.setAya(0.0);
			} else if (spotat.equals(bputstrike)) {
				buyPutAtSpot.setWhereami(Constants.ATM);
				buyPutAtSpot.setAya(0.0);
			} else if (spotat < bputstrike) {
				buyPutAtSpot.setWhereami(Constants.ITM);
				buyPutAtSpot.setAya(roundoff(bputstrike - spotat));
			}
			buyPutAtSpot.classify();
			buyPutAtSpot.setPandl(buyPutAtSpot.getAya() - buyPutAtSpot.getDiya());
			SellPut sellPutAtSpot = SellPut.builder().aya(sputPrice).diya(0.0).strike(sputstrike)
					.build();
			if (spotat > sputstrike) {
				sellPutAtSpot.setWhereami(Constants.OTM);
				sellPutAtSpot.setDiya(0.0);
			} else if (spotat.equals(sputstrike)) {
				sellPutAtSpot.setWhereami(Constants.ATM);
				sellPutAtSpot.setDiya(0.0);
			} else if (spotat < sputstrike) {
				sellPutAtSpot.setWhereami(Constants.ITM);
				sellPutAtSpot.setDiya(roundoff(sputstrike - spotat));
			}
			sellPutAtSpot.classify();
			sellPutAtSpot.setPandl(roundoff(sellPutAtSpot.getAya() - sellPutAtSpot.getDiya()));
			Double pandl = roundoff(buyPutAtSpot.getPandl()+sellPutAtSpot.getPandl());
			Double pandlPerLot =roundoff( pandl * lotsize);
			Double roi = roundoff((pandlPerLot / netinvested) * 100.0);
			BearPutSpreadRow spot = BearPutSpreadRow.builder().buyput(buyPutAtSpot).sellput(sellPutAtSpot).spotprice(spotat).pandl(pandl)
					.totalInvestment(netinvested).plPerLot(pandlPerLot).roi(roi).build();
			spotlist.add(spot);
			System.out.print(spot.getSpotprice() + "\t" + buyPutAtSpot.getDes() + "\t" + buyPutAtSpot.getAya() + "\t" + buyPutAtSpot.getDiya()
			+ "\t" + buyPutAtSpot.getPandl());
	System.out.println("\t" + sellPutAtSpot.getDes() + "\t" + sellPutAtSpot.getAya() + "\t" + sellPutAtSpot.getDiya() + "\t" + sellPutAtSpot.getPandl()
			+ "\t" + spot.getPandl() + "\t" + spot.getPlPerLot() + "\t" + spot.getRoi());
		}
	}

	@Override
	public void evaluate() {
		Scanner myObj = new Scanner(System.in);

		System.out.println(disp);

		System.out.println("Buy PE Strike ");
		String buyputstrike = myObj.nextLine();
		bputStrike = Double.valueOf(buyputstrike);

		System.out.println("Buy PE @: ");
		String buyputprice = myObj.nextLine();
		bputPrice = Double.valueOf(buyputprice);

		System.out.println("Sell PE Strike: ");
		String sellputstrike = myObj.nextLine();
		sputStrike = Double.valueOf(sellputstrike);

		System.out.println("Sell PE @:");
		String sellputprice = myObj.nextLine();
		sputPrice = Double.valueOf(sellputprice);
		
		netdebit = bputPrice - sputPrice;
		
		bputibep = bputStrike - bputPrice;
		bputcbep = bputStrike - netdebit;
		
		sputibep = sputStrike - sputPrice;
		sputcbep = sputStrike - netdebit;

		System.out.println("Lot size: ");
		String lsstr = myObj.nextLine();
		lotsize = Double.valueOf(lsstr);

		System.out.println("Strike Difference: ");
		String sdiff = myObj.nextLine();
		strikediff = Double.valueOf(sdiff);

		System.out.println("Total investment/margin required: ");
		String mstr = myObj.nextLine();
		netinvested = Double.valueOf(mstr);

		System.out.println("=========================================================================================");

		generate();

	}

}
