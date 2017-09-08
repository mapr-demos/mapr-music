package parser;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ParserUtils {

    private static final String VALUE_NOT_DEFINED_SYMBOL = "\\N";

    static Long parseTimeStamp(String rawYear, String rawMonth, String rawDay) {
        int year = (VALUE_NOT_DEFINED_SYMBOL.equals(rawYear) || rawYear.isEmpty()) ? 1 : Integer.parseInt(rawYear);
        int month = (VALUE_NOT_DEFINED_SYMBOL.equals(rawMonth) || rawMonth.isEmpty()) ? 1 : Integer.parseInt(rawMonth);
        int day = (VALUE_NOT_DEFINED_SYMBOL.equals(rawDay) || rawDay.isEmpty()) ? 1 : Integer.parseInt(rawDay);

        if (year == 1 && month == 1 && day == 1) {
            return null;
        } else {
            LocalDateTime dateTime = LocalDateTime.of(year, month, day, 0, 0);
            return dateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
        }
    }
}
