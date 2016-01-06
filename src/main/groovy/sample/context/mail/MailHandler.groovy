package sample.context.mail

import groovy.transform.Canonical
import groovy.util.logging.Slf4j;

import org.springframework.beans.factory.annotation.Value

import sample.context.Dto
import sample.context.StaticComponent

/**
 * メール送受信を行います。
 * low: サンプルではメール送信のI/Fのみ作ってます。実際はPOP3/IMAP等のメール受信もサポートしたり、
 * リターンメールのフォローアップをしたりします。
 *
 * @author jkazama
 */
@StaticComponent
class MailHandler {
    
    /** メール利用可否 */
    @Value('${sample.mail.enable:true}')
    private boolean enable

    /** メールを送信します。 */
    MailHandler send(final SendMail mail) {
        if (!enable) {
            log.info "メールをダミー送信しました。 [$mail.subject]"
            return this
        }
        // low: 外部リソースとの連携でオーバーヘッドが結構発生するので、実際は非同期処理で行う。
        // low: bodyへbodyArgsを置換マッピングした内容をJavaMailなどで送信。
        this
    }
}

/** メール送信パラメタ。low: 実際はかなり多くの項目が関与するのでBuilderにした方が使い勝手が良いです */
@Canonical
class SendMail implements Dto {
    private static final long serialVersionUID = 1L;
    String address
    String subject
    String body
    Map<String, String> bodyArgs = [:]
}
