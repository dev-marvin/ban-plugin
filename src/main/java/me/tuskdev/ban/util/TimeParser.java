package me.tuskdev.ban.util;

import java.util.concurrent.TimeUnit;

public class TimeParser {

    public static Long convert(String string) {
        long time = 0;
        String[] split = string.split("-");

        for (String target : split) {
            char charAt = target.charAt(target.length() - 1);

            int timeInt;
            try {
                timeInt = Integer.parseInt(target.replace(Character.toString(charAt), ""));
            } catch (NumberFormatException exception) {
                return (long) -1;
            }

            for (TimeMultiplier timeMultiplier : TimeMultiplier.values()) {
                if (timeMultiplier.getDiminutive() != charAt) continue;

                time += timeMultiplier.getMultiplier() * timeInt;
            }
        }

        return time;
    }

    public static String format(long time) {
        if (time == 0) return "0s";

        long years = TimeUnit.MILLISECONDS.toDays(time) / 365;
        long months = TimeUnit.MILLISECONDS.toDays(time) / 30 - (years * 12);
        long days = TimeUnit.MILLISECONDS.toDays(time) - (months * 30);
        long hours = TimeUnit.MILLISECONDS.toHours(time) - (days * 24);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - (TimeUnit.MILLISECONDS.toHours(time) * 60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - (TimeUnit.MILLISECONDS.toMinutes(time) * 60);

        StringBuilder stringBuilder = new StringBuilder();

        if (years > 0) stringBuilder.append(years).append("y");
        if (months > 0) stringBuilder.append(years > 0 ? " " : "").append(months).append("m");
        if (days > 0) stringBuilder.append(years > 0 || months > 0 ? " " : "").append(days).append("d");
        if (hours > 0) stringBuilder.append(years > 0 || months > 0 || days > 0 ? " " : "").append(hours).append("h");
        if (minutes > 0) stringBuilder.append(years > 0 || months > 0 || days > 0 || hours > 0 ? " " : "").append(minutes).append("m");
        if (seconds > 0) stringBuilder.append(years > 0 || months > 0 || days > 0 || hours > 0 || minutes > 0 ? " " : (stringBuilder.length() > 0 ? " " : "")).append(seconds).append("s");

        return stringBuilder.toString();
    }

    private enum TimeMultiplier {

        SECONDS(1000L, 's'),
        MINUTES(60000L, 'm'),
        HOURS(3600000L, 'h'),
        DAYS(86400000L, 'd'),
        WEEKS(604800000L, 'w'),
        MONTHS(2592000000L, 'M'),
        YEARS(31536000000L, 'y');

        private final long multiplier;
        private final char diminutive;

        TimeMultiplier(long multiplier, char diminutive) {
            this.multiplier = multiplier;
            this.diminutive = diminutive;
        }

        public long getMultiplier() {
            return multiplier;
        }

        public char getDiminutive() {
            return diminutive;
        }

    }
}