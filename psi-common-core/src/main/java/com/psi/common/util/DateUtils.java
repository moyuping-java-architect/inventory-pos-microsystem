package com.psi.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 日期时间工具类
 * 基于 Java 8+ java.time API 实现，提供日期格式化、时区转换、年龄计算、工作日处理等功能
 * 
 * @author PSI
 * @version 1.0.0
 */
public final class DateUtils {

    private DateUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // ==================== 日期格式常量 ====================

    /** ISO 8601 日期时间格式 */
    public static final String PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss";
    
    /** 标准日期时间格式 */
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    
    /** 仅日期格式 */
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    
    /** 仅时间格式 */
    public static final String PATTERN_TIME = "HH:mm:ss";
    
    /** 中文日期时间格式 */
    public static final String PATTERN_DATETIME_CN = "yyyy年MM月dd日 HH:mm:ss";
    
    /** 中文日期格式 */
    public static final String PATTERN_DATE_CN = "yyyy年MM月dd日";

    // ==================== 时区常量 ====================

    /** 系统默认时区 */
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    
    /** UTC 时区 */
    public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    
    /** 上海时区（东八区） */
    public static final ZoneId ASIA_SHANGHAI = ZoneId.of("Asia/Shanghai");

    // ==================== 日期格式化方法 ====================

    /**
     * 格式化 LocalDateTime
     * @param dateTime 日期时间对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || StringUtils.isEmpty(pattern)) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化 LocalDate
     * @param date 日期对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null || StringUtils.isEmpty(pattern)) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化 LocalTime
     * @param time 时间对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalTime time, String pattern) {
        if (time == null || StringUtils.isEmpty(pattern)) {
            return null;
        }
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 使用默认时区格式化 Instant
     * @param instant 瞬时时间对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(Instant instant, String pattern) {
        return format(instant, pattern, DEFAULT_ZONE_ID);
    }

    /**
     * 使用指定时区格式化 Instant
     * @param instant 瞬时时间对象
     * @param pattern 格式模式
     * @param zoneId 时区
     * @return 格式化后的字符串
     */
    public static String format(Instant instant, String pattern, ZoneId zoneId) {
        if (instant == null || StringUtils.isEmpty(pattern) || zoneId == null) {
            return null;
        }
        return instant.atZone(zoneId).format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化为标准日期时间字符串
     * @param dateTime 日期时间对象
     * @return 格式化后的字符串，如：2024-05-14 10:30:00
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return format(dateTime, PATTERN_DATETIME);
    }

    /**
     * 格式化为日期字符串
     * @param date 日期对象
     * @return 格式化后的字符串，如：2024-05-14
     */
    public static String formatDate(LocalDate date) {
        return format(date, PATTERN_DATE);
    }

    /**
     * 格式化为时间字符串
     * @param time 时间对象
     * @return 格式化后的字符串，如：10:30:00
     */
    public static String formatTime(LocalTime time) {
        return format(time, PATTERN_TIME);
    }

    /**
     * 格式化为中文日期时间字符串
     * @param dateTime 日期时间对象
     * @return 格式化后的字符串，如：2024年05月14日 10:30:00
     */
    public static String formatDateTimeCN(LocalDateTime dateTime) {
        return format(dateTime, PATTERN_DATETIME_CN);
    }

    /**
     * 格式化为中文日期字符串
     * @param date 日期对象
     * @return 格式化后的字符串，如：2024年05月14日
     */
    public static String formatDateCN(LocalDate date) {
        return format(date, PATTERN_DATE_CN);
    }

    // ==================== 日期解析方法 ====================

    /**
     * 解析日期时间字符串（使用标准格式）
     * @param dateStr 日期时间字符串
     * @return LocalDateTime 对象
     */
    public static LocalDateTime parseDateTime(String dateStr) {
        return parseDateTime(dateStr, PATTERN_DATETIME);
    }

    /**
     * 解析日期时间字符串（使用指定格式）
     * @param dateStr 日期时间字符串
     * @param pattern 格式模式
     * @return LocalDateTime 对象
     */
    public static LocalDateTime parseDateTime(String dateStr, String pattern) {
        if (StringUtils.isEmpty(dateStr) || StringUtils.isEmpty(pattern)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析日期字符串（使用标准格式）
     * @param dateStr 日期字符串
     * @return LocalDate 对象
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, PATTERN_DATE);
    }

