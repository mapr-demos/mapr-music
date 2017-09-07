package parser;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ParserUtils {

    static Long parseTimeStamp(String rawYear, String rawMonth, String rawDay) {
        int year = rawYear.isEmpty() ? 1 : Integer.parseInt(rawYear);
        int month = rawMonth.isEmpty() ? 1 : Integer.parseInt(rawMonth);
        int day = rawDay.isEmpty() ? 1 : Integer.parseInt(rawDay);

        if(year == 1 && month == 1 && day == 1){
            return null;
        } else {
            LocalDateTime dateTime = LocalDateTime.of(year, month, day, 0, 0);
            return dateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
        }
    }
}
