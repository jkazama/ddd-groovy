package sample.model.constraints

import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.*

import java.lang.annotation.*

import javax.validation.*
import javax.validation.constraints.*

/**
 * 日付を表現する制約注釈。
 * <p>yyyyMMddの8桁文字列を想定します。
 * 
 * @author jkazama
 */
@Documented
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
@ReportAsSingleViolation
@Size
@Pattern(regexp = "")
@interface DayEmpty {
    String message() default "{error.domain.day}"

    @SuppressWarnings("rawtypes")
    Class<?>[] groups() default []

    @SuppressWarnings("rawtypes")
    Class<? extends Payload>[] payload() default []

    @OverridesAttribute(constraint = Size.class, name = "max")
    int max() default 8
    
    @OverridesAttribute(constraint = Pattern.class, name = "regexp")
    String regexp() default '^\\d{8}|\\d{0}$'

}
