package com.yunxingcloud.yunxingcloud.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GenService {

    private final JdbcTemplate jdbcTemplate;

    public GenService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> listTables() {
        return jdbcTemplate.queryForList(
            "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE()");
    }

    public Map<String, Object> getTableColumns(String tableName) {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY " +
            "FROM information_schema.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE() " +
            "ORDER BY ORDINAL_POSITION", tableName);

        return Map.of("tableName", tableName, "columns", columns);
    }

    public Map<String, Object> generate(String tableName, String packageName, String author) {
        String className = toCamelCase(tableName, true);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entity", generateEntity(tableName, className, packageName, author));
        result.put("controller", generateController(tableName, className, packageName, author));
        result.put("repository", generateRepository(className, packageName));
        result.put("service", generateService(className, packageName));
        result.put("migration", generateMigration(tableName, className));
        result.put("menuSql", generateMenuSql(tableName, className));
        result.put("vueApi", generateVueApi(tableName, className));
        result.put("vueView", generateVueView(tableName, className));

        return result;
    }

    private String generateEntity(String tableName, String className, String pkg, String author) {
        List<Map<String, Object>> cols = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY FROM information_schema.COLUMNS " +
            "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE() ORDER BY ORDINAL_POSITION", tableName);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(".entity;\n\n");
        sb.append("import jakarta.persistence.*;\n");
        sb.append("import java.time.LocalDateTime;\n\n");
        sb.append("@Entity\n@Table(name = \"").append(tableName).append("\")\n");
        sb.append("public class ").append(className).append(" {\n\n");

        boolean hasId = false;
        for (var col : cols) {
            String colName = (String) col.get("COLUMN_NAME");
            String type = (String) col.get("DATA_TYPE");
            String comment = (String) col.get("COLUMN_COMMENT");
            String nullable = (String) col.get("IS_NULLABLE");
            String key = (String) col.get("COLUMN_KEY");

            if ("PRI".equals(key) && !hasId) {
                sb.append("    @Id\n");
                sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                hasId = true;
            }
            sb.append("    @Column(name = \"").append(colName).append("\"");
            if ("NO".equals(nullable)) sb.append(", nullable = false");
            if ("varchar".equalsIgnoreCase(type)) sb.append(", length = 255");
            sb.append(")\n");
            if (comment != null && !comment.isEmpty()) sb.append("    /** ").append(comment).append(" */\n");
            sb.append("    private ").append(mapJavaType(type)).append(" ").append(toCamelCase(colName, false)).append(";\n\n");
        }
        for (var col : cols) {
            String colName = (String) col.get("COLUMN_NAME");
            String javaType = mapJavaType((String) col.get("DATA_TYPE"));
            String getterName = "get" + toCamelCase(colName, true);
            String setterName = "set" + toCamelCase(colName, true);
            sb.append("\n    public ").append(javaType).append(" ").append(getterName).append("() {\n");
            sb.append("        return ").append(toCamelCase(colName, false)).append(";\n    }\n\n");
            sb.append("    public void ").append(setterName).append("(").append(javaType).append(" ").append(toCamelCase(colName, false)).append(") {\n");
            sb.append("        this.").append(toCamelCase(colName, false)).append(" = ").append(toCamelCase(colName, false)).append(";\n    }\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private String generateController(String tableName, String className, String pkg, String author) {
        String var = toCamelCase(tableName, false);
        String repo = className + "Repository";
        return "package " + pkg + ".controller;\n\n" +
               "import " + pkg + ".entity." + className + ";\n" +
               "import " + pkg + ".repository." + repo + ";\n" +
               "import org.springframework.http.ResponseEntity;\n" +
               "import org.springframework.security.access.prepost.PreAuthorize;\n" +
               "import org.springframework.web.bind.annotation.*;\n\n" +
               "import java.util.*;\n\n" +
               "@RestController\n@RequestMapping(\"/api/" + tableName + "\")\n" +
               "public class " + className + "Controller {\n\n" +
               "    private final " + repo + " " + var + "Repo;\n\n" +
               "    public " + className + "Controller(" + repo + " " + var + "Repo) {\n" +
               "        this." + var + "Repo = " + var + "Repo;\n" +
               "    }\n\n" +
               "    @GetMapping\n" +
               "    public ResponseEntity<List<" + className + ">> list() {\n" +
               "        return ResponseEntity.ok(" + var + "Repo.findAll());\n" +
               "    }\n\n" +
               "    @GetMapping(\"/{id}\")\n" +
               "    public ResponseEntity<" + className + "> get(@PathVariable Long id) {\n" +
               "        return " + var + "Repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());\n" +
               "    }\n\n" +
               "    @PreAuthorize(\"hasAuthority('" + tableName + ":write')\")\n" +
               "    @PostMapping\n" +
               "    public ResponseEntity<" + className + "> create(@RequestBody " + className + " entity) {\n" +
               "        return ResponseEntity.ok(" + var + "Repo.save(entity));\n" +
               "    }\n\n" +
               "    @PreAuthorize(\"hasAuthority('" + tableName + ":write')\")\n" +
               "    @PutMapping(\"/{id}\")\n" +
               "    public ResponseEntity<" + className + "> update(@PathVariable Long id, @RequestBody " + className + " body) {\n" +
               "        return " + var + "Repo.findById(id).map(e -> { body.setId(id); return ResponseEntity.ok(" + var + "Repo.save(body)); })\n" +
               "            .orElse(ResponseEntity.notFound().build());\n" +
               "    }\n\n" +
               "    @PreAuthorize(\"hasAuthority('" + tableName + ":write')\")\n" +
               "    @DeleteMapping(\"/{id}\")\n" +
               "    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {\n" +
               "        " + var + "Repo.deleteById(id);\n" +
               "        return ResponseEntity.ok(Map.of(\"success\", true));\n" +
               "    }\n" +
               "}";
    }

    private String generateMigration(String tableName, String className) {
        List<Map<String, Object>> cols = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY, CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS " +
            "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE() ORDER BY ORDINAL_POSITION", tableName);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");
        for (int i = 0; i < cols.size(); i++) {
            var col = cols.get(i);
            String colName = (String) col.get("COLUMN_NAME");
            String type = (String) col.get("DATA_TYPE");
            String nullable = "YES".equals(col.get("IS_NULLABLE")) ? "" : " NOT NULL";
            String key = "PRI".equals(col.get("COLUMN_KEY")) ? " AUTO_INCREMENT PRIMARY KEY" : "";
            String comment = (String) col.get("COLUMN_COMMENT");
            Object charLen = col.get("CHARACTER_MAXIMUM_LENGTH");
            String sqlType = mapSqlType(type, charLen);
            sb.append("    ").append(colName).append(" ").append(sqlType).append(nullable).append(key);
            if (comment != null && !comment.isEmpty()) sb.append(" COMMENT '").append(comment).append("'");
            if (i < cols.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(");\n");
        return sb.toString();
    }

    private String generateMenuSql(String tableName, String className) {
        String name = tableName.replace("_", "");
        return "-- 插入菜单（请在 DataInitializer 中执行）\n" +
               "INSERT INTO sys_menu (name, parent_id, sort_order, path, component, menu_type, perms, visible) VALUES\n" +
               "('" + tableName + "', 11, 7, '/" + name + "', '" + className + "View', 'C', 'system:" + tableName + ":list', true);\n\n" +
               "-- 更新 admin 角色权限\n" +
               "UPDATE role SET permissions = CONCAT(permissions, '," + tableName + ":read," + tableName + ":write') WHERE code = 'admin';";
    }

    private String mapSqlType(String mysqlType, Object charMaxLen) {
        return switch (mysqlType.toLowerCase()) {
            case "varchar" -> {
                long len = charMaxLen instanceof Number ? ((Number) charMaxLen).longValue() : 0;
                yield len > 0 ? "VARCHAR(" + len + ")" : "VARCHAR(255)";
            }
            case "char" -> {
                long len = charMaxLen instanceof Number ? ((Number) charMaxLen).longValue() : 0;
                yield len > 0 ? "CHAR(" + len + ")" : "CHAR(1)";
            }
            case "text", "longtext" -> "TEXT";
            case "int", "tinyint", "smallint" -> "INT";
            case "bigint" -> "BIGINT";
            case "datetime", "timestamp" -> "TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
            case "decimal", "double", "float" -> "DECIMAL(10,2)";
            default -> mysqlType.toUpperCase();
        };
    }

    private String generateRepository(String className, String pkg) {
        return "package " + pkg + ".repository;\n\n" +
               "import " + pkg + ".entity." + className + ";\n" +
               "import org.springframework.data.jpa.repository.JpaRepository;\n\n" +
               "public interface " + className + "Repository extends JpaRepository<" + className + ", Long> {\n" +
               "}";
    }

    private String generateService(String className, String pkg) {
        String repo = className + "Repository";
        String entity = className;
        String var = toCamelCase(className, false);
        return "package " + pkg + ".service;\n\n" +
               "import " + pkg + ".entity." + entity + ";\n" +
               "import " + pkg + ".repository." + repo + ";\n" +
               "import org.springframework.stereotype.Service;\n" +
               "import org.springframework.transaction.annotation.Transactional;\n\n" +
               "import java.util.List;\n" +
               "import java.util.Optional;\n\n" +
               "@Service\n" +
               "public class " + className + "Service {\n\n" +
               "    private final " + repo + " repository;\n\n" +
               "    public " + className + "Service(" + repo + " repository) {\n" +
               "        this.repository = repository;\n" +
               "    }\n\n" +
               "    public List<" + entity + "> findAll() {\n" +
               "        return repository.findAll();\n" +
               "    }\n\n" +
               "    public Optional<" + entity + "> findById(Long id) {\n" +
               "        return repository.findById(id);\n" +
               "    }\n\n" +
               "    @Transactional\n" +
               "    public " + entity + " save(" + entity + " entity) {\n" +
               "        return repository.save(entity);\n" +
               "    }\n\n" +
               "    @Transactional\n" +
               "    public void deleteById(Long id) {\n" +
               "        repository.deleteById(id);\n" +
               "    }\n" +
               "}";
    }

    private String toCamelCase(String name, boolean capitalize) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && !capitalize) { sb.append(parts[i]); }
            else { sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1)); }
        }
        return sb.toString();
    }

    private String generateVueApi(String tableName, String className) {
        String varName = toCamelCase(tableName, false);
        List<Map<String, Object>> cols = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE FROM information_schema.COLUMNS " +
            "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE() ORDER BY ORDINAL_POSITION", tableName);

        StringBuilder fields = new StringBuilder();
        for (var col : cols) {
            String colName = (String) col.get("COLUMN_NAME");
            String tsType = mapTsType((String) col.get("DATA_TYPE"));
            fields.append("  ").append(colName).append(": ").append(tsType).append("\n");
        }

        return "import request from '@/api/request'\n\n" +
               "export interface " + className + " {\n" +
               fields +
               "}\n\n" +
               "export function list" + className + "(params: any) {\n" +
               "  return request.get('/api/" + tableName + "', { params })\n" +
               "}\n\n" +
               "export function get" + className + "(id: number) {\n" +
               "  return request.get(`/api/" + tableName + "/${id}`)\n" +
               "}\n\n" +
               "export function create" + className + "(data: any) {\n" +
               "  return request.post('/api/" + tableName + "', data)\n" +
               "}\n\n" +
               "export function update" + className + "(id: number, data: any) {\n" +
               "  return request.put(`/api/" + tableName + "/${id}`, data)\n" +
               "}\n\n" +
               "export function delete" + className + "(id: number) {\n" +
               "  return request.delete(`/api/" + tableName + "/${id}`)\n" +
               "}\n";
    }

    private String generateVueView(String tableName, String className) {
        String cnName = tableName;
        List<Map<String, Object>> cols = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS " +
            "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE() ORDER BY ORDINAL_POSITION LIMIT 8", tableName);

        StringBuilder colDefs = new StringBuilder();
        for (var col : cols) {
            String colName = (String) col.get("COLUMN_NAME");
            String comment = (String) col.get("COLUMN_COMMENT");
            String label = (comment != null && !comment.isEmpty()) ? comment : colName;
            if ("id".equals(colName)) {
                colDefs.append("  { title: 'ID', key: 'id', width: 60 },\n");
            } else {
                colDefs.append("  { title: '").append(label).append("', key: '").append(colName).append("', width: 120, ellipsis: { tooltip: true } },\n");
            }
        }

        StringBuilder formFields = new StringBuilder();
        boolean first = true;
        for (var col : cols) {
            String colName = (String) col.get("COLUMN_NAME");
            if ("id".equals(colName)) continue;
            if (!first) formFields.append(", ");
            formFields.append(colName).append(": ''");
            first = false;
        }

        StringBuilder formItems = new StringBuilder();
        for (var col : cols) {
            String colName = (String) col.get("COLUMN_NAME");
            if ("id".equals(colName)) continue;
            String comment = (String) col.get("COLUMN_COMMENT");
            String label = (comment != null && !comment.isEmpty()) ? comment : colName;
            formItems.append("            <n-form-item label=\"").append(label).append("\">\n")
                     .append("              <n-input v-model:value=\"form.").append(colName).append("\" />\n")
                     .append("            </n-form-item>\n");
        }

        return "<script setup lang=\"ts\">\n" +
               "import { ref, onMounted, h, computed } from 'vue'\n" +
               "import { list" + className + ", create" + className + ", update" + className + ", delete" + className + " } from '@/api/" + tableName + "'\n" +
               "import type { " + className + " } from '@/api/" + tableName + "'\n" +
               "import { useNotify } from '@/composables/useNotify'\n" +
               "import {\n" +
               "  NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem,\n" +
               "  NInput, NSpace, NPopconfirm,\n" +
               "  darkTheme, lightTheme\n" +
               "} from 'naive-ui'\n" +
               "import { useI18n } from 'vue-i18n'\n" +
               "import type { DataTableColumns } from 'naive-ui'\n\n" +
               "const { t } = useI18n()\n" +
               "const notify = useNotify()\n" +
               "const currentTheme = computed(() => localStorage.getItem(\"theme\") === \"dark\" ? darkTheme : lightTheme)\n" +
               "const items = ref<" + className + "[]>([])\n" +
               "const loading = ref(false)\n" +
               "const saving = ref(false)\n" +
               "const showModal = ref(false)\n" +
               "const editing = ref<" + className + " | null>(null)\n" +
               "const form = ref({ " + formFields + " })\n" +
               "const searchKeyword = ref('')\n\n" +
               "const filteredItems = computed(() => {\n" +
               "  const kw = searchKeyword.value.toLowerCase()\n" +
               "  if (!kw) return items.value\n" +
               "  return items.value.filter(item => {\n" +
               "    return Object.values(item).some(v => String(v).toLowerCase().includes(kw))\n" +
               "  })\n" +
               "})\n\n" +
               "const columns: DataTableColumns<" + className + "> = [\n" +
               colDefs +
               "  {\n" +
               "    title: t('common.actions'), key: 'actions', width: 120,\n" +
               "    render: (row) => h(NSpace, null, {\n" +
               "      default: () => [\n" +
               "        h(NButton, { size: 'tiny', onClick: () => editItem(row) }, { default: () => t('common.edit') }),\n" +
               "        h(NPopconfirm, { onPositiveClick: () => delItem(row.id) }, {\n" +
               "          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),\n" +
               "          default: () => t('common.confirmDelete')\n" +
               "        })\n" +
               "      ]\n" +
               "    })\n" +
               "  },\n" +
               "]\n\n" +
               "async function loadItems() {\n" +
               "  loading.value = true\n" +
               "  try { const res = await list" + className + "(); items.value = res.data } catch {}\n" +
               "  loading.value = false\n" +
               "}\n\n" +
               "function addItem() {\n" +
               "  editing.value = null\n" +
               "  form.value = { " + formFields + " }\n" +
               "  showModal.value = true\n" +
               "}\n\n" +
               "function editItem(item: " + className + ") {\n" +
               "  editing.value = item\n" +
               "  form.value = { ...item }\n" +
               "  showModal.value = true\n" +
               "}\n\n" +
               "async function saveItem() {\n" +
               "  saving.value = true\n" +
               "  try {\n" +
               "    if (editing.value) await update" + className + "(editing.value.id, form.value)\n" +
               "    else await create" + className + "(form.value)\n" +
               "    showModal.value = false\n" +
               "    notify.success(editing.value ? t('common.updateSuccess') : t('common.createSuccess'))\n" +
               "    await loadItems()\n" +
               "  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }\n" +
               "}\n\n" +
               "async function delItem(id: number) {\n" +
               "  await delete" + className + "(id)\n" +
               "  await loadItems()\n" +
               "}\n\n" +
               "onMounted(loadItems)\n" +
               "</script>\n\n" +
               "<template>\n" +
               "  <n-config-provider :theme=\"currentTheme\">\n" +
               "    <div style=\"padding:20px\">\n" +
               "      <n-card title=\"" + cnName + "\">\n" +
               "        <template #header-extra>\n" +
               "          <n-button type=\"primary\" size=\"small\" @click=\"addItem\"><template #icon>＋</template>{{ t('common.add') }}</n-button>\n" +
               "        </template>\n" +
               "        <n-space style=\"margin-bottom:12px\" justify=\"space-between\">\n" +
               "          <n-space>\n" +
               "            <n-input v-model:value=\"searchKeyword\" :placeholder=\"t('common.search')\" size=\"small\" clearable style=\"width:180px\" />\n" +
               "            <n-button type=\"primary\" size=\"small\" @click=\"() => {}\">{{ t('common.search') }}</n-button>\n" +
               "            <n-button size=\"small\" @click=\"searchKeyword = ''\">{{ t('common.reset') }}</n-button>\n" +
               "          </n-space>\n" +
               "          <n-space><n-button size=\"small\" @click=\"loadItems\" secondary>{{ t('common.refresh') }}</n-button></n-space>\n" +
               "        </n-space>\n" +
               "        <n-dataTable :columns=\"columns\" :data=\"filteredItems\" :loading=\"loading\" size=\"small\"\n" +
               "          :bordered=\"false\" :pagination=\"{ pageSize: 10, pageSizes: [10,20,50,100] }\"\n" +
               "          :row-key=\"(row: " + className + ") => row.id\" />\n\n" +
               "        <n-modal v-model:show=\"showModal\" :title=\"editing ? t('common.edit') : t('common.add')\" style=\"width:480px\">\n" +
               "          <n-form label-placement=\"left\" label-width=\"80\">\n" +
               formItems +
               "          </n-form>\n" +
               "          <template #footer>\n" +
               "            <n-space justify=\"end\">\n" +
               "              <n-button @click=\"showModal = false\">{{ t('common.cancel') }}</n-button>\n" +
               "              <n-button type=\"primary\" :loading=\"saving\" @click=\"saveItem\">{{ t('common.save') }}</n-button>\n" +
               "            </n-space>\n" +
               "          </template>\n" +
               "        </n-modal>\n" +
               "      </n-card>\n" +
               "    </div>\n" +
               "  </n-config-provider>\n" +
               "</template>\n";
    }

    private String mapJavaType(String sqlType) {
        return switch (sqlType.toLowerCase()) {
            case "int", "tinyint", "smallint" -> "Integer";
            case "bigint" -> "Long";
            case "varchar", "char", "text", "longtext", "enum" -> "String";
            case "datetime", "timestamp" -> "java.time.LocalDateTime";
            case "decimal", "double", "float" -> "java.math.BigDecimal";
            case "boolean", "bit" -> "Boolean";
            default -> "String";
        };
    }

    private String mapTsType(String sqlType) {
        return switch (sqlType.toLowerCase()) {
            case "int", "tinyint", "smallint", "bigint", "decimal", "double", "float" -> "number";
            case "boolean", "bit" -> "boolean";
            default -> "string";
        };
    }
}
