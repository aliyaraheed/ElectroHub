package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.AdminReport;
import com.example.E_Commerce.dto.ChartData;
import com.example.E_Commerce.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;

@Controller
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/admin/top-rated")
    public String getTopPurchasedProducts(Model model) {

        AdminReport adminReport = reportService.getAdminReport();

        model.addAttribute("report", adminReport);

        return "admin/top-rated";
    }


    @GetMapping("/admin/sales-overview-data")
    public ResponseEntity<ChartData> getSalesOverviewPageData(@RequestParam("type") String type) {
        ChartData chartData = reportService.getChartData(type);
        return ResponseEntity.ok(chartData);
    }


    @GetMapping("/admin/sales-overview")
    public String getSalesOverviewPage(){
        return "admin/sales-overview";
    }


}

