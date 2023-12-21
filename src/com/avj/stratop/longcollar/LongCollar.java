package com.avj.stratop.longcollar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.avj.stratop.common.Constants;
import com.avj.stratop.common.OptionsStrategy;

public class LongCollar extends OptionsStrategy {

	private static final int GO_BELOW_FUTURES_SPOT = 10;
	private static final int GO_ABOVE_FUTURES_SPOT = 10;
	private SellCall sellcall;
	private BuyPut buyput;
	private Double sellcallibep;
	private Double buyputibep;
	private Double sellcallcbep;
	private Double buyputcbep;
	private Double futuresBEP;
	private Double futuresLongPrice;
	private Double diffInFutures;
	private Double strikediff;
	private Double lotsize;
	private Double totalMargin;
	private Double spotat;
	private List<LongCollarRow> spotlist = new ArrayList<>();
	private String disp = null;


	public LongCollar(String dstr) {
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
		sdlist.add(sellcallibep);
		sdlist.add(sellcallcbep);
		sdlist.add(buyputibep);
		sdlist.add(buyputcbep);
		sdlist.add(sellcall.getStrike());
		sdlist.add(buyput.getStrike());

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

		// lowest spot to consider : individual BEP of buy put, go below 5 strikes
		Double lowestputBEP = buyputibep;
		Double start = buyput.getStrike();
		while (true) {
			start = start - strikediff;
			if (start <= lowestputBEP) {
				break;
			}
			if (!sdlist.contains(start)) {
				sdlist.add(start);
			}
		}
		for (int i = 0; i < 5; i++) {
			if (!sdlist.contains(start)) {
				sdlist.add(start);
			}
			start -= strikediff;
		}

		// cover midrange from PUT to CALLS
		Double midrange = buyput.getStrike();
		while (midrange <= sellcall.getStrike()) {
			if (!sdlist.contains(midrange)) {
				sdlist.add(midrange);
			}
			midrange += strikediff;
		}

		// from call strike to call individual BEP
		Double callstart = sellcall.getStrike();
		while (true) {
			if (!sdlist.contains(callstart)) {
				sdlist.add(callstart);
			}
			callstart += strikediff;
			if (callstart > sellcallibep) {
				break;
			}
		}
		for (int i = 0; i < 5; i++) { // add 5 strikes above sold call
			if (!sdlist.contains(callstart)) {
				sdlist.add(callstart);
			}
			callstart += strikediff;
		}

		Collections.sort(sdlist);
		for (Double spotAtExpiry : sdlist) {
			// calculate futures PL per spot 
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

			// calculate sold CE PL per spot
			Double callstrike = sellcall.getStrike();
			SellCall scall = SellCall.builder().aya(0.0).diya(0.0).strike(callstrike).build();
			if (callstrike < spotAtExpiry) {
				scall.setWhereami(Constants.ITM);
				scall.setAya(sellcall.getAya());
				scall.setDiya(roundoff(spotAtExpiry - callstrike));
			} else if (callstrike.equals(spotAtExpiry)) {
				scall.setWhereami(Constants.ATM);
				scall.setAya(sellcall.getAya());
				scall.setDiya(0.0);
			} else if (callstrike > spotAtExpiry) {
				scall.setWhereami(Constants.OTM);
				scall.setAya(sellcall.getAya());
				scall.setDiya(0.0);
			}
			scall.classify();
			scall.setPandl(roundoff(scall.getAya() - scall.getDiya()));
			
			//calculate bought PE PL per spot
			Double putstrike = buyput.getStrike();
			Double putprice = buyput.getDiya();
			BuyPut bput = BuyPut.builder().aya(0.0).diya(0.0).strike(putstrike).build();
			if (putstrike < spotAtExpiry) {
				bput.setWhereami(Constants.OTM);
				bput.setAya(0.0);
				bput.setDiya(putprice);
			} else if (putstrike.equals(spotAtExpiry)) {
				bput.setWhereami(Constants.ATM);
				bput.setAya(0.0);
				bput.setDiya(putprice);
			} else if (putstrike > spotAtExpiry) {
				bput.setWhereami(Constants.ITM);
				bput.setAya(roundoff(putstrike-spotAtExpiry));
				bput.setDiya(putprice);
			}
			bput.classify();
			bput.setPandl(roundoff(bput.getAya() - bput.getDiya()));
			
			Double pandl = roundoff(futrow.getPandl() + scall.getPandl() + bput.getPandl());
			Double pandlPerLot = roundoff(pandl * lotsize);
			Double roi = roundoff(pandlPerLot * 100.0 / totalMargin);
			LongCollarRow spot = LongCollarRow.builder().futures(futrow).sellcall(scall).buyput(bput).pandl(pandl).plPerLot(pandlPerLot).roi(roi)
					.spotprice(spotAtExpiry).build();
			spotlist.add(spot);
		}

		for (LongCollarRow spot : spotlist) {
			Futures fut = spot.getFutures();
			SellCall scall = spot.getSellcall();
			BuyPut bput = spot.getBuyput();
			System.out.println(spot.getSpotprice() + "\t" + fut.getAya() + "\t" + fut.getDiya() + "\t" + fut.getPandl()
					+ "\t" + scall.getDes() + "\t" + scall.getAya() + "\t" + scall.getDiya() + "\t" + scall.getPandl()
					+"\t"  + bput.getDes() +"\t"  +bput.getAya() + "\t" + bput.getDiya() + "\t" + bput.getPandl()
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
		Double callstrike = Double.valueOf(callstrikestr);

		System.out.println("Sell CE @:");
		String callpricestr = myObj.nextLine();
		Double callprice = Double.valueOf(callpricestr);

		System.out.println("Buy PE Strike: ");
		String putstrikestr = myObj.nextLine();
		Double putstrike = Double.valueOf(putstrikestr);

		System.out.println("Buy PE @:");
		String putpricestr = myObj.nextLine();
		Double putprice = Double.valueOf(putpricestr);

		sellcallibep = callstrike + callprice;
		buyputibep = putstrike - putprice;

		Double netcredit = callprice - putprice; // premium collected by selling calls - premium debited to buy puts

		sellcallcbep = callstrike + netcredit;
		buyputcbep = putstrike - netcredit;
		futuresBEP = futuresLongPrice - netcredit;

		System.out.println("Strike difference: ");
		String sdstr = myObj.nextLine();
		strikediff = Double.valueOf(sdstr);

		System.out.println("Lot size: ");
		String lsstr = myObj.nextLine();
		lotsize = Double.valueOf(lsstr);

		System.out.println("Total Invested (Long futures + Sell Call + Buy Puts ): ");
		String nstr = myObj.nextLine();
		totalMargin = Double.valueOf(nstr);

		System.out.println("=========================================================================================");

		sellcall = SellCall.builder().aya(callprice).diya(0.0).strike(callstrike).build();
		buyput = BuyPut.builder().aya(0.0).diya(putprice).strike(putstrike).build();
		generate();
	}
}
