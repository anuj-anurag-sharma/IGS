package tp;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.joda.time.DateTime;

public class Driver {

	public static void main(String[] args) {
		final Queue<Tick> queue = new ConcurrentLinkedQueue<Tick>();
		Thread workerThread = new WorkerThread(queue);
		workerThread.start();
		Subscriber subscriber = new Subscriber() {
			@Override
			public void onUpdate(double value) {
				queue.add(new Tick(value, DateTime.now()));
			}
		};
		subscriber.subscribe();
//		System.err.println(list.size());

	}

}

class Tick {

	Double dbl;

	DateTime time;

	public Tick(Double dbl, DateTime time) {
		this.dbl = dbl;
		this.time = time;
	}

	@Override
	public String toString() {
		return "Tick [dbl=" + dbl + ", time=" + time + "]";
	}

}

class WorkerThread extends Thread {
	
	boolean initialStart = true;
	
	Queue<Tick> queue;

	private int sleepTime;

	public WorkerThread(Queue<Tick> list ) {
		this.queue = list;
	}

	@Override
	public void run() {
		while (true) {
			if(initialStart){
				int currentSec = DateTime.now().getSecondOfMinute();
				if(currentSec < 30){
					sleepTime = 30 - currentSec;
				}
				else {
					sleepTime = 59 - currentSec;
				}
				initialStart = false;
			}
			else {
				sleepTime = 30;
			}
			System.out.println("sleep time set to "+ sleepTime);
			double max = 0.0;
			try {
				Thread.sleep(sleepTime * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Queue size before:  "+queue.size());
			for (Tick tick : queue) {
				System.out.println("Removing "+tick);
				max = Math.max(max, tick.dbl);
				queue.remove(tick);
			}
			System.out.println("Queue size after:  "+queue.size());
			System.out.println("Maximum : "+max);
		}
	}
}
