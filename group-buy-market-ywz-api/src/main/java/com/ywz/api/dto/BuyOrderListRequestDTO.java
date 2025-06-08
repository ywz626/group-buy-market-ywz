package com.ywz.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/7 18:42
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyOrderListRequestDTO {

    private String userId;

}
