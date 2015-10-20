package sample;

import org.joda.time.DateTime;

public class Tick {

	public Tick(DateTime dateTime, Double offerOpen, Double offerHigh, Double offerLow, Double offerClose,
			Long tickCount) {
		this.dateTime = dateTime;
		this.open = offerOpen;
		this.high = offerHigh;
		this.low = offerLow;
		this.close = offerClose;
	}

	public Tick() {

	}

	private DateTime dateTime;

	private Double open;

	private Double high;

	private Double low;

	private Double close;

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Double getOfferOpen() {
		return open;
	}

	public void setOfferOpen(Double offerOpen) {
		this.open = offerOpen;
	}

	public Double getOfferHigh() {
		return high;
	}

	public void setOfferHigh(Double offerHigh) {
		this.high = offerHigh;
	}

	public Double getOfferLow() {
		return low;
	}

	public void setOfferLow(Double offerLow) {
		this.low = offerLow;
	}

	public Double getOfferClose() {
		return close;
	}

	public void setOfferClose(Double offerClose) {
		this.close = offerClose;
	}

	@Override
	public String toString() {
		return "Tick [dateTime=" + dateTime + ", offerOpen=" + open + ", offerHigh=" + high + ", offerLow=" + low
				+ ", offerClose=" + close + "]";
	}

}
