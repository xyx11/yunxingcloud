package com.yunxingcloud.yunxingcloud.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class CsvExportService {

    public ResponseEntity<byte[]> export(String filename, String[] headers, List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (String[] row : rows) {
            sb.append(String.join(",", escape(row))).append("\n");
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(bytes);
    }

    private String[] escape(String[] row) {
        return Arrays.stream(row).map(val -> {
            if (val == null) return "";
            if (val.contains(",") || val.contains("\"") || val.contains("\n"))
                return "\"" + val.replace("\"", "\"\"") + "\"";
            return val;
        }).toArray(String[]::new);
    }
}
