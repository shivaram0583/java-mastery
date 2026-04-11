package com.javamastery.java8;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Demonstrates the new Date/Time API introduced in Java 8 (java.time package).
 *
 * The new API is immutable, thread-safe, and follows ISO-8601 standards.
 * It replaces the problematic java.util.Date and java.util.Calendar classes.
 */
public class DateTimeApiDemo {

    public static void main(String[] args) {
        System.out.println("=== Date/Time API Demo ===\n");

        // --- 1. LocalDate — date without time or timezone ---
        LocalDate today = LocalDate.now();
        LocalDate birthday = LocalDate.of(1990, Month.JUNE, 15);
        LocalDate parsed = LocalDate.parse("2024-12-25");

        System.out.println("Today: " + today);
        System.out.println("Birthday: " + birthday);
        System.out.println("Christmas 2024: " + parsed);
        System.out.println("Day of week: " + today.getDayOfWeek());
        System.out.println("Is leap year: " + today.isLeapYear());

        // --- 2. LocalTime — time without date or timezone ---
        LocalTime now = LocalTime.now();
        LocalTime meetingTime = LocalTime.of(14, 30, 0);
        System.out.println("\nCurrent time: " + now);
        System.out.println("Meeting time: " + meetingTime);
        System.out.println("Is before meeting: " + now.isBefore(meetingTime));

        // --- 3. LocalDateTime — date + time, no timezone ---
        LocalDateTime dateTime = LocalDateTime.of(today, meetingTime);
        System.out.println("\nMeeting datetime: " + dateTime);
        // Immutable: plus/minus return new instances
        LocalDateTime nextWeek = dateTime.plusWeeks(1);
        System.out.println("Next week: " + nextWeek);

        // --- 4. ZonedDateTime — date + time + timezone ---
        ZonedDateTime zonedNow = ZonedDateTime.now();
        ZonedDateTime tokyoTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime newYorkTime = ZonedDateTime.now(ZoneId.of("America/New_York"));

        System.out.println("\nLocal zoned: " + zonedNow);
        System.out.println("Tokyo: " + tokyoTime);
        System.out.println("New York: " + newYorkTime);

        // --- 5. Instant — machine-readable timestamp (epoch seconds) ---
        Instant instant = Instant.now();
        System.out.println("\nInstant: " + instant);
        System.out.println("Epoch seconds: " + instant.getEpochSecond());

        // --- 6. Duration — time-based amount (hours, minutes, seconds) ---
        Duration twoHours = Duration.ofHours(2);
        Duration between = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
        System.out.println("\nTwo hours: " + twoHours);
        System.out.println("Workday duration: " + between);
        System.out.println("Workday in minutes: " + between.toMinutes());

        // --- 7. Period — date-based amount (years, months, days) ---
        Period sixMonths = Period.ofMonths(6);
        Period age = Period.between(birthday, today);
        System.out.println("\nSix months: " + sixMonths);
        System.out.println("Age: " + age.getYears() + " years, " + age.getMonths() + " months");

        // --- 8. ChronoUnit — for calculating differences ---
        long daysBetween = ChronoUnit.DAYS.between(birthday, today);
        System.out.println("\nDays since birthday: " + daysBetween);

        // --- 9. DateTimeFormatter — formatting and parsing ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String formatted = dateTime.format(formatter);
        System.out.println("\nFormatted: " + formatted);

        // Parsing with formatter
        DateTimeFormatter dateOnly = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate parsedDate = LocalDate.parse("06/15/1990", dateOnly);
        System.out.println("Parsed: " + parsedDate);

        // ISO formatters (built-in)
        System.out.println("ISO format: " + today.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}
