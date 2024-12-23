package com.example.E_Commerce.service.impl;

import com.example.E_Commerce.dto.AdminReport;
import com.example.E_Commerce.dto.ChartData;
import com.example.E_Commerce.dto.ChartType;
import com.example.E_Commerce.model.Category;
import com.example.E_Commerce.model.Order;
import com.example.E_Commerce.model.OrderItem;
import com.example.E_Commerce.model.Product;
import com.example.E_Commerce.repository.OrderItemRepository;
import com.example.E_Commerce.repository.OrderRepository;
import com.example.E_Commerce.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final OrderRepository orderRepository;

    public ReportServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @Override
    public AdminReport getAdminReport() {
        AdminReport adminReport = new AdminReport();


        List<Product> topProducts = orderRepository.findTopOrderedProducts(PageRequest.of(0, 10));
        List<AdminReport.ProductInfo> productInfos = topProducts.stream().map(product -> {
            AdminReport.ProductInfo productInfo = new AdminReport.ProductInfo();
            productInfo.setId(product.getId());
            log.info("Taken the product {}", product.getTitle());
            productInfo.setName(product.getTitle());
            List<String> images = product.getImages();
            productInfo.setImgUrl(images != null && !images.isEmpty() ? images.get(0) : null);
            return productInfo;
        }).toList();


        List<Category> topCategories = orderRepository.findTopOrderedCategories(PageRequest.of(0, 10));
        List<AdminReport.CategoryInfo> categoryInfos = topCategories.stream().map(category -> {
            AdminReport.CategoryInfo categoryInfo = new AdminReport.CategoryInfo();
            categoryInfo.setId(category.getId());
            log.info("Taken the category : {}", category.getName());
            categoryInfo.setName(category.getName());
            categoryInfo.setImgUrl(category.getImageName());
            return categoryInfo;
        }).toList();


        adminReport.setProductInfos(productInfos);
        adminReport.setCategoryInfos(categoryInfos);

        return adminReport;
    }

    @Override
    public ChartData getChartData(String type) {
        ChartData chartData = new ChartData();
        chartData.setChartType(ChartType.valueOf(type));

        Map<String, Integer> values = new HashMap<>();

        List<Order> orders = orderRepository.findAll();
        Set<String> dateKeys = new HashSet<>();

        for (Order order : orders) {
            if (order.getCreatedDate() != null) {
                String dateKey;
                switch (type) {
                    case "YEARLY":
                        dateKey = String.valueOf(order.getCreatedDate().getYear());
                        break;
                    case "MONTHLY":
                        dateKey = order.getCreatedDate().getMonth().toString();
                        break;
                    case "WEEKLY":
                        dateKey = "W-" + order.getCreatedDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR);
                        break;
                    default:
                        continue;
                }

                values.put(dateKey, values.getOrDefault(dateKey, 0) + 1);
                dateKeys.add(dateKey);
            }
        }


        LocalDate now = LocalDate.now();
        switch (type) {
            case "YEARLY":
                for (int i = -1; i <= 0; i++) {
                    int year = now.getYear() + i;
                    String yearKey = String.valueOf(year);
                    values.putIfAbsent(yearKey, 0);
                }
                break;
            case "MONTHLY":
                for (int i = -4; i <= 0; i++) {
                    LocalDate monthKey = now.minusMonths(i);
                    values.putIfAbsent(monthKey.getMonth().toString(), 0);
                }
                break;
            case "WEEKLY":
                for (int i = -1; i <= 0; i++) {
                    LocalDate weekKey = now.minusWeeks(i);
                    values.putIfAbsent("W-" + weekKey.get(ChronoField.ALIGNED_WEEK_OF_YEAR), 0);
                }
                break;
        }


        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            log.info("key : {} , value : {}", entry.getKey(), entry.getValue());
        }

        chartData.setValues(values);
        return chartData;
    }





}
