package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupBuyServiceTest {

    @Mock private GroupBuyRepository groupBuyRepo;
    @Mock private GroupRecordRepository groupRecordRepo;
    @Mock private OrderHeadRepository orderRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private GroupBuyService groupBuyService;

    private GroupBuy sampleBuy;
    private OrderHead sampleOrder;

    @BeforeEach
    void setUp() {
        sampleBuy = new GroupBuy();
        sampleBuy.setId(1L);
        sampleBuy.setProductId(100L);
        sampleBuy.setMinMembers(3);
        sampleBuy.setEndTime(LocalDateTime.now().plusDays(1));
        sampleBuy.setGroupPrice(5000L);

        sampleOrder = new OrderHead();
        sampleOrder.setId(200L);
        sampleOrder.setOrderNo("ORD-001");
        sampleOrder.setUsername("user1");
    }

    @Test
    void shouldCreateGroup() {
        when(groupBuyRepo.findById(1L)).thenReturn(Optional.of(sampleBuy));
        when(orderRepo.findById(200L)).thenReturn(Optional.of(sampleOrder));
        GroupRecord saved = new GroupRecord();
        saved.setId(10L);
        saved.setGroupBuyId(1L);
        saved.setIsLeader(true);
        when(groupRecordRepo.save(any())).thenReturn(saved);

        GroupRecord result = groupBuyService.createGroup(1L, 200L, "user1");

        assertThat(result).isNotNull();
        assertThat(result.getGroupBuyId()).isEqualTo(1L);
        assertThat(result.getIsLeader()).isTrue();
    }

    @Test
    void shouldJoinGroup() {
        // Mock leader record
        GroupRecord leader = new GroupRecord();
        leader.setId(1L);
        leader.setGroupBuyId(1L);
        leader.setGroupId(200L);
        leader.setIsLeader(true);
        when(groupRecordRepo.findByGroupId(1L)).thenReturn(List.of(leader));
        when(groupBuyRepo.findById(1L)).thenReturn(Optional.of(sampleBuy));
        when(groupRecordRepo.countByGroupIdAndStatus(1L, "0")).thenReturn(1L);
        when(groupRecordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GroupRecord result = groupBuyService.joinGroup(1L, 300L, "user2");

        assertThat(result).isNotNull();
        verify(groupRecordRepo).save(any());
    }

    @Test
    void shouldThrowWhenGroupExpired() {
        sampleBuy.setEndTime(LocalDateTime.now().minusHours(1));
        when(groupBuyRepo.findById(1L)).thenReturn(Optional.of(sampleBuy));

        assertThatThrownBy(() -> groupBuyService.createGroup(1L, 200L, "user1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("已过期");
    }

    @Test
    void shouldCompleteGroupWhenFull() {
        GroupRecord leader = new GroupRecord();
        leader.setId(1L);
        leader.setGroupBuyId(1L);
        leader.setGroupId(200L);
        leader.setIsLeader(true);
        leader.setOrderId(200L);
        sampleBuy.setMinMembers(3);
        when(groupRecordRepo.findByGroupId(1L)).thenReturn(List.of(leader));
        when(groupBuyRepo.findById(1L)).thenReturn(Optional.of(sampleBuy));
        // countByGroupIdAndStatus returns 2 existing + 1 new = 3 = minMembers
        lenient().when(groupRecordRepo.countByGroupIdAndStatus(1L, "0")).thenReturn(3L);
        when(orderRepo.findById(anyLong())).thenReturn(Optional.of(sampleOrder));
        when(groupRecordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        groupBuyService.joinGroup(1L, 300L, "user2");

        verify(groupRecordRepo, atLeastOnce()).save(any());
    }
}