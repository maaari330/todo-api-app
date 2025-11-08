package com.example.todoapi.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class TimeZoneConverter {
    private static final ZoneId JTC_ZONE = ZoneId.of("Asia/Tokyo");
    private static final ZoneId UTC_ZONE = ZoneOffset.UTC;

    // インスタンス化禁止
    private TimeZoneConverter(){}

    /** JTC -> UTCに変換 */
    public static LocalDateTime toUtc(LocalDateTime jtcDateTime) {
        if (jtcDateTime == null) {
            return null;
        }
        ZonedDateTime jtcZoned = jtcDateTime.atZone(JTC_ZONE);
        return jtcZoned.withZoneSameInstant(UTC_ZONE).toLocalDateTime();
    }

    /** UTC -> JTCに変換 */
    public static LocalDateTime toJtc(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        ZonedDateTime utcZoned = utcDateTime.atZone(UTC_ZONE);
        return utcZoned.withZoneSameInstant(JTC_ZONE).toLocalDateTime();
    }
}