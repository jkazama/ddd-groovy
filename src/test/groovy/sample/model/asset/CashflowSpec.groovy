package sample.model.asset

import sample.*

// CashflowTestのSpock版
class CashflowSpec extends UnitSpecSupport {

	def "過去日付の受渡でキャッシュフロー発生 [例外]"() {
		when:
		Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141117"))

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.Cashflow.beforeEqualsDay"
	}

	def "翌日受渡でキャッシュフロー発生"() {
		when:
		def cf = Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141119"))

		then:
		cf.amount == "1000".toBigDecimal()
		cf.statusType == ActionStatusType.UNPROCESSED
		cf.eventDate.day == "20141118"
		cf.valueDay == "20141119"
	}

	def "実現: 未到来の受渡日 [例外]"() {
		setup:
		CashBalance.getOrNew(rep, "test1", "JPY")
		def cfFuture = fixtures.cf("test1", "1000", "20141118", "20141119").save(rep)

		when:
		cfFuture.realize(rep);

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.Cashflow.realizeDay"
	}
	
	def "実現: キャッシュフローの残高反映検証 0 + 1000 = 1000"() {
		setup:
		CashBalance.getOrNew(rep, "test1", "JPY")
		def cfNormal = fixtures.cf("test1", "1000", "20141117", "20141118").save(rep)

		when:
		def cf = cfNormal.realize(rep)
		def cb = CashBalance.getOrNew(rep, "test1", "JPY")
		
		then:
		cf.statusType == ActionStatusType.PROCESSED
		cb.amount == "1000".toBigDecimal()
	}

	def "実現: 処理済キャッシュフローの再実現 [例外]"() {
		setup:
		CashBalance.getOrNew(rep, "test1", "JPY")
		def cfRealized = fixtures.cf("test1", "1000", "20141117", "20141118").save(rep).realize(rep)

		when:
		cfRealized.realize(rep);

		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.ActionStatusType.unprocessing"
	}
	
	def "実現: 過日キャッシュフローの残高反映検証 0 + 2000 = 2000"() {
		setup:
		CashBalance.getOrNew(rep, "test1", "JPY")
		def cfPast = fixtures.cf("test1", "2000", "20141116", "20141117").save(rep)

		when:
		def cf = cfPast.realize(rep)
		def cb = CashBalance.getOrNew(rep, "test1", "JPY")
		
		then:
		cf.statusType == ActionStatusType.PROCESSED
		cb.amount == "2000".toBigDecimal()
	}
	
	def "キャッシュフロー発生即実現"() {
		setup:
		CashBalance.getOrNew(rep, "test1", "JPY")
		
		when:
		Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141118"))
		def cb = CashBalance.getOrNew(rep, "test1", "JPY")
		
		then:
		cb.amount == "1000".toBigDecimal()
	}
	
}
