package sample.controller

import org.aspectj.lang.annotation.*
import org.springframework.beans.factory.annotation.Autowired

import sample.context.*

/**
 * スレッドローカルに利用者を紐付けるAOPInterceptor。
 * low: 今回は認証機能を用意していないのでダミーです。
 *
 * @author jkazama
 */
@Aspect
@StaticComponent
class LoginInterceptor {

	@Autowired
	private ActorSession session
	
	@Before("execution(* sample.controller.*Controller.*(..))")
	void bindUser() {
		session.bind(Actor.by("sample", ActorRoleType.USER))
	}
	
	@Before("execution(* sample.controller.admin.*Controller.*(..))")
	void bindAdmin() {
		session.bind(Actor.by("admin", ActorRoleType.INTERNAL))
	}
	
	@Before("execution(* sample.controller.system.*Controller.*(..))")
	void bindSystem() {
		session.bind(Actor.System)
	}
	
	@After("execution(* sample.controller..*Controller.*(..))")
	void unbind() {
		session.unbind()
	}

	
}
