package com.avj.stratop.longstraddle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.avj.stratop.common.Constants;
import com.avj.stratop.common.DataDumper;
import com.avj.stratop.common.OptionsStrategy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LongStraddle extends OptionsStrategy {

	private BuyCall buycall;
	private BuyPut buyput;
	private Double diff;
	private Double lotsize;
	private Double netdebit;
	private Double totalinvested;
	private Double spotat;
	private List<LongStraddleRow> spotlist = new ArrayList<>();
	private String disp = null;

	private void generate() {
		List<Double> spots = new ArrayList<>();
		Double callstrike = buycall.getStrike();
		Double putstrike = buyput.getStrike();

		spots.add(callstrike);

		if (!spotat.equals(callstrike)) {
			spots.add(spotat);
		}
		
		// first add the mid range between calls and puts
		Double next = putstrike;
		while (true) {
			next += diff;
			if (next >= callstrike) {
				break;
			}
			spots.add(next);
		}
		// then add the range below puts
		Double cl = buyput.getCbep();
		spots.add(cl);
		spots.add(buyput.getIbep());
		Double lowest = putstrike;
		while (true) {
			lowest = lowest - diff;
			spots.add(lowest);
			if (lowest <= cl) {
				break;
			}
		}
		// go 3 strikes lower
		for (int i = 0; i < 5; i++) {
			lowest = lowest - diff;
			spots.add(lowest);
		}
		// call side BEP + 3 strikes
		Double calliBEP = buycall.getIbep();
		Double callcBEP = buycall.getCbep();
		spots.add(calliBEP);
		spots.add(callcBEP);
		Double start = buycall.getStrike();
		while (start < callcBEP) {
			start = start + diff;
			spots.add(start);
		}
		for (int i = 0; i < 5; i++) {
			start = start + diff;
			spots.add(start);
		}
		Collections.sort(spots);
		spots.forEach(s -> {
			System.out.println(s);
			BuyCall callrow = BuyCall.builder().diya(buycall.getDiya()).aya(0.0).strike(buycall.getStrike()).build();
			BuyPut putrow = BuyPut.builder().diya(buyput.getDiya()).aya(0.0).strike(buyput.getStrike()).build();
			LongStraddleRow spot = LongStraddleRow.builder().spotprice(s).call(callrow).put(putrow).build();
			spotlist.add(spot);
		});

		for (LongStraddleRow spot : spotlist) {
			Double spotprice = spot.getSpotprice();

			BuyCall call = spot.getCall();
			if (spotprice < call.getStrike()) {
				call.setWhereami(Constants.OTM);
				call.setAya(0.0);
			} else if (spotprice.equals(call.getStrike())) {
				call.setWhereami(Constants.ATM);
				call.setAya(0.0);
			} else if (spotprice > call.getStrike()) {
				call.setWhereami(Constants.ITM);
				call.setAya(spotprice - call.getStrike());
			}
			call.classify();
			call.setPandl(call.getAya() - call.getDiya());

			BuyPut put = spot.getPut();
			if (spotprice > put.getStrike()) {
				put.setWhereami(Constants.OTM);
				put.setAya(0.0);
			} else if (spotprice.equals(put.getStrike())) {
				put.setWhereami(Constants.ATM);
				put.setAya(0.0);
			} else if (spotprice < put.getStrike()) {
				put.setWhereami(Constants.ITM);
				put.setAya(put.getStrike() - spotprice);
			}
			put.classify();
			put.setPandl(roundoff(put.getAya() - put.getDiya()));

			spot.setPandl(roundoff(call.getPandl() + put.getPandl()));
			spot.setPlPerLot(roundoff(spot.getPandl() * lotsize));
			Double roi = (spot.getPlPerLot() / totalinvested) * 100.0;
			spot.setRoi(roundoff(roi));
			System.out.print(spot.getSpotprice() + "\t" + call.getDes() + "\t" + call.getAya() + "\t" + call.getDiya()
					+ "\t" + call.getPandl());
			System.out.println("\t" + put.getDes() + "\t" + put.getAya() + "\t" + put.getDiya() + "\t" + put.getPandl()
					+ "\t" + spot.getPandl() + "\t" + spot.getPlPerLot() + "\t" + spot.getRoi());

		}

	}

	public LongStraddle(String str) {
		this.disp = str;
	}

	@Override
	public void evaluate() {
		Scanner myObj = new Scanner(System.in);

		System.out.println(disp);

		System.out.println("Enter spot: ");
		String spotstr = myObj.nextLine();
		spotat = Double.valueOf(spotstr);

		System.out.println("Buy CE,PE Strike ");
		String callstrike = myObj.nextLine();

		System.out.println("Buy CE @: ");
		String callprice = myObj.nextLine();

		String putstrike = callstrike;

		System.out.println("Buy PE @:");
		String putprice = myObj.nextLine();

		System.out.println("Lot size: ");
		String lsstr = myObj.nextLine();
		lotsize = Double.valueOf(lsstr);

		System.out.println("Strike Difference: ");
		String sdiff = myObj.nextLine();
		diff = Double.valueOf(sdiff);

		System.out.println("Total investment: ");
		String nstr = myObj.nextLine();
		totalinvested = Double.valueOf(nstr);

		System.out.println("=========================================================================================");
		Double callpaid = Double.valueOf(callprice);
		Double putpaid = Double.valueOf(putprice);
		netdebit = callpaid + putpaid;
		buycall = BuyCall.builder().aya(0.0).diya(callpaid).strike(Double.valueOf(callstrike)).netdebit(netdebit)
				.build();
		buycall.genBEP();

		buyput = BuyPut.builder().aya(0.0).diya(putpaid).strike(Double.valueOf(putstrike)).netdebit(netdebit).build();
		buyput.genBEP();

		generate();

	}

}
