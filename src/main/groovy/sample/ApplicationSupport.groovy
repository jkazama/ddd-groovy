package sample

import groovy.transform.CompileStatic

import org.springframework.util.Assert

import sample.context.Dto
import sample.context.StaticDto


/**
 * 何らかの行為に関わる処理ステータス概念。
 * 
 * @author jkazama
 */
@CompileStatic
enum ActionStatusType {
	/** 未処理 */
	UNPROCESSED,
	/** 処理中 */
	PROCESSING,
	/** 処理済 */
	PROCESSED,
	/** 取消 */
	CANCELLED,
	/** エラー */
	ERROR

	/** 完了済みのステータス一覧 */
	static final List<ActionStatusType> finishTypes = [PROCESSED, CANCELLED]

	/** 未完了のステータス一覧(処理中は含めない) */
	static final List<ActionStatusType> unprocessingTypes = [UNPROCESSED, ERROR]

	/** 未完了のステータス一覧(処理中も含める) */
	static final List<ActionStatusType> unprocessedTypes = [UNPROCESSED, PROCESSING, ERROR]

	/** 完了済みのステータスの時はtrue */
	boolean isFinish() {
		finishTypes.contains(this)
	}

	/** 未完了のステータス(処理中は含めない)の時はtrue */
	boolean isUnprocessing() {
		unprocessingTypes.contains(this)
	}

	/** 未完了のステータス(処理中も含める)の時はtrue */
	boolean isUnprocessed() {
		unprocessedTypes.contains(this)
	}
}

/**
 * 処理時の実行例外を表現します。
 *
 * @author jkazama
 */
@CompileStatic
class InvocationException extends RuntimeException {
	private static final long serialVersionUID = 1L

	InvocationException(String message, Throwable cause) {
		super(message, cause)
	}

	InvocationException(String message) {
		super(message)
	}

	InvocationException(Throwable cause) {
		super(cause)
	}

}

/**
 * 審査例外を表現します。
 *
 * @author jkazama
 */
@CompileStatic
class ValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L

	private final Warns warns

	/**
	 * フィールドに従属しないグローバルな審査例外を通知するケースで利用してください。
	 * @param message
	 */
	ValidationException(String message) {
		super(message)
		warns = Warns.init(message)
	}

	/**
	 * フィールドに従属する審査例外を通知するケースで利用してください。
	 * @param field
	 * @param message
	 */
	ValidationException(String field, String message) {
		super(message)
		warns = Warns.init(field, message)
	}

	/**
	 * フィールドに従属する審査例外を通知するケースで利用してください。
	 * @param field
	 * @param message
	 * @param messageArgs
	 */
	ValidationException(String field, String message, String[] messageArgs) {
		super(message)
		warns = Warns.init(field, message, messageArgs)
	}
	
	/**
	 * 複数件の審査例外を通知するケースで利用してください。
	 * @param warns
	 */
	ValidationException(final Warns warns) {
		super(warns.head().getMessage())
		this.warns = warns
	}

	/**
	 * @return 発生した審査例外一覧を返します。
	 */
	List<Warn> list() {
		warns.list
	}

	@Override
	String getMessage() {
		warns.head().getMessage()
	}

}

/** 審査例外情報  */
@StaticDto
class Warns implements Dto {
	private static final long serialVersionUID = 1L
	
	List<Warn> list = new ArrayList<>()

	Warns add(String message) {
		add(null, message, null)
	}

	Warns add(String field, String message) {
		add(field, message, null)
	}
	
	Warns add(String field, String message, String[] messageArgs) {
		list.add(new Warn(field, message, messageArgs))
		this
	}

	Warn head() {
		Assert.notEmpty(list)
		list.head()
	}

	boolean nonEmpty() {
		!list.isEmpty()
	}

	static Warns init() {
		new Warns()
	}

	static Warns init(String message) {
		init().add(message)
	}

	static Warns init(String field, String message) {
		init().add(field, message)
	}
	
	static Warns init(String field, String message, String[] messageArgs) {
		init().add(field, message, messageArgs)
	}

}

/**
 * フィールドスコープの審査例外トークン。
 */
@StaticDto
class Warn implements Dto {
	private static final long serialVersionUID = 1L
	String field
	String message
	String[] messageArgs

	/**
	 * @return フィールドに従属しないグローバル例外時はtrue
	 */
	boolean global() {
		!field
	}
}


