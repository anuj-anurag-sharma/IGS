package tp;

public class RandNumGenThread implements Runnable {

	Subscriber subs;

	public RandNumGenThread(Subscriber subs) {
		this.subs = subs;
	}

	@Override
	public void run() {
		while(true){
		try {
			Thread.sleep(40000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		subs.send(Math.random());
		}
	}

}
