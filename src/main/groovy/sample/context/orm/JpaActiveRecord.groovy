package sample.context.orm

import groovy.transform.AnnotationCollector;
import groovy.transform.Canonical;
import groovy.transform.CompileStatic;

import java.io.Serializable

import sample.context.Entity
import sample.util.Validator


/**
 * JPAベースのEntityメタアノテーション
 * 
 * @author jkazama
 */
@javax.persistence.Entity
@AnnotationCollector([CompileStatic, Canonical])
@interface JpaStaticEntity {}

/**
 * JPAベースでActiveRecordの概念を提供するEntity基底クラス。
 * <p>ここでは自インスタンスの状態に依存する簡易な振る舞いのみをサポートします。
 * 実際のActiveRecordモデルにはget/find等の概念も含まれますが、それらは 自己の状態を
 * 変える行為ではなく対象インスタンスを特定する行為(クラス概念)にあたるため、
 * クラスメソッドとして継承先で個別定義するようにしてください。
 * <pre>
 * static Account findAll(final JpaRepository rep) {
 *     rep.findAll(Account)
 * }
 * </pre>
 *
 * @author jkazama
 */
abstract class JpaActiveRecord<T extends Entity> implements Serializable, Entity {

	/**
	 * @return 審査ユーティリティを生成します。
	 */
	protected Validator validator() {
		new Validator()
	}

	/**
	 * 与えられたレポジトリを経由して自身を新規追加します。
	 * @param rep 永続化の際に利用する関連{@link JpaRepository}
	 * @return 自身の情報
	 */
	T save(final JpaRepository rep) {
		rep.save(this)
	}

	/**
	 * 与えられたレポジトリを経由して自身を更新します。
	 * @param rep 永続化の際に利用する関連{@link JpaRepository}
	 */
	public T update(final JpaRepository rep) {
		rep.update(this)
	}

	/**
	 * 与えられたレポジトリを経由して自身を物理削除します。
	 * @param rep 永続化の際に利用する関連{@link JpaRepository}
	 */
	T delete(final JpaRepository rep) {
		rep.delete(this)
	}

	/**
	 * 与えられたレポジトリを経由して自身を新規追加または更新します。
	 * @param rep 永続化の際に利用する関連{@link JpaRepository}
	 */
	T saveOrUpdate(final JpaRepository rep) {
		rep.saveOrUpdate(this)
	}

}
