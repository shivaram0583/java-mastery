package com.javamastery.java8;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Date/Time API")
class DateTimeApiTest {

    @Test
    @DisplayName("LocalDate creation and comparison")
    void localDateCreation() {
        LocalDate date = LocalDate.of(2024, Month.MARCH, 15);

        assertEquals(2024, date.getYear());
        assertEquals(Month.MARCH, date.getMonth());
        assertEquals(15, date.getDayOfMonth());
        assertEquals(DayOfWeek.FRIDAY, date.getDayOfWeek());
    }

    @Test
    @DisplayName("LocalDate is immutable")
    void localDateImmutable() {
        LocalDate original = LocalDate.of(2024, 1, 1);
        LocalDate plusOne = original.plusDays(1);

        // Original is not modified
        assertEquals(LocalDate.of(2024, 1, 1), original);
        assertEquals(LocalDate.of(2024, 1, 2), plusOne);
    }

    @Test
    @DisplayName("Duration measures time-based amount")
    void durationMeasuresTime() {
        Duration twoHours = Duration.ofHours(2);

        assertEquals(120, twoHours.toMinutes());
        assertEquals(7200, twoHours.toSeconds());
    }

    @Test
    @DisplayName("Period measures date-based amount")
    void periodMeasuresDate() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 7, 15);
        Period period = Period.between(start, end);

        assertEquals(0, period.getYears());
        assertEquals(6, period.getMonths());
        assertEquals(14, period.getDays());
    }

    @Test
    @DisplayName("ChronoUnit calculates total difference")
    void chronoUnitDifference() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);

        long days = ChronoUnit.DAYS.between(start, end);
        assertEquals(365, days); // 2024 is a leap year: 366 days, but 365 between Jan 1 and Dec 31
    }

    @Test
    @DisplayName("DateTimeFormatter formats and parses")
    void formatterFormatsAndParses() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String formatted = date.format(formatter);
        assertEquals("15/03/2024", formatted);

        LocalDate parsed = LocalDate.parse("15/03/2024", formatter);
        assertEquals(date, parsed);
    }

    @Test
    @DisplayName("ZonedDateTime handles timezone conversion")
    void zonedDateTimeConversion() {
        ZonedDateTime utc = ZonedDateTime.of(
                LocalDateTime.of(2024, 3, 15, 12, 0),
                ZoneId.of("UTC"));

        ZonedDateTime tokyo = utc.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));

        // Tokyo is UTC+9
        assertEquals(21, tokyo.getHour());
        // Same instant in time
        assertEquals(utc.toInstant(), tokyo.toInstant());
    }

    @Test
    @DisplayName("Instant represents machine-readable timestamp")
    void instantTimestamp() {
        Instant now = Instant.now();
        Instant later = now.plus(1, ChronoUnit.HOURS);

        assertTrue(later.isAfter(now));
        assertEquals(3600, Duration.between(now, later).toSeconds());
    }

    @Test
    @DisplayName("Leap year detection")
    void leapYear() {
        assertTrue(LocalDate.of(2024, 1, 1).isLeapYear());
        assertFalse(LocalDate.of(2023, 1, 1).isLeapYear());
    }
}
