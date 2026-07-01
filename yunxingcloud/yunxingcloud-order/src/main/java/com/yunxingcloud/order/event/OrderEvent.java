package com.yunxingcloud.order.event;

import com.yunxingcloud.order.entity.OrderHead;
import org.springframework.context.ApplicationEvent;

public abstract class OrderEvent extends ApplicationEvent {
    private final OrderHead order;

    public OrderEvent(Object source, OrderHead order) {
        super(source);
        this.order = order;
    }

    public OrderHead getOrder() { return order; }

    public static class Created extends OrderEvent {
        public Created(Object source, OrderHead order) { super(source, order); }
    }

    public static class Paid extends OrderEvent {
        public Paid(Object source, OrderHead order) { super(source, order); }
    }

    public static class Shipped extends OrderEvent {
        private final String carrier;
        private final String trackingNo;
        public Shipped(Object source, OrderHead order, String carrier, String trackingNo) {
            super(source, order);
            this.carrier = carrier;
            this.trackingNo = trackingNo;
        }
        public String getCarrier() { return carrier; }
        public String getTrackingNo() { return trackingNo; }
    }

    public static class Canceled extends OrderEvent {
        private final String reason;
        public Canceled(Object source, OrderHead order, String reason) {
            super(source, order);
            this.reason = reason;
        }
        public String getReason() { return reason; }
    }
}