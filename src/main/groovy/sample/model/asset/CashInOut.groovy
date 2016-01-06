package sample.model.asset

import javax.persistence.*
import javax.validation.constraints.NotNull

import sample.ActionStatusType
import sample.context.*
import sample.context.orm.*
import sample.model.account.*
import sample.model.constraints.*
import sample.model.master.*
import sample.util.*

/**
 * 振込入出金依頼を表現するキャッシュフローアクション。
 * <p>相手方/自社方の金融機関情報は依頼後に変更される可能性があるため、依頼時点の状態を
 * 保持するために非正規化して情報を保持しています。
 * low: 相手方/自社方の金融機関情報は項目数が多いのでサンプル用に金融機関コードのみにしています。
 * 実際の開発ではそれぞれ複合クラス(FinantialInstitution)に束ねるアプローチを推奨します。
 *
 * @author jkazama
 */
@JpaStaticEntity
@NamedQueries([
    @NamedQuery(name = "CashInOut.findUnprocessed", query = "from CashInOut c where c.eventDay=?1 and c.statusType in ?2 order by c.id"),
    @NamedQuery(name = "CashInOut.findAccUnprocessed1", query = "from CashInOut c where c.accountId=?1 and c.currency=?2 and c.withdrawal=?3 and c.statusType in ?4 order by c.id"),
    @NamedQuery(name = "CashInOut.findAccUnprocessed2", query = "from CashInOut c where c.accountId=?1 and c.statusType in ?2 order by c.updateDate desc")])
class CashInOut  extends JpaActiveRecord<CashInOut> {
    private static final long serialVersionUID = 1L;

    /** ID(振込依頼No) */
    @Id
    @IdStr
    String id
    /** 口座ID */
    @AccountId
    String accountId
    /** 通貨 */
    @Currency
    String currency
    /** 金額(絶対値) */
    @AbsAmount
    BigDecimal absAmount
    /** 出金時はtrue */
    boolean withdrawal
    /** 依頼日/日時 */
    @NotNull
    @Embedded
    @AttributeOverrides([
        @AttributeOverride(name = "day", column = @Column(name = "request_day")),
        @AttributeOverride(name = "date", column = @Column(name = "request_date")) ])
    TimePoint requestDate
    /** 発生日 */
    @Day
    String eventDay
    /** 受渡日 */
    @Day
    String valueDay
    /** 相手方金融機関コード */
    @IdStr
    String targetFiCode
    /** 相手方金融機関口座ID */
    @AccountId
    String targetFiAccountId
    /** 自社方金融機関コード */
    @IdStr
    String selfFiCode
    /** 自社方金融機関口座ID */
    @AccountId
    String selfFiAccountId
    /** 処理種別 */
    @NotNull
    @Enumerated(EnumType.STRING)
    ActionStatusType statusType
    /** 更新者 */
    @AccountId
    String updateActor
    /** 更新日 */
    @NotNull
    Date updateDate
    /** キャッシュフローID。処理済のケースでのみ設定されます。low: 実際は調整CFや消込CFの概念なども有 */
    Long cashflowId

    /**
     * 依頼を処理します。
     * <p>依頼情報を処理済にしてキャッシュフローを生成します。
     */
    CashInOut process(final JpaRepository rep) {
        //low: 出金営業日の取得。ここでは単純な営業日を取得
        def now = rep.dh().time.tp()
        // 事前審査
        def v = validator()
        v.verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing")
        v.verify(now.afterEqualsDay(eventDay), "error.CashInOut.afterEqualsDay")
        // 処理済状態を反映
        this.statusType = ActionStatusType.PROCESSED
        this.updateActor = rep.dh().actor().id
        this.updateDate = now.date
        this.cashflowId = Cashflow.register(rep, regCf()).id
        update(rep)
    }
    
    private RegCashflow regCf() {
        def amount = withdrawal ? absAmount.negate() : absAmount
        def cashflowType = withdrawal ? CashflowType.CashOut : CashflowType.CashIn
        // low: 摘要はとりあえずシンプルに。実際はCashInOutへ用途フィールドをもたせた方が良い(生成元メソッドに応じて摘要を変える)
        def remark = withdrawal ? Remarks.CashOut : Remarks.CashIn
        new RegCashflow(accountId: accountId, currency: currency, amount: amount, cashflowType: cashflowType,
            remark: remark, eventDay: eventDay, valueDay: valueDay)
    }

    /**
     * 依頼を取消します。
     * <p>"処理済みでない"かつ"発生日を迎えていない"必要があります。
     */
    CashInOut cancel(final JpaRepository rep) {
        def now = rep.dh().time.tp()
        // 事前審査
        def v = validator()
        v.verify(statusType.isUnprocessing(), "error.ActionStatusType.unprocessing")
        v.verify(now.beforeDay(eventDay), "error.CashInOut.beforeEqualsDay")
        // 取消状態を反映
        this.statusType = ActionStatusType.CANCELLED
        this.updateActor = rep.dh().actor().id
        this.updateDate = now.date
        update(rep)
    }

