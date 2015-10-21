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
		double[] dbl = new double[26];
		for (int i = 1; i <= 26; i++) {
			dbl[i - 1] = (double) i * 100.00;
		}
		CalculationUtil util = new CalculationUtil();
		double z = util.getZeroLagMacd(dbl, 12, 26, 9);
		System.out.println(z);
	}

	// midPrice is midCLosePrice
	// p is 12
	// q is 26
	// r is 9
	public double getZeroLagMacd(double[] midPrice, int p, int q, int r) {

		double zeroLagEMAP = getZeroLagPoint(midPrice, p);
		double zeroLagEMAQ = getZeroLagPoint(midPrice, q);
		double zeroLagMacd = zeroLagEMAP - zeroLagEMAQ;

		this.zeroLagMacd.add(zeroLagMacd);

		Double[] temp = new Double[this.zeroLagMacd.size()];
		double zeroLagEMASingal = getZeroLagPoint(ArrayUtils.toPrimitive(this.zeroLagMacd.toArray(temp)), r);
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
		if(outReal.length - periods < 0){
			return outReal[outReal.length - 1];
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
