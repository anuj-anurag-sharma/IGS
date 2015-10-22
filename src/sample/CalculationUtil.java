package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class CalculationUtil {

	public Core core = new Core();
	public static String H_OPEN = "HeikinOPEN";
	public static String H_CLOSE = "HeikinCLOSE";
	public List<Double> zeroLagMacd = new ArrayList<>();

	public static void main(String[] args) {
		double[] array = /*
							 * { 11230.6, 11229.6, 11225.5, 11229.5, 11226.1,
							 * 11207.6, 11199.7, 11193.6, 11198.3, 11186.1,
							 * 11185.5, 11187.2, 11189.5, 11188.8, 11187.7,
							 * 11186.5, 11186.5, 11181.5, 11183.9, 11192,
							 * 11193.5, 11195.1, 11190.9, 11190.3, 11193.8,
							 * 11196.6, 11196.9, 11189.9, 11198.5, 11199.1,
							 * 11197.7, 11202.5, 11205.5, 11199.6, 11199.6,
							 * 11200.1, 11204.3, 11207.8, 11209.7, 11209.5,
							 * 11212.6, 11210.4, 11206.5, 11205.1, 11200.9,
							 * 11205.9, 11200.4, 11197.2, 11191.1, 11189.9,
							 * 11190.7, 11195.4, 11197.3, 11193, 11192.9,
							 * 11194.6, 11194.3, 11189.7, 11188.1, 11184.7,
							 * 11183.8, 11182.2, 11184.8, 11183.3 };
							 */
		{ 11158.8, 11157.5, 11159.2, 11158.7, 11159.4, 11157.2, 11157.6, 11155.8, 11159.8, 11159.7, 11162.9, 11161.6,
				11157.1, 11158.6, 11162.7, 11162.8, 11162.1, 11163.6, 11161.8, 11163.7, 11160.3, 11159.3, 11158.9,
				11159.4, 11160.4, 11160.8, 11161.9, 11164.6, 11164, 11162.6, 11162.1, 11162.5, 11159.8, 11154.8,
				11154.8, 11154.5, 11156.2, 11156.1, 11156.2, 11154.8, 11155.4, 11154.7, 11154.1, 11154.4, 11153.5,
				11152.1, 11153.7, 11156, 11155.8, 11156.1, 11159.8, 11158.7, 11156.8, 11156.4, 11155.9, 11156.9,
				11156.6, 11156.3, 11156.3, 11154.5, 11154.3, 11154.5, 11153.4, 11153.5, 11154.8, 11155.6, 11156.9,
				11158.5, 11157.9, 11157.9, 11158.5, 11159.5, 11159.8, 11158.3, 11157.5, 11155.4 };
		List<Double> dbl = new ArrayList<Double>();
		for (int i = 1; i < 26; i++) {
			dbl.add(array[i - 1]);
		}
		// for (int i = 26; i < array.length; i++) {
		List<Double> dbl1 = new ArrayList<Double>(dbl);
		for (int j = 0; j < array.length - 1 - 25; j++) {
			CalculationUtil util = new CalculationUtil();
			dbl1.add(array[25 + j]);
			double z = util.getZeroLagMacd(ArrayUtils.toPrimitive(dbl1.toArray(new Double[dbl1.size()])), 12, 26, 9);
			System.out.println(z);
		}
		// }
	}

	// midPrice is midCLosePrice
	// p is 12
	// q is 26
	// r is 9
	/*
	 * public double getZeroLagMacd(double[] midPrice, int p, int q, int r) {
	 * int max = Math.max(p, Math.max(q, r)); if (midPrice.length >= max) {
	 * double zeroLagEMAP = getZeroLagPoint(midPrice, p); double zeroLagEMAQ =
	 * getZeroLagPoint(midPrice, q); double zeroLagMacd = zeroLagEMAP -
	 * zeroLagEMAQ;
	 * 
	 * this.zeroLagMacd.add(zeroLagMacd);
	 * 
	 * Double[] temp = new Double[this.zeroLagMacd.size()]; double
	 * zeroLagEMASingal =
	 * getZeroLagPoint(ArrayUtils.toPrimitive(this.zeroLagMacd.toArray(temp)),
	 * r); return zeroLagMacd - zeroLagEMASingal; } this.zeroLagMacd.add(0.0);
	 * return 0.0; }
	 */

	public double getZeroLagMacd(double[] midPrice, int p, int q, int r) {

		double zeroLagEMAP = getZeroLagPoint(midPrice, p);
		double zeroLagEMAQ = getZeroLagPoint(midPrice, q);
		double zeroLagMacd = zeroLagEMAP - zeroLagEMAQ;

		if (zeroLagMacd != 0) {
			this.zeroLagMacd.add(zeroLagMacd);
		}
		double zeroLagEMASingal = 0;
		if (this.zeroLagMacd.size() >= r) {
			Double[] temp = new Double[this.zeroLagMacd.size()];
			zeroLagEMASingal = getZeroLagPoint(ArrayUtils.toPrimitive(this.zeroLagMacd.toArray(temp)), r);
		}

		return zeroLagMacd - zeroLagEMASingal;

	}

	private double getZeroLagPoint(double[] midPrice, int p) {

		double[] ema1Array = getEMA(0, midPrice.length - 1, midPrice, p);
		double ema2Value = getEMAForLastBar(0, ema1Array.length - 1, ema1Array, p);
		double emaLastValue = getNotZeroLast(ema1Array, 1);
		double diff = emaLastValue - ema2Value;
		double zeroLagEMA = emaLastValue + diff;
		return zeroLagEMA;
	}

	public double[] getEMA(int startIdx, int endIdx, double[] inputValue, int emaPeriod) {
		double expFactor = (2.0 / Double.valueOf((1 + emaPeriod)));
		int counter = 0;
		int startingNoOfZero = 0;
		double[] ema = new double[inputValue.length];

		for (double d : inputValue) {
			if (d == 0) {
				startingNoOfZero++;
				continue;
			}
			if (counter < emaPeriod - 1) {
				ema[counter] = 0.0;
			} else if (counter == emaPeriod - 1) {
				ema[counter] = StatUtils.mean(inputValue, startingNoOfZero, emaPeriod);
			} else {
				ema[counter] = (ema[counter - 1] * (1 - expFactor)) + (d * expFactor);
			}
			counter++;
		}
		return ema;

	}

	public double getEMAForLastBar(int startIdx, int endIdx, double[] inputValue, int emaPeriod) {

		double[] outReal = null;
		outReal = getEMA(startIdx, endIdx, inputValue, emaPeriod);
		return getNotZeroLast(outReal, emaPeriod);

	}

	private double getNotZeroLast(double[] outReal, int periods) {
		for (int i = outReal.length - 1; i > 0; i--) {
			if (outReal[i] != 0) {
				return outReal[i];
			}
		}
		return outReal[outReal.length - periods];
	}

	public double[] getSMA(int startIdx, int endIdx, double[] inputValue, int smaPeriod) {

		double[] outReal = new double[inputValue.length];
		MInteger outBegIdx = new MInteger();
		MInteger outNBElement = new MInteger();
		core.sma(startIdx, endIdx, inputValue, smaPeriod, outBegIdx, outNBElement, outReal);
		return outReal;

	}

	public double getSMAForLastBar(int startIdx, int endIdx, double[] inputValue, int smaPeriod) {

		double[] outReal = new double[inputValue.length];
		MInteger outBegIdx = new MInteger();
		MInteger outNBElement = new MInteger();
		core.sma(startIdx, endIdx, inputValue, smaPeriod, outBegIdx, outNBElement, outReal);
		return outReal[outNBElement.value - 1];

	}

	public Map<String, Double> getHeikinAshi(double[] open, double[] high, double[] close, double[] low) {

		Map<String, Double> retMap = new HashMap<>();

		if (open != null && close != null && close.length == open.length) {
			double openPrice = 0;
			int index = close.length - 2;
			if (open.length == 1) {
				openPrice = open[0];
			} else if (open.length > 1) {
				openPrice = (open[index] + close[index]) / 2;
			}
			retMap.put(H_OPEN, openPrice);
		}

		if (open != null && close != null && high != null && low != null && close.length == open.length
				&& open.length == high.length && high.length == low.length) {

			double closePrice = 0;
			int index = close.length - 1;
			if (open.length > 1) {
				closePrice = (open[index] + close[index] + high[index] + low[index]) / 2;
			}
			retMap.put(H_CLOSE, closePrice);
		} else {
			System.out.println(" All Array size is not equal or one of them is null not calculating close Price");
		}

		return retMap;
	}

}