    /**
     * 解析日期字符串（使用指定格式）
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return LocalDate 对象
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (StringUtils.isEmpty(dateStr) || StringUtils.isEmpty(pattern)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析时间字符串（使用标准格式）
     * @param timeStr 时间字符串
     * @return LocalTime 对象
     */
    public static LocalTime parseTime(String timeStr) {
        return parseTime(timeStr, PATTERN_TIME);
    }

    /**
     * 解析时间字符串（使用指定格式）
     * @param timeStr 时间字符串
     * @param pattern 格式模式
     * @return LocalTime 对象
     */
    public static LocalTime parseTime(String timeStr, String pattern) {
        if (StringUtils.isEmpty(timeStr) || StringUtils.isEmpty(pattern)) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析日期时间字符串为 Instant
     * @param dateStr 日期时间字符串
     * @param pattern 格式模式
     * @param zoneId 时区
     * @return Instant 对象
     */
    public static Instant parseInstant(String dateStr, String pattern, ZoneId zoneId) {
        if (StringUtils.isEmpty(dateStr) || StringUtils.isEmpty(pattern) || zoneId == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern))
                    .atZone(zoneId)
                    .toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 时区转换方法 ====================

    /**
     * 将 Instant 转换为 LocalDateTime（使用默认时区）
     * @param instant 瞬时时间对象
     * @return LocalDateTime 对象
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return toLocalDateTime(instant, DEFAULT_ZONE_ID);
    }

    /**
     * 将 Instant 转换为 LocalDateTime（使用指定时区）
     * @param instant 瞬时时间对象
     * @param zoneId 时区
     * @return LocalDateTime 对象
     */
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zoneId) {
        if (instant == null || zoneId == null) {
            return null;
        }
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * 将 Instant 转换为 LocalDate（使用默认时区）
     * @param instant 瞬时时间对象
     * @return LocalDate 对象
     */
    public static LocalDate toLocalDate(Instant instant) {
        return toLocalDate(instant, DEFAULT_ZONE_ID);
    }

    /**
     * 将 Instant 转换为 LocalDate（使用指定时区）
     * @param instant 瞬时时间对象
     * @param zoneId 时区
     * @return LocalDate 对象
     */
    public static LocalDate toLocalDate(Instant instant, ZoneId zoneId) {
        if (instant == null || zoneId == null) {
            return null;
        }
        return instant.atZone(zoneId).toLocalDate();
    }

    /**
     * 将 LocalDateTime 转换为 Instant（使用默认时区）
     * @param dateTime 日期时间对象
     * @return Instant 对象
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        return toInstant(dateTime, DEFAULT_ZONE_ID);
    }

    /**
     * 将 LocalDateTime 转换为 Instant（使用指定时区）
     * @param dateTime 日期时间对象
     * @param zoneId 时区
     * @return Instant 对象
     */
    public static Instant toInstant(LocalDateTime dateTime, ZoneId zoneId) {
        if (dateTime == null || zoneId == null) {
            return null;
        }
        return dateTime.atZone(zoneId).toInstant();
    }

    /**
     * 将 LocalDate 转换为 Instant（使用默认时区，时间为00:00:00）
     * @param date 日期对象
     * @return Instant 对象
     */
    public static Instant toInstant(LocalDate date) {
        return toInstant(date, DEFAULT_ZONE_ID);
    }

    /**
     * 将 LocalDate 转换为 Instant（使用指定时区，时间为00:00:00）
     * @param date 日期对象
     * @param zoneId 时区
     * @return Instant 对象
     */
    public static Instant toInstant(LocalDate date, ZoneId zoneId) {
        if (date == null || zoneId == null) {
            return null;
        }
        return date.atStartOfDay(zoneId).toInstant();
    }

    /**
     * 时区转换
     * @param dateTime 日期时间对象
     * @param fromZone 原时区
     * @param toZone 目标时区
     * @return 转换后的 LocalDateTime
     */
    public static LocalDateTime convertTimezone(LocalDateTime dateTime, ZoneId fromZone, ZoneId toZone) {
        if (dateTime == null || fromZone == null || toZone == null) {
            return null;
        }
        return dateTime.atZone(fromZone).withZoneSameInstant(toZone).toLocalDateTime();
    }

    /**
     * 使用指定时区格式化 Instant
     * @param instant 瞬时时间对象
     * @param pattern 格式模式
     * @param zoneId 时区
     * @return 格式化后的字符串
     */
    public static String formatWithTimezone(Instant instant, String pattern, ZoneId zoneId) {
        if (instant == null || StringUtils.isEmpty(pattern) || zoneId == null) {
            return null;
        }
        return instant.atZone(zoneId).format(DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== 年龄计算方法 ====================

    /**
     * 根据生日计算年龄（以今天为参考日期）
     * @param birthDate 出生日期
     * @return 年龄
     */
    public static int calculateAge(LocalDate birthDate) {
        return calculateAge(birthDate, LocalDate.now());
    }

    /**
     * 根据生日计算年龄（指定参考日期）
     * @param birthDate 出生日期
     * @param referenceDate 参考日期
     * @return 年龄
     */
    public static int calculateAge(LocalDate birthDate, LocalDate referenceDate) {
        if (birthDate == null || referenceDate == null) {
            return 0;
        }
        if (birthDate.isAfter(referenceDate)) {
            return 0;
        }
        int years = referenceDate.getYear() - birthDate.getYear();
        LocalDate anniversary = birthDate.plusYears(years);
        if (anniversary.isAfter(referenceDate)) {
            years--;
        }
        return years;
    }

    /**
     * 根据生日字符串计算年龄（以今天为参考日期）
     * @param birthDateStr 出生日期字符串（yyyy-MM-dd格式）
     * @return 年龄
     */
    public static int calculateAge(String birthDateStr) {
        return calculateAge(parseDate(birthDateStr));
    }

    /**
     * 根据生日字符串计算年龄（指定参考日期字符串）
     * @param birthDateStr 出生日期字符串（yyyy-MM-dd格式）
     * @param referenceDateStr 参考日期字符串（yyyy-MM-dd格式）
     * @return 年龄
     */
    public static int calculateAge(String birthDateStr, String referenceDateStr) {
        return calculateAge(parseDate(birthDateStr), parseDate(referenceDateStr));
    }

    // ==================== 工作日处理方法 ====================

    /**
     * 判断是否为工作日（不含节假日）
     * @param date 日期对象
     * @return true-工作日，false-非工作日
     */
    public static boolean isWorkday(LocalDate date) {
        return isWorkday(date, null);
    }

    /**
     * 判断是否为工作日（可排除指定节假日）
     * @param date 日期对象
     * @param holidays 节假日集合
     * @return true-工作日，false-非工作日
     */
    public static boolean isWorkday(LocalDate date, Set<LocalDate> holidays) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        // 周末（周六、周日）不是工作日
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        // 节假日不是工作日
        if (holidays != null && holidays.contains(date)) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为周末
     * @param date 日期对象
     * @return true-周末，false-非周末
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 获取下一个工作日（不含节假日）
     * @param date 日期对象
     * @return 下一个工作日
     */
    public static LocalDate nextWorkday(LocalDate date) {
        return nextWorkday(date, null);
    }

    /**
     * 获取下一个工作日（可排除指定节假日）
     * @param date 日期对象
     * @param holidays 节假日集合
     * @return 下一个工作日
     */
    public static LocalDate nextWorkday(LocalDate date, Set<LocalDate> holidays) {
        if (date == null) {
            return null;
        }
        LocalDate nextDay = date.plusDays(1);
        while (!isWorkday(nextDay, holidays)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    /**
     * 获取上一个工作日（不含节假日）
     * @param date 日期对象
     * @return 上一个工作日
     */
    public static LocalDate previousWorkday(LocalDate date) {
        return previousWorkday(date, null);
    }

    /**
     * 获取上一个工作日（可排除指定节假日）
     * @param date 日期对象
     * @param holidays 节假日集合
     * @return 上一个工作日
     */
    public static LocalDate previousWorkday(LocalDate date, Set<LocalDate> holidays) {
        if (date == null) {
            return null;
        }
        LocalDate prevDay = date.minusDays(1);
        while (!isWorkday(prevDay, holidays)) {
            prevDay = prevDay.minusDays(1);
        }
        return prevDay;
    }

    /**
     * 计算两个日期之间的工作日天数（不含节假日）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作日天数
     */
    public static long countWorkdays(LocalDate startDate, LocalDate endDate) {
        return countWorkdays(startDate, endDate, null);
    }

    /**
     * 计算两个日期之间的工作日天数（可排除指定节假日）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param holidays 节假日集合
     * @return 工作日天数
     */
    public static long countWorkdays(LocalDate startDate, LocalDate endDate, Set<LocalDate> holidays) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        if (startDate.isAfter(endDate)) {
            return 0;
        }
        long count = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isWorkday(current, holidays)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    /**
     * 获取两个日期之间的所有工作日（不含节假日）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作日列表
     */
    public static List<LocalDate> getWorkdays(LocalDate startDate, LocalDate endDate) {
        return getWorkdays(startDate, endDate, null);
    }

    /**
     * 获取两个日期之间的所有工作日（可排除指定节假日）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param holidays 节假日集合
     * @return 工作日列表
     */
    public static List<LocalDate> getWorkdays(LocalDate startDate, LocalDate endDate, Set<LocalDate> holidays) {
        List<LocalDate> workdays = new ArrayList<>();
        if (startDate == null || endDate == null) {
            return workdays;
        }
        if (startDate.isAfter(endDate)) {
            return workdays;
        }
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isWorkday(current, holidays)) {
                workdays.add(current);
            }
            current = current.plusDays(1);
        }
        return workdays;
    }

    // ==================== 时间差计算方法 ====================

    /**
     * 计算两个日期之间的天数差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差（结束日期 - 开始日期）
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个日期时间之间的小时差
     * @param start 开始时间
     * @param end 结束时间
     * @return 小时差（结束时间 - 开始时间）
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的分钟差
     * @param start 开始时间
     * @param end 结束时间
     * @return 分钟差（结束时间 - 开始时间）
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 计算两个日期时间之间的秒数差
     * @param start 开始时间
     * @param end 结束时间
     * @return 秒数差（结束时间 - 开始时间）
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * 计算两个瞬时时间之间的毫秒差
     * @param start 开始时间
     * @param end 结束时间
     * @return 毫秒差（结束时间 - 开始时间）
     */
    public static long millisecondsBetween(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MILLIS.between(start, end);
    }

    // ==================== 获取当前时间方法 ====================

    /**
     * 获取今天日期
     * @return 今天日期
     */
    public static LocalDate getToday() {
        return LocalDate.now();
    }

    /**
     * 获取当前日期时间
     * @return 当前日期时间
     */
    public static LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前瞬时时间
     * @return 当前瞬时时间
     */
    public static Instant getInstantNow() {
        return Instant.now();
    }

    /**
     * 获取昨天日期
     * @return 昨天日期
     */
    public static LocalDate getYesterday() {
        return LocalDate.now().minusDays(1);
    }

    /**
     * 获取明天日期
     * @return 明天日期
     */
    public static LocalDate getTomorrow() {
        return LocalDate.now().plusDays(1);
    }

    // ==================== 日期边界方法 ====================

    /**
     * 获取指定日期所在月份的第一天
     * @param date 日期对象
     * @return 该月第一天
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取指定日期所在月份的最后一天
     * @param date 日期对象
     * @return 该月最后一天
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取指定日期所在年份的第一天
     * @param date 日期对象
     * @return 该年第一天
     */
    public static LocalDate getFirstDayOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * 获取指定日期所在年份的最后一天
     * @param date 日期对象
     * @return 该年最后一天
     */
    public static LocalDate getLastDayOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * 获取指定日期所在周的第一天（周一）
     * @param date 日期对象
     * @return 该周第一天
     */
    public static LocalDate getFirstDayOfWeek(LocalDate date) {
        return getFirstDayOfWeek(date, DayOfWeek.MONDAY);
    }

    /**
     * 获取指定日期所在周的第一天（指定星期几）
     * @param date 日期对象
     * @param firstDayOfWeek 一周的第一天
     * @return 该周第一天
     */
    public static LocalDate getFirstDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        if (date == null || firstDayOfWeek == null) {
            return null;
        }
        return date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
    }

    /**
     * 获取指定日期所在周的最后一天（周日）
     * @param date 日期对象
     * @return 该周最后一天
     */
    public static LocalDate getLastDayOfWeek(LocalDate date) {
        return getLastDayOfWeek(date, DayOfWeek.SUNDAY);
    }

    /**
     * 获取指定日期所在周的最后一天（指定星期几）
     * @param date 日期对象
     * @param lastDayOfWeek 一周的最后一天
     * @return 该周最后一天
     */
    public static LocalDate getLastDayOfWeek(LocalDate date, DayOfWeek lastDayOfWeek) {
        if (date == null || lastDayOfWeek == null) {
            return null;
        }
        return date.with(TemporalAdjusters.nextOrSame(lastDayOfWeek));
    }

    /**
     * 获取指定日期的开始时间（00:00:00）
     * @param date 日期对象
     * @return 当天开始时间
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * 获取指定日期的结束时间（23:59:59.999999999）
     * @param date 日期对象
     * @return 当天结束时间
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 获取指定时间所在小时的开始时间
     * @param dateTime 日期时间对象
     * @return 该小时开始时间
     */
    public static LocalDateTime getStartOfHour(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.truncatedTo(ChronoUnit.HOURS);
    }

    /**
     * 获取指定时间所在小时的结束时间
     * @param dateTime 日期时间对象
     * @return 该小时结束时间
     */
    public static LocalDateTime getEndOfHour(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.truncatedTo(ChronoUnit.HOURS).plusHours(1).minusNanos(1);
    }

    // ==================== 日期偏移方法 ====================

    /**
     * 日期加上指定天数
     * @param date 日期对象
     * @param days 天数（正数向后，负数向前）
     * @return 偏移后的日期
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * 日期时间加上指定天数
     * @param dateTime 日期时间对象
     * @param days 天数（正数向后，负数向前）
     * @return 偏移后的日期时间
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }

    /**
     * 日期时间加上指定小时数
     * @param dateTime 日期时间对象
     * @param hours 小时数（正数向后，负数向前）
     * @return 偏移后的日期时间
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    /**
     * 日期时间加上指定分钟数
     * @param dateTime 日期时间对象
     * @param minutes 分钟数（正数向后，负数向前）
     * @return 偏移后的日期时间
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    /**
     * 日期时间加上指定秒数
     * @param dateTime 日期时间对象
     * @param seconds 秒数（正数向后，负数向前）
     * @return 偏移后的日期时间
     */
    public static LocalDateTime addSeconds(LocalDateTime dateTime, long seconds) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusSeconds(seconds);
    }

    /**
     * 日期加上指定月数
     * @param date 日期对象
     * @param months 月数（正数向后，负数向前）
     * @return 偏移后的日期
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        if (date == null) {
            return null;
        }
        return date.plusMonths(months);
    }

    /**
     * 日期加上指定年数
     * @param date 日期对象
     * @param years 年数（正数向后，负数向前）
     * @return 偏移后的日期
     */
    public static LocalDate addYears(LocalDate date, long years) {
        if (date == null) {
            return null;
        }
        return date.plusYears(years);
    }

    // ==================== 日期比较方法 ====================

    /**
     * 判断两个日期是否为同一天
     * @param date1 日期1
     * @param date2 日期2
     * @return true-同一天，false-不同天
     */
    public static boolean isSameDay(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.equals(date2);
    }

    /**
     * 判断两个日期时间是否为同一天
     * @param dateTime1 日期时间1
     * @param dateTime2 日期时间2
     * @return true-同一天，false-不同天
     */
    public static boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }

