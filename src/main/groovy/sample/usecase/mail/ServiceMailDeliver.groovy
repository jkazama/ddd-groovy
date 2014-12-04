package sample.usecase.mail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import sample.context.StaticComponent
import sample.context.mail.*
import sample.context.orm.*
import sample.model.account.Account
import sample.model.asset.CashInOut

/**
 * アプリケーション層のサービスメール送信を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で
 * 呼び出さないように注意してください。
 *
 * @author jkazama
 */
@StaticComponent
class ServiceMailDeliver {

	@Autowired
	private MessageSource msg;
	@Autowired
	private DefaultRepository rep;
	@Autowired
	private PlatformTransactionManager tx;
	@Autowired
	private MailHandler mail;

	/** サービスメールを送信します。 */
	void send(final String accountId, final ServiceMailCreator creator) {
		new TransactionTemplate(tx).execute { status ->
			mail.send(creator.create(Account.load(rep, accountId)))
		}
	}

	/** 出金依頼受付メールを送信します。 */
	void sendWithdrawal(final CashInOut cio) {
		send(cio.accountId, { Account account ->
			// low: 実際のタイトルや本文はDBの設定情報から取得
			def subject = "[$cio.id] 出金依頼受付のお知らせ";
			def body = "{name}様 …省略…";
			def bodyArgs = new HashMap<String, String>()
			bodyArgs.put("name", account.name)
			new SendMail(account.mail, subject, body, bodyArgs);
		})
	}	
}

/** メール送信情報の生成インターフェース */
interface ServiceMailCreator {
	SendMail create(final Account account)
}
