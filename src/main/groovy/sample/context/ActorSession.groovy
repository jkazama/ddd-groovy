package sample.context

import groovy.transform.Canonical

import org.springframework.stereotype.Component

/**
 * スレッドローカルスコープの利用者セッション。
 * low: 今回スコープ外ですが、認証ロジックに#bind/#unbindを組み込んで運用します。
 *
 * @author jkazama
 */
@StaticComponent
class ActorSession {

	private ThreadLocal<Actor> actorLocal = new ThreadLocal<>()

	/** 利用者セッションへ利用者を紐付けます。 */
	ActorSession bind(final Actor actor) {
		actorLocal.set(actor)
		this
	}

	/** 利用者セッションを破棄します。 */
	ActorSession unbind() {
		actorLocal.remove()
		this
	}

	/** 有効な利用者を返します。紐付けされていない時は匿名者が返されます。 */
	Actor actor() {
		actorLocal.get() ?: Actor.Anonymous
	}
}

/**
 * ユースケースにおける利用者を表現します。
 * 
 * @author jkazama
 */
@Canonical
class Actor implements Dto {
	private static final long serialVersionUID = 1L

	/** 匿名利用者 */
	static final Actor Anonymous = Actor.by("unknown", ActorRoleType.ANONYMOUS)
	/** システム利用者定数 */
	static final Actor System = Actor.by("system", ActorRoleType.SYSTEM)
	
	/** 利用者ID */
	String id
	/** 利用者名称 */
	String name
	/** 利用者が持つ{@link ActorRoleType} */
	ActorRoleType roleType
	/** 利用者が使用する{@link Locale} */
	Locale locale
	/** 利用者の接続チャネル名称 */
	String channel
	/** 利用者を特定する外部情報。(IPなど) */
	String source

	static Actor by(String id, ActorRoleType roleType) {
		new Actor(id, id, roleType, Locale.getDefault(), null, null)
	}
	
}

/**
 * 利用者の役割を表現します。
 * @author jkazama
 */
enum ActorRoleType {
	/** 匿名利用者(ID等の特定情報を持たない利用者) */
	ANONYMOUS,
	/** 利用者(主にBtoCの顧客, BtoB提供先社員) */
	USER,
	/** 内部利用者(主にBtoCの社員, BtoB提供元社員) */
	INTERNAL,
	/** システム管理者(ITシステム担当社員またはシステム管理会社の社員) */
	ADMINISTRATOR,
	/** システム(システム上の自動処理) */
	SYSTEM

	boolean isAnonymous() {
		this == ANONYMOUS
	}
	boolean isSystem() {
		this == SYSTEM
	}
	boolean notSystem() {
		!isSystem()
	}
}
