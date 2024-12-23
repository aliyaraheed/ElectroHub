package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.SalesReportResponse;
import com.example.E_Commerce.service.SalesReportService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Controller
@Slf4j
public class SalesReportController {

    private static final Logger log = LoggerFactory.getLogger(SalesReportController.class);
    private final SalesReportService salesReportService;

    public SalesReportController(SalesReportService salesReportService) {
        this.salesReportService = salesReportService;
    }


    @GetMapping("/admin/sales-report")
    public String getSalesReportPage(Model model,
                                     @RequestParam(name = "filterType", required = false) String filterType,
                                     @RequestParam(name = "startDate", required = false) String startDate,
                                     @RequestParam(name = "endDate", required = false) String endDate,
                                     RedirectAttributes attributes) {

        if(filterType == "custom"){
            if(startDate == null || endDate == null){
                attributes.addFlashAttribute("Invalid date inputs or null");
                return "redirect:/admin/sales-report";
            }
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        filterType = filterType != null ? filterType : "custom";
        startDate = (startDate != null && !startDate.isEmpty()) ? startDate : null;
        endDate = (endDate != null && !endDate.isEmpty()) ? endDate : null;

        LocalDate localStartDate = null;
        LocalDate localEndDate = null;

        if (startDate != null) {
            localStartDate = LocalDate.parse(startDate, dateFormatter);
        }
        if (endDate != null) {
            localEndDate = LocalDate.parse(endDate, dateFormatter);
        }

        switch (filterType) {
            case "daily":
                localStartDate = LocalDate.now();
                localEndDate = LocalDate.now();
                break;
            case "weekly":
                localStartDate = LocalDate.now().minusDays(6);
                localEndDate = LocalDate.now();
                break;
            case "monthly":
                localStartDate = LocalDate.now().minusDays(29);
                localEndDate = LocalDate.now();
                break;
            case "yearly":
                localStartDate = LocalDate.now().minusYears(1);
                localEndDate = LocalDate.now();
                break;
            case "custom":
                break;
            default:
                throw new IllegalArgumentException("Invalid filter type: " + filterType);
        }

        SalesReportResponse salesReport = salesReportService.generateSalesReport(localStartDate, localEndDate);

        log.info("Sales report generated with orders count: {}, total revenue: {}",
                salesReport.getTotalOrders(), salesReport.getTotalRevenue());

        String formattedStartDate = localStartDate != null ? localStartDate.format(dateFormatter) : "";
        String formattedEndDate = localEndDate != null ? localEndDate.format(dateFormatter) : "";

        model.addAttribute("salesReport", salesReport);
        model.addAttribute("orders", salesReport.getOrder());
        model.addAttribute("filterType", filterType);
        model.addAttribute("startDate", formattedStartDate);
        model.addAttribute("endDate", formattedEndDate);

        return "admin/sales-report";
    }


}
