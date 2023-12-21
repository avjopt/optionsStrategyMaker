package com.avj.stratop.shortcollar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.avj.stratop.common.Constants;
import com.avj.stratop.common.OptionsStrategy;

/**
 * @author avjoshi
 *
 */
public class ShortCollar extends OptionsStrategy {

	private static final int GO_BELOW_FUTURES_SPOT = 10;
	private static final int GO_ABOVE_FUTURES_SPOT = 10;
	private BuyCall buycall;
	private SellPut sellput;
	private Double sellputibep;
	private Double buycallibep;
	private Double sellputcbep;
	private Double buycallcbep;
	private Double futuresBEP;
	private Double futuresShortPrice;
	private Double diffInFutures;
	private Double strikediff;
	private Double lotsize;
	private Double totalMargin;
	private Double spotat;
	private List<ShortCollarRow> spotlist = new ArrayList<>();
	private String disp = null;

	public ShortCollar(String dstr) {
		disp = dstr;
	}

	private void generate() {

		List<Double> sdlist = new ArrayList<>();
		// First populate 5 below spotat and 5 above spotat with stepsInFutures

		sdlist.add(spotat); // add spot
		if (!sdlist.contains(futuresShortPrice)) {
			sdlist.add(futuresShortPrice);
		}
		sdlist.add(futuresBEP);
		sdlist.add(sellputibep);
		sdlist.add(sellputcbep);
		sdlist.add(buycallibep);
		sdlist.add(buycallcbep);

		sdlist.add(sellput.getStrike());
		sdlist.add(buycall.getStrike());

		Double fstart = futuresShortPrice;
		for (int i = 0; i < GO_BELOW_FUTURES_SPOT; i++) {
			fstart = fstart - diffInFutures;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		fstart = futuresShortPrice;
		for (int i = 0; i < GO_ABOVE_FUTURES_SPOT; i++) {
			fstart = fstart + diffInFutures;
			if (!sdlist.contains(fstart)) {
				sdlist.add(fstart);
			}
		}

		// lowest spot to consider : individual BEP of buy put, go below 5 strikes
		Double lowestputBEP = sellputibep;
		Double start = sellput.getStrike();
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
		Double midrange = sellput.getStrike();
		while (midrange <= buycall.getStrike()) {
			if (!sdlist.contains(midrange)) {
				sdlist.add(midrange);
			}
			midrange += strikediff;
		}

		// from call strike to call individual BEP
		Double callstart = buycall.getStrike();
		while (true) {
			if (!sdlist.contains(callstart)) {
				sdlist.add(callstart);
			}
			callstart += strikediff;
			if (callstart > buycallibep) {
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
			if (spotAtExpiry >= futuresShortPrice) {
				futaya = 0.0;
				futdiya = spotAtExpiry - futuresShortPrice;
			} else {
				futaya = futuresShortPrice - spotAtExpiry;
				futdiya = 0.0;
			}
			double pl = futaya - futdiya;
			Futures futrow = Futures.builder().aya(roundoff(futaya)).diya(roundoff(futdiya)).fspot(spotAtExpiry)
					.pandl(roundoff(pl)).build();

			// calculate sold PE PL per spot
			Double putstrike = sellput.getStrike();
			Double putprice = sellput.getAya();
			SellPut sput = SellPut.builder().aya(0.0).diya(0.0).strike(putstrike).build();
			if (putstrike < spotAtExpiry) {
				sput.setWhereami(Constants.OTM);
				sput.setAya(putprice);
				sput.setDiya(0.0);
			} else if (putstrike.equals(spotAtExpiry)) {
				sput.setWhereami(Constants.ATM);
				sput.setAya(putprice);
				sput.setDiya(0.0);
			} else if (putstrike > spotAtExpiry) {
				sput.setWhereami(Constants.ITM);
				sput.setAya(putprice);
				sput.setDiya(roundoff(putstrike - spotAtExpiry));
			}
			sput.classify();
			sput.setPandl(roundoff(sput.getAya() - sput.getDiya()));

			// calculate bought CE PL per spot
			Double callstrike = buycall.getStrike();
			Double callprice = buycall.getDiya();
			BuyCall bcall = BuyCall.builder().aya(0.0).diya(0.0).strike(callstrike).build();
			if (callstrike < spotAtExpiry) {
				bcall.setWhereami(Constants.ITM);
				bcall.setAya(roundoff(spotAtExpiry - callstrike));
				bcall.setDiya(callprice);
			} else if (callstrike.equals(spotAtExpiry)) {
				bcall.setWhereami(Constants.ATM);
				bcall.setAya(0.0);
				bcall.setDiya(callprice);
			} else if (callstrike > spotAtExpiry) {
				bcall.setWhereami(Constants.OTM);
				bcall.setAya(0.0);
				bcall.setDiya(callprice);
			}
			bcall.classify();
			bcall.setPandl(roundoff(bcall.getAya() - bcall.getDiya()));

			Double pandl = roundoff(futrow.getPandl() + sput.getPandl() + bcall.getPandl());
			Double pandlPerLot = roundoff(pandl * lotsize);
			Double roi = roundoff(pandlPerLot * 100.0 / totalMargin);
			ShortCollarRow spot = ShortCollarRow.builder().futures(futrow).sellput(sput).buycall(bcall).pandl(pandl).plPerLot(pandlPerLot)
					.roi(roi).spotprice(spotAtExpiry).build();
			spotlist.add(spot);
		}

		for (ShortCollarRow spot : spotlist) {
			Futures fut = spot.getFutures();
			SellPut sput = spot.getSellput();
			BuyCall bcall = spot.getBuycall();
			System.out.println(spot.getSpotprice() + "\t" + fut.getAya() + "\t" + fut.getDiya() + "\t" + fut.getPandl()
					+ "\t" + sput.getDes() + "\t" + sput.getAya() + "\t" + sput.getDiya() + "\t" + sput.getPandl()
					+ "\t" + bcall.getDes() + "\t" + bcall.getAya() + "\t" + bcall.getDiya() + "\t" + bcall.getPandl()
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
		String lpstr = myObj.nextLine();
		futuresShortPrice = Double.valueOf(lpstr);

		System.out.println("Steps in futures for which calculation is required: ");
		String sdiff = myObj.nextLine();
		diffInFutures = Double.valueOf(sdiff);

		System.out.println("Sell PE Strike: ");
		String putstrikestr = myObj.nextLine();
		Double putstrike = Double.valueOf(putstrikestr);

		System.out.println("Sell PE @:");
		String putpricestr = myObj.nextLine();
		Double putprice = Double.valueOf(putpricestr);

		System.out.println("Buy CE Strike: ");
		String callstrikestr = myObj.nextLine();
		Double callstrike = Double.valueOf(callstrikestr);

		System.out.println("Buy CE @:");
		String callpricestr = myObj.nextLine();
		Double callprice = Double.valueOf(callpricestr);

		buycallibep = callstrike + callprice;
		sellputibep = putstrike - putprice;

		Double netcredit = putprice - callprice; // premium collected by selling puts - premium debited to buy calls

		buycallcbep = callstrike + netcredit;
		sellputcbep = putstrike - netcredit;
		futuresBEP = futuresShortPrice + netcredit;

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

		sellput = SellPut.builder().aya(putprice).diya(0.0).strike(putstrike).build();
		buycall = BuyCall.builder().aya(0.0).diya(callprice).strike(callstrike).build();
		generate();
	}
}
