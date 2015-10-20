package tp;

public class Subscriber {

	public void send(double random) {
		System.out.println(random + " is received");
		onUpdate(random);
	}

	public void onUpdate(double random) {
		
	}
	
	public void subscribe(){
		Thread t = new Thread(new RandNumGenThread(this),"RandomGenThread");
		t.start();
	}

}
