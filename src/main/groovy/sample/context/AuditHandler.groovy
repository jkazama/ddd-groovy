package sample.context

import org.slf4j.*
import org.springframework.beans.factory.annotation.Autowired

import sample.*

/**
 * 利用者監査やシステム監査(定時バッチや日次バッチ等)などを取り扱います。
 * <p>暗黙的な適用を望む場合は、AOPとの連携も検討してください。
 * low: 実際はLoggerだけでなく、システムスキーマの監査テーブルへ書きだされます。(開始時と完了時で別TXにする事で応答無し状態を検知可能)
 * low: Loggerを利用する時はlogger.xmlを利用してファイル等に吐き出す
 *
 * @author jkazama
 */
@StaticComponent
class AuditHandler {

	static final Logger loggerActor = LoggerFactory.getLogger("Audit.Actor")
	static final Logger loggerEvent = LoggerFactory.getLogger("Audit.Event")

	@Autowired
	private ActorSession session;
	
	/** 与えた処理に対し、監査ログを記録します。 */
	def <V> V audit(String message, Closure<V> clos) {
		logger().trace(msg(message, "[開始]", null))
		long start = System.currentTimeMillis();
		try {
			def v = clos.call()
			logger().info(msg(message, "[完了]", start))
			v
		} catch (ValidationException e) {
			logger().warn(msg(message, "[審例]", start))
			throw e
		} catch (Exception e) {
			logger().error(msg(message, "[例外]", start))
			throw new InvocationException("error.Exception", e)
		}
	}
	
	private Logger logger() {
		session.actor().roleType.isSystem() ? loggerEvent : loggerActor
	}
	
	private String msg(String message, String prefix, Long startMillis) {
		def actor = session.actor()
		def sb = new StringBuilder("$prefix ")
		if (actor.roleType.isAnonymous()) sb << "[${actor.source}] "
		else if (actor.roleType.notSystem()) sb << "[${actor.id}] "
		sb << message
		startMillis ? sb << " [${System.currentTimeMillis() - startMillis}ms]" : sb
	}

}
