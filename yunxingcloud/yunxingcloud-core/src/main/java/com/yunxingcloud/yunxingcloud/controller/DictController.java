package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysDictData;
import com.yunxingcloud.yunxingcloud.entity.SysDictType;
import com.yunxingcloud.yunxingcloud.repository.SysDictDataRepository;
import com.yunxingcloud.yunxingcloud.repository.SysDictTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "字典管理", description = "系统字典类型和数据的增删改查")
@RestController
@RequestMapping("/api/dict")
public class DictController {

    private final SysDictTypeRepository dictTypeRepository;
    private final SysDictDataRepository dictDataRepository;
    private final I18nService i18n;

    public DictController(SysDictTypeRepository dictTypeRepository,
                          SysDictDataRepository dictDataRepository,
                          I18nService i18n) {
        this.dictTypeRepository = dictTypeRepository;
        this.dictDataRepository = dictDataRepository;
        this.i18n = i18n;
    }

    // ---- Dict Type endpoints ----

    @GetMapping("/types")
    public ResponseEntity<List<SysDictType>> listTypes() {
        return ResponseEntity.ok(dictTypeRepository.findAll());
    }

    @GetMapping("/types/{id}")
    public ResponseEntity<SysDictType> getType(@PathVariable Long id) {
        return dictTypeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/types")
    public ResponseEntity<?> createType(@RequestBody SysDictType dictType) {
        if (dictTypeRepository.existsByDictType(dictType.getDictType())) {
            return ResponseEntity.badRequest().body(Map.of("message", i18n.msg("dict.type_exists")));
        }
        return ResponseEntity.ok(dictTypeRepository.save(dictType));
    }

    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PutMapping("/types/{id}")
    public ResponseEntity<?> updateType(@PathVariable Long id, @RequestBody SysDictType body) {
        return dictTypeRepository.findById(id).map(type -> {
            type.setDictName(body.getDictName());
            type.setDictType(body.getDictType());
            type.setStatus(body.getStatus());
            type.setRemark(body.getRemark());
            return ResponseEntity.ok(dictTypeRepository.save(type));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @Transactional
    @DeleteMapping("/types/{id}")
    public ResponseEntity<Map<String, Object>> deleteType(@PathVariable Long id) {
        return dictTypeRepository.findById(id).map(type -> {
            dictDataRepository.deleteByDictType(type.getDictType());
            dictTypeRepository.delete(type);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ---- Dict Data endpoints ----

    @GetMapping("/data")
    public ResponseEntity<List<SysDictData>> listAllData() {
        return ResponseEntity.ok(dictDataRepository.findAll());
    }

    @GetMapping("/data/{dictType}")
    public ResponseEntity<List<SysDictData>> listDataByType(@PathVariable String dictType) {
        return ResponseEntity.ok(dictDataRepository.findByDictTypeOrderBySortOrder(dictType));
    }

    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/data")
    public ResponseEntity<SysDictData> createData(@RequestBody SysDictData data) {
        return ResponseEntity.ok(dictDataRepository.save(data));
    }

    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PutMapping("/data/{id}")
    public ResponseEntity<?> updateData(@PathVariable Long id, @RequestBody SysDictData body) {
        return dictDataRepository.findById(id).map(data -> {
            data.setDictType(body.getDictType());
            data.setDictLabel(body.getDictLabel());
            data.setDictValue(body.getDictValue());
            data.setCssClass(body.getCssClass());
            data.setListClass(body.getListClass());
            data.setIsDefault(body.getIsDefault());
            data.setSortOrder(body.getSortOrder());
            data.setStatus(body.getStatus());
            data.setRemark(body.getRemark());
            return ResponseEntity.ok(dictDataRepository.save(data));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('dict:write')")
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/data/{id}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable Long id) {
        return dictDataRepository.findById(id).map(data -> {
            dictDataRepository.delete(data);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder sb = new StringBuilder("字典类型,字典标签,字典键值,排序\n");
        dictDataRepository.findAll().forEach(d -> sb.append(String.format("%s,%s,%s,%d\n", d.getDictType(), d.getDictLabel(), d.getDictValue(), d.getSortOrder() != null ? d.getSortOrder() : 0)));
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=dict_data.csv").header("Content-Type", "text/csv; charset=UTF-8").body(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @PreAuthorize("hasAuthority('dict:write')")
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData(@RequestBody List<Map<String, String>> items) {
        int count = 0;
        for (var item : items) {
            try {
                SysDictData d = new SysDictData();
                d.setDictType(item.get("dictType")); d.setDictLabel(item.get("dictLabel"));
                d.setDictValue(item.get("dictValue")); d.setSortOrder(0);
                dictDataRepository.save(d); count++;
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(Map.of("success", (Object) true, "message", i18n.msg("dict.import_count", count)));
    }
}
