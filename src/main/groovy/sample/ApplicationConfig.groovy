package sample

import groovy.transform.CompileStatic

import javax.validation.Validator

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource
import org.springframework.context.annotation.*
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 *
 * @author jkazama
 */
@CompileStatic
@Configuration
class ApplicationConfig {}

/** SpringMvcの拡張コンフィギュレーション */
@CompileStatic
@Configuration
class WebMvcConfig extends WebMvcConfigurerAdapter {
	@Autowired
	private MessageSource message;
	
	/** BeanValidationメッセージのUTF-8に対応したValidator。 */
	@Bean
	LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
		factory.setValidationMessageSource(message);
		return factory;
	}
	
	/** 標準Validatorの差し替えをします。 */
	@Override
	org.springframework.validation.Validator getValidator() {
		return validator();
	}
}
