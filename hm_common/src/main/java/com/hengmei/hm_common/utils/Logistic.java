package com.hengmei.hm_common.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * 曲线拟合算法！！
 * 
 * @author xdq
 *
 */
public class Logistic {

	public static final int STRAIGHT_LINE = 0, POLY2 = 1, POLY3 = 2, POLY4 = 3, EXPONENTIAL = 4, POWER = 5, LOG = 6,
			RODBARD = 7, GAMMA_VARIATE = 8, LOG2 = 9, RODBARD2 = 10, EXP_WITH_OFFSET = 11, GAUSSIAN = 12,
			EXP_RECOVERY = 13;

	private static final int CUSTOM = 20;

	public static final int IterFactor = 500;

	public static final String[] fitList = { "Straight Line", "2nd Degree Polynomial", "3rd Degree Polynomial",
			"4th Degree Polynomial", "Exponential", "Power", "Log", "Rodbard", "Gamma Variate", "y = a+b*ln(x-c)",
			"Rodbard (NIH Image)", "Exponential with Offset", "Gaussian", "Exponential Recovery" };
//	位置是 从0 开始
	public static final String[] fList = { "y = a+bx", "y = a+bx+cx^2", "y = a+bx+cx^2+dx^3", "y = a+bx+cx^2+dx^3+ex^4",
			"y = a*exp(bx)", "y = ax^b", "y = a*ln(bx)", "y = d+(a-d)/(1+(x/c)^b)", "y = a*(x-b)^c*exp(-(x-b)/d)",
			"y = a+b*ln(x-c)", "y = d+(a-d)/(1+(x/c)^b)", "y = a*exp(-bx) + c",
			"y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))", "y=a*(1-exp(-b*x)) + c" };

	private static final double alpha = -1.0; 
	private static final double beta = 0.5; 
	private static final double gamma = 2.0; 
	private static final double root2 = 1.414214;

	private static DecimalFormat[] df;
	private static DecimalFormat[] sf;
	private static DecimalFormatSymbols dfs;

	private int fit; 
	private double[] xData, yData; 
	private int numPoints; 
	private int numParams; 
	private int numVertices; 
	private int worst;
	private int nextWorst; 
	private int best; 
	private double[][] simp;
	private double[] next; 
	private int numIter; 
	private int maxIter; 
	private int restarts; 

	private String R2;

	private static int defaultRestarts = 2;
	private int nRestarts; 
	private static double maxError = 1e-10; 
	private double[] initialParams; 
	private long time; 
	private String customFormula;
	private int customParamCount;
	
	private double[] initialValues;
//	long ss = (long) 1e-10;
	
	public Logistic(double[] xData, double[] yData) {
		this.xData = xData;
		this.yData = yData;
		numPoints = xData.length;
	}

	public void doFit(int fitType) {
		doFit(fitType, false);
	}

	public void doFit(int fitType, boolean showSettings) {
		if (fitType < STRAIGHT_LINE || (fitType > EXP_RECOVERY && fitType != CUSTOM))
			throw new IllegalArgumentException("Invalid fit type");
		int saveFitType = fitType;
		if (fitType == RODBARD2) {
			double[] temp;
			temp = xData;
			xData = yData;
			yData = temp;
			fitType = RODBARD;
		}


		fit = fitType;
		initialize();
		if (initialParams != null) {
			for (int i = 0; i < numParams; i++)
				simp[0][i] = initialParams[i];
			initialParams = null;
		}
		if (showSettings)
			
			;
		long startTime = System.currentTimeMillis();
		restart(0);

		numIter = 0;
		boolean done = false;
		double[] center = new double[numParams];
		while (!done) {
			numIter++;
			for (int i = 0; i < numParams; i++)
				center[i] = 0.0;
			
			for (int i = 0; i < numVertices; i++)
				if (i != worst)
					for (int j = 0; j < numParams; j++)
						center[j] += simp[i][j];
			
			for (int i = 0; i < numParams; i++) {
				center[i] /= numParams;
				next[i] = center[i] + alpha * (simp[worst][i] - center[i]);
			}
			sumResiduals(next);
			
			if (next[numParams] <= simp[best][numParams]) {
				newVertex();
				
				for (int i = 0; i < numParams; i++)
					next[i] = center[i] + gamma * (simp[worst][i] - center[i]);
				sumResiduals(next);
				
				if (next[numParams] <= simp	[worst][numParams])
					newVertex();
			}
			
			else if (next[numParams] <= simp[nextWorst][numParams]) {
				newVertex();
			}
			
			else {
				for (int i = 0; i < numParams; i++)
					next[i] = center[i] + beta * (simp[worst][i] - center[i]);
				sumResiduals(next);
				
				if (next[numParams] <= simp[nextWorst][numParams]) {
					newVertex();
				}
			
				else {
					for (int i = 0; i < numVertices; i++) {
						if (i != best) {
							for (int j = 0; j < numVertices; j++)
								simp[i][j] = beta * (simp[i][j] + simp[best][j]);
							sumResiduals(simp[i]);
						}
					}
				}
			}
			order();

			double rtol = 2 * Math.abs(simp[best][numParams] - simp[worst][numParams])
					/ (Math.abs(simp[best][numParams]) + Math.abs(simp[worst][numParams]) + 0.0000000001);

			if (numIter >= maxIter)
				done = true;
			else if (rtol < maxError) {
				restarts--;
				if (restarts < 0)
					done = true;
				else
					restart(best);
			}
		}
		fitType = saveFitType;
		time = System.currentTimeMillis() - startTime;
	}

