package com.fobbes.fobbesapp;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.compatible.ScaleGestureDetector;

public class PlotGraphView extends GraphView{
	public class PlotViewContentView extends GraphViewContentView{
		private boolean scaling = false;
		public PlotViewContentView(Context context){
			super(context);
		}		
		
		/**
		 * @param event
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			boolean handled = false;
			// first scale
			if (scaleDetector != null) {
				scaleDetector.onTouchEvent(event);
				handled = scaleDetector.isInProgress();
			}
			if(!handled){
				if(!scaling){
					if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
						
						//Get Canvas x-coordinate
						float x = event.getX();
						//Translate coordinate into percentage of canvas
						x = x/this.getWidth();
						//Get start and end of viewport
						double viewportStart = super.getViewportStart();
						double viewportEnd = super.getViewportSize() + viewportStart;
						double viewportArea = viewportEnd - viewportStart;
						float point = (float) ((x*viewportArea) + viewportStart);
						String note = getNote(point);
						if (note != null)
							if(note.length() != 0)
								toastwarning(note);
					}
					environment.synchMovement(event);
				}
				scaling = false;
			}
			else{
				environment.zeroLastX();
				scaling = true;
				environment.invalidateAll();
			}
			return true;
		}
	}
	public static class PlotGraphData extends GraphViewData{
		public String note;
		public PlotGraphData(double valueX, double valueY, String note){
			super(valueX, valueY);
			this.note = note;
		}
		public boolean hasNote(){
			if(note == "")
				return false;
			return true;
		}
	}
	private final Paint paintBackground;
	private boolean drawBackground;
	private Context ctx;
	private TrendlineActivity environment;
	private PlotGraphSeries series;
	private int schedule;
	private String name;
	public ScaleGestureDetector scaleDetector;

	public PlotGraphView(Context context, String title, String pollName, TrendlineActivity demo, int sched) {
		super(context, title);
		name = pollName;
		PlotViewContentView temp = new PlotViewContentView(context);
		temp.setTag("contentView");
		addView(temp, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
		environment = demo;
		schedule = sched;
		ctx = context;
		paintBackground = new Paint();
		paintBackground.setARGB(255, 20, 40, 60);
		paintBackground.setStrokeWidth(4);
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeriesStyle style) {
		// draw background
		double lastEndY = 0;
		double lastEndX = 0;
		if (drawBackground) {
			float startY = graphheight + border;
			for (int i = 0; i < values.length; i++) {
				double valY = values[i].valueY - minY;
				double ratY = valY / diffY;
				double y = graphheight * ratY;

				double valX = values[i].valueX - minX;
				double ratX = valX / diffX;
				double x = graphwidth * ratX;

				float endX = (float) x + (horstart + 1);
				float endY = (float) (border - y) + graphheight +2;

				if (i > 0) {
					// fill space between last and current point
					int numSpace = (int) ((endX - lastEndX) / 3f) +1;
					for (int xi=0; xi<numSpace; xi++) {
						float spaceX = (float) (lastEndX + ((endX-lastEndX)*xi/(numSpace-1)));
						float spaceY = (float) (lastEndY + ((endY-lastEndY)*xi/(numSpace-1)));

						// start => bottom edge
						float startX = spaceX;

						// do not draw over the left edge
						if (startX-horstart > 1) {
							canvas.drawLine(startX, startY, spaceX, spaceY, paintBackground);
						}
					}
				}
				lastEndY = endY;
				lastEndX = endX;
			}
		}

		// draw data
		paint.setStrokeWidth(style.thickness);
		paint.setColor(style.color);
		
		lastEndY = 0;
		lastEndX = 0;
		for (int i = 0; i < values.length; i++) {
			double valY = values[i].valueY - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;

			double valX = values[i].valueX - minX;
			double ratX = valX / diffX;
			double x = graphwidth * ratX;

			if (i > 0) {
				float startX = (float) lastEndX + (horstart + 1);
				float startY = (float) (border - lastEndY) + graphheight;
				float endX = (float) x + (horstart + 1);
				float endY = (float) (border - y) + graphheight;

				canvas.drawLine(startX, startY, endX, endY, paint);
			}
			if(series.getDataByIndex(values[i].valueX).note.length() != 0)
				canvas.drawCircle((float)x+horstart+1, (float)(border-y)+graphheight, (float)8.0, paint);
			lastEndY = y;
			lastEndX = x;
		}
	}

	public boolean getDrawBackground() {
		return drawBackground;
	}
	
	public int getSched(){
		return schedule;
	}
	
	public String getName(){
		return name;
	}
	
	public void addSeries(PlotGraphSeries series){
		super.addSeries(series);
		this.series = series;
	}
	
	public PlotGraphSeries getSeries(){
		return this.series;
	}
	
	public String getNote(double xcoord){
		PlotGraphData data = series.getData(xcoord);
		if(data!=null){
			return data.note;
		}
		return null;
	}
	
	@Override
	synchronized public void setScalable(boolean scalable){
		super.setScalable(scalable);
		scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				environment.onScale((float) detector.getScaleFactor());					
				return true;
			}
			/*@Override
			public void onScaleEnd(ScaleGestureDetector detector){
				toastwarning("Heyeyeyey");
				setAllScaleFactor((float) detector.getScaleFactor());
			}*/
		});
	}
	
	public boolean isScaling(){
		return scaleDetector.isInProgress();
	}

	/**
	 * @param drawBackground true for a light blue background under the graph line
	 */
	public void setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}
	@Override
	public void toastwarning(String texthere) {
		Toast.makeText(ctx, texthere, Toast.LENGTH_SHORT).show();
	}
	public PlotViewContentView getContentView(){
		return (PlotViewContentView) findViewWithTag("contentView");
	}
	
	public double[] getFlimsyAverage(){
		double start = super.getViewportStart();
		double span = super.getViewportSize();
		double end = start + span;
		PlotGraphData[] values = series.getValues();
		int numDays = (int) span/10800000;
		double[] yvals = new double[numDays+1];
		double[] count = new double[numDays+1];
		for(PlotGraphData i : values){
			if(i.valueX >= start && i.valueX <= end){
				double fromStart = i.valueX - start;
				yvals[(int) fromStart/10800000] += i.valueY;
				count[(int) fromStart/10800000] ++;
			}
		}
		
		//Divide the sums by the number of datapoints
		//Marks periods with no data with -9999999
		for(int i=0; i<yvals.length;i++){
			if(count[i]!= 0)
				yvals[i] = yvals[i]/count[i];
			else
				yvals[i] = -9999999;
		}
		
		return yvals;
		
	}
	public double[] getAverages(double st){
		double start = st;
		double span = super.getViewportSize();
		double end = start + span;
		PlotGraphData[] values = series.getValues();
		int binSize = 10800000;
		switch(schedule){
			case 1:
				return null;
			case 2:
				binSize = 3600000;
				break;
			case 3:
				break;
			case 4:
				binSize = 86400000;
				break;
			case 5:
				binSize = 604800000;
		}
		int numBins = (int) span/binSize;
		double[] yvals = new double[numBins+1];
		double[] count = new double[numBins+1];
		for(PlotGraphData i : values){
			if(i.valueX >= start && i.valueX <= end){
				double fromStart = i.valueX - start;
				yvals[(int) fromStart/binSize] += i.valueY;
				count[(int) fromStart/binSize] ++;
			}
		}
		
		//Divide the sums by the number of datapoints
		//Marks periods with no data with -9999999
		for(int i=0; i<yvals.length;i++){
			if(count[i]!= 0)
				yvals[i] = yvals[i]/count[i];
			else
				yvals[i] = -9999999;
		}
		
		return yvals;
	}
	
	public double getMinTimestamp(){
		double start = super.getViewportStart();
		double span = super.getViewportSize();
		double end = start + span;
		PlotGraphData[] values = series.getValues();		
		
		double min = end;
		for(PlotGraphData i : values){
			if(i.valueX >= start && i.valueX <= end){
				if(i.valueX < min)
					min = i.valueX;
			}
		}
		return min;
	}
	public double[] getUnscheduledAverages(TimeBin timeBin, double st){
		double start = st;
		double span = super.getViewportSize();
		double end = start + span;
		PlotGraphData[] values = series.getValues();
		double binSize = (timeBin.getEnd() - timeBin.getBeginning());
		if(binSize == 0){
			Log.e("Analyze", "End: "+timeBin.getEnd()+" Beginning: "+timeBin.getBeginning());
		}
		int numBins = (int) (span/binSize);
		//Log.e("Analyze", "numBins: "+numBins+ " binSize: "+binSize+" span: "+span);
		double[] yvals = new double[numBins+1];
		double[] count = new double[numBins+1];
		for(PlotGraphData i : values){
			if(i.valueX >= start && i.valueX <= end){
				double fromStart = i.valueX - start;
				yvals[(int) (fromStart/binSize)] += i.valueY;
				count[(int) (fromStart/binSize)] ++;
			}
		}
		
		//Divide the sums by the number of datapoints
		//Marks periods with no data with -9999999
		for(int i=0; i<yvals.length;i++){
			if(count[i]!= 0)
				yvals[i] = yvals[i]/count[i];
			else
				yvals[i] = -9999999;
		}
		
		return yvals;
	}
	public double getWindowedMean(){
		double start = super.getViewportStart();
		double span = super.getViewportSize();
		double end = start + span;
		
		PlotGraphData[] values = series.getValues();
		double sum = 0;
		int count = 0;
		for(PlotGraphData i : values){
			if (i.valueX >= start && i.valueX <= end){
				sum += i.valueY;
				count++;
			}
		}
		if(count == 0)
			return 0;
		sum = sum/count;
		return sum;
	}
	public double getWindowedSum(){
		double start = super.getViewportStart();
		double span = super.getViewportSize();
		double end = start + span;
		PlotGraphData[] values = series.getValues();
		double sum = 0;
		for(PlotGraphData i : values){
			if (i.valueX >= start && i.valueX <= end){
				sum += i.valueY;
			}
		}
		return sum;
	}
	public PlotGraphData[] getPoints(){
		return series.getValues();
	}
	public ArrayList<TimeBin> getTimebins(PlotGraphView y) throws MergeException{
		ArrayList<TimeBin> timeBins = new ArrayList<TimeBin>();
		double start = super.getViewportStart();
		double span = super.getViewportSize();
		double end = start + span;
		PlotGraphData[] xData = getPoints();
		PlotGraphData[] yData = y.getPoints();
		ArrayList<Double> yRepeating = new ArrayList<Double>();
		for(PlotGraphData i : xData){
			if(i.valueX >= start && i.valueX <= end){
				double xStamp = i.valueX;
				for(PlotGraphData i1 : yData){
					if(i1.valueX >= start && i1.valueX <= end){
						double yStamp = i1.valueX;
						if(yStamp >= xStamp){
							yRepeating.add(yStamp-xStamp);
						}
					}	
				}
			}	
		}
		if(yRepeating.size() <= 2){
			throw new MergeException();
		}
		yRepeating = mergeSort(yRepeating);
		ArrayList<Double> yDensity = new ArrayList<Double>();
		for(int i=0; i<yRepeating.size()-1; i++){
			yDensity.add(yRepeating.get(i+1) - yRepeating.get(i));
		}
		if(yDensity.size() <= 2){
			throw new MergeException();
		}
		double sum = 0;
		for(double i : yDensity){
			sum = sum+i;
		}
		double mean = sum / yDensity.size();
		
		double standardDev = 0;
		for(double i : yDensity){
			standardDev = standardDev + ((i-mean)*(i-mean));
		}
		standardDev = Math.sqrt(standardDev/yDensity.size());
		double median;
		if((yDensity.size() % 2) == 0){
			median = (yDensity.get(yDensity.size()/2) + yDensity.get((yDensity.size()/2)+1))/2;
		}
		else{
			median = yDensity.get((yDensity.size()/2)+1);
		}
		double threshold = median +(standardDev/mean);
		long min = yRepeating.get(0).longValue();
		for(int i=1; i<yRepeating.size(); i++){
			if((yRepeating.get(i) - yRepeating.get(i-1)) >= threshold){
				if(yRepeating.get(i-1) != min){
					//Log.e("Analyze", "Min: "+min+" i: "+i+" Threshold: "+threshold);
					TimeBin newBin = new TimeBin(min, yRepeating.get(i-1).longValue());
					if(newBin.getEnd() - newBin.getBeginning() > 60000)
						timeBins.add(newBin);
					min = yRepeating.get(i).longValue();
				}	
			}
		}
			
		
		return timeBins;
	}
	public ArrayList<Double> mergeSort(ArrayList<Double> l)throws MergeException{
		if(l.size() == 1)
			return l;
		if(l.size() < 1){
			throw new MergeException();
		}
		ArrayList<Double> left = new ArrayList<Double>(), right = new ArrayList<Double>();
		int middle = l.size() / 2;
		for(int i=0; i<middle; i++){
			left.add(l.get(i));
		}
		for(int i=middle; i<l.size(); i++){
			right.add(l.get(i));
		}
		left = mergeSort(left);
		right = mergeSort(right);
		return merge(left, right);
	}
	public ArrayList<Double> merge(ArrayList<Double> l1, ArrayList<Double> l2){
		ArrayList<Double> result = new ArrayList<Double>();
		while (l1.size() > 0 || l2.size() > 0){
			if(l1.size() > 0 && l2.size() > 0){
				if(l1.get(0) <= l2.get(0)){
					result.add(l1.get(0));
					l1.remove(0);
				}
				else{
					result.add(l2.get(0));
					l2.remove(0);
				}
			}
			else if(l1.size() > 0){
				result.add(l1.get(0));
				l1.remove(0);
			}
			else if(l2.size() > 0){
				result.add(l2.get(0));
				l2.remove(0);
			}
		}
		return result;
	}
	
	public class TimeBin{
		private long beginning, end;
		public TimeBin(long beg, long en){
			beginning = beg;
			end = en;			
		}
		public long getBeginning(){
			return beginning;
		}
		public long getEnd(){
			return end;
		}
		public String getBinString(){
			String binString = " between ";
			long begForString;
			String begUnitString;
			if(beginning < 60000){
				begForString = beginning/1000;
				begUnitString = "seconds";
			}
			else if(beginning < 3600000){
				begForString = beginning/60000;
				begUnitString = "minutes";
			}
			else if(beginning < 86400000){
				begForString = beginning/3600000;
				begUnitString = "hours";
			}
			else{
				begForString = beginning/86400000;
				begUnitString = "days";
			}
			binString = binString + begForString;
			
			binString = binString + "and ";

			long endForString;
			String endUnitString;
			if(end < 60000){
				endForString = end/1000;
				endUnitString = "seconds";
			}
			else if(end < 3600000){
				endForString = end/60000;
				endUnitString = "minutes";
			}	
			else if(end < 86400000){
				endForString = end/3600000;
				endUnitString = "hours";
			}	
			else{
				endForString = end/86400000;
				endUnitString = "days";
			}
			if (endUnitString == begUnitString && endForString == begForString){
				binString = " at " + begForString + " "+begUnitString;
			}
			else{
				binString = " between " +begForString + " "+begUnitString + " and "+endForString+" "+ endUnitString;
			}
			return binString;
		}
	}
}