    /**
     * 判断两个日期是否为同一个月
     * @param date1 日期1
     * @param date2 日期2
     * @return true-同一个月，false-不同月
     */
    public static boolean isSameMonth(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getYear() == date2.getYear() && date1.getMonth() == date2.getMonth();
    }

    /**
     * 判断两个日期是否为同一年
     * @param date1 日期1
     * @param date2 日期2
     * @return true-同一年，false-不同年
     */
    public static boolean isSameYear(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getYear() == date2.getYear();
    }

    /**
     * 判断日期1是否在日期2之前
     * @param date1 日期1
     * @param date2 日期2
     * @return true-date1在date2之前，false-否则
     */
    public static boolean isBefore(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isBefore(date2);
    }

    /**
     * 判断日期1是否在日期2之后
     * @param date1 日期1
     * @param date2 日期2
     * @return true-date1在date2之后，false-否则
     */
    public static boolean isAfter(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isAfter(date2);
    }

    /**
     * 判断日期是否在指定范围内（包含边界）
     * @param date 待判断日期
     * @param start 开始日期
     * @param end 结束日期
     * @return true-在范围内，false-不在范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * 判断是否为今天
     * @param date 日期对象
     * @return true-今天，false-非今天
     */
    public static boolean isToday(LocalDate date) {
        return isSameDay(date, LocalDate.now());
    }

