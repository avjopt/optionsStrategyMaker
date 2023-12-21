package com.avj.stratop.bullcallspread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.avj.stratop.common.Constants;
import com.avj.stratop.common.OptionsStrategy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BullCallSpread extends OptionsStrategy {

	private SellCall sellcall;
	private BuyCall buycall;
	private Double strikediff;
	private Double lotsize;
	private Double netinvested;
	private Double netDebit;
	private Double spotat;
	private Double cbep;
	private String disp;

	public BullCallSpread(String str) {
		this.disp = str;
	}

	private void calculateNetDebit() {
		netDebit = buycall.getDiya() - sellcall.getAya();
		System.out.println("Net debit: " + netDebit);
	}

	private void calculateCombinedBEP() {
		cbep = buycall.getStrike() + netDebit;
		System.out.println("Combined BEP: " + cbep);

	}

	private Double findNearestLowerStrikeOfSpot(Double spot) {
		Double start = buycall.getStrike();
		while (true) {
			start -= strikediff;
			if (start < spot) {
				break;
			}
		}
		return start;
	}

	private void generate() {
		List<Double> spots = new ArrayList<>();
		spots.add(spotat);
		Double nearestLowerStrike = findNearestLowerStrikeOfSpot(spotat);
		spots.add(nearestLowerStrike);
		double spotd = nearestLowerStrike;
		for (int i = 0; i < 5; i++) {
			spotd -= strikediff;
			spots.add(spotd);
		}

		spotd = nearestLowerStrike;
		while (spotd < buycall.getStrike()) {
			spotd += strikediff;
			spots.add(spotd);
		}
		spots.add(buycall.getStrike());
		Double start = buycall.getStrike();
		while (start <= sellcall.getStrike()) {
			start += strikediff;
			spots.add(start);
		}
		for (int i = 0; i < 5; i++) {
			start += strikediff;
			spots.add(start);
		}
		spots.add(buycall.getIbep());
		spots.add(cbep);
		spots.add(sellcall.getIbep());
		Collections.sort(spots);
		List<BullCallSpreadRow> spotlist = new ArrayList<>();
		for (Double sp : spots) {
			BuyCall buycallrow = BuyCall.builder().aya(0.0).diya(buycall.getDiya()).strike(buycall.getStrike()).build();
			SellCall sellcallrow = SellCall.builder().aya(sellcall.getAya()).diya(0.0).strike(sellcall.getStrike())
					.build();
			BullCallSpreadRow spot = BullCallSpreadRow.builder().spotprice(sp).sellcall(sellcallrow).buycall(buycallrow).build();
			spotlist.add(spot);
		}

		for (BullCallSpreadRow spot : spotlist) {
			Double spotprice = spot.getSpotprice();

			BuyCall bcall = spot.getBuycall();
			Double buyCallStrike = bcall.getStrike();
			if (spotprice < buyCallStrike) {
				bcall.setWhereami(Constants.OTM);
				bcall.setAya(0.0);
			} else if (spotprice.equals(buyCallStrike)) {
				bcall.setWhereami(Constants.ATM);
				bcall.setAya(0.0);
			} else if (spotprice > buyCallStrike) {
				bcall.setWhereami(Constants.ITM);
				bcall.setAya(spotprice - buyCallStrike);
			}
			bcall.classify();
			bcall.setPandl(roundoff(bcall.getAya() - bcall.getDiya()));
			System.out.print(spot.getSpotprice() + "\t" + bcall.getDes() + "\t" + roundoff(bcall.getAya()) + "\t"
					+ roundoff(bcall.getDiya()) + "\t" + roundoff(bcall.getPandl()));

			SellCall scall = spot.getSellcall();
			Double sellCallStrike = scall.getStrike();
			if (spotprice < sellCallStrike) {
				scall.setWhereami(Constants.OTM);
				scall.setDiya(0.0);
			} else if (spotprice.equals(sellCallStrike)) {
				scall.setWhereami(Constants.ATM);
				scall.setDiya(0.0);
			} else if (spotprice > sellCallStrike) {
				scall.setWhereami(Constants.ITM);
				scall.setDiya(spotprice - sellCallStrike);
			}
			scall.classify();
			scall.setPandl(scall.getAya() - scall.getDiya());

			System.out.print(spot.getSpotprice() + "\t" + scall.getDes() + "\t" + roundoff(scall.getAya()) + "\t"
					+ roundoff(scall.getDiya()) + "\t" + roundoff(scall.getPandl()) + "\t");

			spot.setPandl(roundoff(scall.getPandl() + bcall.getPandl()));
			spot.setPlPerLot(roundoff(spot.getPandl() * lotsize));
			Double roi = roundoff((spot.getPlPerLot() / netinvested) * 100);
			spot.setRoi(roi);
			System.out.println("\t" + spot.getPandl() + "\t" + roundoff(spot.getPlPerLot()) + "\t" + spot.getRoi());
		}

	}

	@Override
	public void evaluate() {

		Scanner myObj = new Scanner(System.in);

		System.out.println(disp);
		System.out.println("Spot: ");
		String spotatstr = myObj.nextLine();
		spotat = Double.valueOf(spotatstr);

		System.out.println("Buy CE Strike: ");
		String buycallstrike = myObj.nextLine();

		System.out.println("Buy CE @: ");
		String buycallprice = myObj.nextLine();

		System.out.println("Sell CE Strike: ");
		String sellcallstrike = myObj.nextLine();

		System.out.println("Sell CE @: ");
		String sellcallprice = myObj.nextLine();

		System.out.println("Lot size: ");
		String lotsizestr = myObj.nextLine();
		lotsize = Double.valueOf(lotsizestr);

		System.out.println("Strike Difference: ");
		String sdiffstr = myObj.nextLine();
		strikediff = Double.valueOf(sdiffstr);

		System.out.println("Total (margin + premium paid): ");
		String margin = myObj.nextLine();
		netinvested = Double.valueOf(margin);

		System.out.println("*****************************************************************************************");
		buycall = BuyCall.builder().aya(0.0).diya(Double.valueOf(buycallprice)).strike(Double.valueOf(buycallstrike))
				.build();
		buycall.genBEP();

		sellcall = SellCall.builder().aya(Double.valueOf(sellcallprice)).diya(0.0)
				.strike(Double.valueOf(sellcallstrike)).build();
		sellcall.genBEP();

		calculateNetDebit();
		calculateCombinedBEP();

		generate();

	}

}
