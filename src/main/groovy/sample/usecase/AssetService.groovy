package sample.usecase

import java.util.concurrent.Callable

import sample.context.*
import sample.model.asset.*

/**
 * 資産ドメインに対する顧客ユースケース処理。
 *
 * @author jkazama
 */
@StaticService
class AssetService extends ServiceSupport {

    /**
     * 未処理の振込依頼情報を検索します。
     * low: 参照系は口座ロックが必要無いケースであれば@Transactionalでも十分
     * low: CashInOutは情報過多ですがアプリケーション層では公開対象を特定しにくい事もあり、
     * UI層に最終判断を委ねています。
     */
    List<CashInOut> findUnprocessedCashOut() {
        String accId = actor().id
        tx(accId, LockType.READ, { CashInOut.findUnprocessed(rep, accId) })
    }

    /**
     * 振込出金依頼をします。
     * low: 公開リスクがあるためUI層には必要以上の情報を返さない事を意識します。
     * low: 監査ログの記録は状態を変えうる更新系ユースケースでのみ行います。
     * low: ロールバック発生時にメールが飛ばないようにトランザクション境界線を明確に分離します。
     * low: Callbackが深くなるのはJava7前提での制約なので、ここから
     * 単純に可読性を上げるなら内部処理を別メソッドで切り出すなどして対応します。
     * @return 振込出金依頼ID
     */
    String withdraw(final RegCashOut p) {
        audit.audit("振込出金依頼をします", {
            p.accountId = actor().id; // 顧客側はログイン利用者で強制上書き
            // low: 口座IDロック(WRITE)とトランザクションをかけて振込処理
            CashInOut cio = tx(actor().id, LockType.WRITE, { CashInOut.withdraw(rep, p) })
            // low: トランザクション確定後に出金依頼を受付した事をメール通知します。
            mail.sendWithdrawal(cio)
            cio.id
        })
    }

}
