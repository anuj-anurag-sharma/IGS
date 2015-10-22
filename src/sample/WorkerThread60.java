package sample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;

import au.com.bytecode.opencsv.CSVWriter;

public class WorkerThread60 extends Thread {

	boolean initialStart = true;

	Queue<OHLC> ohlcList = null;

	List<Double> openList = new ArrayList<Double>();

	List<Double> highList = new ArrayList<Double>();

	List<Double> lowList = new ArrayList<Double>();

	List<Double> closeList = new ArrayList<Double>();

	// List<Double> calculatedSma2Pds;

	// List<Double> calculatedSma3Pds;

	List<Double> calculatedZeroLagMacd;

	// List<Map<String, Double>> heikinAshiList;

	File file = new File("60SecondsChart.csv");

	CSVWriter csvWriter = null;

	Queue<Tick> queue;

	private int sleepTime;

	CalculationUtil util;

	public WorkerThread60(Queue<Tick> queue, Queue<OHLC> ohlcList) {
		this.queue = queue;
		this.ohlcList = ohlcList;
		try {
			csvWriter = new CSVWriter(new FileWriter(file), ',');
			String[] line = { "Date", "Open", "High", "Low", "Close", "Zero Lag" };
			csvWriter.writeNext(line);
			csvWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// calculatedSma2Pds = new ArrayList<Double>();
		// calculatedSma3Pds = new ArrayList<Double>();
		calculatedZeroLagMacd = new ArrayList<Double>();
		// heikinAshiList = new ArrayList<Map<String, Double>>();
		util = new CalculationUtil();
	}

	@Override
	public void run() {
		/*
		 * for (int i = 0; i < 26; i++) { closeList.add(i * 1000.0);
		 * openList.add(i * 1000.0); highList.add(i * 1000.0); lowList.add(i *
		 * 1000.0);
		 * util.getZeroLagMacd(ArrayUtils.toPrimitive(closeList.toArray(new
		 * Double[closeList.size()])), 12, 26, 9); }
		 */
		while (true) {

			if (initialStart) {
				int currentSec = DateTime.now().getSecondOfMinute();
				sleepTime = 60 - currentSec;
				initialStart = false;
			} else {
				sleepTime = 60;
			}
			try {
				Thread.sleep(sleepTime * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (queue) {
				int i = 0;
				double open = 0.0;
				double high = 0.0;
				double low = 0.0;
				double close = 0.0;
				int size = queue.size();
				for (Tick tick : queue) {
					if (i == 0) {
						open = tick.getOfferOpen();
						low = tick.getOfferLow();
					} else {
						low = Math.min(low, tick.getOfferLow());
					}
					if (i == size - 1) {
						close = tick.getOfferClose();
					}
					high = Math.max(high, tick.getOfferHigh());

					i++;
					queue.remove(tick);
				}
				// TickObject object = new TickObject(open, close, high, low);
				// System.out.println(DateTime.now() + ":" + object);
				openList.add(open);
				highList.add(high);
				lowList.add(low);
				closeList.add(close);
				// calculateSma();
				calculateZeroLagMacd();
				// calculateHeikinAshi();
				OHLC ohlc = new OHLC(null, null, calculatedZeroLagMacd, null);
				ohlcList.add(ohlc);
				String[] line = formCsvArray(open, high, low, close,
						calculatedZeroLagMacd.get(calculatedZeroLagMacd.size() - 1));
				csvWriter.writeNext(line);
				try {
					csvWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	/*
	 * private void calculateHeikinAshi() { Map<String, Double> heikinAshi =
	 * util.getHeikinAshi( ArrayUtils.toPrimitive(openList.toArray(new
	 * Double[openList.size()])), ArrayUtils.toPrimitive(highList.toArray(new
	 * Double[highList.size()])), ArrayUtils.toPrimitive(closeList.toArray(new
	 * Double[closeList.size()])), ArrayUtils.toPrimitive(lowList.toArray(new
	 * Double[lowList.size()]))); heikinAshiList.add(heikinAshi); }
	 */

	/*
	 * private void calculateSma() { double sma2Pds = util.getSMAForLastBar(0,
	 * closeList.size() - 1, ArrayUtils.toPrimitive(closeList.toArray(new
	 * Double[closeList.size()])), 2); calculatedSma2Pds.add(sma2Pds); double
	 * sma3Pds = util.getSMAForLastBar(0, closeList.size() - 1,
	 * ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])),
	 * 3); calculatedSma3Pds.add(sma3Pds); }
	 */

	private String[] formCsvArray(double open, double high, double low, double close, Double double1) {
		String[] line = new String[6];
		line[0] = String.valueOf(DateTime.now());
		line[1] = String.valueOf(open);
		line[2] = String.valueOf(high);
		line[3] = String.valueOf(low);
		line[4] = String.valueOf(close);
		line[5] = String.valueOf(double1);
		return line;
	}

	private void calculateZeroLagMacd() {
		if (closeList.size() < 26) {
			calculatedZeroLagMacd.add(0.0);
			return;
		}
		double zeroLagMacd = new CalculationUtil()
				.getZeroLagMacd(ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])), 12, 26, 9);
		calculatedZeroLagMacd.add(zeroLagMacd);
	}
}
