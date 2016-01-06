package sample.model.master

import javax.persistence.*

import sample.context.orm.*
import sample.model.constraints.*

/**
 * サービス事業者の決済金融機関を表現します。
 * low: サンプルなので支店や名称、名義といったなど本来必須な情報をかなり省略しています。(通常は全銀仕様を踏襲します)
 *
 * @author jkazama
 */
@JpaStaticEntity
@NamedQuery(name = "SelfFiAccount.load", query = "from SelfFiAccount a where a.category=?1 and a.currency=?2")
class SelfFiAccount  extends JpaActiveRecord<SelfFiAccount> {
    private static final long serialVersionUID = 1L

    /** ID */
    @Id
    @GeneratedValue
    Long id
    /** 利用用途カテゴリ */
    @Category
    String category
    /** 通貨 */
    @Currency
    String currency
    /** 金融機関コード */
    @IdStr
    String fiCode
    /** 金融機関口座ID */
    @AccountId
    String fiAccountId

    static SelfFiAccount load(final JpaRepository rep, String category, String currency) {
        List<SelfFiAccount> list = rep.tmpl().find("SelfFiAccount.load", category, currency)
        if (list.isEmpty()) throw new IllegalStateException("自社金融機関情報が登録されていません。[$category:$currency]");
        list.head()
    }

}
