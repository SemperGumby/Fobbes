package com.fobbes.fobbesapp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.fobbes.fobbesapp.PlotGraphView.TimeBin;

public class Analyze extends AsyncTask<Void, Void, Void>{

	Context ctx;
	private DatabaseManager db;
	private PlotGraphView[] graphs;
	private double minstamp, maxstamp;
	private TextView text;
	private int selected;
	private int[] schedArray;
	private TrendlineActivity parentActivity;
	private String returnString;
	

	public Analyze(PlotGraphView[] g, double min, double max, TextView t, int sel, int[] sched, TrendlineActivity parent){
		super();
		graphs = g;
		minstamp = min;
		maxstamp = max;
		text = t;
		selected = sel;
		schedArray = sched;
		parentActivity = parent;
	}
	public boolean analyze(double[][] averages, double minstamp, double maxstamp, TextView text, int selected) {
		for(int i = 0; i<averages.length; i++){
			for(int i1 = 0; i1<averages[i].length; i1++){
				//Log.e("Analyze", averages[i][i1] + " Average for trend "+i+" at day "+i1);
			}
		}
		
		//Sum the products of compatible datapoints. Keep track of the number of points for which we don't have data
		int[] numMissing = new int[averages.length];
		double[] sumXY = new double[averages.length];
		for(int i=0; i<averages.length; i++){
			double sum = 0;
			int missing = 0;
			if(i != selected)
				for(int i1=0; i1<averages[selected].length; i1++){
					if(averages[selected][i1] != -9999999 && averages[i][i1] != -9999999)
						sum += (averages[selected][i1]*averages[i][i1]);
					else
						missing++;
				}
			else{
				for(int i1 = 0; i1<averages[selected].length; i1++){
					if (averages[selected][i1] == -9999999)
						missing++;
				}
			}
			sumXY[i] = sum;
			numMissing[i] = missing;
		}
		/*for(int i = 0; i<sumXY.length; i++){
			Log.e("Analyze", "sumXY for trend "+(i)+": "+sumXY[i]);
		}*/
		double[] sumValues = new double[averages.length];
		for(int i = 0; i< averages.length; i++){
			double sum = 0;
			for(double i1 : averages[i]){
				if(i1 != -9999999)
					sum+=i1;
			}
			sumValues[i] = sum;
		}		
		int n = averages[selected].length;
		if(n == 0){
			Log.e("Math", "n=0");
			return false;
		}

		double[] sumXYSquared = new double[averages.length];
		for(int i = 0; i<averages.length; i++){			
			if(i != selected){
				if(n-numMissing[i] > 0)
					sumXYSquared[i] = sumXY[i] - ((sumValues[selected] *sumValues[i])/(n-numMissing[i]));
			}
		}
		/*for(int i = 0; i<sumValues.length; i++){
			Log.e("Analyze", "sumValues for trend "+i+": "+sumValues[i]);
		}*/
		
		double[] sumSquares = new double[averages.length];
		for(int i = 0; i<averages.length; i++){
			double sum = 0;
			for(double i1 : averages[i]){
				sum+=(i1*i1);
			}
			sumSquares[i] = sum;
		}
		/*for(int i = 0; i<sumSquares.length; i++){
			Log.e("Analyze", "sumSquares for trend "+i+": "+sumSquares[i]);
		}*/
		double[] meanArray = new double[averages.length];
		for(int i = 0; i<averages.length; i++){
			
			double mean = 0;
			for (double i1: averages[i]){
				mean = mean + i1;
			}
			Log.e("Analyze", "sum: "+mean+" n: "+averages[i].length);
			mean = mean / averages[i].length;
			meanArray[i] = mean;
		}	
		
		double[] sS = new double[averages.length];
		for(int i=0; i<averages.length; i++){
			
			sS[i] = (sumSquares[i]-(sumValues[i]*sumValues[i])/n);
			//Log.e("SSQ", "SSQ: Trend "+i+": "+sS[i]);
		}
		
		double[] sSquared = new double[averages.length];
		for(int i=0; i<averages.length; i++){
			sSquared[i] = sS[i]/n;
			//Log.e("Math", "SSQuared: "+sSquared[i]);
		}
		
		double[] s = new double[averages.length];
		for(int i=0; i<averages.length; i++){
			s[i] = Math.sqrt(sSquared[i]);
			//Log.e("Standard Dev", "Trend "+i+" standard dev: "+s[i]);
		}
		
		
		//Calculate Pearson correlation, where X = selected and Y = i
		double[] rArray = new double[averages.length];
		/*for(int i=0; i<averages.length; i++){
			double r;
			if(i == selected)
				r = 1;
			else{
				double innerSum = 0;
				for(int i1=0; i1<averages[selected].length; i1++){
					innerSum = innerSum+(((averages[selected][i1]-meanArray[selected])/s[selected])*((averages[i][i1]-meanArray[i])/s[i]));
				}
				r = (1/(n-1))*innerSum;
			}
			rArray[i] = r;
		}*/
		for(int i=0; i<averages.length; i++){
			double r;
			if (i == selected)
				r=1;
			else{
				r=(sumXYSquared[i]/(Math.sqrt(sS[selected]*sS[i])));
			}
			rArray[i] = r;
		}
		String statString = "Mean: "+ meanArray[selected] +"\n";
		for(int i = 0; i< rArray.length; i++){
			if(i!=selected){
				String corrString = "";
				if(rArray[i]>=.80)
					corrString = " has a strong, positive correlation to ";
				if(rArray[i]>=.50 && rArray[i]<.80)
					corrString = " has a moderate, positive correlation to ";
				if(rArray[i]>=.30 && rArray[i]<.50)
					corrString = " has a weak, positive correlation to ";
				if(rArray[i]<=-.80)
					corrString = " has a strong, negative correlation to ";
				if(rArray[i]<=-.50 && rArray[i]>-.80)
					corrString = " has a moderate, negative correlation to ";
				if(rArray[i]<=-.30 && rArray[i]>-.50)
					corrString = " has a weak, negative correlation to ";
				if(corrString != "")
					statString = statString +("Trend "+selected+corrString+" trend "+i+"\n");
				
			}	
		}
		text.setText(statString);
		
		return true;		
	}
	
