package sample;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.joda.time.DateTime;

import com.iggroup.api.positions.sprintmarkets.createSprintMarketPositionV1.Direction;

public class OrderProcessingThread60 extends Thread {

	private BlockingQueue<OHLC> ohlcList;

	public OrderProcessingThread60(BlockingQueue<OHLC> ohlcList) {
		super.setName("OrderProcessingThread60");
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
			System.out.println(Thread.currentThread().getName() + " starts at " + DateTime.now() + "--------");
			Decision decision = decide(ohlc);
			if (decision != Decision.NOTHING) {
				Direction direction = null;
				switch (decision) {
				case ABOVE: {
					direction = Direction.BUY;
					break;
				}
				default: {
					direction = Direction.SELL;
					break;
				}
				}
				ABCD.trade(direction);
			}
			System.out.println("*****" + ohlc + "*****");
			System.out.println("Decision : " + decision);
			System.out.println(Thread.currentThread().getName() + " ends at " + DateTime.now() + "--------");
			System.out.println(
					"-------------------------------------------------------------------------------------------------------------");
		}
	}

	private Decision decide(OHLC ohlc) {
		Decision decision = Decision.NOTHING;
		List<Double> ml = ohlc.calculatedZeroLagMacd; // C4
		if (ml.size() <= 2) {
			System.out.println("Decision Point 1");
			decision = Decision.NOTHING;
			return decision;
		}

		Double ml1 = ml.get(ml.size() - 1); // t
		Double ml2 = ml.get(ml.size() - 2); // t-1
		Double ml3 = ml.get(ml.size() - 3); // t-2

		if (ml1 > 0 && ml2 > 0 && ml3 > 0) {
			if (ml3 > ml2 && ml2 < ml1) {
				if (0.05 <= (ml1 / ml2 - 1) && (ml1 / ml2 - 1) <= 1.0) {
					System.out.println("Decision Point 2");
					decision = Decision.ABOVE;
					return decision;
				} else {
					decision = Decision.NOTHING;
					System.out.println("Decision Point 3");
					return decision;
				}
			} else if (ml3 < ml2 && ml2 > ml1) {
				if (0.3 <= ml1 / ml2 && (ml1 / ml2) <= 0.95) {
					System.out.println("Decision Point 4");
					decision = Decision.BELOW;
					return decision;
				} else {
					System.out.println("Decision Point 5");
					decision = Decision.NOTHING;
					return decision;
				}
			} else {
				System.out.println("Decision Point 6");
				decision = Decision.NOTHING;
				return decision;
			}
		} else if (ml1 < 0 && ml2 < 0 && ml3 < 0) {
			if (ml3 > ml2 && ml2 < ml1) {
				if (0.3 <= ml1 / ml2 && (ml1 / ml2) <= 0.95) {
					System.out.println("Decision Point 7");
					decision = Decision.ABOVE;
					return decision;
				} else {
					System.out.println("Decision Point 8");
					decision = Decision.NOTHING;
					return decision;
				}
			} else if (ml3 < ml2 && ml2 > ml1) {
				if (0.05 <= (ml1 / ml2 - 1) && (ml1 / ml2 - 1) <= 1.0) {
					System.out.println("Decision Point 9");
					decision = Decision.BELOW;
					return decision;
				} else {
					decision = Decision.NOTHING;
					System.out.println("Decision Point 10");
					return decision;
				}
			} else {
				System.out.println("Decision Point 11");
				decision = Decision.NOTHING;
				return decision;
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
