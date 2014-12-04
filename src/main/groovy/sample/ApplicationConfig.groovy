package sample

import groovy.transform.CompileStatic

import javax.validation.Validator

import org.springframework.context.MessageSource
import org.springframework.context.annotation.*
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

import com.fasterxml.jackson.databind.ObjectMapper

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 *
 * @author jkazama
 */
@CompileStatic
@Configuration
class ApplicationConfig {

	/**
	 * BeanValidationメッセージのUTF-8に対応したValidator。
	 * <p>WebMvcAutoConfigurationAdapterの拡張もあわせて確認してください。
	 */
	@Bean
	Validator validator(final MessageSource message) {
		def factory = new LocalValidatorFactoryBean()
		factory.setValidationMessageSource(message)
		factory
	}

	/**
	 * Jackson(JSON変換ライブラリ)の日付フォーマットをISOベースに変換しています。
	 */
	@Bean
	ObjectMapper objectMapper() {
		def bean = new Jackson2ObjectMapperFactoryBean()
		bean.setIndentOutput(true)
		bean.setSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
		bean.afterPropertiesSet()
		bean.getObject()
	}
}