	public String crossCorrelate(){
		//get selected graph's mean
		
		double selectedMean = graphs[selected].getWindowedMean();
		DecimalFormat dec = new DecimalFormat("#.###");
		double selectedSum = graphs[selected].getWindowedSum();
		String statString = ("Mean:"+dec.format(selectedMean)+"\n"+"Sum:"+selectedSum+"\n");
		//Outer loop; increments the lag time, starting from +/-1 and ending at +/-7
		for(int lag = 0; lag < 8; lag++){
			for(int i=0; i<graphs.length; i++){
				if(i != selected){
					try{
						//create a DoubleMapping for each pair of comparable trends
						DoubleMapping mapping = new DoubleMapping(graphs[selected], graphs[i], schedArray[selected], schedArray[i], lag);
						double sumXY = 0;
						for(int i1 = 0; i1<mapping.size();i1++){
							double[] pair = mapping.getMapping(i1);
							sumXY = sumXY + (pair[0]*pair[1]);
						}
						//Clusterfuck of data, some of which isn't being used right now, but may be useful later
						double sumX = mapping.getSumX();
						double sumY = mapping.getSumY();
						int n = mapping.size();
						if(n > 9){
							String lagString = "";
							double sumXYSquared = sumXY - ((sumX * sumY)/n);
							Log.i("Analyze","Pairing trend "+selected+" vs trend "+i+" at lag "+lag+" sumXYSquared: "+sumXYSquared);
							double sumSquaresX = mapping.getSumSquaresX();
							Log.i("Analyze","Pairing trend "+selected+" vs trend "+i+" at lag "+lag+" sumSquares x: "+sumSquaresX);
							double sumSquaresY = mapping.getSumSquaresY();
							Log.i("Analyze","Pairing trend "+selected+" vs trend "+i+" at lag "+lag+" sumSquares y: "+sumSquaresY);
							double xMean = mapping.getMeanX();
							double yMean = mapping.getMeanY();
							double sSX = (sumSquaresX - (sumX*sumX)/n);
							Log.i("Analyze","Pairing trend "+selected+" vs trend "+i+" at lag "+lag+" sSX: "+sSX);
							double sSY = (sumSquaresY - (sumY*sumY)/n);
							Log.i("Analyze","Pairing trend "+selected+" vs trend "+i+" at lag "+lag+" sSY: "+sSY);
							double sSquaredX = (sSX/n);
							Log.i("Analyze","Pairing trend "+selected+" vs trend "+i+" at lag "+lag+" sSquaredX: "+sSquaredX);
							if(sSquaredX <= 0)
								Log.e("Analyze", "Trend: "+i+" Lag: "+lag+" X sumsquares: "+sumSquaresX+" n: "+n);
							double sSquaredY = (sSY/n);
							Log.i("Analyze","Pairing trend "+selected+" vs trend "+i+" at lag "+lag+" sSquaredY: "+sSquaredY);
							if(sSquaredY <= 0)
								Log.e("Analyze", "Trend: "+i+" Lag: "+lag+" Y sumsquares: "+sumSquaresY+" n: "+n);
							double sX = Math.sqrt(sSquaredX);
							double sY = Math.sqrt(sSquaredY);
							double r = (sumXYSquared/(Math.sqrt(sSX*sSY)));
							
							
							Log.i("Analyze","Pairing trend "+selected+" vs trend "+i+" at lag "+lag+" r: "+r);
							if(r == 1.0 || r == -1.0)
								Log.i("Analyze", "Trend "+selected+" vs trend "+i+": sumXYSquared:"+sumXYSquared+" sqrt:"+Math.sqrt(sSX*sSY)+" n:"+n);
							String corrString = "";
							if(r>=.5)
								corrString = "Strong, positive match to ";
							if(r>=.30 && r<.50)
								corrString = "Moderate, positive match to ";
							if(r>=.10 && r<.30)
								corrString = "Weak, positive match to ";
							if(r<=-.50)
								corrString = "Strong, negative match to ";
							if(r<=-.30 && r>-.50)
								corrString = "Moderate, negative match to ";
							if(r<=-.10 && r>-.30)
								corrString = "Weak, negative match to ";
							
							//Pro Version
							if(corrString != "")
								statString = statString+(corrString+graphs[i].getName()+"\n-"+mapping.getLagString()+"\n\n"); 
							
							//Basic Version
							//statString = statString+"Please purchase Insight Pro from the app store to use all analysis features";
						}
						else{
							
							Log.i("Analyze", "Not enough data points in pairing trend: "+selected+" vs trend: "+i+" at lag "+lag);
						}
					}
					catch(MergeException e){
						
					}
				}
			}
				
		}
		return statString;
	}
	@Override
	protected Void doInBackground(Void... params){
		returnString = crossCorrelate();
		if(! isCancelled()){
			parentActivity.setAnalyzeText(returnString);
		}	
		return null;		
	}
	@Override
	protected void onPreExecute() {
		parentActivity.setProgressBarIndeterminateVisibility(true);
	}
	@Override
	protected void onPostExecute(Void result) {
		parentActivity.setProgressBarIndeterminateVisibility(false);
	}
	@Override
	protected void onCancelled(){
		//parentActivity.setProgressBarIndeterminateVisibility(false);
	}
	//Mapping class used for cross-correlation
	private class DoubleMapping{
		private ArrayList<Double> xList;
		private ArrayList<Double> yList;
		private String lagString;
		public DoubleMapping(PlotGraphView x, PlotGraphView y, int xSched, int ySched, int lag) throws MergeException{			
			double[] xData;
			double[] yData;
			double start = x.getMinTimestamp();
			if(xSched != 1){
				xData = x.getAverages(start);
				if(xSched < ySched){
					String unitString;
					if(xSched == 2)
						unitString = "hour";
					else if(xSched == 3)
						unitString = "hours";
					else if(xSched == 4)
						unitString = "day";
					else 
						unitString = "week";
					if(xSched == 3)
						lagString = " at "+(lag*3) + " "+unitString;
					else{
						lagString = " at "+lag + " "+unitString;
						if(lag > 1)
							lagString = lagString+"s";
					}	
				}
			}	
			else{
				ArrayList<TimeBin> timeBins = x.getTimebins(y);
				if(lag >= timeBins.size()){
					Log.i("Analyze", "Not enough time bins for lag "+lag);
					throw new MergeException();
				}
				xData = x.getUnscheduledAverages(timeBins.get(lag), start);
			}	
			if(ySched != 1){
				yData = y.getAverages(start);
				if(ySched <= xSched){
					String unitString;
					if(ySched == 2)
						unitString = "hour";
					else if(ySched == 3)
						unitString = "hours";
					else if(ySched == 4)
						unitString = "day";
					else 
						unitString = "week";
					if(ySched == 3)
						lagString = " at "+(lag*3) + " "+unitString;
					else{
						lagString = " at "+ lag + " "+unitString;
						if(lag > 1)
							lagString = lagString+"s";
					}	
				}
			}	
			else{
				ArrayList<TimeBin> timeBins = y.getTimebins(x);
				if(lag >= timeBins.size()){
					Log.i("Analyze", "Not enough time bins for lag "+lag);
					throw new MergeException();
				}
				yData = y.getUnscheduledAverages(timeBins.get(lag), start);
				lagString = timeBins.get(lag).getBinString();
			}	
			xList = new ArrayList<Double>();
			yList = new ArrayList<Double>();
			if(xSched < ySched){
				//x more frequent than y, apply negative lag to x
				for(int i=lag; i<yData.length && i<xData.length+lag; i++){
					if(xData[i-lag] != -9999999 && yData[i] != -9999999){
						xList.add(xData[i-lag]);
						yList.add(yData[i]);
					}
				}
			}
			else{
				//x less or equally frequent than y, apply positive lag to y
				for(int i=0; i<yData.length - lag && i<xData.length; i++){
					if(xData[i] != -9999999 && yData[i+lag] != -9999999){
						xList.add(xData[i]);
						yList.add(yData[i+lag]);
					}
				}
			}
			lagString = lagString + " later";
			if(lag == 0){
				lagString = " at the same time";
			}
		}
		
		public int size(){
			return xList.size();
		}
		
		public double[] getMapping(int index){
			//Returns a pair of values at the given index or null if index is out of bounds
			double[] returnVal = new double[2];
			if(index >= size())
				return null;
			returnVal[0] = xList.get(index);
			returnVal[1] = yList.get(index);
			return returnVal;
		}
		
		public double getSumX(){
			double sum = 0;
			for(double i : xList){
				sum = sum + i;
			}
			return sum;
		}
		
		public double getSumY(){
			double sum = 0;
			for(double i : yList){
				sum = sum + i;
			}
			return sum;
		}
		
		public double getSumSquaresX(){
			double sum = 0;
			for(double i : xList){
				sum = sum + (i*i);
			}
			return sum;
		}
		
		public double getSumSquaresY(){
			double sum = 0;
			for(double i : yList){
				sum = sum + (i*i);
			}
			return sum;
		}
		
		public double getMeanX(){
			double mean = getSumX()/size();
			return mean;
		}
		
		public double getMeanY(){
			double mean = getSumY()/size();
			return mean;
		}
		
		public String getLagString(){
			return lagString;
		}
	}
	public class AnalyzeException extends Exception{
		public AnalyzeException(String message){
			super(message);
		}
	}

}
