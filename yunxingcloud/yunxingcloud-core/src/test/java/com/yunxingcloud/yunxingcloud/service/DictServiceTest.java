package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysDictData;
import com.yunxingcloud.yunxingcloud.entity.SysDictType;
import com.yunxingcloud.yunxingcloud.repository.SysDictDataRepository;
import com.yunxingcloud.yunxingcloud.repository.SysDictTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictServiceTest {

    @Mock private SysDictTypeRepository dictTypeRepository;
    @Mock private SysDictDataRepository dictDataRepository;
    @InjectMocks private DictService dictService;

    private SysDictType sampleType;
    private SysDictData sampleData;

    @BeforeEach
    void setUp() {
        sampleType = new SysDictType();
        sampleType.setId(1L);
        sampleType.setDictName("测试字典");
        sampleType.setDictType("test_type");
        sampleType.setStatus("0");

        sampleData = new SysDictData();
        sampleData.setId(1L);
        sampleData.setDictType("test_type");
        sampleData.setDictLabel("测试标签");
        sampleData.setDictValue("test_value");
        sampleData.setSortOrder(1);
    }

    @Test
    void shouldListAllTypes() {
        when(dictTypeRepository.findAll()).thenReturn(List.of(sampleType));
        var result = dictService.listTypes();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDictName()).isEqualTo("测试字典");
    }

    @Test
    void shouldGetTypeById() {
        when(dictTypeRepository.findById(1L)).thenReturn(Optional.of(sampleType));
        var result = dictService.getType(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getDictName()).isEqualTo("测试字典");
    }

    @Test
    void shouldReturnEmptyForMissingType() {
        when(dictTypeRepository.findById(99L)).thenReturn(Optional.empty());
        var result = dictService.getType(99L);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCreateType() {
        when(dictTypeRepository.existsByDictType("test_type")).thenReturn(false);
        when(dictTypeRepository.save(any())).thenReturn(sampleType);
        var result = dictService.createType(sampleType);
        assertThat(result).isNotNull();
        verify(dictTypeRepository).save(sampleType);
    }

    @Test
    void shouldThrowWhenTypeExists() {
        when(dictTypeRepository.existsByDictType("test_type")).thenReturn(true);
        assertThatThrownBy(() -> dictService.createType(sampleType))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUpdateType() {
        when(dictTypeRepository.findById(1L)).thenReturn(Optional.of(sampleType));
        when(dictTypeRepository.save(any())).thenReturn(sampleType);
        var body = new SysDictType();
        body.setDictName("更新名称");
        body.setDictType("test_type");
        body.setStatus("1");
        var result = dictService.updateType(1L, body);
        assertThat(result.getDictName()).isEqualTo("更新名称");
        verify(dictTypeRepository).save(sampleType);
    }

    @Test
    void shouldThrowWhenUpdateMissingType() {
        when(dictTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dictService.updateType(99L, sampleType))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDeleteTypeWithData() {
        when(dictTypeRepository.findById(1L)).thenReturn(Optional.of(sampleType));
        dictService.deleteType(1L);
        verify(dictDataRepository).deleteByDictType("test_type");
        verify(dictTypeRepository).delete(sampleType);
    }

    @Test
    void shouldListDataByType() {
        when(dictDataRepository.findByDictTypeOrderBySortOrder("test_type"))
                .thenReturn(List.of(sampleData));
        var result = dictService.listDataByType("test_type");
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldCreateData() {
        when(dictDataRepository.save(any())).thenReturn(sampleData);
        var result = dictService.createData(sampleData);
        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdateData() {
        when(dictDataRepository.findById(1L)).thenReturn(Optional.of(sampleData));
        when(dictDataRepository.save(any())).thenReturn(sampleData);
        var body = new SysDictData();
        body.setDictLabel("更新标签");
        body.setDictValue("update_value");
        var result = dictService.updateData(1L, body);
        assertThat(result.getDictLabel()).isEqualTo("更新标签");
    }

    @Test
    void shouldDeleteData() {
        when(dictDataRepository.findById(1L)).thenReturn(Optional.of(sampleData));
        dictService.deleteData(1L);
        verify(dictDataRepository).delete(sampleData);
    }

    @Test
    void shouldExportCsv() {
        sampleData.setDictLabel("标签,带逗号");
        when(dictDataRepository.findAll()).thenReturn(List.of(sampleData));
        String csv = dictService.exportCsv();
        assertThat(csv).contains("字典类型,字典标签,字典键值,排序");
        assertThat(csv).contains("test_type");
    }

    @Test
    void shouldImportData() {
        when(dictDataRepository.save(any())).thenReturn(sampleData);
        var items = List.of(Map.of("dictType", "test", "dictLabel", "label", "dictValue", "val"));
        int count = dictService.importData(items);
        assertThat(count).isEqualTo(1);
        verify(dictDataRepository, times(1)).save(any());
    }

    @Test
    void shouldSkipInvalidImportRows() {
        var items = List.of(Map.of("invalid", "data"));
        int count = dictService.importData(items);
        assertThat(count).isEqualTo(0);
        verify(dictDataRepository, never()).save(any());
    }
}
