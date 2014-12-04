package sample.usecase

import java.util.concurrent.Callable

import org.slf4j.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import sample.context.*
import sample.context.orm.DefaultRepository
import sample.usecase.mail.ServiceMailDeliver
import sample.usecase.report.ServiceReportExporter

/**
 * ユースケースサービスの基底クラス。
 *
 * @author jkazama
 */
abstract class ServiceSupport {

	@Autowired
	protected MessageSource msg

	@Autowired
	protected DomainHelper dh
	@Autowired
	protected DefaultRepository rep
	@Autowired
	protected PlatformTransactionManager txManager
	@Autowired
	protected IdLockHandler idLock

	@Autowired
	protected AuditHandler audit
	@Autowired
	protected ServiceMailDeliver mail
	@Autowired
	protected ServiceReportExporter report

	/** i18nメッセージ変換を行います。 */
	protected String msg(String message) {
		msg.getMessage(message, null, message, Locale.getDefault())
	}

	/** 利用者を返します。 */
	protected Actor actor() {
		dh.actor()
	}

	/** トランザクション処理を実行します。 */
	protected <V> V tx(Closure<V> clos) {
		new TransactionTemplate(txManager).execute { status -> clos.call() }
	}

	/** 口座ロック付でトランザクション処理を実行します。 */
	protected <V> V tx(String accountId, LockType lockType, final Callable<V> callable) {
		idLock.lock(accountId, lockType, { tx(callable)	})
	}

}
