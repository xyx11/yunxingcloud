package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupBuyService {

    private final GroupBuyRepository groupBuyRepo;
    private final GroupRecordRepository groupRecordRepo;
    private final OrderHeadRepository orderRepo;

    public GroupBuyService(GroupBuyRepository groupBuyRepo, GroupRecordRepository groupRecordRepo,
                           OrderHeadRepository orderRepo) {
        this.groupBuyRepo = groupBuyRepo;
        this.groupRecordRepo = groupRecordRepo;
        this.orderRepo = orderRepo;
    }

    /** 开团 (团长) */
    @Transactional
    public GroupRecord createGroup(Long groupBuyId, Long orderId, String username) {
        GroupBuy gb = groupBuyRepo.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("拼团活动不存在"));
        if (!"0".equals(gb.getStatus())) throw new IllegalStateException("拼团活动已结束");
        if (gb.getEndTime().isBefore(LocalDateTime.now())) throw new IllegalStateException("拼团已过期");

        OrderHead order = orderRepo.findById(orderId).orElseThrow();
        GroupRecord record = new GroupRecord();
        record.setGroupId(order.getId()); // 用团长订单号作为团ID
        record.setGroupBuyId(groupBuyId);
        record.setOrderId(orderId);
        record.setUsername(username);
        record.setIsLeader(true);
        record.setStatus("0");
        return groupRecordRepo.save(record);
    }

    /** 参团 */
    @Transactional
    public GroupRecord joinGroup(Long groupId, Long orderId, String username) {
        List<GroupRecord> members = groupRecordRepo.findByGroupId(groupId);
        if (members.isEmpty()) throw new IllegalArgumentException("团不存在");
        GroupRecord leader = members.get(0);

        GroupBuy gb = groupBuyRepo.findById(leader.getGroupBuyId()).orElseThrow();
        if (!"0".equals(gb.getStatus())) throw new IllegalStateException("拼团活动已结束");

        GroupRecord record = new GroupRecord();
        record.setGroupId(groupId);
        record.setGroupBuyId(leader.getGroupBuyId());
        record.setOrderId(orderId);
        record.setUsername(username);
        record.setIsLeader(false);
        record.setStatus("0");
        record = groupRecordRepo.save(record);

        // 检查是否成团
        long count = groupRecordRepo.countByGroupIdAndStatus(groupId, "0");
        if (count >= gb.getMinMembers()) {
            completeGroup(groupId);
        }
        return record;
    }

    /** 成团: 标记所有成员状态 */
    private void completeGroup(Long groupId) {
        groupRecordRepo.findByGroupId(groupId).forEach(r -> {
            r.setStatus("1");
            groupRecordRepo.save(r);
            // 更新订单金额为拼团价
            orderRepo.findById(r.getOrderId()).ifPresent(order -> {
                GroupBuy gb = groupBuyRepo.findById(r.getGroupBuyId()).orElse(null);
                if (gb != null) {
                    order.setActualAmount(gb.getGroupPrice());
                    orderRepo.save(order);
                }
            });
        });
    }

    /** 拼团失败: 超时未成团, 退款 */
    @Transactional
    public void expireTimeoutGroups() {
        List<GroupBuy> expired = groupBuyRepo.findByStatusAndEndTimeBefore("0", LocalDateTime.now());
        for (GroupBuy gb : expired) {
            List<GroupRecord> records = groupRecordRepo.findByGroupBuyIdAndStatus(gb.getId(), "0");
            for (GroupRecord r : records) {
                r.setStatus("2"); // 标记失败
                groupRecordRepo.save(r);
                // 取消关联订单 (触发退款)
                orderRepo.findById(r.getOrderId()).ifPresent(order -> {
                    order.setStatus("4");
                    order.setRemark("拼团失败自动取消");
                    orderRepo.save(order);
                });
            }
            gb.setStatus("1"); // 活动结束
            groupBuyRepo.save(gb);
        }
    }
}