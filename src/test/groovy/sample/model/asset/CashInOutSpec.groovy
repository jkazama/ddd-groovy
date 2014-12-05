package sample.model.asset

import sample.ActionStatusType
import sample.UnitSpecSupport
import sample.ValidationException
import sample.util.DateUtils

class CashInOutSpec extends UnitSpecSupport {

	String ccy = "JPY"
	String accId = "test"
	String baseDay = "20141118"

	def setup() {
		// 残高1000円の口座(test)を用意
		fixtures.acc(accId).save(rep);
		fixtures.fiAcc(accId, Remarks.CashOut, ccy).save(rep)
		fixtures.cb(accId, baseDay, ccy, "1000").save(rep)
	}

	def "振込入出金検索の結果件数確認"() {
		setup:
		def cio = fixtures.cio(accId, "300", true)
		cio.updateDate = DateUtils.date("20141118")
		cio.save(rep)

		expect:
		CashInOut.find(rep, findParam(from, to, statusTypes)).size() == result

		//low: ちゃんとやると大変なので最低限の検証
		where:
		from		| to		| statusTypes						|| result
		"20141118"	|"20141119"	| ActionStatusType.values() as List	|| 1
		"20141118"	|"20141119"	| [ActionStatusType.UNPROCESSED]	|| 1
		"20141118"	|"20141119"	| [ActionStatusType.PROCESSED]		|| 0
		"20141119"	|"20141120"	| [ActionStatusType.UNPROCESSED]	|| 0
	}

	private FindCashInOut findParam(String fromDay, String toDay, List statusTypes) {
		new FindCashInOut(ccy, statusTypes as ActionStatusType[], fromDay, toDay)
	}

	def "超過の出金依頼 [例外]"() {
		when:
		CashInOut.withdraw(rep, new RegCashOut(accId, ccy, "1001".toBigDecimal()))

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.CashInOut.withdrawAmount"
	}

	def "0円出金の出金依頼 [例外]"() {
		when:
		CashInOut.withdraw(rep, new RegCashOut(accId, ccy, BigDecimal.ZERO))

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.domain.AbsAmount.zero"
	}

	def "通常の出金依頼"() {
		when:
		def normal = CashInOut.withdraw(rep, new RegCashOut(accId, ccy, "300".toBigDecimal()))

		then:
		normal.accountId == accId
		normal.currency == ccy
		normal.absAmount == "300".toBigDecimal()
		normal.withdrawal == true
		normal.requestDate.day == baseDay
		normal.eventDay == baseDay
		normal.valueDay == "20141121"
		normal.targetFiCode == Remarks.CashOut + "-$ccy"
		normal.targetFiAccountId == "FI" + accId
		normal.selfFiCode == Remarks.CashOut + "-$ccy"
		normal.selfFiAccountId == "xxxxxx"
		normal.statusType == ActionStatusType.UNPROCESSED
		normal.cashflowId == null
	}

	def "拘束額を考慮した出金依頼 [例外]"() {
		setup:
		CashInOut.withdraw(rep, new RegCashOut(accId, ccy, "300".toBigDecimal()))

		when:
		CashInOut.withdraw(rep, new RegCashOut(accId, ccy, "701".toBigDecimal()))

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.CashInOut.withdrawAmount"
	}

	def "キャッシュフロー未発生の依頼を取消"() {
		setup:
		def normal = fixtures.cio(accId, "300", true).save(rep)

		when:
		normal.cancel(rep)

		then:
		normal.statusType == ActionStatusType.CANCELLED
	}

	def "発生日を迎えた場合は取消できない [例外]"() {
		setup:
		def today = fixtures.cio(accId, "300", true)
		today.setEventDay("20141118")
		today.save(rep)

		when:
		today.cancel(rep)

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.CashInOut.beforeEqualsDay"
	}

	def "キャッシュフロー未発生の依頼をエラー状態に変更"() {
		setup:
		def normal = fixtures.cio(accId, "300", true).save(rep)

		when:
		normal.error(rep)

		then:
		normal.statusType == ActionStatusType.ERROR
	}

	def "処理済の時はエラーにできない [例外]"() {
		setup:
		def today = fixtures.cio(accId, "300", true)
		today.setEventDay("20141118")
		today.setStatusType(ActionStatusType.PROCESSED)
		today.save(rep)

		when:
		today.error(rep)

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.ActionStatusType.unprocessing"
	}

	def "発生日到来処理"() {
		setup:
		def normal = fixtures.cio(accId, "300", true)
		normal.setEventDay("20141118")
		normal.save(rep)

		when:
		normal.process(rep)
		def cf = Cashflow.load(rep, normal.getCashflowId())

		then:
		normal.statusType == ActionStatusType.PROCESSED
		normal.cashflowId != null
		cf.accountId == accId
		cf.currency == ccy
		cf.amount == "-300".toBigDecimal()
		cf.cashflowType == CashflowType.CashOut
		cf.remark == Remarks.CashOut
		cf.eventDate.day == "20141118"
		cf.valueDay == "20141121"
		cf.statusType == ActionStatusType.UNPROCESSED
	}

	def "発生日未到来の処理 [例外]"() {
		setup:
		def future = fixtures.cio(accId, "300", true).save(rep)

		when:
		future.process(rep)

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.CashInOut.afterEqualsDay"
	}
}
