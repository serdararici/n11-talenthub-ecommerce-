package com.n11.talenthub.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private boolean success;
    private String paymentId;
    private String conversationId;
    private String errorMessage;
    private String errorCode;
    private OrderResponse order;
}