    /**
     * 依頼をエラー状態にします。
     * <p>処理中に失敗した際に呼び出してください。
     * low: 実際はエラー事由などを引数に取って保持する
     */
    CashInOut error(final JpaRepository rep) {
        validator().verify(statusType.isUnprocessed(), "error.ActionStatusType.unprocessing")

        this.statusType = ActionStatusType.ERROR
        this.updateActor = rep.dh().actor().id
        this.updateDate = rep.dh().time.date()
        update(rep)
    }

    /** 振込入出金依頼を返します。 */
    static CashInOut load(final JpaRepository rep, String id) {
        rep.load(CashInOut, id)
    }

    /** 未処理の振込入出金依頼一覧を検索します。  low: criteriaベース実装例 */
    static List<CashInOut> find(final JpaRepository rep, final FindCashInOut p) {
        // low: 通常であれば事前にfrom/toの期間チェックを入れる
        def criteria = rep.criteria(CashInOut.class)
        criteria.equal("currency", p.currency)
        criteria.inValues("statusType", p.statusTypes)
        criteria.between("updateDate", DateUtils.date(p.updFromDay), DateUtils.dateTo(p.updToDay))
        rep.tmpl().find(criteria.sortDesc("updateDate").result())
    }

    /** 当日発生で未処理の振込入出金一覧を検索します。 */
    static List<CashInOut> findUnprocessed(final JpaRepository rep) {
        rep.tmpl().find("CashInOut.findUnprocessed", rep.dh().time.day, ActionStatusType.unprocessedTypes)
    }

    /** 未処理の振込入出金一覧を検索します。(口座別) */
    static List<CashInOut> findUnprocessed(final JpaRepository rep, String accountId, String currency,
            boolean withdrawal) {
        rep.tmpl().find("CashInOut.findAccUnprocessed1", accountId, currency, withdrawal, ActionStatusType.unprocessedTypes)
    }
    
    /** 未処理の振込入出金一覧を検索します。(口座別) */
    static List<CashInOut> findUnprocessed(final JpaRepository rep, String accountId) {
        rep.tmpl().find("CashInOut.findAccUnprocessed2", accountId, ActionStatusType.unprocessedTypes)
    }

    /** 出金依頼をします。 */
    static CashInOut withdraw(final JpaRepository rep, final RegCashOut p) {
        def dh = rep.dh()
        def now = dh.time.tp()
        // low: 発生日は締め時刻等の兼ね合いで営業日と異なるケースが多いため、別途DB管理される事が多い
        def eventDay = now.day
        // low: 実際は各金融機関/通貨の休日を考慮しての T+N 算出が必要
        def valueDay = dh.time.dayPlus(3)
        
        // 事前審査
        def v = new Validator()
        v.verifyField(0 < p.getAbsAmount().signum(), "absAmount", "error.domain.AbsAmount.zero")
        boolean canWithdraw = Asset.by(p.accountId).canWithdraw(rep, p.currency, p.absAmount, valueDay)
        v.verifyField(canWithdraw, "absAmount", "error.CashInOut.withdrawAmount")

        // 出金依頼情報を登録
        def uid = dh.uid.generate(CashInOut.class.getSimpleName())
        def acc = FiAccount.load(rep, p.accountId, Remarks.CashOut, p.currency)
        def selfAcc = SelfFiAccount.load(rep, Remarks.CashOut, p.currency)
        def updateActor = dh.actor().getId();
        p.create(now, uid, eventDay, valueDay, acc, selfAcc, updateActor).save(rep)
    }
}

/** 振込入出金依頼の検索パラメタ low: 通常は顧客視点/社内視点で利用条件が異なる */
@StaticDto
class FindCashInOut implements Dto {
    private static final long serialVersionUID = 1L
    @CurrencyEmpty
    String currency
    ActionStatusType[] statusTypes
    @Day
    String updFromDay
    @Day
    String updToDay
}

/** 振込出金の依頼パラメタ */
@StaticDto
class RegCashOut implements Dto {
    private static final long serialVersionUID = 1L
    @AccountId
    String accountId;
    @Currency
    String currency;
    @AbsAmount
    BigDecimal absAmount;

    CashInOut create(final TimePoint now, String id, String eventDay, String valueDay, final FiAccount acc,
            final SelfFiAccount selfAcc, String updActor) {
        new CashInOut(id: id, accountId: accountId, currency: currency, absAmount: absAmount,
            withdrawal: true, requestDate: now, eventDay: eventDay, valueDay: valueDay,
            targetFiCode: acc.fiCode, targetFiAccountId: acc.fiAccountId, selfFiCode: selfAcc.fiCode, selfFiAccountId: selfAcc.fiAccountId,
            statusType: ActionStatusType.UNPROCESSED, updateActor: updActor, updateDate: now.date, cashflowId: (Long)null)
    }
}
