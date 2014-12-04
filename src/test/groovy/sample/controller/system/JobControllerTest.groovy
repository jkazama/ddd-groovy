package sample.controller.system

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.junit.Test

import sample.*
import sample.model.asset.*

//low: 簡易な正常系検証が中心。100万保有のsampleを前提としてしまっています。
class JobControllerTest extends WebTestSupport {

	@Override
	protected String prefix() {
		return "/system/job";
	}

	@Test
	void processDay() {
		String currentDay = time.day
		assertThat(currentDay, is("20141118"))
		performGet("/daily/processDay")
		assertThat(time.day, is("20141119"))
		performGet("/daily/processDay")
		assertThat(time.day, is("20141120"))
		time.daySet(currentDay)
	}

	@Test
	void closingCashOut() {
		// 当日発生の振込出金依頼を準備
		def co = fixtures.cio("sample", "3000", true)
		co.setEventDay(time.day)
		co.save(rep)
		assertThat(CashInOut.load(rep, co.getId()), hasProperty("statusType", is(ActionStatusType.UNPROCESSED)))
		// 実行検証
		performGet("/daily/closingCashOut");
		assertThat(CashInOut.load(rep, co.getId()), hasProperty("statusType", is(ActionStatusType.PROCESSED)))
	}

	@Test
	void realizeCashflow() {
		// 当日実現のキャッシュフローを準備
		def cf = fixtures.cf("sample", "3000", "20141117", "20141118").save(rep)
		assertThat(Cashflow.load(rep, cf.getId()), hasProperty("statusType", is(ActionStatusType.UNPROCESSED)))
		assertThat(CashBalance.getOrNew(rep, "sample", "JPY"),
				hasProperty("amount", is(new BigDecimal("1000000.0000"))))
		// 実行検証
		performGet("/daily/realizeCashflow")
		assertThat(Cashflow.load(rep, cf.getId()), hasProperty("statusType", is(ActionStatusType.PROCESSED)))
		rep.flushAndClear()
		assertThat(CashBalance.getOrNew(rep, "sample", "JPY"),
				hasProperty("amount", is(new BigDecimal("1003000.0000"))))
	}

}
