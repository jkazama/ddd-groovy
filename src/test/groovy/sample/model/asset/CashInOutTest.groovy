package sample.model.asset

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.junit.*

import sample.*
import sample.util.DateUtils

//low: 簡易な正常系検証が中心。依存するCashflow/CashBalanceの単体検証パスを前提。
class CashInOutTest  extends UnitTestSupport {

	String ccy = "JPY";
	String accId = "test";
	String baseDay = "20141118";

	@Before
	void before() {
		// 残高1000円の口座(test)を用意
		fixtures.acc(accId).save(rep);
		fixtures.fiAcc(accId, Remarks.CashOut, ccy).save(rep)
		fixtures.cb(accId, baseDay, ccy, "1000").save(rep)
	}
	
	@Test
	void find() {
		def cio = fixtures.cio(accId, "300", true)
		cio.updateDate = DateUtils.date("20141118")
		cio.save(rep);
		//low: ちゃんとやると大変なので最低限の検証
		assertThat(
			CashInOut.find(rep, findParam("20141118", "20141119")),
			hasSize(1))
		assertThat(
			CashInOut.find(rep, findParam("20141118", "20141119", ActionStatusType.UNPROCESSED)),
			hasSize(1))
		assertThat(
			CashInOut.find(rep, findParam("20141118", "20141119", ActionStatusType.PROCESSED)),
			empty())
		assertThat(
			CashInOut.find(rep, findParam("20141119", "20141120", ActionStatusType.UNPROCESSED)),
			empty())
	}
	
	private FindCashInOut findParam(String fromDay, String toDay, ActionStatusType... statusTypes) {
		return new FindCashInOut(ccy, statusTypes, fromDay, toDay);
	}

	@Test
	void withdrawal() {
		// 超過の出金依頼 [例外]
		try {
			CashInOut.withdraw(rep, new RegCashOut(accId, ccy, "1001".toBigDecimal()))
			fail()
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.CashInOut.withdrawAmount"))
		}

		// 0円出金の出金依頼 [例外]
		try {
			CashInOut.withdraw(rep, new RegCashOut(accId, ccy, BigDecimal.ZERO))
			fail()
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.domain.AbsAmount.zero"))
		}

		// 通常の出金依頼
		def normal = CashInOut.withdraw(rep, new RegCashOut(accId, ccy, "300".toBigDecimal()))
		assertThat(normal, allOf(
			hasProperty("accountId", is(accId)), hasProperty("currency", is(ccy)),
			hasProperty("absAmount", is(new BigDecimal(300))), hasProperty("withdrawal", is(true)),
			hasProperty("requestDate", hasProperty("day", is(baseDay))),
			hasProperty("eventDay", is(baseDay)), hasProperty("valueDay", is("20141121")),
			hasProperty("targetFiCode", is(Remarks.CashOut + "-$ccy")),
			hasProperty("targetFiAccountId", is("FI" + accId)),
			hasProperty("selfFiCode", is(Remarks.CashOut + "-$ccy")),
			hasProperty("selfFiAccountId", is("xxxxxx")),
			hasProperty("statusType", is(ActionStatusType.UNPROCESSED)),
			hasProperty("cashflowId", is(nullValue()))))

		// 拘束額を考慮した出金依頼 [例外]
		try {
			CashInOut.withdraw(rep, new RegCashOut(accId, ccy, "701".toBigDecimal()))
			fail()
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.CashInOut.withdrawAmount"))
		}
	}

	@Test
	void cancel() {
		// CF未発生の依頼を取消
		def normal = fixtures.cio(accId, "300", true).save(rep)
		assertThat(normal.cancel(rep), hasProperty("statusType", is(ActionStatusType.CANCELLED)))
		
		// 発生日を迎えた場合は取消できない [例外]
		def today = fixtures.cio(accId, "300", true)
		today.setEventDay("20141118")
		today.save(rep)
		try {
			today.cancel(rep)
			fail()
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.CashInOut.beforeEqualsDay"))
		}
	}

	@Test
	void error() {
		def normal = fixtures.cio(accId, "300", true).save(rep)
		assertThat(normal.error(rep), hasProperty("statusType", is(ActionStatusType.ERROR)))
		
		// 処理済の時はエラーにできない [例外]
		def today = fixtures.cio(accId, "300", true)
		today.setEventDay("20141118")
		today.setStatusType(ActionStatusType.PROCESSED)
		today.save(rep)
		try {
			today.error(rep)
			fail()
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.ActionStatusType.unprocessing"))
		}
	}
	
	@Test
	void process() {
		// 発生日未到来の処理 [例外]
		def future = fixtures.cio(accId, "300", true).save(rep)
		try {
			future.process(rep)
			fail()
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.CashInOut.afterEqualsDay"))
		}

		// 発生日到来処理
		def normal = fixtures.cio(accId, "300", true)
		normal.setEventDay("20141118")
		normal.save(rep)
		assertThat(normal.process(rep), allOf(
			hasProperty("statusType", is(ActionStatusType.PROCESSED)),
			hasProperty("cashflowId", not(nullValue()))))
		// 発生させたキャッシュフローの検証
		assertThat(Cashflow.load(rep, normal.getCashflowId()), allOf(
			hasProperty("accountId", is(accId)),
			hasProperty("currency", is(ccy)),
			hasProperty("amount", is(new BigDecimal("-300"))),
			hasProperty("cashflowType", is(CashflowType.CashOut)),
			hasProperty("remark", is(Remarks.CashOut)),
			hasProperty("eventDate", hasProperty("day", is("20141118"))),
			hasProperty("valueDay", is("20141121")),
			hasProperty("statusType", is(ActionStatusType.UNPROCESSED))))
	}
	
}
