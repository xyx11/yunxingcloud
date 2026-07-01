package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class ProductImportService {

    private final ProductRepository repo;

    public ProductImportService(ProductRepository repo) { this.repo = repo; }

    /** Excel 导入商品 */
    public Map<String, Object> importExcel(MultipartFile file) {
        int success = 0, fail = 0;
        List<String> errors = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 跳过表头
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Product p = new Product();
                    p.setName(getCell(row, 0));
                    p.setDescription(getCell(row, 1));
                    p.setPrice(parsePrice(getCell(row, 2)));
                    p.setStock(parseInt(getCell(row, 3), 0));
                    p.setStatus("0");
                    repo.save(p);
                    success++;
                } catch (Exception e) {
                    fail++;
                    errors.add("行" + (i+1) + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Excel 解析失败", e);
        }
        return Map.of("success", success, "fail", fail, "errors", errors);
    }

    /** Excel 导出 */
    public byte[] exportExcel() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("商品列表");
            Row header = sheet.createRow(0);
            String[] cols = {"ID","名称","描述","价格(元)","库存","销量","状态"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            List<Product> products = repo.findAll();
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getDescription());
                row.createCell(3).setCellValue(p.getPrice() / 100.0);
                row.createCell(4).setCellValue(p.getStock());
                row.createCell(5).setCellValue(p.getSales() != null ? p.getSales() : 0);
                row.createCell(6).setCellValue("0".equals(p.getStatus()) ? "上架" : "下架");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Excel 导出失败", e);
        }
    }

    private String getCell(Row row, int idx) {
        Cell cell = row.getCell(idx);
        return cell != null ? cell.toString().trim() : "";
    }
    private Long parsePrice(String v) {
        double d = Double.parseDouble(v.replace("¥","").trim());
        return (long)(d * 100);
    }
    private int parseInt(String v, int def) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return def; }
    }
}