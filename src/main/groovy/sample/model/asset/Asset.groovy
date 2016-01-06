package sample.model.asset

import sample.context.orm.JpaRepository
import sample.util.Calculator

/**
 * 口座の資産概念を表現します。
 * asset配下のEntityを横断的に取り扱います。
 * low: 実際の開発では多通貨や執行中/拘束中のキャッシュフローアクションに対する考慮で、サービスによってはかなり複雑になります。
 *
 * @author jkazama
 */
class Asset {
    /** 口座ID */
    final String id
    private Asset(String id) {
        this.id = id
    }
    
    /** 口座IDに紐付く資産概念を返します。 */
    static Asset by(String accountId) {
        new Asset(accountId)
    }
    
    /**
     * 振込出金可能か判定します。
     * <p>0 <= 口座残高 + 未実現キャッシュフロー - (出金依頼拘束額 + 出金依頼額)
     * low: 判定のみなのでscale指定は省略。余力金額を返す時はきちんと指定する
     */
    boolean canWithdraw(final JpaRepository rep, String currency, BigDecimal absAmount, String valueDay) {
        def calc = Calculator.init(CashBalance.getOrNew(rep, id, currency).amount)
        Cashflow.findUnrealize(rep, id, currency, valueDay).each { calc.add(it.amount) }
        CashInOut.findUnprocessed(rep, id, currency, true).each { calc.add(it.absAmount.negate()) }
        calc.add(absAmount.negate())
        0 <= calc.decimal().signum()
    }

}
