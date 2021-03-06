package sample.model.constraints

import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.*

import java.lang.annotation.*

import javax.validation.*
import javax.validation.constraints.*

/**
 * 文字列ID(必須)を表現する制約注釈。
 *
 * @author jkazama
 */
@Documented
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotNull
@Size
@Pattern(regexp = "")
@interface IdStr {
    String message() default "{error.domain.idStr}"

    @SuppressWarnings("rawtypes")
    Class<?>[] groups() default []

    @SuppressWarnings("rawtypes")
    Class<? extends Payload>[] payload() default []

    @OverridesAttribute(constraint = Size.class, name = "max")
    int max() default 32

    @OverridesAttribute(constraint = Pattern.class, name = "regexp")
    String regexp() default '^\\p{ASCII}*$'

    @OverridesAttribute(constraint = Pattern.class, name = "flags")
    Pattern.Flag[] flags() default []

}
