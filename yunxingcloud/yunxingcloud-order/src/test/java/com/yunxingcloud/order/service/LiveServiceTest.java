package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.LiveRoom;
import com.yunxingcloud.order.repository.LiveRoomRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveServiceTest {

    @Mock private LiveRoomRepository liveRoomRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private LiveService service;

    @Test
    void shouldListRooms() {
        LiveRoom r = new LiveRoom(); r.setId(1L); r.setTitle("直播间1"); r.setStatus("1");
        when(liveRoomRepo.findByStatus("1", Sort.by(Sort.Direction.DESC, "startTime")))
            .thenReturn(List.of(r));

        List<LiveRoom> result = service.rooms();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("直播间1");
    }

    @Test
    void shouldGetRoomDetail() {
        LiveRoom r = new LiveRoom();
        r.setId(1L); r.setTitle("直播间1"); r.setStatus("1");
        r.setProductIds("1,2");
        when(liveRoomRepo.findById(1L)).thenReturn(Optional.of(r));
        when(productRepo.findAllById(any())).thenReturn(List.of());

        Map<String, Object> result = service.roomDetail(1L);
        assertThat(result).isNotNull();
        assertThat(result.get("room")).isEqualTo(r);
    }

    @Test
    void shouldReturnNullForMissingRoom() {
        when(liveRoomRepo.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.roomDetail(99L)).isNull();
    }
}