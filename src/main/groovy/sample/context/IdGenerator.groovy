package sample.context

import java.util.concurrent.atomic.AtomicLong

/**
 * ID生成用のユーティリティコンポーネント。
 * <p>IDフォーマットが必要なケースで利用してください。
 * low: サンプルなのでメモリベースですが、実際は永続化の必要性があるためDBに依存します。
 *
 * @author jkazama
 */
@StaticComponent
class IdGenerator {

	private Map<String, AtomicLong> values = new HashMap<>()

	/** IDキーに応じたIDを自動生成します。 */
	String generate(String key) {
		switch (key) {
			case "CashInOut":
				return formatCashInOut(nextValue(key))
			default:
				throw new IllegalArgumentException("サポートされない生成キーです [$key]");
		}
	}

	//low: 実際は固定桁数化や0パディング含むちゃんとしたコード整形が必要です。
	private String formatCashInOut(long v) {
		"CIO$v"
	}

	private synchronized long nextValue(String key) {
		if (!values.containsKey(key)) values[key] = new AtomicLong(0)
		values[key].incrementAndGet()
	}
}
