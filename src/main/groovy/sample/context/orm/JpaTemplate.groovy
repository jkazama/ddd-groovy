package sample.context.orm

import groovy.transform.CompileStatic

import javax.persistence.*
import javax.persistence.criteria.CriteriaQuery

/**
 * JPAのEntityManagerに対する簡易アクセサ。
 * セッション毎に生成して利用してください。
 * <p>Hibernateへの依存を許容し、2次キャッシュ/クエリキャッシュ含めてきちんとした実装をしたいのであれば
 * SessionFactoryを個別定義してHibernateTemplateを利用してしまう方が直感的に実装できます。
 * ※ここではJPA縛りがある事を前提にこのクラスを作成しています。
 *
 * @author jkazama
 */
@CompileStatic
class JpaTemplate {

    final EntityManager em

    JpaTemplate(final EntityManager em) {
        this.em = em
    }

    /**
     * Criteria検索をします。
     * low: ページング系は省略。実装する時はQuery#setFirstResult/Query#setMaxResultsあたりを利用
     * @param criteria 検索条件
     * @return　検索結果
     */
    def <T extends Entity> List<T> find(CriteriaQuery<T> criteria) {
        return em.createQuery(criteria).getResultList();
    }

    /**
     * JPQL検索をします。
     * low: 引数はMap(名前付き)でやるべきですが、サンプルなので可変引数で割り切り。
     * @param qlName NativeQuery名称
     * @param args　JPQL設定引数。
     * @return　検索結果
     */
    def <T extends Entity> List<T> find(String qlName, Object... args) {
        bind(em.createNamedQuery(qlName), args).getResultList()
    }

    protected Query bind(final Query query, Object... args) {
        args.eachWithIndex { Object v, int idx -> query.setParameter(idx + 1, v) }
        query
    }

    /**
     * JPQL実行をします。
     * @param ql JPQL文字列
     * @param args　JPQLバインド引数。
     * @return　実行件数
     */
    int execute(String qlName, Object... args) {
        bind(em.createNamedQuery(qlName), args).executeUpdate()
    }

    /**
     * SQL検索をします。
     * @param returnClass 戻り値クラス。(フィールド名称とSQLのselectフィールド名称が一致している必要があります)
     * @param sql SQL文字列
     * @param args SQLバインド引数
     * @return 検索結果
     */
    def <T extends Entity> List<T> findBySql(Class<T> returnClass, String sql, Object... args) {
        bind(em.createNativeQuery(sql, returnClass), args).getResultList()
    }

    /**
     * SQL実行をします。
     * @param sql SQL文字列
     * @param args SQLバインド引数
     * @return 実行件数
     */
    int executeSql(String sql, Object... args) {
        bind(em.createNativeQuery(sql), args).executeUpdate()
    }

}
