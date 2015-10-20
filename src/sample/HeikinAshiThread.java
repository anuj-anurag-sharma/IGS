package sample;

import java.util.List;
import java.util.Map;

public class HeikinAshiThread extends Thread {

	private List<Map<String, Double>> heikinAshiList;

	public HeikinAshiThread(List<Map<String, Double>> heikinAshiList) {
		super.setName("HeikinAshiThread");
		this.heikinAshiList = heikinAshiList;
	}

	@Override
	public void run() {
		while (true) {
			try {
				processHeikinAshi();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void processHeikinAshi() throws InterruptedException {

		synchronized (heikinAshiList) {
			if (heikinAshiList.isEmpty()) {
				heikinAshiList.wait();
			}
			int index = heikinAshiList.size() - 1;
			if (index > 1) {
				System.out.println("Heikin Ashi Thread - start");
				System.out.println(heikinAshiList.get(index).get(IndicatorUtil.H_CLOSE) + ":"
						+ heikinAshiList.get(index).get(IndicatorUtil.H_OPEN));
				System.out.println(heikinAshiList.get(index - 1).get(IndicatorUtil.H_CLOSE) + ":"
						+ heikinAshiList.get(index - 1).get(IndicatorUtil.H_OPEN));
				System.out.println(heikinAshiList.get(index - 2).get(IndicatorUtil.H_CLOSE) + ":"
						+ heikinAshiList.get(index - 2).get(IndicatorUtil.H_OPEN));
				System.out.println("Heikin Ashi Thread - end");
				heikinAshiList.wait();
			}
		}
	}

}