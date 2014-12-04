package sample.usecase

import org.springframework.transaction.annotation.Transactional

import sample.context.*
import sample.model.asset.*

/**
 * 資産ドメインに対する社内ユースケース処理。
 *
 * @author jkazama
 */
@StaticService
class AssetAdminService extends ServiceSupport {

	/**
	 * 振込入出金依頼を検索します。
	 * low: 口座横断的なので割り切りでREADロックはかけません。
	 */
	@Transactional
	List<CashInOut> findCashInOut(final FindCashInOut p) {
		CashInOut.find(rep, p)
	}

	/**
	 * 振込出金依頼を締めます。
	 */
	void closingCashOut() {
		audit.audit("振込出金依頼の締め処理をする", { tx { closingCashOutInTx() } })
	}

	private void closingCashOutInTx() {
		//low: 以降の処理は口座単位でfilter束ねしてから実行する方が望ましい。
		//low: 大量件数の処理が必要な時はそのままやるとヒープが死ぬため、idソートでページング分割して差分実行していく。
		CashInOut.findUnprocessed(rep).each { cio ->
			//low: TX内のロックが適切に動くかはIdLockHandlerの実装次第。
			// 調整が難しいようなら大人しく営業停止時間(IdLock必要な処理のみ非活性化されている状態)を作って、
			// ロック無しで一気に処理してしまう方がシンプル。
			idLock.lock(cio.accountId, LockType.WRITE, {
				try {
					cio.process(rep)
					//low: SQLの発行担保。扱う情報に相互依存が無く、セッションキャッシュはリークしがちなので都度消しておく。
					rep.flushAndClear()
				} catch (Exception e) {
					log.error "[$cio.id] 振込出金依頼の締め処理に失敗しました。", e
					try {
						cio.error(rep)
						rep.flush()
					} catch (Exception ex) {
						//low: 2重障害(恐らくDB起因)なのでloggerのみの記載に留める
					}
				}
			})
		}
	}

	/**
	 * キャッシュフローを実現します。
	 * <p>受渡日を迎えたキャッシュフローを残高に反映します。
	 */
	void realizeCashflow() {
		audit.audit("キャッシュフローを実現する", { tx { realizeCashflowInTx() } })
	}

	private void realizeCashflowInTx() {
		//low: 日回し後の実行を想定
		String day = dh.time.day
		Cashflow.findDoRealize(rep, day).each { cf ->
			idLock.lock(cf.accountId, LockType.WRITE, {
				try {
					cf.realize(rep)
					rep.flushAndClear()
				} catch (Exception e) {
					log.error "[$cf.id] キャッシュフローの実現に失敗しました。", e
					try { cf.error(rep); rep.flush() } catch (Exception ex) {}
				}
			})
		}
	}


}
