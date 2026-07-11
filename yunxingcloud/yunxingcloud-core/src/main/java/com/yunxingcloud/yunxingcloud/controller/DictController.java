package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysDictData;
import com.yunxingcloud.yunxingcloud.entity.SysDictType;
import com.yunxingcloud.yunxingcloud.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "字典管理", description = "系统字典类型和数据的增删改查")
@RestController
@RequestMapping("/api/dict")
public class DictController {

    private final DictService dictService;
    private final I18nService i18n;

    public DictController(DictService dictService, I18nService i18n) {
        this.dictService = dictService;
        this.i18n = i18n;
    }

    // ---- Dict Type endpoints ----

    @Operation(summary = "查询字典类型列表")
    @GetMapping("/types")
    public ResponseEntity<List<SysDictType>> listTypes() {
        return ResponseEntity.ok(dictService.listTypes());
    }

    @Operation(summary = "查询字典类型详情")
    @GetMapping("/types/{id}")
    public ResponseEntity<SysDictType> getType(@PathVariable Long id) {
        return dictService.getType(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "新增字典类型")
    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/types")
    public ResponseEntity<?> createType(@RequestBody SysDictType dictType) {
        try {
            return ResponseEntity.ok(dictService.createType(dictType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", i18n.msg(e.getMessage())));
        }
    }

    @Operation(summary = "修改字典类型")
    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PutMapping("/types/{id}")
    public ResponseEntity<?> updateType(@PathVariable Long id, @RequestBody SysDictType body) {
        try {
            return ResponseEntity.ok(dictService.updateType(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/types/{id}")
    public ResponseEntity<Map<String, Object>> deleteType(@PathVariable Long id) {
        try {
            dictService.deleteType(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ---- Dict Data endpoints ----

    @Operation(summary = "查询全部字典数据")
    @GetMapping("/data")
    public ResponseEntity<List<SysDictData>> listAllData() {
        return ResponseEntity.ok(dictService.listAllData());
    }

    @Operation(summary = "按类型查询字典数据")
    @GetMapping("/data/{dictType}")
    public ResponseEntity<List<SysDictData>> listDataByType(@PathVariable String dictType) {
        return ResponseEntity.ok(dictService.listDataByType(dictType));
    }

    @Operation(summary = "新增字典数据")
    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/data")
    public ResponseEntity<SysDictData> createData(@RequestBody SysDictData data) {
        return ResponseEntity.ok(dictService.createData(data));
    }

    @Operation(summary = "修改字典数据")
    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PutMapping("/data/{id}")
    public ResponseEntity<?> updateData(@PathVariable Long id, @RequestBody SysDictData body) {
        try {
            return ResponseEntity.ok(dictService.updateData(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/data/{id}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable Long id) {
        try {
            dictService.deleteData(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "导出字典CSV")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        String csv = dictService.exportCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=dict_data.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @Operation(summary = "导入字典数据")
    @PreAuthorize("hasAuthority('dict:write')")
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData(@RequestBody List<Map<String, String>> items) {
        int count = dictService.importData(items);
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("dict.import_count", count)));
    }
}
