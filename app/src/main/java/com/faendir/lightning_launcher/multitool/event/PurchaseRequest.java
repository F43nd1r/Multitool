package com.faendir.lightning_launcher.multitool.event;

/**
 * @author F43nd1r
 * @since 29.09.2016
 */

public class PurchaseRequest {
    private final String productId;

    public PurchaseRequest(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