	public int doCustomFit(String equation, double[] initialValues, boolean showSettings) {
		customFormula = null;
		customParamCount = 0;
		
		if (customParamCount == 0)
			return 0;
		customFormula = equation;
		String code = "var x, a, b, c, d, e;\n" + "function dummy() {}\n" + equation + ";\n"; // starts
		
		this.initialValues = initialValues;
		doFit(CUSTOM, showSettings);
		return customParamCount;
	}


	void initialize() {
		
		numParams = getNumParams();
		numVertices = numParams + 1; 
		simp = new double[numVertices][numVertices];
		next = new double[numVertices];

		double firstx = xData[0];
		double firsty = yData[0];
		double lastx = xData[numPoints - 1];
		double lasty = yData[numPoints - 1];
		double xmean = (firstx + lastx) / 2.0;
		double ymean = (firsty + lasty) / 2.0;
		double miny = firsty, maxy = firsty;
		if (fit == GAUSSIAN) {
			for (int i = 1; i < numPoints; i++) {
				if (yData[i] > maxy)
					maxy = yData[i];
				if (yData[i] < miny)
					miny = yData[i];
			}
		}
		double slope;
		if ((lastx - firstx) != 0.0)
			slope = (lasty - firsty) / (lastx - firstx);
		else
			slope = 1.0;
		double yintercept = firsty - slope * firstx;
		maxIter = IterFactor * numParams * numParams; 
		restarts = defaultRestarts;
		nRestarts = 0;
		switch (fit) {
		case STRAIGHT_LINE:
			simp[0][0] = yintercept;
			simp[0][1] = slope;
			break;
		case POLY2:
			simp[0][0] = yintercept;
			simp[0][1] = slope;
			simp[0][2] = 0.0;
			break;
		case POLY3:
			simp[0][0] = yintercept;
			simp[0][1] = slope;
			simp[0][2] = 0.0;
			simp[0][3] = 0.0;
			break;
		case POLY4:
			simp[0][0] = yintercept;
			simp[0][1] = slope;
			simp[0][2] = 0.0;
			simp[0][3] = 0.0;
			simp[0][4] = 0.0;
			break;
		case EXPONENTIAL:
			simp[0][0] = 0.1;
			simp[0][1] = 0.01;
			break;
		case EXP_WITH_OFFSET:
			simp[0][0] = 0.1;
			simp[0][1] = 0.01;
			simp[0][2] = 0.1;
			break;
		case EXP_RECOVERY:
			simp[0][0] = 0.1;
			simp[0][1] = 0.01;
			simp[0][2] = 0.1;
			break;
		case GAUSSIAN:
			simp[0][0] = miny; // a0
			simp[0][1] = maxy; // a1
			simp[0][2] = xmean; // x0
			simp[0][3] = 3.0; // sigma
			break;
		case POWER:
			simp[0][0] = 0.0;
			simp[0][1] = 1.0;
			break;
		case LOG:
			simp[0][0] = 0.5;
			simp[0][1] = 0.05;
			break;
		case RODBARD:
		case RODBARD2:// 初始化执行语句部分
			simp[0][0] = firsty;
			simp[0][1] = 1.0;
			simp[0][2] = xmean;
			simp[0][3] = lasty;
			break;
		case GAMMA_VARIATE:
		
			simp[0][0] = firstx;
			double ab = xData[getMax(yData)] - firstx;
			simp[0][2] = Math.sqrt(ab);
			simp[0][3] = Math.sqrt(ab);
			simp[0][1] = yData[getMax(yData)] / (Math.pow(ab, simp[0][2]) * Math.exp(-ab / simp[0][3]));
			break;
		case LOG2:
			simp[0][0] = 0.5;
			simp[0][1] = 0.05;
			simp[0][2] = 0.0;
			break;
		case CUSTOM:
			
			if (initialValues != null && initialValues.length >= numParams) {
				for (int i = 0; i < numParams; i++)
					simp[0][i] = initialValues[i];
			} else {
				for (int i = 0; i < numParams; i++)
					simp[0][i] = 1.0;
			}
			break;
		}
	}


