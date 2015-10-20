package sample;

import java.util.List;
import java.util.Map;

public class OHLC {

	List<Double> calculatedSma2Pds;

	List<Double> calculatedSma3Pds;

	List<Double> calculatedZeroLagMacd;

	List<Map<String, Double>> heikinAshiList;

	public OHLC(List<Double> calculatedSma2Pds, List<Double> calculatedSma3Pds, List<Double> calculatedZeroLagMacd,
			List<Map<String, Double>> heikinAshiList) {
		this.calculatedSma2Pds = calculatedSma2Pds;
		this.calculatedSma3Pds = calculatedSma3Pds;
		this.calculatedZeroLagMacd = calculatedZeroLagMacd;
		this.heikinAshiList = heikinAshiList;
	}

	@Override
	public String toString() {
		return "OHLC [calculatedSma2Pds=" + calculatedSma2Pds + ", calculatedSma3Pds=" + calculatedSma3Pds
				+ ", calculatedZeroLagMacd=" + calculatedZeroLagMacd + ", heikinAshiList=" + heikinAshiList + "]";
	}

}
