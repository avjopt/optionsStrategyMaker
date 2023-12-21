package com.avj.stratop.bullputspread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.avj.stratop.common.Constants;
import com.avj.stratop.common.OptionsStrategy;

public class BullPutSpread extends OptionsStrategy {

	private BuyPut buyput;
	private SellPut sellput;
	private Double strikediff;
	private Double cbep;
	private Double lotsize;
	private Double netinvested;
	private Double spotat;
	private List<BullPutSpreadRow> spotlist = new ArrayList<>();
	private String disp = null;

	public BullPutSpread(String str) {
		this.disp = str;
	}

	private void generate() {

		List<Double> spotdl = new ArrayList<>();
		// First add individual BEPs
		spotdl.add(spotat);
		spotdl.add(buyput.getIbep());
		spotdl.add(sellput.getIbep());
		spotdl.add(cbep);

		Double lowestBEP = buyput.getIbep();
		Double start = buyput.getStrike();

		while (start > lowestBEP) {
			start = start - strikediff;
			spotdl.add(start);
		}
		// go 4 strikes below lowest BEP
		int i = 0;
		while (i < 4) {
			start = start - strikediff;
			spotdl.add(start);
			i++;
		}
		// Cover range between buy and sell
		Double midrangestart = buyput.getStrike();
		while (midrangestart < sellput.getStrike()) {
			spotdl.add(midrangestart);
			midrangestart += strikediff;
		}
		// Add 6 strikes above bought PE
		Double sellputstrike = sellput.getStrike();
		i = 0;
		while (i < 6) {
			spotdl.add(sellputstrike);
			sellputstrike += strikediff;
			i++;
		}
		Collections.sort(spotdl);
		Double bputstrike = buyput.getStrike();
		Double sputstrike = sellput.getStrike();
		for (Double spotat : spotdl) {

			SellPut sellPutAtSpot = SellPut.builder().aya(sellput.getAya()).diya(sputstrike - spotat).strike(sputstrike)
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
			
			BuyPut buyPutAtSpot = BuyPut.builder().aya(bputstrike - spotat).diya(buyput.getDiya()).strike(bputstrike)
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
			buyPutAtSpot.setPandl(roundoff(buyPutAtSpot.getAya() - buyPutAtSpot.getDiya()));
			
			
			Double pandl = roundoff(buyPutAtSpot.getPandl() + sellPutAtSpot.getPandl());
			Double pandlPerLot = roundoff(pandl * lotsize);
			Double roi = roundoff((pandlPerLot / netinvested) * 100.0);
			BullPutSpreadRow spot = BullPutSpreadRow.builder().buyput(buyPutAtSpot).sellput(sellPutAtSpot).spotprice(spotat).pandl(pandl)
					.totalInvestment(netinvested).plPerLot(pandlPerLot).roi(roi).build();
			spotlist.add(spot);
			System.out.print(spot.getSpotprice() + "\t" + buyPutAtSpot.getDes() + "\t" + roundoff(buyPutAtSpot.getAya()) + "\t"
					+ roundoff(buyPutAtSpot.getDiya()) + "\t" + roundoff(buyPutAtSpot.getPandl()));
			System.out.println("\t" + sellPutAtSpot.getDes() + "\t" + roundoff(sellPutAtSpot.getAya()) + "\t"
					+ roundoff(sellPutAtSpot.getDiya()) + "\t" + roundoff(sellPutAtSpot.getPandl()) + "\t" + roundoff(spot.getPandl()) + "\t"
					+ roundoff(spot.getPlPerLot()) + "\t" + roundoff(spot.getRoi()));
		}
	}

	@Override
	public void evaluate() {
		Scanner myObj = new Scanner(System.in);

		System.out.println(disp);
		
		System.out.println("Spot price: ");
		String strspotat = myObj.nextLine();
		spotat = Double.valueOf(strspotat);

		System.out.println("Sell PE Strike ");
		String sellputstrike = myObj.nextLine();

		System.out.println("Sell PE @: ");
		String sellputprice = myObj.nextLine();

		System.out.println("Buy PE Strike: ");
		String buyputstrike = myObj.nextLine();

		System.out.println("Buy PE @:");
		String buyputprice = myObj.nextLine();

		System.out.println("Lot size: ");
		String lsstr = myObj.nextLine();
		lotsize = Double.valueOf(lsstr);

		System.out.println("Strike Difference: ");
		String sdiff = myObj.nextLine();
		strikediff = Double.valueOf(sdiff);

		System.out.println("Total investment: ");
		String mstr = myObj.nextLine();
		netinvested = Double.valueOf(mstr);

		System.out.println("=========================================================================================");

		Double sellputaya = Double.valueOf(sellputprice);
		Double buyputdiya = Double.valueOf(buyputprice);
		Double netcredit = sellputaya - buyputdiya;
		sellput = SellPut.builder().aya(sellputaya).diya(0.0).strike(Double.valueOf(sellputstrike)).build();
		
		sellput.setNetCredit(netcredit);
		sellput.genBEP();

		buyput = BuyPut.builder().aya(0.0).diya(buyputdiya).strike(Double.valueOf(buyputstrike)).build();
		buyput.genBEP();

		cbep = sellput.getStrike()- netcredit;
		generate();

	}

}
