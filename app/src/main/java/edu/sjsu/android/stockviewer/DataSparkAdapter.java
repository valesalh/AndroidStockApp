package edu.sjsu.android.stockviewer;

import com.robinhood.spark.SparkAdapter;

import java.util.List;

public class DataSparkAdapter extends SparkAdapter {

    private List<TimeSeriesDaily> data;

    public DataSparkAdapter(List<TimeSeriesDaily> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int index) {
        return data.get(index);
    }

    @Override
    public float getY(int index) {
        TimeSeriesDaily day = data.get(index);
        return (float) day.getClose();
    }

}
