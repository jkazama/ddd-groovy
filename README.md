ddd-groovy
---

## はじめに

[ddd-java](https://github.com/jkazama/ddd-java)のGroovy版サンプル実装です。  

本サンプルでは[SpringBoot](http://projects.spring.io/spring-boot/)と[Groovy](http://groovy.codehaus.org/)を利用してドメインモデリングの実装例を示します。SpringBootは1.2、Groovyは2.4を利用しています。  
※JavaDocに記載をしていますが、サンプルに特化させているので実際の製品コードが含まれているわけではありません。  

### レイヤリングの考え方

オーソドックスな三層モデルですが、横断的な解釈としてインフラ層を考えます。

- UI層 - ユースケース処理を公開(必要に応じてリモーティングや外部サイトを連携)
- アプリケーション層 - ユースケース処理を集約(外部リソースアクセスも含む)
- ドメイン層 - 純粋なドメイン処理(外部リソースに依存しない)
- インフラ層 - DIコンテナやORM、各種ライブラリ、メッセージリソースの提供

UI層の公開処理は異なる種類のクライアント利用を想定してRESTfulAPIでの公開を前提とします。(API利用前提のサーバ解釈)

### SpringBootの利用方針

SpringBootは様々な利用方法が可能ですが、本サンプルでは以下のポリシーを用います。

- DBの設定等、なるべく標準定義をそのまま利用する。
- 設定ファイルはymlを用いる。Bean定義にxml等の拡張ファイルは用いない。
- ライブラリ化しないので@Beanによる将来拡張性を考慮せずにクラス単位でBeanベタ登録。
- 例外処理は終端(RestErrorAdvice/RestErrorCotroller)で定義。whitelabel機能は無効化。
- サンプル用途しかないため、色々と前提置きが必要なProfileは利用しない。

### コーディング方針

Groovy2.3以上を前提としています。(JDKは7以上を想定)

- Better Java 的な位置づけで利用
- できるだけCompileStaticを利用
- アノテーションが過剰にならないようにAnnotationCollectorを利用
- 名称も既存クラスと重複しても良いのでなるべく簡潔に
- インターフェースの濫用をしない

### パッケージ構成

パッケージ/リソース構成については以下を参照してください。

```
main
  groovy
    sample
      context                         … インフラ層
      controller                      … UI層
      model                           … ドメイン層
      usecase                         … アプリケーション層
      util                            … 汎用ユーティリティ
      - Application.java              … 実行可能な起動クラス
  resources
    - application.yml                 … 設定ファイル
    - messages-validation.properties  … 例外メッセージリソース
    - messages.properties             … メッセージリソース
```

## サンプルユースケース

サンプルユースケースとしては以下を想定します。

- **口座残高100万円を持つ顧客**が出金依頼(発生 T, 受渡 T + 3)をする。
- **システム**が営業日を進める。
- **システム**が出金依頼を確定する。(確定させるまでは依頼取消行為を許容)
- **システム**が受渡日を迎えた入出金キャッシュフローを口座残高へ反映する。

## 動作確認

サンプルはGradleを利用しているので、IDEやコンソールで動作確認を行うことができます。

### STS(Eclipse)

開発IDEである[STS](https://spring.io/tools/sts)で本サンプルを利用するには、事前に以下の手順を行っておく必要があります。
※EclipseにSpringIDEプラグインを入れても可

- JDK7以上のインストール
- Gradleプラグインのインストール
- Groovyプラグインのインストール

次の手順で本サンプルをプロジェクト化してください。  
※コンパイルエラーになる時は、Javaコンパイラの設定が1.7以上になっているかを確認してください。

1. パッケージエクスプローラから「右クリック -> Import」で*Gradle Project*を選択して*Next*を押下
1. *Root folder:*にダウンロードした*ddd-groovy*ディレクトリを指定して*Build Model*を押下
1. *Project*で*ddd-groovy*を選択後、*Finish*を押下(依存ライブラリダウンロードがここで行われます)

次の手順で本サンプルを実行してください。

1. *Application.groovy*に対し「右クリック -> Run As -> Java Application」
1. *Console*タブに「Started Application」という文字列が出力されればポート8080で起動が完了

※Groovyプラグインのバージョンによっては[GROOVY-7278](https://jira.codehaus.org/browse/GROOVY-7278)の不具合に引っかかるので注意してください。

### コンソール

Windows/Macのコンソールから実行するにはGradleのコンソールコマンドで行います。  
※事前にJDK7以上のインストールが必要です。

1. ダウンロードした*ddd-groovy*ディレクトリ直下へコンソールで移動
1. 「gradlew bootRun」を実行
1. コンソールに「Started Application」という文字列が出力されればポート8080で起動が完了

### ブラウザ

STSまたはコンソールで8080ポートでサーバを立ち上げた後、ブラウザから下記URLへアクセスする事でRESTfulAPIの実行テストを実施可能です。  
※本来なら情報更新系処理はPOSTで取り扱うべきですが、UIの無いデモ用にGETでのアクセスを許容しています。  
※パラメタは?key=valueで繋げて渡してください。

顧客向けユースケース

- http://localhost:8080/asset/cio/withdraw  
振込出金依頼 [accountId: sample, currency: JPY, absAmount: 出金依頼金額]
- http://localhost:8080/asset/cio/unprocessedOut  
振込出金依頼未処理検索

社内向けユースケース

- http://localhost:8080/admin/asset/cio  
振込入出金依頼検索 [updFromDay: 更新From(yyyyMMdd), updToDay: 更新To(yyyyMMdd)]

バッチ向けユースケース

- http://localhost:8080/system/job/daily/processDay  
営業日を進める(単純日回しのみ)
- http://localhost:8080/system/job/daily/closingCashOut  
当営業日の出金依頼を締める
- http://localhost:8080/system/job/daily/realizeCashflow  
入出金キャッシュフローを実現する(受渡日に残高へ反映)


## License

本サンプルのライセンスはコード含めて全て*MIT License*です。  
Spring Bootを用いたプロジェクト立ち上げ時のベース実装サンプルとして気軽にご利用ください。

