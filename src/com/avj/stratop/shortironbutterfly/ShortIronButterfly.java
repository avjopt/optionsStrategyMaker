package com.avj.stratop.shortironbutterfly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.avj.stratop.common.Constants;
import com.avj.stratop.common.OptionsStrategy;

public class ShortIronButterfly extends OptionsStrategy { // Short Strangle + Long Strangle

	private Double strikediff;
	private Double spotat;
	private Double bcallPrice;
	private Double bputPrice;
	private Double scallPrice;
	private Double sputPrice;

	private Double bcallStrike;
	private Double bputStrike;
	private Double scallStrike;
	private Double sputStrike;

	private Double bcallIbep;
	private Double bputIbep;
	private Double scallIbep;
	private Double sputIbep;

	private Double bcallCbep;
	private Double bputCbep;
	private Double scallCbep;
	private Double sputCbep;

	private Double lotsize;
	private Double netinvested;
	private String disp = null;

	public ShortIronButterfly(String str) {
		this.disp = str;
	}

	private void generate() {
		List<Double> spots = new ArrayList<>();
		spots.add(spotat);

		if (!spots.contains(scallStrike)) {
			spots.add(scallStrike);
		}
		if (!spots.contains(sputStrike)) {
			spots.add(sputStrike);
		}
		if (!spots.contains(bcallStrike)) {
			spots.add(bcallStrike);
		}
		if (!spots.contains(bputStrike)) {
			spots.add(bputStrike);
		}
		if (!spots.contains(bcallIbep)) {
			spots.add(bcallIbep);
		}
		if (!spots.contains(bputIbep)) {
			spots.add(bputIbep);
		}
		if (!spots.contains(scallIbep)) {
			spots.add(scallIbep);
		}
		if (!spots.contains(sputIbep)) {
			spots.add(sputIbep);
		}
		if (!spots.contains(bcallCbep)) {
			spots.add(bcallCbep);
		}
		if (!spots.contains(bputCbep)) {
			spots.add(bputCbep);
		}
		if (!spots.contains(scallCbep)) {
			spots.add(scallCbep);
		}
		if (!spots.contains(sputCbep)) {
			spots.add(sputCbep);
		}

		Double start = bputStrike;
		while (true) {
			if (start >= bcallStrike) {
				break;
			}
			start += strikediff;
			if (!spots.contains(start)) {
				spots.add(start);
			}
		}
		// max strike
		Double max = bcallStrike;
		for (int i = 0; i < 8; i++) {
			max += strikediff;
			if (spots.contains(max)) {
				spots.add(max);
			}
		}
		// min strike
		Double min = bputStrike;
		for (int i = 0; i < 8; i++) {
			min -= strikediff;
			if (!spots.contains(min)) {
				spots.add(min);
			}
		}

		Collections.sort(spots);
		String header = "Spot\tStrikeWillbe\tAya\tDiya\tPL" + "\tStrikeWillbe\tAya\tDiya\tPL"
				+ "\tStrikeWillbe\tAya\tDiya\tPL"
				+ "\tStrikeWillbe\tAya\tDiya\tPL\tPandL\tPLPerLot\tTotalInvestment\tROI";
		System.out.println(header);
		for (Double spot : spots) {

			SellCall scall = SellCall.builder().aya(scallPrice).diya(0.0).strike(scallStrike).build();
			if (spot < scallStrike) {
				scall.setWhereami(Constants.OTM);
				scall.setDiya(0.0);
			} else if (spot.equals(scallStrike)) {
				scall.setWhereami(Constants.ATM);
				scall.setDiya(0.0);
			} else if (spot > scallStrike) {
				scall.setWhereami(Constants.ITM);
				scall.setDiya(spot - scallStrike);
			}
			scall.classify();
			scall.setPandl(roundoff(scall.getAya() - scall.getDiya()));

			SellPut sput = SellPut.builder().aya(sputPrice).diya(0.0).strike(sputStrike).build();
			if (spot < sputStrike) {
				sput.setWhereami(Constants.ITM);
				sput.setDiya(sputStrike - spot);
			} else if (spot.equals(sputStrike)) {
				sput.setWhereami(Constants.ATM);
				sput.setDiya(0.0);
			} else if (spot > sputStrike) {
				sput.setWhereami(Constants.OTM);
				sput.setDiya(0.0);
			}
			sput.classify();
			sput.setPandl(roundoff(sput.getAya() - sput.getDiya()));

			BuyCall bcall = BuyCall.builder().aya(0.0).diya(bcallPrice).strike(bcallStrike).build();
			if (spot < bcallStrike) {
				bcall.setWhereami(Constants.OTM);
				bcall.setAya(0.0);
			} else if (spot.equals(bcallStrike)) {
				bcall.setWhereami(Constants.ATM);
				bcall.setAya(0.0);
			} else if (spot > bcallStrike) {
				bcall.setWhereami(Constants.ITM);
				bcall.setAya(spot - bcallStrike);
			}
			bcall.classify();
			bcall.setPandl(roundoff(bcall.getAya() - bcall.getDiya()));

			BuyPut bput = BuyPut.builder().aya(0.0).diya(bputPrice).strike(bputStrike).build();
			if (spot < bputStrike) {
				bput.setWhereami(Constants.ITM);
				bput.setAya(bputStrike - spot);
			} else if (spot.equals(bputStrike)) {
				bput.setWhereami(Constants.ATM);
				bput.setAya(0.0);
			} else if (spot > bputStrike) {
				bput.setWhereami(Constants.OTM);
				bput.setAya(0.0);
			}
			bput.classify();
			bput.setPandl(roundoff(bput.getAya() - bput.getDiya()));

			ShortIronButterflyRow.builder().buycall(bcall).buyput(bput).sellcall(scall).sellput(sput).build();
			Double netPL = bcall.getPandl() + bput.getPandl() + scall.getPandl() + sput.getPandl();
			netPL = roundoff(netPL);
			Double plPerLot = roundoff(netPL * lotsize);
			Double roi = roundoff(plPerLot * 100.0 / netinvested);

			StringBuilder sb = new StringBuilder();
			sb.append(spot + "\t" + scall.getDes() + "\t" + scall.getAya() + "\t" + scall.getDiya() + "\t"
					+ scall.getPandl());
			sb.append(sput.getDes() + "\t" + sput.getAya() + "\t" + sput.getDiya() + "\t" + sput.getPandl());
			sb.append("\t"+bcall.getDes() + "\t" + bcall.getAya() + "\t" + bcall.getDiya() + "\t" + bcall.getPandl());
			sb.append("\t"+bput.getDes() + "\t" + bput.getAya() + "\t" + bput.getDiya() + "\t" + bput.getPandl());
			sb.append("\t" + netPL + "\t" + plPerLot + "\t" + netinvested + "\t" + roi);
			System.out.println("\n" + sb.toString());
		}

	}

