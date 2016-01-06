package sample.controller

import groovy.transform.CompileStatic

import javax.validation.Valid

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import sample.ActionStatusType
import sample.context.Dto
import sample.context.RestStaticController
import sample.context.StaticDto
import sample.model.asset.*
import sample.usecase.AssetService
import sample.util.TimePoint

/**
 * 資産に関わる顧客のUI要求を処理します。
 *
 * @author jkazama
 */
@RestStaticController
@RequestMapping("/asset")
class AssetController {
    
    @Autowired
    private AssetService service;

    /** 未処理の振込依頼情報を検索します。 */
    @RequestMapping(value = "/cio/unprocessedOut")
    List<CashOutUI> findUnprocessedCashOut() {
        service.findUnprocessedCashOut().collect { CashOutUI.by(it) }
    }

    /**
     * 振込出金依頼をします。
     * low: 実際は状態を変えうる行為なのでPOSTですが、デモ用にGETでも処理できるようにしています。
     * low: RestControllerの標準の振る舞いとしてプリミティブ型はJSON化されません。(解析時の優先順位の関係だと思いますが)
     * ちゃんとやりたい時はResponseEntityを戻り値として、DtoやMapを包むと良いと思います。
     */
    @RequestMapping(value = "/cio/withdraw", method = [ RequestMethod.POST, RequestMethod.GET ])
    String withdraw(@Valid RegCashOut p) {
        service.withdraw(p)
    }

}

/** 振込出金依頼情報の表示用Dto */
@StaticDto
class CashOutUI implements Dto {
    private static final long serialVersionUID = 1L
    String id
    String currency
    BigDecimal absAmount
    TimePoint requestDate
    String eventDay
    String valueDay
    ActionStatusType statusType
    Date updateDate
    Long cashflowId

    static CashOutUI by(final CashInOut cio) {
        new CashOutUI(cio.id, cio.currency, cio.absAmount, cio.requestDate,
            cio.eventDay, cio.valueDay, cio.statusType, cio.updateDate, cio.cashflowId)
    }
}
