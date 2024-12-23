package com.example.E_Commerce.dto;

import java.util.Map;

public class ChartData {

    private ChartType chartType;

    private Map<String,Integer> values;

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public Map<String, Integer> getValues() {
        return values;
    }

    public void setValues(Map<String, Integer> values) {
        this.values = values;
    }
}
