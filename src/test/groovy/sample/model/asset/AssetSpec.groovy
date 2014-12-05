package sample.model.asset

import sample.UnitSpecSupport

// AssetTestのSpock版
class AssetSpec extends UnitSpecSupport {

	def "出金可能額の判定検証"() {
		setup:
		// 残高   +  未実現キャッシュフロー - 出金依頼拘束額 = 出金可能額
		// 10000 + (1000 - 2000) - 8000 = 1000
		fixtures.acc("test").save(rep)
		fixtures.cb("test", "20141118", "JPY", "10000").save(rep)
		fixtures.cf("test", "1000", "20141118", "20141120").save(rep)
		fixtures.cf("test", "-2000", "20141119", "20141121").save(rep)
		fixtures.cio("test", "8000", true).save(rep)
		
		expect:
		Asset.by(accId).canWithdraw(rep, ccy, amt.toBigDecimal(), day) == result
		
		where:
		accId	| ccy	| amt		| day		|| result
		"test"	| "JPY"	| "1000"	| "20141121"|| true
		"test"	| "JPY"	| "1001"	| "20141121"|| false
	}
	
}
