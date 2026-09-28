package com.grabfood.cacheevict.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Đối tượng Menu thể hiện thông tin món ăn trên GrabFood.
 * Hỗ trợ nhận diện trường 'dish_name' từ JSON request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @JsonProperty("dish_name")
    private String dishName;

    private BigDecimal price;
}
