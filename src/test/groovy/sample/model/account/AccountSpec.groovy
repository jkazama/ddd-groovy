package sample.model.account

import static org.junit.Assert.*
import sample.UnitSpecSupport
import sample.ValidationException

// AccountTestのSpock版
class AccountSpec extends UnitSpecSupport {

	def "通常口座が取得できる"() {
		setup:
		fixtures.acc("normal").save(rep)
		
		when:
		def acc = Account.loadActive(rep, "normal")
		
		then:
		acc.id == "normal"
		acc.statusType == AccountStatusType.NORMAL
	}

	def "退会時に例外が発生する"() {
		setup:
		def withdrawal = fixtures.acc("withdrawal")
		withdrawal.setStatusType(AccountStatusType.WITHDRAWAL)
		withdrawal.save(rep)
		
		when:
		Account.loadActive(rep, "withdrawal")
		
		then:
		def e = thrown(ValidationException)
		e.getMessage() == "error.Account.loadActive"
	}
}
