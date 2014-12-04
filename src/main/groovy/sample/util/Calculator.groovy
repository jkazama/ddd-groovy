package sample.util

import groovy.transform.CompileStatic

import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicReference

/**
 * 計算ユーティリティ。
 * <p>単純計算の簡易化を目的とした割り切った実装なのでスレッドセーフではありません。
 *
 * @author jkazama
 */
@CompileStatic
final class Calculator {

	private final AtomicReference<BigDecimal> value = new AtomicReference<>()
	/** 小数点以下桁数 */
	private int scale = 0
	/** 端数定義。標準では切り捨て */
	private RoundingMode mode = RoundingMode.DOWN
	/** 計算の都度端数処理をする時はtrue */
	private boolean roundingAlways = false
	/** scale未設定時の除算scale値 */
	private int defaultScale = 18

	private Calculator(Number v) {
		this.value.set(v.toBigDecimal())
	}

	/**
	 * 計算前処理定義。
	 * @param scale 小数点以下桁数
	 */
	Calculator scale(int scl) {
		scale(scl, RoundingMode.DOWN)
	}

	/**
	 * 計算前処理定義。
	 * @param scale 小数点以下桁数
	 * @param mode 端数定義
	 */
	Calculator scale(int scl, RoundingMode mode) {
		this.scale = scl
		this.mode = mode
		this
	}

	/**
	 * 計算前処理定義。
	 * @param roundingAlways 計算の都度端数処理をする時はtrue
	 */
	Calculator roundingAlways(boolean roundingAlways) {
		this.roundingAlways = roundingAlways
		this
	}

	/**
	 * 与えた計算値を自身が保持する値に加えます。
	 * @param v 計算値
	 */
	Calculator add(Number v) {
		add(v.toBigDecimal())
	}

	/**
	 * 与えた計算値を自身が保持する値に加えます。
	 * @param v 計算値
	 */
	Calculator add(BigDecimal v) {
		value.set(rounding(decimal().add(v)))
		this
	}

	private BigDecimal rounding(BigDecimal v) {
		roundingAlways ? v.setScale(scale, mode) : v;
	}

	/**
	 * 自身が保持する値へ与えた計算値を引きます。
	 * @param v 計算値
	 */
	Calculator subtract(Number v) {
		subtract(v.toBigDecimal())
	}

	/**
	 * 自身が保持する値へ与えた計算値を引きます。
	 * @param v 計算値
	 */
	Calculator subtract(BigDecimal v) {
		value.set(roundingAlways ? decimal().subtract(v).setScale(scale, mode) : decimal().subtract(v))
		this
	}

	/**
	 * 自身が保持する値へ与えた計算値を掛けます。
	 * @param v 計算値
	 */
	Calculator multiply(Number v) {
		multiply(v.toBigDecimal())
	}

	/**
	 * 自身が保持する値へ与えた計算値を掛けます。
	 * @param v 計算値
	 */
	Calculator multiply(BigDecimal v) {
		value.set(roundingAlways ? decimal().multiply(v).setScale(scale, mode) : decimal().multiply(v))
		this
	}

	/**
	 * 与えた計算値で自身が保持する値を割ります。
	 * @param v 計算値
	 */
	Calculator divideBy(Number v) {
		divideBy(v.toBigDecimal())
	}

	/**
	 * 与えた計算値で自身が保持する値を割ります。
	 * @param v 計算値
	 */
	Calculator divideBy(BigDecimal v) {
		value.set(roundingAlways ? decimal().divide(v, scale, mode) : decimal().divide(v, defaultScale, mode))
		this
	}

	/**
	 * 計算結果をint型で返します。
	 * @return 計算結果
	 */
	int intValue() {
		decimal().intValue()
	}

	/**
	 * 計算結果をlong型で返します。
	 * @return 計算結果
	 */
	long longValue() {
		decimal().longValue()
	}

	/**
	 * 計算結果をBigDecimal型で返します。
	 * @return 計算結果
	 */
	BigDecimal decimal() {
		value.get()?.setScale(scale, mode) ?: BigDecimal.ZERO
	}

	/**
	 * @return 開始値0で初期化されたCalculator
	 */
	static Calculator init() {
		new Calculator(BigDecimal.ZERO);
	}

	/**
	 * @param v 初期値
	 * @return 初期化されたCalculator
	 */
	static Calculator init(Number v) {
		new Calculator(v)
	}
	
}
