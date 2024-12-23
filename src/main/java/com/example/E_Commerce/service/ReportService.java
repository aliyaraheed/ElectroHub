package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.AdminReport;
import com.example.E_Commerce.dto.ChartData;

import java.util.List;

public interface ReportService {
    AdminReport getAdminReport();

    ChartData getChartData(String type);
}
