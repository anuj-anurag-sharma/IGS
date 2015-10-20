package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;

public class WorkerThread extends Thread {

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

	Queue<Tick> queue;

	private int sleepTime;

	IndicatorUtil util;

	public WorkerThread(Queue<Tick> queue, Queue<OHLC> ohlcList) {
		this.queue = queue;
		this.ohlcList = ohlcList;
		calculatedSma2Pds = new ArrayList<Double>();
		calculatedSma3Pds = new ArrayList<Double>();
		calculatedZeroLagMacd = new ArrayList<Double>();
		heikinAshiList = new ArrayList<Map<String, Double>>();
		util = new IndicatorUtil();
	}

	@Override
	public void run() {
		for (int i = 0; i < 27; i++) {
			closeList.add(i * 1000.0);
			openList.add(i * 1000.0);
			highList.add(i * 1000.0);
			lowList.add(i * 1000.0);
		}
		while (true) {

			if (initialStart) {
				int currentSec = DateTime.now().getSecondOfMinute();
				if (currentSec < 30) {
					sleepTime = 30 - currentSec;
				} else {
					sleepTime = 59 - currentSec;
				}
				initialStart = false;
			} else {
				sleepTime = 30;
			}
//			System.out.println("sleep time set to " + sleepTime);
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
//				TickObject object = new TickObject(open, close, high, low);
//				System.out.println(DateTime.now() + ":" + object);
				openList.add(open);
				highList.add(high);
				lowList.add(low);
				closeList.add(close);
				calculateSma();
				calculateZeroLagMacd();
				calculateHeikinAshi();
				OHLC ohlc = new OHLC(calculatedSma2Pds, calculatedSma3Pds, calculatedZeroLagMacd, heikinAshiList);
				ohlcList.add(ohlc);
			}

		}
	}

	private void calculateHeikinAshi() {
		Map<String, Double> heikinAshi = util.getHeikinAshi(
				ArrayUtils.toPrimitive(openList.toArray(new Double[openList.size()])),
				ArrayUtils.toPrimitive(highList.toArray(new Double[highList.size()])),
				ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])),
				ArrayUtils.toPrimitive(lowList.toArray(new Double[lowList.size()])));
		heikinAshiList.add(heikinAshi);
	}

	private void calculateSma() {
		double sma2Pds = util.getSMAForLastBar(0, closeList.size() - 1,
				ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])), 2);
		calculatedSma2Pds.add(sma2Pds);
		double sma3Pds = util.getSMAForLastBar(0, closeList.size() - 1,
				ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])), 3);
		calculatedSma3Pds.add(sma3Pds);
	}

	private void calculateZeroLagMacd() {
		double zeroLagMacd = util
				.getZeroLagMacd(ArrayUtils.toPrimitive(closeList.toArray(new Double[closeList.size()])), 12, 26, 9);
		calculatedZeroLagMacd.add(zeroLagMacd);
	}
}
