package sample.model.account

import javax.persistence.*

import sample.ValidationException
import sample.context.orm.*
import sample.model.constraints.*

/**
 * 口座に紐づく金融機関口座を表現します。
 * <p>口座を相手方とする入出金で利用します。
 * low: サンプルなので支店や名称、名義といった本来必須な情報をかなり省略しています。(通常は全銀仕様を踏襲します)
 *
 * @author jkazama
 */
@JpaStaticEntity
@NamedQuery(name = "FiAccount.load", query = "from FiAccount a where a.accountId=?1 and a.category=?2 and a.currency=?3")
class FiAccount extends JpaActiveRecord<FiAccount> {
	private static final long serialVersionUID = 1L;

	/** ID */
	@Id
	@GeneratedValue
	Long id
	/** 口座ID */
	@AccountId
	String accountId
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

	static FiAccount load(final JpaRepository rep, String accountId, String category, String currency) {
		List<FiAccount> list = rep.tmpl().find("FiAccount.load", accountId, category, currency)
		if (list.isEmpty()) throw new ValidationException("error.Entity.load");
		list.head()
	}
	
}
