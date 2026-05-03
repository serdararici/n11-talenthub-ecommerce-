package com.n11.talenthub.order.service;

import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import com.n11.talenthub.order.dto.PaymentRequest;
import com.n11.talenthub.order.entity.Order;
import com.n11.talenthub.order.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IyzicoService {

    private static final String DEFAULT_IDENTITY_NUMBER = "74300864791";
    private static final String DEFAULT_PHONE = "+905350000000";
    private static final String DEFAULT_IP = "85.34.78.112";
    private static final String DEFAULT_CITY = "Istanbul";
    private static final String DEFAULT_COUNTRY = "Turkey";

    private final Options iyzicoOptions;

    public Payment charge(Order order, PaymentRequest req, String buyerIp) {
        CreatePaymentRequest paymentRequest = buildRequest(order, req, buyerIp);
        try {
            Payment payment = Payment.create(paymentRequest, iyzicoOptions);
            log.info("Iyzico response: status={}, conversationId={}, paymentId={}",
                    payment.getStatus(), payment.getConversationId(), payment.getPaymentId());

            if (!"success".equalsIgnoreCase(payment.getStatus())) {
                String msg = StringUtils.hasText(payment.getErrorMessage())
                        ? payment.getErrorMessage()
                        : "Payment declined";
                log.warn("Payment failed for order {}: [{}] {}", order.getId(), payment.getErrorCode(), msg);
                throw new PaymentException(msg, payment.getErrorCode());
            }
            return payment;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Iyzico API error for order {}", order.getId(), e);
            throw new PaymentException("Payment service is temporarily unavailable", "IYZICO_ERROR");
        }
    }

    private CreatePaymentRequest buildRequest(Order order, PaymentRequest req, String buyerIp) {
        CreatePaymentRequest r = new CreatePaymentRequest();
        r.setLocale(Locale.TR.getValue());
        r.setConversationId(String.valueOf(order.getId()));
        r.setCurrency(Currency.TRY.name());
        r.setInstallment(1);
        r.setBasketId(String.valueOf(order.getId()));
        r.setPaymentChannel(PaymentChannel.WEB.name());
        r.setPaymentGroup(PaymentGroup.PRODUCT.name());
        r.setPrice(order.getTotalAmount());
        r.setPaidPrice(order.getTotalAmount());

        r.setPaymentCard(buildCard(req));
        r.setBuyer(buildBuyer(order, req, buyerIp));

        Address address = buildAddress(
                fallback(req.getBillingContactName(), req.getCardHolderName()),
                fallback(req.getBillingCity(), DEFAULT_CITY),
                fallback(req.getBillingCountry(), DEFAULT_COUNTRY),
                fallback(req.getBillingAddress(), order.getShippingAddress())
        );
        r.setBillingAddress(address);
        r.setShippingAddress(address);
        r.setBasketItems(buildBasketItems(order));
        return r;
    }

    private PaymentCard buildCard(PaymentRequest req) {
        PaymentCard card = new PaymentCard();
        card.setCardHolderName(req.getCardHolderName());
        card.setCardNumber(req.getCardNumber());
        card.setExpireMonth(req.getExpireMonth());
        card.setExpireYear(req.getExpireYear());
        card.setCvc(req.getCvc());
        card.setRegisterCard(0);
        return card;
    }

    private Buyer buildBuyer(Order order, PaymentRequest req, String buyerIp) {
        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(order.getUserId()));
        buyer.setName(req.getBuyerFirstName());
        buyer.setSurname(req.getBuyerLastName());
        buyer.setEmail(order.getUserEmail());
        buyer.setGsmNumber(fallback(req.getBuyerPhone(), DEFAULT_PHONE));
        buyer.setIdentityNumber(fallback(req.getBuyerIdentityNumber(), DEFAULT_IDENTITY_NUMBER));
        buyer.setIp(StringUtils.hasText(buyerIp) ? buyerIp : DEFAULT_IP);
        buyer.setRegistrationAddress(order.getShippingAddress());
        buyer.setCity(fallback(req.getBillingCity(), DEFAULT_CITY));
        buyer.setCountry(fallback(req.getBillingCountry(), DEFAULT_COUNTRY));
        return buyer;
    }

    private Address buildAddress(String contactName, String city, String country, String addressLine) {
        Address address = new Address();
        address.setContactName(contactName);
        address.setCity(city);
        address.setCountry(country);
        address.setAddress(addressLine);
        return address;
    }

    private List<BasketItem> buildBasketItems(Order order) {
        return order.getItems().stream().map(item -> {
            BasketItem bi = new BasketItem();
            bi.setId(String.valueOf(item.getProductId()));
            bi.setName(item.getProductName());
            bi.setCategory1("Ürünler");
            bi.setItemType(BasketItemType.PHYSICAL.name());
            bi.setPrice(item.getSubtotal());
            return bi;
        }).collect(Collectors.toList());
    }

    private String fallback(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
