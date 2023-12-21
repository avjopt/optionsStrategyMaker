package com.avj.stratop.shortstrangle;

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
public class ShortStrangle extends OptionsStrategy {

	private SellCall sellcall;
	private SellPut sellput;
	private Double diff;
	private Double lotsize;
	private Double margin;
	private List<ShortStrangleRow> spotlist = new ArrayList<>();
	private String disp = null;

	private void generate() {
		Set<Double> spots = new HashSet<>();
		Double callstrike = sellcall.getStrike();
		Double putstrike = sellput.getStrike();

		// first add the mid range between calls and puts
		spots.add(putstrike);
		spots.add(callstrike);
		Double incr = putstrike;
		while (incr <= callstrike) {
			incr += diff;
			spots.add(incr);
		}
		// then add the range below puts
		Double cl = sellput.getCombinedBEP();
		spots.add(sellput.getIndividulBEP());
		spots.add(cl);
		Double lowest = putstrike;
		while (true) {
			lowest = lowest - diff;
			spots.add(lowest);
			if (lowest <= cl) {
				break;
			}
		}
		// go 3 strikes lower
		for (int i = 0; i < 3; i++) {
			lowest = lowest - diff;
			spots.add(lowest);
		}
		// call side BEP + 3 strikes
		Double calliBEP = sellcall.getIndividulBEP();
		Double callcBEP = sellcall.getCombinedBEP();
		spots.add(calliBEP);
		spots.add(callcBEP);
		Double start = sellcall.getStrike();
		while (start <= callcBEP) {
			start = start + diff;
			spots.add(start);
		}
		for (int i = 0; i < 3; i++) {
			start = start + diff;
			spots.add(start);
		}
		List<Double> sintlist = new ArrayList<>(spots);
		Collections.sort(sintlist);
		sintlist.forEach(s -> {
			SellCall callrow = SellCall.builder().diya(0.0).aya(sellcall.getAya()).strike(sellcall.getStrike()).build();
			SellPut putrow = SellPut.builder().diya(0.0).aya(sellput.getAya()).strike(sellput.getStrike()).build();
			ShortStrangleRow spot = ShortStrangleRow.builder().spotprice(s).call(callrow).put(putrow).build();
			spotlist.add(spot);
		});
		
		List<ShortStrangleOutput> soplist = new ArrayList<>();

		for (ShortStrangleRow spot : spotlist) {
			Double spotprice = spot.getSpotprice();

			SellCall call = spot.getCall();
			if (spotprice < call.getStrike()) {
				call.setWhereami(Constants.OTM);
				call.setDiya(0.0);
			} else if (spotprice.equals(call.getStrike())) {
				call.setWhereami(Constants.ATM);
				call.setDiya(0.0);
			} else if (spotprice > call.getStrike()) {
				call.setWhereami(Constants.ITM);
				call.setDiya(spotprice - call.getStrike());
			}
			call.classify();
			call.setPandl(call.getAya() - call.getDiya());

			SellPut put = spot.getPut();
			if (spotprice > put.getStrike()) {
				put.setWhereami(Constants.OTM);
				put.setDiya(0.0);
			} else if (spotprice.equals(put.getStrike())) {
				put.setWhereami(Constants.ATM);
				put.setDiya(0.0);
			} else if (spotprice < put.getStrike()) {
				put.setWhereami(Constants.ITM);
				put.setDiya(put.getStrike() - spotprice);
			}
			put.classify();
			put.setPandl(roundoff(put.getAya() - put.getDiya()));

			spot.setPandl(roundoff(call.getPandl() + put.getPandl()));
			spot.setPlPerLot(roundoff(spot.getPandl() * lotsize));
			Double roi = (spot.getPlPerLot() / margin) * 100.0;
			spot.setRoi(roundoff(roi));
			System.out.print(spot.getSpotprice() + "\t" + call.getDes() + "\t" + call.getAya() + "\t" + call.getDiya()
					+ "\t" + call.getPandl());
			System.out.println("\t" + put.getDes() + "\t" + put.getAya() + "\t" + put.getDiya() + "\t" + put.getPandl()
					+ "\t" + spot.getPandl() + "\t" + spot.getPlPerLot() + "\t" + spot.getRoi());
			ShortStrangleOutput sop = ShortStrangleOutput.builder().spot(spot.getSpotprice())
					.calldes(call.getDes()).callAya(call.getAya()).callDiya(call.getDiya()).callPandL(call.getPandl())
					.putdes(put.getDes()).putAya(put.getAya()).putDiya(put.getDiya()).putPandL(put.getPandl())
					.spotPandL(spot.getPandl()).spotPandLPerLot(spot.getPlPerLot()).roi(spot.getRoi()).build();
			soplist.add(sop);

		}

		DataDumper dumper = new DataDumper();
		dumper.setShortStrangleList(soplist);
		dumper.dump("ShortStrangle");
	}

	public ShortStrangle(String str) {
		this.disp = str;
	}

	@Override
	public void evaluate() {
		Scanner myObj = new Scanner(System.in);

		System.out.println(disp);

		System.out.println("Sell CE Strike ");
		String callstrike = myObj.nextLine();

		System.out.println("Sell CE @: ");
		String callprice = myObj.nextLine();

		System.out.println("Sell PE Strike: ");
		String putstrike = myObj.nextLine();

		System.out.println("Sell PE @:");
		String putprice = myObj.nextLine();

		System.out.println("Lot size: ");
		String lsstr = myObj.nextLine();
		lotsize = Double.valueOf(lsstr);

		System.out.println("Strike Difference: ");
		String sdiff = myObj.nextLine();
		diff = Double.valueOf(sdiff);

		System.out.println("Total investment/margin required: ");
		String mstr = myObj.nextLine();
		margin = Double.valueOf(mstr);

		System.out.println("=========================================================================================");
		sellcall = SellCall.builder().aya(Double.valueOf(callprice)).diya(0.0).strike(Double.valueOf(callstrike)).build();
		sellcall.genNetCredit(Double.valueOf(putprice));
		sellcall.genBEP();

		sellput = SellPut.builder().aya(Double.valueOf(putprice)).diya(0.0).strike(Double.valueOf(putstrike)).build();
		sellput.genNetCredit(Double.valueOf(callprice));
		sellput.genBEP();

		generate();

	}

}
