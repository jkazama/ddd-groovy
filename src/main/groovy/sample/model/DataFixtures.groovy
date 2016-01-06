package sample.model

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.*
import org.springframework.transaction.support.*

import sample.ActionStatusType
import sample.context.*
import sample.context.orm.JpaRepository
import sample.model.account.*
import sample.model.asset.*
import sample.model.master.*
import sample.util.*

/**
 * データ生成用のサポートコンポーネント。
 * <p>テストや開発時の簡易マスタデータ生成を目的としているため本番での利用は想定していません。
 * low: 実際の開発では開発/テスト環境のみ有効となるよう細かなプロファイル指定が必要となります。
 *
 * @author jkazama
 */
@Component
class DataFixtures {

    @Autowired
    private JpaRepository rep
    @Autowired
    private Timestamper time
    @Autowired
    private IdGenerator uid
    @Autowired
    private PlatformTransactionManager tx

    @PostConstruct
    void initialize() {
        new TransactionTemplate(tx).execute { status ->
            initializeInTx()
        }
    }

    void initializeInTx() {
        String ccy = "JPY"
        String baseDay = "20141118"
        time.daySet(baseDay);

        // 自社金融機関
        selfFiAcc(Remarks.CashOut, ccy).save(rep)

        // 口座: sample
        String idSample = "sample";
        acc(idSample).save(rep);
        fiAcc(idSample, Remarks.CashOut, ccy).save(rep)
        cb(idSample, baseDay, ccy, "1000000").save(rep)
    }
    
    // account
    
    /** 口座の簡易生成 */
    Account acc(String id) {
        new Account(id, id, "hoge@example.com", AccountStatusType.NORMAL)
    }
    
    /** 口座に紐付く金融機関口座の簡易生成 */
    FiAccount fiAcc(String accountId, String category, String currency) {
        new FiAccount(null, accountId, category, currency, "$category-$currency", "FI$accountId")
    }

    // asset

    /** 口座残高の簡易生成 */
    CashBalance cb(String accountId, String baseDay, String currency, String amount) {
        new CashBalance(null, accountId, baseDay, currency, new BigDecimal(amount), new Date())
    }
    
    /** キャッシュフローの簡易生成 */
    Cashflow cf(String accountId, String amount, String eventDay, String valueDay) {
        cfReg(accountId, amount, valueDay).create(TimePoint.by(eventDay), sample.context.Actor.Anonymous.id)
    }

    /** キャッシュフロー登録パラメタの簡易生成 */
    RegCashflow cfReg(String accountId, String amount, String valueDay) {
        new RegCashflow(accountId, "JPY", new BigDecimal(amount), CashflowType.CashIn, "cashIn", null, valueDay)
    }
    
    /** 振込入出金依頼の簡易生成。 [発生日(T+1)/受渡日(T+3)] */
    CashInOut cio(String accountId, String absAmount, boolean withdrawal) {
        new CashInOut(uid.generate(CashInOut.class.getSimpleName()), accountId, "JPY",
            new BigDecimal(absAmount), withdrawal, time.tp(), time.dayPlus(1), time.dayPlus(3), "tFiCode", "tFiAccId",
            "sFiCode", "sFiAccId", ActionStatusType.UNPROCESSED, "dummy", time.date(), null)
    }

    // master
    
    /** 自社金融機関口座の簡易生成 */
    SelfFiAccount selfFiAcc(String category, String currency) {
        new SelfFiAccount(null, category, currency, "$category-$currency", "xxxxxx")
    }
    
}
