package com.fobbes.fobbesapp;

import android.util.Log;

import com.fobbes.fobbesapp.PlotGraphView.PlotGraphData;
import com.jjoe64.graphview.GraphViewSeries;

public class PlotGraphSeries extends GraphViewSeries{
	public PlotGraphData[] values;
	public PlotGraphSeries(PlotGraphData[] values){
		super(values);
		this.values = values;
	}
	public PlotGraphSeries(String description, GraphViewSeriesStyle style, PlotGraphData[] values){
		super(description, style, values);
		this.values = values;
	}
	//Returns a datapoint within 33 minutes of xcoord
	//TODO: change with scale
	public PlotGraphData getData(double xcoord){
		for(PlotGraphData i : values){
			if(i.valueX >= (xcoord-200000000) && i.valueX <= (xcoord+200000000)){
				return i;
			}
		}
		return null;
	}
	//Returns a datapoint at EXACTLY the xcoordinate
	public PlotGraphData getDataByIndex(double xcoord){
		for(PlotGraphData i : values){
			if (i.valueX == xcoord)
				return i;
		}
		return null;
	}
	
	public PlotGraphData[] getValues(){
		return values;
	}
}
