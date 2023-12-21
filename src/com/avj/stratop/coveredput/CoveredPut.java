package com.avj.stratop.coveredput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.avj.stratop.common.Constants;
import com.avj.stratop.common.OptionsStrategy;

public class CoveredPut extends OptionsStrategy {

	private static final int GO_BELOW_FUTURES_SPOT = 10;
	private static final int GO_ABOVE_FUTURES_SPOT = 10;
	private static final int GO_ABOVE_PUT_SELL = 10;
	private static final int GO_BELOW_PUT_SELL = 10;
	private Double futuresSellPrice;
	private SellPut sellput;
	private Double sellPutBEP;
	private Double futuresBEP;
	private Double diffInFutures;
	private Double strikediff;
	private Double lotsize;
	private Double totalMargin;
	private Double spotat;
	private List<CoveredPutRow> spotlist = new ArrayList<>();
	private String disp = null;

	public CoveredPut(String dstr) {
		disp = dstr;
	}

	private void generate() {

		List<Double> sdlist = new ArrayList<>();
		// First populate 5 below spotat and 5 above spotat with stepsInFutures

		sdlist.add(spotat); // add spot
		if (!sdlist.contains(futuresSellPrice)) {
			sdlist.add(futuresSellPrice);
		}
		sdlist.add(futuresBEP);

		Double fstart = futuresSellPrice;
		for (int i = 0; i < GO_BELOW_FUTURES_SPOT; i++) {
			fstart = fstart - diffInFutures;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		fstart = futuresSellPrice;
		for (int i = 0; i < GO_ABOVE_FUTURES_SPOT; i++) {
			fstart = fstart + diffInFutures;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		sdlist.add(sellput.getStrike());
		fstart = sellput.getStrike();
		for (int i = 0; i < GO_BELOW_PUT_SELL; i++) {
			fstart = fstart - strikediff;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		fstart = sellput.getStrike();
		for (int i = 0; i < GO_ABOVE_PUT_SELL; i++) {
			fstart = fstart + strikediff;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		sdlist.add(sellPutBEP); // add call sell BEP
		Collections.sort(sdlist);
		for (Double spotAtExpiry : sdlist) {
			double futaya = 0.0;
			double futdiya = 0.0;
			if (spotAtExpiry <= futuresSellPrice) {
				futaya = futuresSellPrice - spotAtExpiry;
				futdiya = 0.0;
			} else {
				futaya = 0.0;
				futdiya = spotAtExpiry - futuresSellPrice;
			}
			double pl = futaya - futdiya;
			Futures futrow = Futures.builder().aya(roundoff(futaya)).diya(roundoff(futdiya)).fspot(spotAtExpiry)
					.pandl(roundoff(pl)).build();

			Double putstrike = sellput.getStrike();
			SellPut sput = SellPut.builder().aya(sellput.getAya()).diya(0.0).strike(putstrike).build();
			if (putstrike < spotAtExpiry) {
				sput.setWhereami(Constants.OTM);
				sput.setDiya(0.0);
			} else if (putstrike.equals(spotAtExpiry)) {
				sput.setWhereami(Constants.ATM);
				sput.setDiya(0.0);
			} else if (putstrike > spotAtExpiry) {
				sput.setWhereami(Constants.ITM);
				sput.setDiya(roundoff(putstrike - spotAtExpiry));
			}
			sput.classify();
			sput.setPandl(roundoff(sput.getAya() - sput.getDiya()));
			Double pandl = roundoff(sput.getPandl() + futrow.getPandl());
			Double pandlPerLot = roundoff(pandl * lotsize);
			Double roi = roundoff(pandlPerLot * 100.0 / totalMargin);
			CoveredPutRow spot = CoveredPutRow.builder().futures(futrow).sellput(sput).pandl(pandl).plPerLot(pandlPerLot).roi(roi)
					.spotprice(spotAtExpiry).build();
			spotlist.add(spot);
		}

		for (CoveredPutRow spot : spotlist) {
			SellPut sput = spot.getSellput();
			Futures fut = spot.getFutures();
			System.out.println(spot.getSpotprice() + "\t" + fut.getAya() + "\t" + fut.getDiya() + "\t" + fut.getPandl()
					+ "\t" + sput.getDes() + "\t" + sput.getAya() + "\t" + sput.getDiya() + "\t" + sput.getPandl()
					+ "\t" + spot.getPandl() + "\t" + spot.getPlPerLot() + "\t" + spot.getRoi());

		}
	}

	@Override
	public void evaluate() {
		Scanner myObj = new Scanner(System.in);

		System.out.println(disp);

		System.out.println("Spot at: ");
		String spotstr = myObj.nextLine();
		spotat = Double.valueOf(spotstr);

		System.out.println("Short Futures at: ");
		String fstr = myObj.nextLine();
		futuresSellPrice = Double.valueOf(fstr);

		System.out.println("Steps in futures for which calculation is required: ");
		String sdiff = myObj.nextLine();
		diffInFutures = Double.valueOf(sdiff);

		System.out.println("Sell PE Strike: ");
		String putstrikestr = myObj.nextLine();
		Double putstrike = Double.valueOf(putstrikestr);

		System.out.println("Sell PE @:");
		String putpricestr = myObj.nextLine();
		Double putprice = Double.valueOf(putpricestr);
		sellPutBEP = putstrike - putprice;
		futuresBEP = futuresSellPrice + putprice;

		System.out.println("Strike difference: ");
		String sdstr = myObj.nextLine();
		strikediff = Double.valueOf(sdstr);

		System.out.println("Lot size: ");
		String lsstr = myObj.nextLine();
		lotsize = Double.valueOf(lsstr);

		System.out.println("Total margin(Short futures + Sell Put): ");
		String nstr = myObj.nextLine();
		totalMargin = Double.valueOf(nstr);

		System.out.println("=========================================================================================");

		sellput = SellPut.builder().aya(putprice).diya(0.0).strike(putstrike).build();
		generate();
	}
}
