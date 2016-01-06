package sample.model.account

import javax.persistence.*
import javax.validation.constraints.NotNull

import sample.ValidationException
import sample.context.orm.*
import sample.model.constraints.*

/**
 * 口座を表現します。
 * low: サンプル用に必要最低限の項目だけ
 *
 * @author jkazama
 */
@JpaStaticEntity
class Account extends JpaActiveRecord<Account> {
    private static final long serialVersionUID = 1L

    /** 口座ID */
    @Id
    @AccountId
    String id
    /** 口座名義 */
    @Name
    String name
    /** メールアドレス */
    @Email
    String mail
    /** 口座状態 */
    @Enumerated(EnumType.STRING)
    @NotNull
    AccountStatusType statusType

    static Account load(final JpaRepository rep, String id) {
        rep.load(Account, id)
    }

    /** 有効な口座を返します。 */
    static Account loadActive(final JpaRepository rep, String id) {
        def acc = load(rep, id)
        if (acc.statusType.inacitve()) throw new ValidationException("error.Account.loadActive")
        acc
    }
}

/** 口座状態を表現します。 */
enum AccountStatusType {
    /** 通常 */
    NORMAL,
    /** 退会 */
    WITHDRAWAL
    boolean inacitve() {
        this == WITHDRAWAL
    }
}
