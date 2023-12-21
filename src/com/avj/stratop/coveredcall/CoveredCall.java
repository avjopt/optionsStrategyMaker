package com.avj.stratop.coveredcall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.avj.stratop.common.Constants;
import com.avj.stratop.common.OptionsStrategy;

public class CoveredCall extends OptionsStrategy {

	private static final int GO_BELOW_FUTURES_SPOT = 10;
	private static final int GO_ABOVE_FUTURES_SPOT = 10;
	private static final int GO_ABOVE_CALL_SELL = 5;
	private static final int GO_BELOW_CALL_SELL = 5;
	private Double sellCallPrice;
	private Double sellCallStrike;
	private Double sellCallBEP;
	private Double futuresBEP;
	private Double futuresLongPrice;
	private Double diffInFutures;
	private Double strikediff;
	private Double lotsize;
	private Double totalMargin;
	private Double spotat;
	private List<CoveredCallRow> spotlist = new ArrayList<>();
	private String disp = null;

	public CoveredCall(String dstr) {
		disp = dstr;
	}

	private void generate() {

		List<Double> sdlist = new ArrayList<>();
		// First populate 5 below spotat and 5 above spotat with stepsInFutures

		sdlist.add(spotat); // add spot
		if (!sdlist.contains(futuresLongPrice)) {
			sdlist.add(futuresLongPrice);
		}
		sdlist.add(futuresBEP);

		Double fstart = futuresLongPrice;
		for (int i = 0; i < GO_BELOW_FUTURES_SPOT; i++) {
			fstart = fstart - diffInFutures;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		fstart = futuresLongPrice;
		for (int i = 0; i < GO_ABOVE_FUTURES_SPOT; i++) {
			fstart = fstart + diffInFutures;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		sdlist.add(sellCallStrike);
		fstart = sellCallStrike;
		for (int i = 0; i < GO_BELOW_CALL_SELL; i++) {
			fstart = fstart - strikediff;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		fstart = sellCallStrike;
		for (int i = 0; i < GO_ABOVE_CALL_SELL; i++) {
			fstart = fstart + strikediff;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		sdlist.add(sellCallBEP); // add call sell BEP
		Collections.sort(sdlist);
		for (Double spotAtExpiry : sdlist) {
			double futaya = 0.0;
			double futdiya = 0.0;
			if (spotAtExpiry <= futuresLongPrice) {
				futaya = 0.0;
				futdiya = futuresLongPrice - spotAtExpiry;
			} else {
				futaya = spotAtExpiry - futuresLongPrice;
				futdiya = 0.0;
			}
			double pl = futaya - futdiya;
			Futures futrow = Futures.builder().aya(roundoff(futaya)).diya(roundoff(futdiya)).fspot(spotAtExpiry)
					.pandl(roundoff(pl)).build();

			SellCall scall = SellCall.builder().aya(0.0).diya(0.0).strike(sellCallStrike).build();
			if (sellCallStrike < spotAtExpiry) {
				scall.setWhereami(Constants.ITM);
				scall.setAya(sellCallPrice);
				scall.setDiya(roundoff(spotAtExpiry - sellCallStrike));
			} else if (sellCallStrike.equals(spotAtExpiry)) {
				scall.setWhereami(Constants.ATM);
				scall.setAya(sellCallPrice);
				scall.setDiya(0.0);
			} else if (sellCallStrike > spotAtExpiry) {
				scall.setWhereami(Constants.OTM);
				scall.setAya(sellCallPrice);
				scall.setDiya(0.0);
			}
			scall.classify();
			scall.setPandl(roundoff(scall.getAya() - scall.getDiya()));
			Double pandl = roundoff(scall.getPandl() + futrow.getPandl());
			Double pandlPerLot = roundoff(pandl * lotsize);
			Double roi = roundoff(pandlPerLot * 100.0 / totalMargin);
			CoveredCallRow spot = CoveredCallRow.builder().futures(futrow).sellcall(scall).pandl(pandl).plPerLot(pandlPerLot).roi(roi)
					.spotprice(spotAtExpiry).build();
			spotlist.add(spot);
		}

		for (CoveredCallRow spot : spotlist) {
			SellCall scall = spot.getSellcall();
			Futures fut = spot.getFutures();
			System.out.println(spot.getSpotprice() + "\t" + fut.getAya() + "\t" + fut.getDiya() + "\t" + fut.getPandl()
					+ "\t" + scall.getDes() + "\t" + scall.getAya() + "\t" + scall.getDiya() + "\t" + scall.getPandl()
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

		System.out.println("Long Futures at: ");
		String lpstr = myObj.nextLine();
		futuresLongPrice = Double.valueOf(lpstr);

		System.out.println("Steps in futures for which calculation is required: ");
		String sdiff = myObj.nextLine();
		diffInFutures = Double.valueOf(sdiff);

		System.out.println("Sell CE Strike: ");
		String callstrikestr = myObj.nextLine();
		sellCallStrike = Double.valueOf(callstrikestr);

		System.out.println("Sell CE @:");
		String callpricestr = myObj.nextLine();
		sellCallPrice = Double.valueOf(callpricestr);
		sellCallBEP = sellCallStrike + sellCallPrice;
		futuresBEP = futuresLongPrice - sellCallPrice;

		System.out.println("Strike difference: ");
		String sdstr = myObj.nextLine();
		strikediff = Double.valueOf(sdstr);

		System.out.println("Lot size: ");
		String lsstr = myObj.nextLine();
		lotsize = Double.valueOf(lsstr);

		System.out.println("Total margin(Long futures + Sell Call): ");
		String nstr = myObj.nextLine();
		totalMargin = Double.valueOf(nstr);

		System.out.println("=========================================================================================");

		generate();
	}
}
