package sample.usecase

import sample.context.StaticService

/**
 * サービスマスタドメインに対する社内ユースケース処理。
 *
 * @author jkazama
 */
@StaticService
class MasterAdminService  extends ServiceSupport {

	/**
	 * 営業日を進めます。
	 * low: 実際はスレッドセーフの考慮やDB連携含めて、色々とちゃんと作らないとダメです。
	 */
	void processDay() {
		audit.audit("営業日を進める", {
			dh.time.daySet(dh.time.dayPlus(1))
		})
	}
}