	@Override
	public void evaluate() {
		Scanner scanner = new Scanner(System.in);

		System.out.println(disp);

		System.out.println("Spot price: ");
		String strspotat = scanner.nextLine();
		spotat = Double.valueOf(strspotat);

		System.out.println("Sell CE,PE Strike: ");
		scallStrike = Double.valueOf(scanner.nextLine());
		sputStrike = scallStrike;

		System.out.println("Sell CE @: ");
		scallPrice = Double.valueOf(scanner.nextLine());

		System.out.println("Sell PE @: ");
		sputPrice = Double.valueOf(scanner.nextLine());

		System.out.println("Buy CE Strike: ");
		bcallStrike = Double.valueOf(scanner.nextLine());

		System.out.println("Buy CE @:");
		bcallPrice = Double.valueOf(scanner.nextLine());

		System.out.println("Buy PE Strike: ");
		bputStrike = Double.valueOf(scanner.nextLine());

		System.out.println("Buy PE @:");
		bputPrice = Double.valueOf(scanner.nextLine());

		Double netcredit = (scallPrice + sputPrice) - (bcallPrice + bputPrice);

		scallIbep = scallStrike + scallPrice;
		sputIbep = sputStrike - sputPrice;
		bcallIbep = bcallStrike + bcallPrice;
		bputIbep = bputStrike - bputPrice;

		scallCbep = scallStrike + netcredit;
		sputCbep = sputStrike - netcredit;
		bcallCbep = bcallStrike + netcredit;
		bputCbep = bputStrike - netcredit;

		System.out.println("Lot size: ");
		String lsstr = scanner.nextLine();
		lotsize = Double.valueOf(lsstr);

		System.out.println("Strike Difference: ");
		String sdiff = scanner.nextLine();
		strikediff = Double.valueOf(sdiff);

		System.out.println("Total investment: ");
		String mstr = scanner.nextLine();
		netinvested = Double.valueOf(mstr);

		System.out.println("=========================================================================================");

		generate();
	}

}