	void restart(int n) {
		
		for (int i = 0; i < numParams; i++) {
			simp[0][i] = simp[n][i];
		}
		sumResiduals(simp[0]);
		double[] step = new double[numParams];
		for (int i = 0; i < numParams; i++) {
			step[i] = simp[0][i] / 2.0;
			if (step[i] == 0.0) 
				
				step[i] = 0.01;
		}
		
		double[] p = new double[numParams];
		double[] q = new double[numParams];
		for (int i = 0; i < numParams; i++) {
			p[i] = step[i] * (Math.sqrt(numVertices) + numParams - 1.0) / (numParams * root2);
			q[i] = step[i] * (Math.sqrt(numVertices) - 1.0) / (numParams * root2);
		}
		
		for (int i = 1; i < numVertices; i++) {
			for (int j = 0; j < numParams; j++) {
				simp[i][j] = simp[i - 1][j] + q[j];
			}
			simp[i][i - 1] = simp[i][i - 1] + p[i - 1];
			sumResiduals(simp[i]);
		}
		
		best = 0;
		worst = 0;
		nextWorst = 0;
		order();
		nRestarts++;
	}

	/** 获取参数的个数！！ */
	public int getNumParams() {
		switch (fit) {
		case STRAIGHT_LINE:
			return 2;
		case POLY2:
			return 3;
		case POLY3:
			return 4;
		case POLY4:
			return 5;
		case EXPONENTIAL:
			return 2;
		case POWER:
			return 2;
		case LOG:
			return 2;
		case RODBARD:
		case RODBARD2:
			return 4;
		case GAMMA_VARIATE:
			return 4;
		case LOG2:
			return 3;
		case EXP_WITH_OFFSET:
			return 3;
		case GAUSSIAN:
			return 4;
		case EXP_RECOVERY:
			return 3;
		case CUSTOM:
			return customParamCount;
		}
		return 0;
	}

	
	public double f(double[] p, double x) {
		
		return f(fit, p, x);
	}

	
	public static double f(int fit, double[] p, double x) {
		double y;
		switch (fit) {
		case STRAIGHT_LINE:
			return p[0] + p[1] * x;
		case POLY2:
			return p[0] + p[1] * x + p[2] * x * x;
		case POLY3:
			return p[0] + p[1] * x + p[2] * x * x + p[3] * x * x * x;
		case POLY4:
			return p[0] + p[1] * x + p[2] * x * x + p[3] * x * x * x + p[4] * x * x * x * x;
		case EXPONENTIAL:
			return p[0] * Math.exp(p[1] * x);
		case EXP_WITH_OFFSET:
			return p[0] * Math.exp(p[1] * x * -1) + p[2];
		case EXP_RECOVERY:
			return p[0] * (1 - Math.exp(-p[1] * x)) + p[2];
		case GAUSSIAN:
			return p[0] + (p[1] - p[0]) * Math.exp(-(x - p[2]) * (x - p[2]) / (2.0 * p[3] * p[3]));
		case POWER:
			if (x == 0.0)
				return 0.0;
			else
				return p[0] * Math.exp(p[1] * Math.log(x)); // y=ax^b
		case LOG:
			if (x == 0.0)
				x = 0.5;
			return p[0] * Math.log(p[1] * x);
		case RODBARD:
			double ex;
			if (x == 0.0)
				ex = 0.0;
			else
				ex = Math.exp(Math.log(x / p[2]) * p[1]);
			y = p[0] - p[3];
			y = y / (1.0 + ex);
			return y + p[3];
		case GAMMA_VARIATE:
			if (p[0] >= x)
				return 0.0;
			if (p[1] <= 0)
				return -100000.0;
			if (p[2] <= 0)
				return -100000.0;
			if (p[3] <= 0)
				return -100000.0;

			double pw = Math.pow((x - p[0]), p[2]);
			double e = Math.exp((-(x - p[0])) / p[3]);
			return p[1] * pw * e;
		case LOG2:
			double tmp = x - p[2];
			if (tmp < 0.001)
				tmp = 0.001;
			return p[0] + p[1] * Math.log(tmp);
		case RODBARD2:
			if (x <= p[0])
				y = 0.0;
			else {
				y = (p[0] - x) / (x - p[3]);
				y = Math.exp(Math.log(y) * (1.0 / p[1])); // y=y**(1/b)
				y = y * p[2];
			}
			return y;
		default:
			return 0.0;
		}
	}

