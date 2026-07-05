package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysPost;
import com.yunxingcloud.yunxingcloud.repository.SysPostRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PostService {

    private final SysPostRepository postRepository;
    private final JdbcTemplate jdbc;

    public PostService(SysPostRepository postRepository, JdbcTemplate jdbc) {
        this.postRepository = postRepository;
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(
            "SELECT p.id, p.post_code as postCode, p.post_name as postName, p.sort_order as sortOrder, " +
            "p.status, p.remark, p.created_at as createdAt, COUNT(u.id) as user_count " +
            "FROM sys_post p LEFT JOIN users u ON p.id = u.post_id GROUP BY p.id ORDER BY p.sort_order");
    }

    public Optional<SysPost> get(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public SysPost create(SysPost post) {
        if (postRepository.existsByPostCode(post.getPostCode())) {
            throw new IllegalArgumentException("post.code_exists");
        }
        return postRepository.save(post);
    }

    @Transactional
    public SysPost update(Long id, SysPost body) {
        return postRepository.findById(id).map(post -> {
            post.setPostCode(body.getPostCode());
            post.setPostName(body.getPostName());
            post.setSortOrder(body.getSortOrder());
            post.setStatus(body.getStatus());
            post.setRemark(body.getRemark());
            return postRepository.save(post);
        }).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        SysPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        postRepository.delete(post);
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder("岗位编码,岗位名称,排序,状态\n");
        postRepository.findAll().forEach(p -> sb.append(String.format("%s,%s,%d,%s\n",
                p.getPostCode(), p.getPostName(),
                p.getSortOrder() != null ? p.getSortOrder() : 0,
                "0".equals(p.getStatus()) ? "正常" : "停用")));
        return sb.toString();
    }
}
