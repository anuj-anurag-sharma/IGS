package sample;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class OrderProcessingThread extends Thread {

	private BlockingQueue<OHLC> ohlcList;

	public OrderProcessingThread(BlockingQueue<OHLC> ohlcList) {
		super.setName("OrderProcessingThread");
		this.ohlcList = ohlcList;
	}

	@Override
	public void run() {
		while (true) {
			OHLC ohlc = null;
			try {
				ohlc = ohlcList.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName() + " starts####");
			// processZeroLag(ohlc.calculatedZeroLagMacd);
			// processSma(ohlc.calculatedSma);
			// processHeikinAshi(ohlc.heikinAshiList);
			String decision = decide(ohlc);
			System.out.println("*****" + ohlc + "*****");
			System.out.println("Decision : " + decision);
			System.out.println(Thread.currentThread().getName() + " ends####");
			System.out.println("-------------------------------------------------------------------------------------------------------------");
		}
	}

	private String decide(OHLC ohlc) {
		String decision = "NO TRADE";
		List<Map<String, Double>> hal = ohlc.heikinAshiList; // C1
		List<Double> sma2 = ohlc.calculatedSma2Pds; // C2
		List<Double> sma3 = ohlc.calculatedSma3Pds; // C3
		List<Double> ml = ohlc.calculatedZeroLagMacd; // C4
		boolean haFlag = false;
		boolean macdFlag = false;
		if (hal.size() <= 1 || ml.size() <= 1) {
			System.out.println("Decision Point 1");
			decision = "NO TRADE";
			return decision;
		}
		Double ha1_Open = hal.get(hal.size() - 1).get(IndicatorUtil.H_OPEN);
		Double ha2_Close = hal.get(hal.size() - 2).get(IndicatorUtil.H_CLOSE);

		haFlag = ha1_Open >= ha2_Close;

		Double ml1 = ml.get(ml.size() - 1);
		Double ml2 = ml.get(ml.size() - 2);
		macdFlag = ml1 > ml2;

		// System.out.println("haFlag : " + haFlag);
		// System.out.println("macdFlag : " + macdFlag);
		// System.out.println("in decide " + (haFlag && macdFlag));

		if ((haFlag && macdFlag)) {
			if (ml1 > 0 && ml2 > 0) {
				if ((ml1 / ml2) - 1 >= 0.05 && (ml1 / ml2 - 1) <= 1.0) {
					if ((sma2.get(sma2.size() - 1) >= sma2.get(sma2.size() - 2))
							&& (sma3.get(sma3.size() - 1) >= sma3.get(sma3.size() - 2))) {
						System.out.println("Decision Point 2");
						decision = "TRADE IN ABOVE DIRECTION";
						return decision;
					} else {
						System.out.println("Decision Point 3");
						decision = "NO TRADE";
						return decision;
					}
				} else {
					System.out.println("Decision Point 4");
					decision = "NO TRADE";
					return decision;
				}
			} else if (ml1 < 0 && ml2 < 0) {
				if ((ml1 / ml2) >= 0.3 && (ml1 / ml2 <= 0.95)) {
					if ((sma2.get(sma2.size() - 1) >= sma2.get(sma2.size() - 2))
							&& (sma3.get(sma3.size() - 1) >= sma3.get(sma3.size() - 2))) {
						System.out.println("Decision Point 5");
						decision = "TRADE IN ABOVE DIRECTION";
						return decision;
					} else {
						decision = "NO TRADE";
						return decision;
					}
				} else {
					decision = "NO TRADE";
					return decision;
				}
			}
		}

		else if ((haFlag == false && macdFlag == false)) {
			if (ml1 > 0 && ml2 > 0) {
				if ((ml1 / ml2) >= 0.3 && (ml1 / ml2) <= 0.95) {
					if ((sma2.get(sma2.size() - 1) <= sma2.get(sma2.size() - 2))
							&& (sma3.get(sma3.size() - 1) <= sma3.get(sma3.size() - 2))) {
						System.out.println("Decision Point 6");
						decision = "TRADE IN BELOW DIRECTION";
						return decision;
					} else {
						System.out.println("Decision Point 7");
						decision = "NO TRADE";
						return decision;
					}
				} else {
					System.out.println("Decision Point 8");
					decision = "NO TRADE";
					return decision;
				}
			} else if (ml1 < 0 && ml2 < 0) {
				if ((ml1 / ml2 - 1) >= 0.05 && (ml1 / ml2 - 1 <= 1.0)) {
					if ((sma2.get(sma2.size() - 1) <= sma2.get(sma2.size() - 2))
							&& (sma3.get(sma3.size() - 1) <= sma3.get(sma3.size() - 2))) {
						System.out.println("Decision Point 9");
						decision = "TRADE IN BELOW DIRECTION";
						return decision;
					} else {
						System.out.println("Decision Point 10");
						decision = "NO TRADE";
						return decision;
					}
				} else {
					System.out.println("Decision Point 11");
					decision = "NO TRADE";
					return decision;
				}
			}
		}
		System.out.println("Decision Point 12");
		return decision;
	}

	private void processZeroLag(List<Double> closeList) {

		int index = closeList.size() - 1;
		if (index > 1) {
			System.out.println("ZeroLagMacd - start");
			System.out.println(closeList.get(index));
			System.out.println(closeList.get(index - 1));
			System.out.println(closeList.get(index - 2));
			System.out.println("ZeroLagMacd - end");
		}

		else {
			System.out.println("ZeroLag processing - not happening because index is " + index);
		}

	}

	private void processSma(List<Double> closeList) {

		int index = closeList.size() - 1;
		if (index > 1) {
			System.out.println("SMA - start");
			System.out.println(closeList.get(index));
			System.out.println(closeList.get(index - 1));
			System.out.println(closeList.get(index - 2));
			System.out.println("SMA - end");
		} else {
			System.out.println("SMA processing - not happening because index is " + index);
		}

	}

	private void processHeikinAshi(List<Map<String, Double>> heikinAshiList) {

		int index = heikinAshiList.size() - 1;
		if (index > 1) {
			System.out.println("Heikin Ashi - start");
			System.out.println(heikinAshiList.get(index).get(IndicatorUtil.H_CLOSE) + ":"
					+ heikinAshiList.get(index).get(IndicatorUtil.H_OPEN));
			System.out.println(heikinAshiList.get(index - 1).get(IndicatorUtil.H_CLOSE) + ":"
					+ heikinAshiList.get(index - 1).get(IndicatorUtil.H_OPEN));
			System.out.println(heikinAshiList.get(index - 2).get(IndicatorUtil.H_CLOSE) + ":"
					+ heikinAshiList.get(index - 2).get(IndicatorUtil.H_OPEN));
			System.out.println("Heikin Ashi - end");
		} else {
			System.out.println("Heikin Ashi processing - not happening because index is " + index);
		}
	}

}
