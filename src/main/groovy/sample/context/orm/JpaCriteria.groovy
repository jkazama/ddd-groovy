package sample.context.orm

import groovy.transform.CompileStatic;

import javax.persistence.criteria.*

import sample.context.Entity

/**
 * JPAのCriteriaBuilderラッパー。
 * <p>Criteriaの簡易的な取り扱いを可能にします。
 * low: 必要最低限の処理を割り切りで実装
 *
 * @author jkazama
 */
@CompileStatic
class JpaCriteria<T extends Entity> {
    final CriteriaBuilder cb
    final CriteriaQuery<T> query
    final Root<T> root
    final Set<Predicate> predicates = new LinkedHashSet<>()
    final Set<Order> orders = new LinkedHashSet<>()

    JpaCriteria(Class<T> clazz, CriteriaBuilder cb) {
        this.cb = cb
        this.query = cb.createQuery(clazz)
        this.root = query.from(clazz)
    }

    /** null一致条件を付与します。 */
    JpaCriteria<T> isNull(String field) {
        predicates.add(cb.isNull(root.get(field)))
        this
    }

    /** 一致条件を付与します。 */
    JpaCriteria<T> equal(String field, final Object value) {
        if (value) predicates.add(cb.equal(root.get(field), value))
        this
    }

    /** like条件を付与します。 */
    // low: 本番で利用する際はエスケープポリシーを明確にする必要あり。HibernateのMatchMode的なアプローチが安全
    JpaCriteria<T> like(String field, String value) {
        if (value) predicates.add(cb.like(root.get(field) as Expression<String>, value))
        this
    }

    /** in条件を付与します。 */
    JpaCriteria<T> inValues(String field, final Object[] values) {
        if (values && 0 < values.length) predicates.add(root.get(field).in(values))
        this
    }

    /** between条件を付与します。 */
    JpaCriteria<T> between(String field, Date from, Date to) {
        if (from && to) predicates.add(cb.between(root.<Date> get(field), from, to))
        this
    }

    /** between条件を付与します。 */
    JpaCriteria<T> between(String field, String from, String to) {
        if (from && to) predicates.add(cb.between(root.<String> get(field), from, to))
        this
    }

    /** 昇順条件を加えます。 */
    JpaCriteria<T> sort(String field) {
        orders.add(cb.asc(root.get(field)))
        this
    }

    /** 降順条件を加えます。 */
    JpaCriteria<T> sortDesc(String field) {
        orders.add(cb.desc(root.get(field)))
        this
    }

    /** 実行クエリを生成して返します。 */
    CriteriaQuery<T> result() {
        def q = query.where(predicates as Predicate[])
        orders.isEmpty() ? q : q.orderBy(orders as Order[])
    }

}
