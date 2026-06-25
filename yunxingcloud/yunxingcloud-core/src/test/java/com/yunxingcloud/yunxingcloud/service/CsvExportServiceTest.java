package com.yunxingcloud.yunxingcloud.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExportServiceTest {

    private final CsvExportService service = new CsvExportService();

    @Test void shouldExportCsv() {
        ResponseEntity<byte[]> resp = service.export("test.csv",
                new String[]{"col1", "col2"},
                List.of(new String[]{"a", "b"}, new String[]{"c", "d"}));
        assertNotNull(resp.getBody());
        String csv = new String(resp.getBody());
        assertTrue(csv.contains("col1,col2"));
        assertTrue(csv.contains("a,b"));
        assertTrue(csv.contains("c,d"));
    }

    @Test void shouldHandleNullValues() {
        var rows = List.<String[]>of(new String[]{null});
        ResponseEntity<byte[]> resp = service.export("test.csv", new String[]{"col1"}, rows);
        assertNotNull(resp.getBody());
    }

    @Test void shouldEscapeCommas() {
        var rows = List.<String[]>of(new String[]{"val,with,comma"});
        ResponseEntity<byte[]> resp = service.export("test.csv", new String[]{"col1"}, rows);
        String csv = new String(resp.getBody());
        assertTrue(csv.contains("\"val,with,comma\""));
    }
}
