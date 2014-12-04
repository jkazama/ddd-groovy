package sample.model.constraints

import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.RUNTIME

import java.lang.annotation.*

import javax.validation.*
import javax.validation.constraints.*

/**
 * 絶対値の金額(必須)を表現する制約注釈。
 * 
 * @author jkazama
 */
@Documented
@Constraint(validatedBy = [])
@Target(FIELD)
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotNull
@Digits(integer = 16, fraction = 4)
@DecimalMin("0.00")
@interface AbsAmount {
	String message() default "{error.domain.absAmount}"

	@SuppressWarnings("rawtypes")
	Class<?>[] groups() default []

	@SuppressWarnings("rawtypes")
	Class<? extends Payload>[] payload() default []

	@OverridesAttribute(constraint = Digits.class, name = "integer")
	int integer() default 16

	@OverridesAttribute(constraint = Digits.class, name = "fraction")
	int fraction() default 4
}
