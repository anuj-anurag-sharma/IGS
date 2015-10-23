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
		double[] array = { 11158.8, 11157.5, 11159.2, 11158.7, 11159.4, 11157.2, 11157.6, 11155.8, 11159.8, 11159.7,
				11162.9, 11161.6, 11157.1, 11158.6, 11162.7, 11162.8, 11162.1, 11163.6, 11161.8, 11163.7, 11160.3,
				11159.3, 11158.9, 11159.4, 11160.4, 11160.8, 11161.9, 11164.6, 11164, 11162.6, 11162.1, 11162.5,
				11159.8, 11154.8, 11154.8, 11154.5, 11156.2, 11156.1, 11156.2, 11154.8, 11155.4, 11154.7, 11154.1,
				11154.4, 11153.5, 11152.1, 11153.7, 11156, 11155.8, 11156.1, 11159.8, 11158.7, 11156.8, 11156.4,
				11155.9, 11156.9, 11156.6, 11156.3, 11156.3, 11154.5, 11154.3, 11154.5, 11153.4, 11153.5, 11154.8,
				11155.6, 11156.9, 11158.5, 11157.9, 11157.9, 11158.5, 11159.5, 11159.8, 11158.3, 11157.5, 11155.4,
				11156.9 };
		CalculationUtil util = new CalculationUtil();
		List<Double> dbl = new ArrayList<Double>();
		for (int i = 0; i < array.length; i++) {
			dbl.add(array[i]);
			System.out.println(
					util.getZeroLagMacd(ArrayUtils.toPrimitive(dbl.toArray(new Double[dbl.size()])), 12, 26, 9));
		}

	}

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
		if (emaLastValue == 0 || ema2Value == 0) {
			return 0.0;
		}
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
		if (outReal.length < periods) {
			return 0.0;
		}
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
