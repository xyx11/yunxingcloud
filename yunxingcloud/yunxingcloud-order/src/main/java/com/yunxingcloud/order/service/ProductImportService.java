package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.yunxingcloud.common.core.CsvUtils;

@Service
public class ProductImportService {

    private final ProductRepository repo;

    public ProductImportService(ProductRepository repo) { this.repo = repo; }

    /** CSV 导入 (name,description,price,stock) */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> importCsv(MultipartFile file) {
        int success = 0, fail = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // 跳过表头
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length < 4) { fail++; continue; }
                try {
                    Product p = new Product();
                    p.setName(cols[0].replace("\"", "").trim());
                    p.setDescription(cols.length > 1 ? cols[1].replace("\"", "").trim() : "");
                    p.setPrice(parsePrice(cols[2]));
                    p.setStock(Integer.parseInt(cols[3].trim()));
                    p.setStatus("0");
                    repo.save(p);
                    success++;
                } catch (Exception e) {
                    fail++;
                    errors.add("行 " + cols[0] + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 导入失败", e);
        }
        return Map.of("success", success, "fail", fail, "errors", errors);
    }

    /** CSV 导出 */
    public byte[] exportCsv() {
        List<String[]> rows = repo.findAll().stream()
                .map(p -> new String[]{String.valueOf(p.getId()),
                        p.getName() != null ? p.getName() : "",
                        p.getDescription() != null ? p.getDescription() : "",
                        String.format("%.2f", p.getPrice() / 100.0),
                        String.valueOf(p.getStock()),
                        String.valueOf(p.getSales() != null ? p.getSales() : 0),
                        "0".equals(p.getStatus()) ? "上架" : "下架"})
                .toList();
        return CsvUtils.toCsv(new String[]{"ID", "名称", "描述", "价格(元)", "库存", "销量", "状态"}, rows).getBytes(StandardCharsets.UTF_8);
    }

    private Long parsePrice(String v) {
        v = v.replace("¥", "").replace("\"", "").trim();
        return (long)(Double.parseDouble(v) * 100);
    }
}