    /**
     * 判断是否为本周
     * @param date 日期对象
     * @return true-本周，false-非本周
     */
    public static boolean isThisWeek(LocalDate date) {
        if (date == null) {
            return false;
        }
        LocalDate startOfWeek = getFirstDayOfWeek(LocalDate.now());
        LocalDate endOfWeek = getLastDayOfWeek(LocalDate.now());
        return isBetween(date, startOfWeek, endOfWeek);
    }

    /**
     * 判断是否为本月
     * @param date 日期对象
     * @return true-本月，false-非本月
     */
    public static boolean isThisMonth(LocalDate date) {
        return isSameMonth(date, LocalDate.now());
    }

    /**
     * 判断是否为本年
     * @param date 日期对象
     * @return true-本年，false-非本年
     */
    public static boolean isThisYear(LocalDate date) {
        return isSameYear(date, LocalDate.now());
    }

    // ==================== 相对时间方法 ====================

    /**
     * 获取相对时间描述（如：刚刚、5分钟前、2小时前、3天前）
     * @param dateTime 日期时间对象
     * @return 相对时间描述
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + "小时前";
        } else if (minutes < 10080) {
            long days = minutes / 1440;
            return days + "天前";
        } else if (minutes < 43200) {
            long weeks = minutes / 10080;
            return weeks + "周前";
        } else {
            return formatDate(dateTime.toLocalDate());
        }
    }

    /**
     * 获取相对时间描述（从Instant转换）
     * @param instant 瞬时时间对象
     * @return 相对时间描述
     */
    public static String getRelativeTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return getRelativeTime(toLocalDateTime(instant));
    }

    // ==================== 日期名称方法 ====================

    /**
     * 获取星期几的中文名称
     * @param date 日期对象
     * @return 星期几的中文名称（如：星期一）
     */
    public static String getWeekDayName(LocalDate date) {
        if (date == null) {
            return null;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY: return "星期一";
            case TUESDAY: return "星期二";
            case WEDNESDAY: return "星期三";
            case THURSDAY: return "星期四";
            case FRIDAY: return "星期五";
            case SATURDAY: return "星期六";
            case SUNDAY: return "星期日";
            default: return "";
        }
    }

    /**
     * 获取月份的中文名称
     * @param month 月份枚举
     * @return 月份的中文名称（如：一月）
     */
    public static String getMonthName(Month month) {
        if (month == null) {
            return null;
        }
        switch (month) {
            case JANUARY: return "一月";
            case FEBRUARY: return "二月";
            case MARCH: return "三月";
            case APRIL: return "四月";
            case MAY: return "五月";
            case JUNE: return "六月";
            case JULY: return "七月";
            case AUGUST: return "八月";
            case SEPTEMBER: return "九月";
            case OCTOBER: return "十月";
            case NOVEMBER: return "十一月";
            case DECEMBER: return "十二月";
            default: return "";
        }
    }

    // ==================== 中国节假日方法 ====================

    /**
     * 创建指定年份的中国法定节假日集合
     * @param year 年份
     * @return 节假日集合
     */
    public static Set<LocalDate> createHolidays(int year) {
        Set<LocalDate> holidays = new HashSet<>();
        
        // 元旦（1月1日-3日）
        holidays.add(LocalDate.of(year, 1, 1));
        holidays.add(LocalDate.of(year, 1, 2));
        holidays.add(LocalDate.of(year, 1, 3));
        
        // 春节（农历正月初一，3天假期+2天调休）
        int springFestivalMonth = getSpringFestivalMonth(year);
        int springFestivalDay = getSpringFestivalDay(year);
        holidays.add(LocalDate.of(year, springFestivalMonth, springFestivalDay));
        holidays.add(LocalDate.of(year, springFestivalMonth, springFestivalDay + 1));
        holidays.add(LocalDate.of(year, springFestivalMonth, springFestivalDay + 2));
        holidays.add(LocalDate.of(year, springFestivalMonth, springFestivalDay + 3));
        holidays.add(LocalDate.of(year, springFestivalMonth, springFestivalDay + 4));
        
        // 清明节（4月4-6日）
        holidays.add(LocalDate.of(year, 4, 4));
        holidays.add(LocalDate.of(year, 4, 5));
        holidays.add(LocalDate.of(year, 4, 6));
        
        // 劳动节（5月1-3日）
        holidays.add(LocalDate.of(year, 5, 1));
        holidays.add(LocalDate.of(year, 5, 2));
        holidays.add(LocalDate.of(year, 5, 3));
        
        // 端午节（6月22-24日）
        holidays.add(LocalDate.of(year, 6, 22));
        holidays.add(LocalDate.of(year, 6, 23));
        holidays.add(LocalDate.of(year, 6, 24));
        
        // 中秋节（9月15-17日）
        holidays.add(LocalDate.of(year, 9, 15));
        holidays.add(LocalDate.of(year, 9, 16));
        holidays.add(LocalDate.of(year, 9, 17));
        
        // 国庆节（10月1-7日）
        holidays.add(LocalDate.of(year, 10, 1));
        holidays.add(LocalDate.of(year, 10, 2));
        holidays.add(LocalDate.of(year, 10, 3));
        holidays.add(LocalDate.of(year, 10, 4));
        holidays.add(LocalDate.of(year, 10, 5));
        holidays.add(LocalDate.of(year, 10, 6));
        holidays.add(LocalDate.of(year, 10, 7));
        
        return holidays;
    }

    /**
     * 获取春节所在月份（2024-2043年）
     * @param year 年份
     * @return 月份（1或2）
     */
    private static int getSpringFestivalMonth(int year) {
        int[] months = {1, 2, 2, 1, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 1, 2, 2, 1, 2, 2};
        int index = year - 2024;
        return months[Math.min(index, months.length - 1)];
    }

    /**
     * 获取春节所在日期（2024-2043年）
     * @param year 年份
     * @return 日期
     */
    private static int getSpringFestivalDay(int year) {
        int[] days = {10, 29, 17, 29, 15, 5, 25, 10, 30, 19, 9, 28, 17, 6, 26, 14, 3, 23, 11, 31};
        int index = year - 2024;
        return days[Math.min(index, days.length - 1)];
    }

    /**
     * 判断是否为中国法定节假日
     * @param date 日期对象
     * @return true-节假日，false-非节假日
     */
    public static boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        Set<LocalDate> holidays = createHolidays(date.getYear());
        return holidays.contains(date);
    }

    // ==================== 时长格式化方法 ====================

    /**
     * 将秒数格式化为可读的时长字符串
     * @param seconds 秒数
     * @return 时长字符串（如：2天3小时5分钟）
     */
    public static String formatDuration(long seconds) {
        if (seconds < 0) {
            seconds = -seconds;
        }
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append("秒");
        }
        return sb.toString();
    }

    /**
     * 将时长字符串解析为秒数
     * @param durationStr 时长字符串（如：2天3小时5分钟）
     * @return 秒数
     */
    public static long parseDuration(String durationStr) {
        if (StringUtils.isEmpty(durationStr)) {
            return 0;
        }
        long seconds = 0;
        String[] parts = durationStr.split("(?=[0-9]+)");
        for (int i = 0; i < parts.length; i += 2) {
            if (i + 1 >= parts.length) break;
            long value = Long.parseLong(parts[i]);
            String unit = parts[i + 1];
            switch (unit) {
                case "天": seconds += value * 86400; break;
                case "小时": seconds += value * 3600; break;
                case "分钟": seconds += value * 60; break;
                case "秒": seconds += value; break;
            }
        }
        return seconds;
    }
}