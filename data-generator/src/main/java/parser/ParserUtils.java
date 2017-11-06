package parser;

import model.JsonDateDay;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ParserUtils {

    private static final String VALUE_NOT_DEFINED_SYMBOL = "\\N";

    static Long parseTimeStamp(String rawYear, String rawMonth, String rawDay) {
        int year = (VALUE_NOT_DEFINED_SYMBOL.equals(rawYear) || rawYear.isEmpty()) ? 1 : Integer.parseInt(rawYear);
        int month = (VALUE_NOT_DEFINED_SYMBOL.equals(rawMonth) || rawMonth.isEmpty()) ? 1 : Integer.parseInt(rawMonth);
        int day = (VALUE_NOT_DEFINED_SYMBOL.equals(rawDay) || rawDay.isEmpty()) ? 1 : Integer.parseInt(rawDay);

        if (year == 1) {
            return null;
        } else {
            LocalDateTime dateTime = LocalDateTime.of(year, month, day, 0, 0);
            return dateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
        }
    }

    static JsonDateDay parseDateDay(String rawYear, String rawMonth, String rawDay) {

        String year = (VALUE_NOT_DEFINED_SYMBOL.equals(rawYear) || rawYear.isEmpty()) ? "0" : rawYear;
        String month = (VALUE_NOT_DEFINED_SYMBOL.equals(rawMonth) || rawMonth.isEmpty()) ? "1" : rawMonth;
        String day = (VALUE_NOT_DEFINED_SYMBOL.equals(rawDay) || rawDay.isEmpty()) ? "1" : rawDay;
        String dateDayString = String.format("%s-%s-%s", year, month, day);

        return new JsonDateDay(dateDayString);
    }
}
