package sample.model.asset

import sample.UnitSpecSupport;
import spock.lang.Unroll;

// CashBalanceTestのSpock版
class CashBalanceSpec extends UnitSpecSupport {

	@Unroll
	def "残高反映検証: 10.02 + #v = #result [#cmt]"() {
		setup:
		def cb = fixtures.cb("test1", "20141118", "USD", "10.02").save(rep)
		
		expect:
		// 10.02 + 11.51 = 21.53
		cb.add(rep, v.toBigDecimal()).amount == result.toBigDecimal()
		
		where:
		cmt 					|v			|| result
		"通常"					| "11.51"	|| "21.53"
		"端数切捨確認"				| "11.516"	|| "21.53"
		"マイナス値/マイナス残許容"	| "-10.03" 	|| "-0.01"
	}

}
