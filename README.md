sbchain
=============

ブロックチェーンによる通貨取引システム

## 説明
`sbchain`はブロックチェーンを用いて暗号通貨の取引を管理するシステムです。このAPIはRESTアーキテクチャ
設計です。

## デモ

以下のURLにアプリケーションを公開しています。※デモアプリケーションであるため実際の通貨は扱いません。
- <https://murmuring-depths-71832.herokuapp.com>
## 要件
- Java 11
- Maven 4.0.0
- Bouncy Castle 1.57
- Gson 2.8.6

## 使用方法
このAPIは以下のパスでリクエストを受け付けます。

| パス           | HTTPメソッド | 内容                                                    |
| ------------- |-------------|--------------------------------------------------------|
| /info         | GET         | ブロックチェーンの基本情報を提供します。                      |
| /balance      | GET         | 所持している残高を提供します。                              |
| /purchase     | POST        | 通貨を購入リクエストを受理します。                           |
| /transaction  | POST        | 通貨を送金リクエストを受理します。                           |
| /chain        | GET         | ブロックチェーンに格納済みの取引データ（ブロック）を提供します。  |
| /pool         | GET         | ブロックチェーンに未格納の取引データを提供します。              |

各パスへの詳細なリクエスト方法は[Javadoc](apidocs/)の `SBChainServer.java` クラスのドキュメント
に記載されていますのでそちらをご覧ください。

このAPIを使用するためには「ウォレット」を所持している必要があります。ウォレットを作成して通貨の取引を
ブラウザから行うことができるWebアプリケーションを以下に公開していますのでこちらもご参照ください。

- GitHub: <https://github.com/shungonk/wallet_app>
- Heroku: <https://frozen-beyond-11362.herokuapp.com>

## ローカルでの起動
1. jdk-11, mavenをインストールします。

2. リポジトリのクローンをローカルに作成します。
    ```console
    $ git clone https://github.com/shungonk/sbchain.git
    ```

3. リポジトリのディレクトリに移動し、mavenでビルドします。
    ```console
    $ cd sbchain
    $ mvn clean install -DskipTests=true
    ```

4. 環境変数`PORT`を設定し、jarファイルを実行します。
    ```console
    $ export PORT=5000
    $ java -jar target/sbchain-1.0-jar-with-dependencies.jar
    ```

## ドキュメント
[Javadoc](apidocs/)

## 作者
[shungonk](https://github.com/shungonk)

## 参考情報
<https://medium.com/programmers-blockchain/blockchain-development-mega-guide-5a316e6d10df>
