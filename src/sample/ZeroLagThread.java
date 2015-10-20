package sample;

import java.util.List;

public class ZeroLagThread extends Thread {

	private List<Double> calculatedZeroLagMacd;

	public ZeroLagThread(List<Double> calculatedZeroLagMacd) {
		super.setName("ZeroLagThread");
		this.calculatedZeroLagMacd = calculatedZeroLagMacd;
	}

	@Override
	public void run() {
		while (true) {
			try {
				processZeroLag();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void processZeroLag() throws InterruptedException {

		synchronized (calculatedZeroLagMacd) {
			if (calculatedZeroLagMacd.isEmpty()) {
				calculatedZeroLagMacd.wait();
			}
			int index = calculatedZeroLagMacd.size() - 1;
			if (index > 1) {
				System.out.println("ZeroLagMacd Thread - start");
				System.out.println(calculatedZeroLagMacd.get(index));
				System.out.println(calculatedZeroLagMacd.get(index - 1));
				System.out.println(calculatedZeroLagMacd.get(index - 2));
				System.out.println("ZeroLagMacd Thread - end");
				calculatedZeroLagMacd.wait();
			}
		}

	}

}
