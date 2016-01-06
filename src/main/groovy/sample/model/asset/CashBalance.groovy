package sample.model.asset

import java.math.RoundingMode

import javax.persistence.*
import javax.validation.constraints.NotNull

import sample.context.orm.*
import sample.model.constraints.*
import sample.util.Calculator

/**
 * 口座残高を表現します。
 *
 * @author jkazama
 */
@JpaStaticEntity
@NamedQueries([
    @NamedQuery(name = "CashBalance.findAcc", query = "from CashBalance c where c.accountId=?1 and c.currency=?2 order by c.baseDay desc"),
    @NamedQuery(name = "CashBalance.findAccWithDay", query = "from CashBalance c where c.accountId=?1 and c.currency=?2 and c.baseDay=?3 order by c.baseDay desc") ])
class CashBalance extends JpaActiveRecord<CashBalance> {
    private static final long serialVersionUID = 1L

    /** ID */
    @Id
    @GeneratedValue
    Long id
    /** 口座ID */
    @AccountId
    String accountId
    /** 基準日 */
    @Day
    String baseDay
    /** 通貨 */
    @Currency
    String currency
    /** 金額 */
    @Amount
    BigDecimal amount
    /** 更新日 */
    @NotNull
    Date updateDate

    /**
     * 残高へ指定した金額を反映します。
     * low ここではCurrencyを使っていますが、実際の通貨桁数や端数処理定義はDBや設定ファイル等で管理されます。
     */
    CashBalance add(final JpaRepository rep, BigDecimal addAmount) {
        def scale = java.util.Currency.getInstance(currency).getDefaultFractionDigits()
        def mode = RoundingMode.DOWN
        this.amount = Calculator.init(amount).scale(scale, mode).add(addAmount).decimal()
        update(rep)
    }

    /**
     * 指定口座の残高を取得します。(存在しない時は繰越保存後に取得します)
     * low: 複数通貨の適切な考慮や細かい審査は本筋でないので割愛。
     */
    static CashBalance getOrNew(final JpaRepository rep, String accountId, String currency) {
        List<CashBalance> list = rep.tmpl().find("CashBalance.findAccWithDay", accountId, currency, rep.dh().time.day)
        list.isEmpty() ? create(rep, accountId, currency) : list.head()
    }

    private static CashBalance create(final JpaRepository rep, String accountId, String currency) {
        def now = rep.dh().time.tp()
        List<CashBalance> list = rep.tmpl().find("CashBalance.findAcc", accountId, currency)
        BigDecimal amount = list.isEmpty() ? BigDecimal.ZERO : list.head().amount
        new CashBalance(null, accountId, now.day, currency, amount, now.date).save(rep)
    }

}
