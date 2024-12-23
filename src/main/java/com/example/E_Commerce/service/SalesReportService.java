package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.SalesReportResponse;

import java.time.LocalDate;

public interface SalesReportService {

    SalesReportResponse generateSalesReport(LocalDate startDate, LocalDate endDate);
}
