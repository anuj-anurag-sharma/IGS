package sample;

import java.util.List;

public class SmaThread extends Thread {

	private List<Double> calculatedSma;

	public SmaThread(List<Double> calculatedSma) {
		super.setName("SmaThread");
		this.calculatedSma = calculatedSma;
	}

	@Override
	public void run() {
		while (true) {
			try {
				processSma();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void processSma() throws InterruptedException {

		synchronized (calculatedSma) {
			if (calculatedSma.isEmpty()) {
				calculatedSma.wait();
			}
			int index = calculatedSma.size() - 1;
			if (index > 1) {
				System.out.println("SMA Thread - start");
				System.out.println(calculatedSma.get(index));
				System.out.println(calculatedSma.get(index - 1));
				System.out.println(calculatedSma.get(index - 2));
				System.out.println("SMA Thread - end");
				calculatedSma.wait();
			}
		}

	}

}
