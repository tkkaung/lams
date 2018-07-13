package ntu.celt.eUreka2.internal;

import java.util.Arrays;

public class MathStatistic {
	Double[] data;
	double size;

	public MathStatistic(Double[] data) {
		this.data = data;
		size = data.length;
	}

	public double getMean() {
		double sum = 0.0;
		for (double a : data)
			sum += a;
		return sum / size;
	}

	public double getVariance() {
		double mean = getMean();
		double temp = 0;
		for (double a : data)
			temp += (mean - a) * (mean - a);
		return temp / (size-1);
	}

	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	public double median() {
		double[] b = new double[data.length];
		System.arraycopy(data, 0, b, 0, b.length);
		Arrays.sort(b);

		if (data.length % 2 == 0) {
			return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
		} else {
			return b[b.length / 2];
		}
	}

}
