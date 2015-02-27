package sample.context.rest

import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j

import java.util.ArrayList;
import java.util.Map

import javax.persistence.EntityNotFoundException
import javax.validation.ConstraintViolationException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.*
import org.springframework.http.*
import org.springframework.validation.*
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.*

import sample.*

/**
 * REST用の例外Map変換サポート。
 * <p>AOPアドバイスで全てのRestControllerに対して例外処理を当て込みます。
 *
 * @author jkazama
 */
@CompileStatic
@ControllerAdvice(annotations = RestController.class)
@Slf4j
class RestErrorAdvice {

	@Autowired
	private MessageSource msg;

	@ExceptionHandler(ServletRequestBindingException)
	ResponseEntity<Map<String, String[]>> handleServletRequestBinding(ServletRequestBindingException e) {
		log.warn e.getMessage()
		new ErrorHolder(msg, "error.ServletRequestBinding").result(HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException)
	ResponseEntity<Map<String, String[]>> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e) {
		log.warn e.getMessage()
		new ErrorHolder(msg, "error.HttpMediaTypeNotAcceptable").result(HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(EntityNotFoundException)
	ResponseEntity<Map<String, String[]>> handleEntityNotFoundException(EntityNotFoundException e) {
		log.warn e.getMessage()
		new ErrorHolder(msg, "error.EntityNotFoundException").result(HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(ConstraintViolationException)
	ResponseEntity<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
		log.warn e.getMessage()
		def warns = Warns.init()
		e.getConstraintViolations().each { v ->  warns.add(v.getPropertyPath().toString(), v.getMessage()) }
		new ErrorHolder(msg, warns.list).result(HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(BindException)
	ResponseEntity<Map<String, String[]>> handleBind(BindException e) {
		log.warn e.getMessage()
		def warns = Warns.init()
		e.getAllErrors().each { oe ->
			String field = ""
			if (1 == oe.getCodes().length) {
				field = fieldGet(oe.getCodes()[0])
			} else if (1 < oe.getCodes().length) {
				// low: プリフィックスは冗長なので外してます
				field = fieldGet(oe.getCodes()[1])
			}
			List<String> args = []
			oe.getArguments().each { arg ->
				if (arg instanceof MessageSourceResolvable) return
				args.add(arg.toString())
			}
			String message = oe.getDefaultMessage()
			if (0 <= oe.getCodes()[0].indexOf("typeMismatch")) {
				message = oe.getCodes()[2]
			}
			warns.add(field, message, args as String[])
		}
		new ErrorHolder(msg, warns.list).result(HttpStatus.BAD_REQUEST)
	}

	protected String fieldGet(String field) {
		field ? field.substring(field.indexOf('.') + 1) : ""
	}

	@ExceptionHandler(ValidationException)
	ResponseEntity<Map<String, String[]>> handleValidation(ValidationException e) {
		log.warn e.getMessage()
		new ErrorHolder(msg, e).result(HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(Exception)
	ResponseEntity<Map<String, String[]>> handleException(Exception e) {
		log.error "予期せぬ例外が発生しました。", e
		new ErrorHolder(msg, "error.Exception", "サーバー側で問題が発生した可能性があります。")
				.result(HttpStatus.INTERNAL_SERVER_ERROR)
	}
}

/** 例外情報のスタックを表現します。 */
class ErrorHolder {
	private Map<String, List<String>> errors = new HashMap<>()
	private MessageSource msg

	ErrorHolder(final MessageSource msg) {
		this.msg = msg
	}

	ErrorHolder(final MessageSource msg, final ValidationException e) {
		this(msg, e.list())
	}

	ErrorHolder(final MessageSource msg, final List<Warn> warns) {
		this.msg = msg
		warns.each { it.global() ? errorGlobal(it.message) : error(it.field, it.message) }
	}

	ErrorHolder(final MessageSource msg, String globalMsgKey, String... msgArgs) {
		this.msg = msg
		errorGlobal(globalMsgKey, msgArgs)
	}

	ErrorHolder errorGlobal(String msgKey, String defaultMsg, String... msgArgs) {
		if (!errors.containsKey("")) errors.put("", [])
		errors[""].add(msg.getMessage(msgKey, msgArgs, defaultMsg, Locale.getDefault()))
		this
	}

	ErrorHolder errorGlobal(String msgKey, String... msgArgs) {
		errorGlobal(msgKey, msgKey, msgArgs)
	}

	ErrorHolder error(String field, String msgKey, String... msgArgs) {
		if (!errors.containsKey(field)) errors[field] = []
		errors[field].add(msg.getMessage(msgKey, msgArgs, msgKey, Locale.getDefault()))
		this
	}

	ResponseEntity<Map<String, String[]>> result(HttpStatus status) {
		new ResponseEntity(errors.entrySet().collect { v -> [v.key, v.value] }, status)
	}
}
