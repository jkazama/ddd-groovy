package sample.util

import groovy.transform.Canonical
import groovy.transform.CompileStatic;

/**
 * 日付と日時のペアを表現します。
 * <p>0:00に営業日切り替えが行われないケースなどでの利用を想定しています。
 *
 * @author jkazama
 */
@CompileStatic
@Canonical
class TimePoint implements Serializable {
	private static final long serialVersionUID = 1L
	
	/** 日付(営業日) */
	String day
	/** 日付におけるシステム日時 */
	Date date

	/** 指定日付と同じか。(day == targetDay) */
	boolean equalsDay(String targetDay) {
		day == targetDay
	}

	/** 指定日付よりも前か。(day < targetDay) */
	boolean beforeDay(String targetDay) {
		DateUtils.date(day).before(DateUtils.date(targetDay))
	}

	/** 指定日付以前か。(day <= targetDay) */
	boolean beforeEqualsDay(String targetDay) {
		equalsDay(targetDay) || beforeDay(targetDay);
	}

	/** 指定日付よりも後か。(targetDay < day) */
	boolean afterDay(String targetDay) {
		DateUtils.date(day).after(DateUtils.date(targetDay));
	}

	/** 指定日付以降か。(targetDay <= day) */
	boolean afterEqualsDay(String targetDay) {
		equalsDay(targetDay) || afterDay(targetDay);
	}

	/** 日付を元にTimePointを生成します。 */
	static TimePoint by(String day) {
		new TimePoint(day, DateUtils.date(day));
	}
}
