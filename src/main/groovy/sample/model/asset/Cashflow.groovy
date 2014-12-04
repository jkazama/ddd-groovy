package sample.model.asset

import javax.persistence.*
import javax.validation.constraints.NotNull

import sample.*
import sample.context.Dto
import sample.context.StaticDto;
import sample.context.orm.*
import sample.model.constraints.*
import sample.util.*

/**
 * 入出金キャッシュフローを表現します。
 * キャッシュフローは振込/振替といったキャッシュフローアクションから生成される確定状態(依頼取消等の無い)の入出金情報です。
 * low: 概念を伝えるだけなので必要最低限の項目で表現しています。
 * low: 検索関連は主に経理確認や帳票等での利用を想定します
 *
 * @author jkazama
 */
@JpaStaticEntity
@NamedQueries([
	@NamedQuery(name = "Cashflow.findDoRealize", query = "from Cashflow c where c.valueDay=?1 and c.statusType in ?2 order by c.id"),
	@NamedQuery(name = "Cashflow.findUnrealize", query = "from Cashflow c where c.valueDay<=?1 and c.statusType in ?2 order by c.id")])
class Cashflow  extends JpaActiveRecord<Cashflow> {
	private static final long serialVersionUID = 1L

	/** ID */
	@Id
	@GeneratedValue
	Long id
	/** 口座ID */
	@AccountId
	String accountId
	/** 通貨 */
	@Currency
	String currency
	/** 金額 */
	@Amount
	BigDecimal amount
	/** 入出金 */
	@Enumerated(EnumType.STRING)
	@NotNull
	CashflowType cashflowType
	/** 摘要 */
	@Category
	String remark
	/** 発生日/日時 */
	@Embedded
	@AttributeOverrides([
		@AttributeOverride(name = "day", column = @Column(name = "event_day")),
		@AttributeOverride(name = "date", column = @Column(name = "event_date")) ])
	@NotNull
	TimePoint eventDate
	/** 受渡日 */
	@Day
	String valueDay
	/** 処理種別 */
	@Enumerated(EnumType.STRING)
	@NotNull
	ActionStatusType statusType
	/** 更新者 */
	@AccountId
	String updateActor
	/** 更新日 */
	@NotNull
	Date updateDate

	/**
	 * キャッシュフローを処理済みにして残高へ反映します。
	 */
	Cashflow realize(final JpaRepository rep) {
		def now = rep.dh().time.tp()
		def v = validator()
		v.verify(canRealize(rep), "error.Cashflow.realizeDay")
		v.verify(statusType.isUnprocessing(), "error.ActionStatusType.unprocessing")

		this.statusType = ActionStatusType.PROCESSED
		this.updateActor = rep.dh().actor().id
		this.updateDate = now.getDate()
		update(rep)
		CashBalance.getOrNew(rep, accountId, currency).add(rep, amount)
		this
	}
	
	/**
	 * キャッシュフローをエラー状態にします。
	 * <p>処理中に失敗した際に呼び出してください。
	 * low: 実際はエラー事由などを引数に取って保持する
	 */
	Cashflow error(final JpaRepository rep) {
		validator().verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing")

		this.statusType = ActionStatusType.ERROR
		this.updateActor = rep.dh().actor().id
		this.updateDate = rep.dh().time.date()
		update(rep)
	}

	/**
	 * キャッシュフローを実現(受渡)可能か判定します。
	 */
	boolean canRealize(final JpaRepository rep) {
		rep.dh().time.tp().afterEqualsDay(valueDay)
	}

	static Cashflow load(final JpaRepository rep, Long id) {
		rep.load(Cashflow, id)
	}
	
	/**
	 * 指定受渡日時点で未実現のキャッシュフロー一覧を検索します。
	 */
	static List<Cashflow> findUnrealize(final JpaRepository rep, String valueDay) {
		rep.tmpl().find("Cashflow.findUnrealize", valueDay, ActionStatusType.unprocessedTypes)
	}

	/**
	 * 指定受渡日で実現対象となるキャッシュフロー一覧を検索します。
	 */
	static List<Cashflow> findDoRealize(final JpaRepository rep, String valueDay) {
		rep.tmpl().find("Cashflow.findDoRealize", valueDay, ActionStatusType.unprocessedTypes)
	}
	
	/**
	 * キャッシュフローを登録します。
	 * 受渡日を迎えていた時はそのまま残高へ反映します。
	 */
	static Cashflow register(final JpaRepository rep, final RegCashflow p) {
		def now = rep.dh().time.tp();
		def v = new Validator()
		v.checkField(now.beforeEqualsDay(p.getValueDay()), "valueDay", "error.Cashflow.beforeEqualsDay")
		v.verify()
		def cf = p.create(now, rep.dh().actor().id).save(rep)
		cf.canRealize(rep) ? cf.realize(rep) : cf
	}
	
}

/** キャッシュフロー種別。 low: 各社固有です。摘要含めラベルはなるべくmessages.propertiesへ切り出し */
enum CashflowType {
	/** 振込入金 */
	CashIn,
	/** 振込出金 */
	CashOut,
	/** 振替入金 */
	CashTransferIn,
	/** 振替出金 */
	CashTransferOut
}

/** 入出金キャッシュフローの登録パラメタ。  */
@StaticDto
class RegCashflow implements Dto {
	private static final long serialVersionUID = 1L
	@AccountId
	String accountId
	@Currency
	String currency
	@Amount
	BigDecimal amount
	@NotNull
	CashflowType cashflowType
	@Category
	String remark
	/** 未設定時は営業日を設定 */
	@DayEmpty
	String eventDay
	@Day
	String valueDay

	Cashflow create(final TimePoint now, String updActor) {
		def eventDate = eventDay == null ? now : new TimePoint(day: eventDay, date: now.date)
		new Cashflow(id: (Long)null, accountId: accountId, currency: currency, amount: amount, cashflowType: cashflowType,
			remark: remark, eventDate: eventDate, valueDay: valueDay, statusType: ActionStatusType.UNPROCESSED,
			updateActor: updActor, updateDate: now.date)
	}
}
