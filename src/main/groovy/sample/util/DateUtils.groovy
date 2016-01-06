package sample.util

import groovy.transform.CompileStatic

import java.text.*

// low: サンプル用のサポートUtils。実際のプロジェクトではcommons-lang等のライブラリを利用してください。
@CompileStatic
abstract class DateUtils {
    static final String fmtDay = "yyyyMMdd"

    static Date date(String day) {
        new SimpleDateFormat(fmtDay).parse(day)
    }
    
    static Date dateTo(String day) {
        Calendar cal = date(day).toCalendar()
        cal.add(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        cal.getTime()
    }
    
    static String dayFormat(Date date) {
        date.format(fmtDay)
    }

    
}
