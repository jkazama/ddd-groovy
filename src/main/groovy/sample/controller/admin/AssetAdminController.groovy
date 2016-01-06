package sample.controller.admin

import javax.validation.Valid

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import sample.context.RestStaticController
import sample.model.asset.*
import sample.usecase.AssetAdminService

/**
 * 資産に関わる社内のUI要求を処理します。
 *
 * @author jkazama
 */
@RestStaticController
@RequestMapping("/admin/asset")
class AssetAdminController {

    @Autowired
    private AssetAdminService service;

    /** 未処理の振込依頼情報を検索します。 */
    @RequestMapping(value = "/cio")
    List<CashInOut> findCashInOut(@Valid FindCashInOut p) {
        service.findCashInOut(p);
    }

}
