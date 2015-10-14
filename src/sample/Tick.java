package sample;

import org.joda.time.DateTime;

public class Tick {

	private DateTime dateTime;

	private Double offerOpen;

	private Double offerHigh;

	private Double offerLow;

	private Double offerClose;

	private Long tickCount;

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Double getOfferOpen() {
		return offerOpen;
	}

	public void setOfferOpen(Double offerOpen) {
		this.offerOpen = offerOpen;
	}

	public Double getOfferHigh() {
		return offerHigh;
	}

	public void setOfferHigh(Double offerHigh) {
		this.offerHigh = offerHigh;
	}

	public Double getOfferLow() {
		return offerLow;
	}

	public void setOfferLow(Double offerLow) {
		this.offerLow = offerLow;
	}

	public Double getOfferClose() {
		return offerClose;
	}

	public void setOfferClose(Double offerClose) {
		this.offerClose = offerClose;
	}

	public Long getTickCount() {
		return tickCount;
	}

	public void setTickCount(Long tickCount) {
		this.tickCount = tickCount;
	}

	@Override
	public String toString() {
		return "Tick [dateTime=" + dateTime + ", offerOpen=" + offerOpen + ", offerHigh=" + offerHigh + ", offerLow="
				+ offerLow + ", offerClose=" + offerClose + ", tickCount=" + tickCount + "]";
	}

}
