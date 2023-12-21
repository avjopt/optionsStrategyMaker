package com.avj.stratop.main;

import java.util.Scanner;

import com.avj.stratop.bearcallspread.BearCallSpread;
import com.avj.stratop.bearputspread.BearPutSpread;
import com.avj.stratop.bullcallspread.BullCallSpread;
import com.avj.stratop.bullputspread.BullPutSpread;
import com.avj.stratop.coveredcall.CoveredCall;
import com.avj.stratop.coveredput.CoveredPut;
import com.avj.stratop.longcollar.LongCollar;
import com.avj.stratop.longironbutterfly.LongIronButterfly;
import com.avj.stratop.longironcondor.LongIronCondor;
import com.avj.stratop.longstraddle.LongStraddle;
import com.avj.stratop.longstrangle.LongStrangle;
import com.avj.stratop.shortcollar.ShortCollar;
import com.avj.stratop.shortironbutterfly.ShortIronButterfly;
import com.avj.stratop.shortironcondor.ShortIronCondor;
import com.avj.stratop.shortstraddle.ShortStraddle;
import com.avj.stratop.shortstrangle.ShortStrangle;

/**
 * @author avjoshi
 *
 */
public class StrategyMain {

	private static final int COVERED_CALL = 1; // tested
	private static final int LONG_COLLAR = 2; // tested
	private static final int COVERED_PUT = 3; // tested
	private static final int SHORT_COLLAR = 4; // tested
	private static final int SHORT_STRANGLE = 5; // tested
	private static final int LONG_STRANGLE = 6; //tested
	private static final int SHORT_STRADDLE = 7; //tested
	private static final int LONG_STRADDLE = 8; // tested
	private static final int BEAR_CALL_SPREAD = 9; // tested
	
	private static final int BEAR_PUT_SPREAD = 10;//tested
	
	private static final int BULL_CALL_SPREAD = 11; // tested
	private static final int BULL_PUT_SPREAD = 12; // tested
	
	private static final int SHORT_IRON_BUTTERFLY = 13; // tested
	
	private static final int LONG_IRON_BUTTERFLY = 14; //tested
	private static final int SHORT_IRON_CONDOR = 15; //tested
	private static final int LONG_IRON_CONDOR = 16; //tested

	private static void invalidStrategyExit() {
		System.out.println("Non-existing strategy input; exiting...");
		System.exit(-1);
	}

	public static void main(String[] args) {

		try {
			Scanner myObj = new Scanner(System.in);
		// @formatter:off
		System.out.println("Enter number for strategy: " // @formatter: off
				+ "\n 1.  Covered Call  "
				+ "\n 2.  Long Collar "
				+ "\n 3.  Covered Put "
				+ "\n 4.  Short Collar "
				+ "\n 5.  Short Strangle "
				+ "\n 6.  Long Strangle "
				+ "\n 7.  Short Straddle "
				+ "\n 8.  Long Straddle "
				+ "\n 9.  Bear Call Spread "
				+ "\n 10. Bear Put Spread "
				+ "\n 11. Bull Call Spread "
				+ "\n 12. Bull Put Spread "
				+ "\n 13. Short Iron Butterfly "
				+ "\n 14. Long Iron Butterfly "
				+ "\n 15. Short Iron Condor "
				+ "\n 16. Long Iron Condor "
				+ "\n ");
		// @formatter:on
			String st = myObj.nextLine();
			int stlong = Integer.valueOf(st.trim());

			switch (stlong) {
			case SHORT_STRANGLE:
				ShortStrangle shortStrangle = new ShortStrangle("Enter details for short strangle: ");
				shortStrangle.evaluate();
				break;
			case LONG_STRANGLE:
				LongStrangle longStrangle = new LongStrangle("Enter details for long strangle: ");
				longStrangle.evaluate();
				break;
			case SHORT_STRADDLE:
				ShortStraddle shortStraddle = new ShortStraddle("Enter details for short straddle: ");
				shortStraddle.evaluate();
				break;
			case LONG_STRADDLE: // straddle = same strike strangle
				LongStraddle longStraddle = new LongStraddle("Enter details for long straddle: ");
				longStraddle.evaluate();
				break;
			case BEAR_CALL_SPREAD: // Sell Call, Buy OTM call
				BearCallSpread bearCallSpread = new BearCallSpread("Enter details for Bear Call Spread: ");
				bearCallSpread.evaluate();
				break;
			case BEAR_PUT_SPREAD: // Buy put, sell OTM put
				BearPutSpread bearPutSpread = new BearPutSpread("Enter details for Bear Put Spread: ");
				bearPutSpread.evaluate();
				break;
			case BULL_CALL_SPREAD:
				BullCallSpread bullCallSpread = new BullCallSpread("Enter details for Bull Call Spread: ");
				bullCallSpread.evaluate();
				break;
			case BULL_PUT_SPREAD:
				BullPutSpread bullPutSpread = new BullPutSpread("Enter details for Bull Put Spread: ");
				bullPutSpread.evaluate();
				break;
			case COVERED_CALL:
				CoveredCall coveredCall = new CoveredCall("Enter details for Covered call: ");
				coveredCall.evaluate();
				break;
			case LONG_COLLAR:
				LongCollar longCollar = new LongCollar("Enter details for Long Collar: ");
				longCollar.evaluate();
				break;
			case COVERED_PUT:
				CoveredPut coveredPut = new CoveredPut("Enter details for Covered Put: ");
				coveredPut.evaluate();
				break;
			case SHORT_COLLAR:
				ShortCollar shortCollar = new ShortCollar("Enter details for Short Collar: ");
				shortCollar.evaluate();
				break;
			case SHORT_IRON_BUTTERFLY:
				ShortIronButterfly shortIronButterfly = new ShortIronButterfly("Enter details for Short Iron Butterfly: ");
				shortIronButterfly.evaluate();
				break;
			case LONG_IRON_BUTTERFLY:
				LongIronButterfly longIronButterfly  = new LongIronButterfly("Enter details for Long Iron Butterfly: ");
				longIronButterfly.evaluate();
				break;
			case SHORT_IRON_CONDOR:
				ShortIronCondor shortIronCondor = new ShortIronCondor("Enter details for Short Iron Condor: ");
				shortIronCondor.evaluate();
				break;
			case LONG_IRON_CONDOR:
				LongIronCondor longIronCondor = new LongIronCondor("Enter details for Long Iron Condor: ");
				longIronCondor.evaluate();
				break;
			default:
				StrategyMain.invalidStrategyExit();
				break;

			}
		} catch (Exception e) {
			System.out.println("Caught exception: " + e.getMessage());
		}
	}

}
