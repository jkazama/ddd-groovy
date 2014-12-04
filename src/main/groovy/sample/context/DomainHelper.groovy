package sample.context

import org.springframework.beans.factory.annotation.Autowired

/**
 * ドメイン処理を行う上で必要となるインフラ層コンポーネントへのアクセサを提供します。
 *
 * @author jkazama
 */
@StaticComponent
class DomainHelper {

	/** 日時ユーティリティ */
	@Autowired
	Timestamper time
	@Autowired
	ActorSession actorSession
	/** ID生成ユーティリティ */
	@Autowired
	IdGenerator uid

	/** ログイン中のユースケース利用者 */
	Actor actor() {
		actorSession.actor()
	}
}
