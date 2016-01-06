package sample.util

import groovy.transform.CompileStatic;
import sample.ValidationException
import sample.Warns

/**
 * 審査例外Builder。
 *
 * @author jkazama
 */
@CompileStatic
class Validator {

    private Warns warns = Warns.init()

    /** 審査を行います。validがfalseの時に例外を内部にスタックします。 */
    Validator check(boolean valid, String message) {
        if (!valid)    warns.add(message);
        this
    }

    /** 個別属性の審査を行います。validがfalseの時に例外を内部にスタックします。 */
    Validator checkField(boolean valid, String field, String message) {
        if (!valid) warns.add(field, message)
        this
    }

    /** 審査を行います。失敗した時は即時に例外を発生させます。 */
    Validator verify(boolean valid, String message) {
        check(valid, message).verify()
    }

    /** 個別属性の審査を行います。失敗した時は即時に例外を発生させます。 */
    Validator verifyField(boolean valid, String field, String message) {
        checkField(valid, field, message).verify()
    }

    /** 検証します。事前に行ったcheckで例外が存在していた時は例外を発生させます。 */
    Validator verify() {
        if (hasWarn()) throw new ValidationException(warns)
        clear()
    }

    boolean hasWarn() {
        warns.nonEmpty()
    }

    Validator clear() {
        warns.list.clear()
        this
    }
}