	/** 获得各个参数！！ */
	public double[] getParams() {
		order();
		return simp[best];
	}

	
	public double[] getResiduals() {
		int saveFit = fit;
		if (fit == RODBARD2)
			fit = RODBARD;
		double[] params = getParams();
		double[] residuals = new double[numPoints];
		if (fit == CUSTOM) {
			for (int i = 0; i < numPoints; i++)
				residuals[i] = yData[i] - f(params, xData[i]);
		} else {
			for (int i = 0; i < numPoints; i++)
				residuals[i] = yData[i] - f(fit, params, xData[i]);
		}
		fit = saveFit;
		return residuals;
	}


	
	public double getSumResidualsSqr() {
		double sumResidualsSqr = (getParams())[getNumParams()];
		return sumResidualsSqr;
	}

	public double getSD() {
		double[] residuals = getResiduals();
		int n = residuals.length;
		double sum = 0.0, sum2 = 0.0;
		for (int i = 0; i < n; i++) {
			sum += residuals[i];
			sum2 += residuals[i] * residuals[i];
		}
		double stdDev = (n * sum2 - sum * sum) / n;
		return Math.sqrt(stdDev / (n - 1.0));
	}

	
	public double getRSquared() {
		double sumY = 0.0;
		for (int i = 0; i < numPoints; i++)
			sumY += yData[i];
		double mean = sumY / numPoints;
		double sumMeanDiffSqr = 0.0;
		for (int i = 0; i < numPoints; i++)
			sumMeanDiffSqr += sqr(yData[i] - mean);
		double rSquared = 0.0;
		if (sumMeanDiffSqr > 0.0)
			rSquared = 1.0 - getSumResidualsSqr() / sumMeanDiffSqr;
		return rSquared;
	}

	
	public double getFitGoodness() {
		double sumY = 0.0;
		for (int i = 0; i < numPoints; i++)
			sumY += yData[i];
		double mean = sumY / numPoints;
		double sumMeanDiffSqr = 0.0;
		int degreesOfFreedom = numPoints - getNumParams();
		double fitGoodness = 0.0;
		for (int i = 0; i < numPoints; i++) {
			sumMeanDiffSqr += sqr(yData[i] - mean);
		}
		if (sumMeanDiffSqr > 0.0 && degreesOfFreedom != 0)
			fitGoodness = 1.0 - (getSumResidualsSqr() / degreesOfFreedom) * ((numPoints) / sumMeanDiffSqr);

		return fitGoodness;
	}

	/**
	 * 多项式结果！
	 */
	public String [] getResultString() {
		String [] results = new String[2];
		 results[0] = "\nFormula: " + getFormula() + "\nTime: " + time + "ms" + "\nNumber of iterations: "
				+ getIterations() + " (" + getMaxIterations() + ")" + "\nR^2: " + d2s(getRSquared(), 4)  + "\nNumber of restarts: " + (nRestarts - 1) + " ("
				+ defaultRestarts + ")" + "\nSum of residuals squared: " + d2s(getSumResidualsSqr(), 4)
				+ "\nStandard deviation: " + d2s(getSD(), 4) + "\nR^2: " + d2s(getRSquared(), 4) + "\nParameters:";
		char pChar = 'a';
		double[] pVal = getParams();
		results[1]="";
		for (int i = 0; i < numParams; i++) {
			results[1] += ("\n" + pChar + "=" + d2s(pVal[i], 6));
			pChar++;
		}

		R2 = d2s(getRSquared(), 4);
		results[1] += ("\nR2=" + R2);
		return results;
	}

