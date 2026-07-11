package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysPost;
import com.yunxingcloud.yunxingcloud.repository.SysPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private SysPostRepository postRepository;

    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private PostService postService;

    private SysPost samplePost;

    @BeforeEach
    void setUp() {
        samplePost = new SysPost();
        samplePost.setId(1L);
        samplePost.setPostCode("CEO");
        samplePost.setPostName("首席执行官");
        samplePost.setSortOrder(1);
        samplePost.setStatus("0");
        samplePost.setRemark("公司最高领导");
    }

    @Test
    void shouldListPosts() {
        when(jdbc.queryForList(anyString())).thenReturn(List.of(
                Map.of("id", 1L, "postCode", "CEO", "postName", "首席执行官", "user_count", 3)
        ));

        var result = postService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("postCode")).isEqualTo("CEO");
        assertThat(result.get(0).get("postName")).isEqualTo("首席执行官");
        assertThat(result.get(0).get("user_count")).isEqualTo(3);
        verify(jdbc).queryForList(anyString());
    }

    @Test
    void shouldGetPostById() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));

        var result = postService.get(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getPostCode()).isEqualTo("CEO");
        assertThat(result.get().getPostName()).isEqualTo("首席执行官");
    }

    @Test
    void shouldCreatePost() {
        when(postRepository.existsByPostCode("CEO")).thenReturn(false);
        when(postRepository.save(any())).thenReturn(samplePost);

        var result = postService.create(samplePost);

        assertThat(result).isNotNull();
        assertThat(result.getPostName()).isEqualTo("首席执行官");
        verify(postRepository).save(samplePost);
    }

    @Test
    void shouldThrowWhenCreateDuplicatePostCode() {
        when(postRepository.existsByPostCode("CEO")).thenReturn(true);

        assertThatThrownBy(() -> postService.create(samplePost))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("post.code_exists");
    }

    @Test
    void shouldUpdatePost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
        when(postRepository.save(any())).thenReturn(samplePost);

        var body = new SysPost();
        body.setPostCode("CTO");
        body.setPostName("首席技术官");
        body.setSortOrder(2);
        body.setStatus("1");
        body.setRemark("技术负责人");

        var result = postService.update(1L, body);

        assertThat(result.getPostCode()).isEqualTo("CTO");
        assertThat(result.getPostName()).isEqualTo("首席技术官");
        assertThat(result.getSortOrder()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo("1");
        assertThat(result.getRemark()).isEqualTo("技术负责人");
        verify(postRepository).save(samplePost);
    }

    @Test
    void shouldThrowWhenUpdateNonExistentPost() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        var body = new SysPost();
        assertThatThrownBy(() -> postService.update(99L, body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post not found: 99");
    }

    @Test
    void shouldDeletePost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));

        postService.delete(1L);

        verify(postRepository).delete(samplePost);
    }

    @Test
    void shouldExportCsv() {
        when(postRepository.findAll()).thenReturn(List.of(samplePost));

        var csv = postService.exportCsv();

        assertThat(csv).contains("岗位编码,岗位名称,排序,状态");
        assertThat(csv).contains("CEO");
        assertThat(csv).contains("首席执行官");
        assertThat(csv).contains("1");
        assertThat(csv).contains("正常");
        verify(postRepository).findAll();
    }
}
