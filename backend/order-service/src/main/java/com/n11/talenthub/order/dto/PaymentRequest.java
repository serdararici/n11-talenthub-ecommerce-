package com.n11.talenthub.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    @Pattern(regexp = "\\d{16}", message = "Card number must contain only digits")
    private String cardNumber;

    @NotBlank(message = "Expire month is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expire month must be 01-12")
    private String expireMonth;

    @NotBlank(message = "Expire year is required")
    @Pattern(regexp = "^\\d{4}$", message = "Expire year must be 4 digits")
    private String expireYear;

    @NotBlank(message = "CVC is required")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVC must be 3 or 4 digits")
    private String cvc;

    @NotBlank(message = "Buyer first name is required")
    private String buyerFirstName;

    @NotBlank(message = "Buyer last name is required")
    private String buyerLastName;

    // Optional — defaults applied in IyzicoService if blank
    private String buyerPhone;
    private String buyerIdentityNumber;
    private String billingContactName;
    private String billingAddress;
    private String billingCity;
    private String billingCountry;
}
