package sample.model.constraints

import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.*

import java.lang.annotation.*

import javax.validation.*
import javax.validation.constraints.*

/**
 * メールアドレス(必須)を表現する制約注釈。
 * low: ちゃんとやると大変なので未実装です。HibernateのEmailValidatorを利用しても良いですが、
 * 恐らく最終的に固有のConstraintValidatorを作らされる事になると思います。
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
@interface Email {
	String message() default "{error.domain.email}"

	@SuppressWarnings("rawtypes")
	Class<?>[] groups() default []

	@SuppressWarnings("rawtypes")
	Class<? extends Payload>[] payload() default []

	@OverridesAttribute(constraint = Size.class, name = "max")
	int max() default 256

	@OverridesAttribute(constraint = Pattern.class, name = "regexp")
	String regexp() default '.*'

	@OverridesAttribute(constraint = Pattern.class, name = "flags")
	Pattern.Flag[] flags() default []

}
