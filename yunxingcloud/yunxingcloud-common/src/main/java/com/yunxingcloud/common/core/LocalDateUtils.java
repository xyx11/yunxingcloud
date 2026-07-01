package com.yunxingcloud.common.core;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class LocalDateUtils {

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateUtils() {}

    public static String today() { return LocalDate.now().format(DATE_FMT); }
    public static String now() { return LocalDateTime.now().format(DATETIME_FMT); }
    public static long daysBetween(LocalDate from, LocalDate to) { return ChronoUnit.DAYS.between(from, to); }
    public static LocalDate daysAgo(int n) { return LocalDate.now().minusDays(n); }
    public static boolean isExpired(LocalDateTime time, int minutes) {
        return time != null && time.plusMinutes(minutes).isBefore(LocalDateTime.now());
    }
}