	double sqr(double d) {
		return d * d;
	}

	public static String d2s(double n, int decimalPlaces) {
		if (Double.isNaN(n))
			return "NaN";
		if (n == Float.MAX_VALUE) 
			return "3.4e38";
		double np = n;
		if (n < 0.0)
			np = -n;
		if (df == null) {
			dfs = new DecimalFormatSymbols(Locale.US);
			df = new DecimalFormat[10];
			df[0] = new DecimalFormat("0", dfs);
			df[1] = new DecimalFormat("0.0", dfs);
			df[2] = new DecimalFormat("0.00", dfs);
			df[3] = new DecimalFormat("0.000", dfs);
			df[4] = new DecimalFormat("0.0000", dfs);
			df[5] = new DecimalFormat("0.00000", dfs);
			df[6] = new DecimalFormat("0.000000", dfs);
			df[7] = new DecimalFormat("0.0000000", dfs);
			df[8] = new DecimalFormat("0.00000000", dfs);
			df[9] = new DecimalFormat("0.000000000", dfs);
		}

		decimalPlaces = 9 ;

		if (decimalPlaces < 0) {
			decimalPlaces = -decimalPlaces;
			if (decimalPlaces > 9)
				decimalPlaces = 9;
			if (sf == null) {
				sf = new DecimalFormat[10];
				sf[1] = new DecimalFormat("0.0E0", dfs);
				sf[2] = new DecimalFormat("0.00E0", dfs);
				sf[3] = new DecimalFormat("0.000E0", dfs);
				sf[4] = new DecimalFormat("0.0000E0", dfs);
				sf[5] = new DecimalFormat("0.00000E0", dfs);
				sf[6] = new DecimalFormat("0.000000E0", dfs);
				sf[7] = new DecimalFormat("0.0000000E0", dfs);
				sf[8] = new DecimalFormat("0.00000000E0", dfs);
				sf[9] = new DecimalFormat("0.000000000E0", dfs);
			}
			if (Double.isInfinite(n))
				return "" + n;
			else
				return sf[decimalPlaces].format(n); 
		}
		if (decimalPlaces < 0)
			decimalPlaces = 0;
		if (decimalPlaces > 9)
			decimalPlaces = 9;
		return df[decimalPlaces].format(n);
	}

	
	void sumResiduals(double[] x) {
		x[numParams] = 0.0;
		if (fit == CUSTOM) {
			for (int i = 0; i < numPoints; i++)
				x[numParams] = x[numParams] + sqr(f(x, xData[i]) - yData[i]);
		} else {
			for (int i = 0; i < numPoints; i++)
				x[numParams] = x[numParams] + sqr(f(fit, x, xData[i]) - yData[i]);
		}
	}

	
	void newVertex() {
		for (int i = 0; i < numVertices; i++)
			simp[worst][i] = next[i];
	}

	
	void order() {
		for (int i = 0; i < numVertices; i++) {
			if (simp[i][numParams] < simp[best][numParams])
				best = i;
			if (simp[i][numParams] > simp[worst][numParams])
				worst = i;
		}
		nextWorst = best;
		for (int i = 0; i < numVertices; i++) {
			if (i != worst) {
				if (simp[i][numParams] > simp[nextWorst][numParams])
					nextWorst = i;
			}
		}
	
	}

	
	public int getIterations() {
		return numIter;
	}


	public int getMaxIterations() {
		return maxIter;
	}


	public void setMaxIterations(int x) {
		maxIter = x;
	}

	
	public int getRestarts() {
		return defaultRestarts;
	}

	
	public void setRestarts(int n) {
		defaultRestarts = n;
	}

	public void setInitialParameters(double[] params) {
		initialParams = params;
	}

	public static int getMax(double[] array) {
		double max = array[0];
		int index = 0;
		for (int i = 1; i < array.length; i++) {
			if (max < array[i]) {
				max = array[i];
				index = i;
			}
		}
		return index;
	}

	public double[] getXPoints() {
		return xData;
	}

	public double[] getYPoints() {
		return yData;
	}

	public int getFit() {
		return fit;
	}

	public String getName() {
		if (fit == CUSTOM)
			return "User-defined";
		else
			return fitList[fit];
	}

	public String getFormula() {
		if (fit == CUSTOM)
			return customFormula;
		else
			return fList[fit];
	}

	public String getR2() {
		return R2;
	}

	public void setR2(String r2) {
		R2 = r2;
	}
}
