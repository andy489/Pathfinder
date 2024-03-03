package com.pathfinder.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {

    public static Integer calcYearsBetween(Date from, LocalDate to){
        return Period.between(convertToLocalDate(from), to).getYears();
    }

    private static LocalDate convertToLocalDate(Date dateToConvert) {
        return LocalDate.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault());
    }

}
