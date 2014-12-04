package sample.context.boot

import groovy.transform.CompileStatic

import javax.validation.Validator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import org.springframework.web.servlet.config.annotation.EnableWebMvc


/**
 * SpringMVCにおいて常に標準Validator(ValidationMessages)を利用してしまう問題に対する対処。
 *
 * @author jkazama
 */
@CompileStatic
@Configuration
@EnableWebMvc
@EnableConfigurationProperties([WebMvcProperties, ResourceProperties])
class WebMvcAutoConfigurationAdapter extends org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {

	@Autowired
	Validator validator

	@Override
	org.springframework.validation.Validator getValidator() {
		new SpringValidatorAdapter(validator)
	}
}
