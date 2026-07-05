package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysDictData;
import com.yunxingcloud.yunxingcloud.entity.SysDictType;
import com.yunxingcloud.yunxingcloud.repository.SysDictDataRepository;
import com.yunxingcloud.yunxingcloud.repository.SysDictTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DictService {

    private final SysDictTypeRepository dictTypeRepository;
    private final SysDictDataRepository dictDataRepository;

    public DictService(SysDictTypeRepository dictTypeRepository,
                       SysDictDataRepository dictDataRepository) {
        this.dictTypeRepository = dictTypeRepository;
        this.dictDataRepository = dictDataRepository;
    }

    // ---- Dict Type ----

    public List<SysDictType> listTypes() {
        return dictTypeRepository.findAll();
    }

    public Optional<SysDictType> getType(Long id) {
        return dictTypeRepository.findById(id);
    }

    @Transactional
    public SysDictType createType(SysDictType dictType) {
        if (dictTypeRepository.existsByDictType(dictType.getDictType())) {
            throw new IllegalArgumentException("dict.type_exists");
        }
        return dictTypeRepository.save(dictType);
    }

    @Transactional
    public SysDictType updateType(Long id, SysDictType body) {
        return dictTypeRepository.findById(id).map(type -> {
            type.setDictName(body.getDictName());
            type.setDictType(body.getDictType());
            type.setStatus(body.getStatus());
            type.setRemark(body.getRemark());
            return dictTypeRepository.save(type);
        }).orElseThrow(() -> new IllegalArgumentException("Dict type not found: " + id));
    }

    @Transactional
    public void deleteType(Long id) {
        SysDictType type = dictTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dict type not found: " + id));
        dictDataRepository.deleteByDictType(type.getDictType());
        dictTypeRepository.delete(type);
    }

    // ---- Dict Data ----

    public List<SysDictData> listAllData() {
        return dictDataRepository.findAll();
    }

    public List<SysDictData> listDataByType(String dictType) {
        return dictDataRepository.findByDictTypeOrderBySortOrder(dictType);
    }

    @Transactional
    public SysDictData createData(SysDictData data) {
        return dictDataRepository.save(data);
    }

    @Transactional
    public SysDictData updateData(Long id, SysDictData body) {
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
            return dictDataRepository.save(data);
        }).orElseThrow(() -> new IllegalArgumentException("Dict data not found: " + id));
    }

    @Transactional
    public void deleteData(Long id) {
        SysDictData data = dictDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dict data not found: " + id));
        dictDataRepository.delete(data);
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder("字典类型,字典标签,字典键值,排序\n");
        dictDataRepository.findAll().forEach(d -> sb.append(String.format("%s,%s,%s,%d\n",
                d.getDictType(), d.getDictLabel(), d.getDictValue(),
                d.getSortOrder() != null ? d.getSortOrder() : 0)));
        return sb.toString();
    }

    @Transactional
    public int importData(List<Map<String, String>> items) {
        int count = 0;
        for (var item : items) {
            try {
                String dictType = item.get("dictType");
                if (dictType == null || dictType.isBlank()) continue;
                SysDictData d = new SysDictData();
                d.setDictType(dictType);
                d.setDictLabel(item.get("dictLabel"));
                d.setDictValue(item.get("dictValue"));
                d.setSortOrder(0);
                dictDataRepository.save(d);
                count++;
            } catch (Exception e) {
                // skip invalid rows
            }
        }
        return count;
    }
}
