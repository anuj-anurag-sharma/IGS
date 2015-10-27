package sample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import au.com.bytecode.opencsv.CSVWriter;

public class WorkerThread30 extends Thread {

	private static final Logger LOGGER = Logger.getLogger(WorkerThread30.class);

	boolean initialStart = true;

	Queue<OHLC> ohlcList = null;

	List<Double> openList = new ArrayList<Double>();

	List<Double> highList = new ArrayList<Double>();

	List<Double> lowList = new ArrayList<Double>();

	List<Double> closeList = new ArrayList<Double>();

	List<Double> calculatedSma2Pds;

	List<Double> calculatedSma3Pds;

	List<Double> calculatedZeroLagMacd;

	List<Map<String, Double>> heikinAshiList;

	File file = new File("30SecondsChart.csv");

	CSVWriter csvWriter = null;

	Queue<Tick> queue;

	private int sleepTime;

	public WorkerThread30(Queue<Tick> queue, Queue<OHLC> ohlcList) {
		this.setName("Worker-30");
		this.queue = queue;
		this.ohlcList = ohlcList;
		calculatedSma2Pds = new ArrayList<Double>();
		calculatedSma3Pds = new ArrayList<Double>();
		calculatedZeroLagMacd = new ArrayList<Double>();
		heikinAshiList = new ArrayList<Map<String, Double>>();
		try {
			csvWriter = new CSVWriter(new FileWriter(file), ',');
			String[] line = { "Date", "Open", "High", "Low", "Close", "Zero Lag", "Sma2 Period", "Sma3 Period",
					"Heikin Ashi Open", "Heikin Ashi Close" };
			csvWriter.writeNext(line);
			csvWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {

			if (initialStart) {
				int currentSec = DateTime.now().getSecondOfMinute();
				if (currentSec < 30) {
					sleepTime = 30 - currentSec;
				} else {
					sleepTime = 59 - currentSec;
				}
				LOGGER.info(Thread.currentThread().getName() + " sleep time set to " + sleepTime);
				initialStart = false;
			} else {
				sleepTime = 30;
			}
			try {
				Thread.sleep(sleepTime * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			LOGGER.info(Thread.currentThread().getName() + " is awake at " + DateTime.now());
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
				openList.add(open);
				highList.add(high);
				lowList.add(low);
				closeList.add(close);
				calculateSma();
				calculateZeroLagMacd();
				calculateHeikinAshi();
				// ensure that zeroLagMacd is calculated for 1 hr.
				if (calculatedZeroLagMacd.size() > 119 && DateTime
						.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"))).getHourOfDay() >= 8) {
					OHLC ohlc = new OHLC(calculatedSma2Pds, calculatedSma3Pds, calculatedZeroLagMacd, heikinAshiList);
					ohlcList.add(ohlc);
					if (calculatedZeroLagMacd.size() > 200) {
						calculatedZeroLagMacd = calculatedZeroLagMacd.subList(81, 201);
					}
				}
				exportTo30SecCsv(csvWriter, open, high, low, close);
			}

		}
	}

	private void exportTo30SecCsv(CSVWriter writer, double open, double high, double low, double close) {
		String[] line = formCsvArray(open, high, low, close,
				calculatedZeroLagMacd.get(calculatedZeroLagMacd.size() - 1),
				calculatedSma2Pds.get(calculatedSma2Pds.size() - 1),
				calculatedSma3Pds.get(calculatedSma3Pds.size() - 1),
				heikinAshiList.get(heikinAshiList.size() - 1).get(CalculationUtil.H_OPEN),
				heikinAshiList.get(heikinAshiList.size() - 1).get(CalculationUtil.H_CLOSE));
		writer.writeNext(line);
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String[] formCsvArray(double open, double high, double low, double close, Double double1, Double double2,
			Double double3, Double double4, Double double5) {
		String[] line = new String[10];
		line[0] = String.valueOf(DateTime.now());
		line[1] = String.valueOf(open);
		line[2] = String.valueOf(high);
		line[3] = String.valueOf(low);
		line[4] = String.valueOf(close);
		line[5] = String.valueOf(double1);
		line[6] = String.valueOf(double2);
		line[7] = String.valueOf(double3);
		line[8] = String.valueOf(double4);
		line[9] = String.valueOf(double5);
		return line;
	}

	private void calculateHeikinAshi() {
		Map<String, Double> heikinAshi = new CalculationUtil().getHeikinAshi(
				ArrayUtils.toPrimitive(openList.toArray(new Double[openList.size()])),
				ArrayUtils.toPrimitive(highList.toArray(new Double[highList.size()])),
				ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])),
				ArrayUtils.toPrimitive(lowList.toArray(new Double[lowList.size()])));
		heikinAshiList.add(heikinAshi);
	}

	private void calculateSma() {
		if (closeList.size() < 2) {
			calculatedSma2Pds.add(0.0);
		} else {
			double sma2Pds = new CalculationUtil().getSMAForLastBar(0, closeList.size() - 1,
					ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])), 2);
			calculatedSma2Pds.add(sma2Pds);
		}
		if (closeList.size() < 3) {
			calculatedSma3Pds.add(0.0);
		} else {
			double sma3Pds = new CalculationUtil().getSMAForLastBar(0, closeList.size() - 1,
					ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])), 3);
			calculatedSma3Pds.add(sma3Pds);
		}
	}

	private void calculateZeroLagMacd() {
		double zeroLagMacd = new CalculationUtil()
				.getZeroLagMacd(ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])), 12, 26, 9);
		calculatedZeroLagMacd.add(zeroLagMacd);
	}
}
