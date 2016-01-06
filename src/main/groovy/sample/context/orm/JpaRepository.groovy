package sample.context.orm

import groovy.transform.CompileStatic;

import java.io.Serializable

import javax.persistence.*

import org.springframework.beans.factory.annotation.Autowired

import sample.context.*
import sample.context.Entity

/**
 * JPAのRepository基底実装。
 * <p>Springが提供するJpaRepositoryとは役割が異なる点に注意してください。
 * 本コンポーネントはRepositoryとEntityの1-n関係を実現するためにSpringDataの基盤を
 * 利用しない形で単純なJPA実装を提供します。
 * <p>JpaRepositoryを継承して作成されるRepositoryの粒度はデータソース単位となります。
 *
 * @author jkazama
 */
@CompileStatic
abstract class JpaRepository implements Repository {

    @Autowired
    private DomainHelper dh
    
    @Override
    DomainHelper dh() {
        dh
    }
    
    /**
     * 管理するEntityManagerを返します。
     * <p>継承先で管理したいデータソースのEntityManagerを返してください。
     */
    abstract EntityManager em()

    def <T extends Entity> JpaCriteria criteria(Class<T> clazz) {
        new JpaCriteria<T>(clazz, em().getCriteriaBuilder())
    }

    /**
     * JPA操作の簡易アクセサを生成します。
     * <p>JpaTemplateは呼出しの都度生成されます。
     */
    JpaTemplate tmpl() {
        new JpaTemplate(em())
    }

    def <T extends Entity> T get(Class<T> clazz, Serializable id) {
        em().find(clazz, id) as T
    }

    def <T extends Entity> T load(Class<T> clazz, Serializable id) {
        em().getReference(clazz, id) as T
    }

    def <T extends Entity> boolean exists(Class<T> clazz, Serializable id) {
        get(clazz, id)
    }

    def <T extends Entity> T getOne(Class<T> clazz) {
        em().createQuery("from " + clazz.getSimpleName()).getSingleResult() as T
    }

    def <T extends Entity> List<T> findAll(Class<T> clazz) {
        em().createQuery("from " + clazz.getSimpleName()).getResultList()
    }

    def <T extends Entity> T save(T entity) {
        em().persist(entity)
        entity as T
    }

    def <T extends Entity> T saveOrUpdate(T entity) {
        em().merge(entity) as T
    }

    def <T extends Entity> T update(T entity) {
        em().merge(entity) as T
    }

    def <T extends Entity> T delete(T entity) {
        em().remove(entity)
        entity
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティを全てDBと同期(SQL発行)します。
     * <p>SQL発行タイミングを明確にしたい箇所で呼び出すようにしてください。バッチ処理などでセッションキャッシュが
     * メモリを逼迫するケースでは#flushAndClearを定期的に呼び出してセッションキャッシュの肥大化を防ぐようにしてください。
     */
    JpaRepository flush() {
        em().flush()
        this
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティをDBと同期化した上でセッションキャッシュを初期化します。
     * <p>大量の更新が発生するバッチ処理などでは暗黙的に保持されるセッションキャッシュがメモリを逼迫して
     * 大きな問題を引き起こすケースが多々見られます。定期的に本処理を呼び出してセッションキャッシュの
     * サイズを定量に維持するようにしてください。
     */
    JpaRepository flushAndClear() {
        em().flush()
        em().clear()
        this
    }
    
}

/** 標準スキーマのRepositoryを表現します。 */
@org.springframework.stereotype.Repository
class DefaultRepository extends JpaRepository {
    @PersistenceContext
    EntityManager em
    @Override
    EntityManager em() {
        em
    }
}
