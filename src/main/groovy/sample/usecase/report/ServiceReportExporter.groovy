package sample.usecase.report

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.transaction.PlatformTransactionManager

import sample.context.StaticComponent
import sample.context.orm.DefaultRepository
import sample.context.report.ReportHandler
import sample.model.asset.FindCashInOut

/**
 * アプリケーション層のレポート出力を行います。
 * <p>独自にトランザクションを管理するので、サービスのトランザクション内で
 * 呼び出さないように注意してください。
 * low: コード量が多くなるため今回のサンプルでは対象外とします。
 *
 * @author jkazama
 */
@StaticComponent
class ServiceReportExporter {

    @Autowired
    private MessageSource msg
    @Autowired
    private DefaultRepository rep
    @Autowired
    private PlatformTransactionManager tx
    @Autowired
    private ReportHandler report //low: サンプルでは未実装なので利用しない

    /**　振込入出金情報をCSV出力します。 */
    byte[] exportCashInOut(final FindCashInOut p) {
        //low: バイナリ生成。条件指定を可能にしたオンラインダウンロードを想定。
        new byte[0]
    }

    /**　振込入出金情報を帳票出力します。 */
    void exportFileCashInOut(String baseDay) {
        //low: 特定のディレクトリへのファイル出力。ジョブ等での利用を想定
    }

    
}
