package com.example.E_Commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {

    private Integer cartId;
     List<cartItemDTO> cartItems;


    public static class cartItemDTO{
        private Long cartItemId;
        private String productName;
    }
}
