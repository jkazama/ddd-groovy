package sample.model.constraints

import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.*

import java.lang.annotation.*

import javax.validation.*
import javax.validation.constraints.*

/**
 * 名称(必須)を表現する制約注釈。
 * low: 実際は姓名(ミドルネーム)の考慮やモノ系の名称などを意識する必要があります。
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
@interface Name {
    String message() default "{error.domain.name}"

    @SuppressWarnings("rawtypes")
    Class<?>[] groups() default []

    @SuppressWarnings("rawtypes")
    Class<? extends Payload>[] payload() default []

    @OverridesAttribute(constraint = Size.class, name = "max")
    int max() default 30

    @OverridesAttribute(constraint = Pattern.class, name = "regexp")
    String regexp() default '.*'

    @OverridesAttribute(constraint = Pattern.class, name = "flags")
    Pattern.Flag[] flags() default []

}
