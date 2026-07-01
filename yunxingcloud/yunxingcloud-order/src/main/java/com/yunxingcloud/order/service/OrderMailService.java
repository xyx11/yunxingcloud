package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderMailService {

    private static final Logger log = LoggerFactory.getLogger(OrderMailService.class);
    private final JavaMailSender mailSender;

    public OrderMailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOrderPlaced(OrderHead order, String userEmail) {
        if (userEmail == null || mailSender == null) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(userEmail);
            msg.setSubject("[YXCLOUD] 订单已确认 — " + order.getOrderNo());
            msg.setText(String.format("""
                    亲爱的 %s：

                    您的订单 %s 已确认，应付金额 ¥%.2f。
                    请在 %s 前完成支付，逾期将自动取消。

                    — YXCLOUD 商城
                    """,
                    order.getUsername(), order.getOrderNo(),
                    (order.getActualAmount() != null ? order.getActualAmount() : order.getTotalAmount()) / 100.0,
                    order.getExpireAt() != null ? order.getExpireAt().toString() : "15分钟内"));
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("下单邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendPaymentSuccess(OrderHead order, String userEmail) {
        if (userEmail == null || mailSender == null) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(userEmail);
            msg.setSubject("[YXCLOUD] 支付成功 — " + order.getOrderNo());
            msg.setText(String.format("""
                    亲爱的 %s：

                    订单 %s 已支付成功，金额 ¥%.2f。
                    我们将尽快安排发货。

                    — YXCLOUD 商城
                    """,
                    order.getUsername(), order.getOrderNo(),
                    (order.getActualAmount() != null ? order.getActualAmount() : order.getTotalAmount()) / 100.0));
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("支付邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendShipped(OrderHead order, String userEmail, String carrier, String trackingNo) {
        if (userEmail == null || mailSender == null) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(userEmail);
            msg.setSubject("[YXCLOUD] 订单已发货 — " + order.getOrderNo());
            msg.setText(String.format("""
                    亲爱的 %s：

                    订单 %s 已发货！
                    快递公司：%s
                    物流单号：%s

                    — YXCLOUD 商城
                    """,
                    order.getUsername(), order.getOrderNo(),
                    carrier != null ? carrier : "待更新",
                    trackingNo != null ? trackingNo : "待更新"));
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("发货邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendRefund(OrderHead order, String userEmail, Long refundAmount) {
        if (userEmail == null || mailSender == null) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(userEmail);
            msg.setSubject("[YXCLOUD] 退款处理 — " + order.getOrderNo());
            msg.setText(String.format("""
                    亲爱的 %s：

                    订单 %s 已退款 ¥%.2f。
                    退款金额将原路返回。

                    — YXCLOUD 商城
                    """,
                    order.getUsername(), order.getOrderNo(), refundAmount / 100.0));
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("退款邮件发送失败: {}", e.getMessage());
        }
    }
}