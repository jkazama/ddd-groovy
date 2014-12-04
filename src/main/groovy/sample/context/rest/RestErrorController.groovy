package sample.context.rest

import javax.servlet.http.HttpServletRequest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.*
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.ServletRequestAttributes

import sample.*
import sample.context.RestStaticController

/**
 * REST用の例外ハンドリングを行うController。
 * <p>application.ymlの"error.path"属性との組合せで有効化します。
 * あわせて"error.whitelabel.enabled: false"でwhitelabelを無効化しておく必要があります。
 * 
 * @author jkazama
 */
@RestStaticController
class RestErrorController implements ErrorController {

	@Autowired
	ErrorAttributes errorAttributes

	@Override
	String getErrorPath() {
		"/error"
	}

	@RequestMapping("/error")
	Map<String, Object> error(HttpServletRequest request) {
		errorAttributes.getErrorAttributes(new ServletRequestAttributes(request), false)
	}
}
