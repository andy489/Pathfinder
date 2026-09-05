package com.pathfinder.util;

import java.time.LocalDate;
import java.time.Period;

public class DateUtil {

    public static Integer calcYearsBetween(LocalDate from, LocalDate to) {
        return Period.between(from, to).getYears();
    }
}
