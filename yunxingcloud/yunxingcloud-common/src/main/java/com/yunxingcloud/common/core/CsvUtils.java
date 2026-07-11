package com.yunxingcloud.common.core;

import java.util.List;
import java.util.StringJoiner;

public final class CsvUtils {

    private CsvUtils() {}

    public static String toCsv(String[] headers, List<String[]> rows) {
        StringBuilder sb = new StringBuilder(String.join(",", headers)).append("\n");
        for (String[] row : rows) {
            StringJoiner joiner = new StringJoiner(",");
            for (String val : row) {
                joiner.add(escape(val));
            }
            sb.append(joiner).append("\n");
        }
        return sb.toString();
    }

    private static String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
