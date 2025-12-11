# 猫コレクター概要
- Spring AIを利用して、テキストと画像から「猫」かどうかを判定
- 画像判定はビジョン対応モデルで実施（既存のAI同様の猫検出）
- 指定したURLをクロールして画像を取得
- 猫でない画像を自動で除外し、猫画像のみを収集
- 収集した猫画像からAIでプロフィール（name／profile／color）を生成
- 重複画像をMD5ハッシュで検出し排除
- 収集結果を静的サイトとして自動生成
    - index.html に猫一覧を表示
    - 各猫の詳細ページ（画像とプロフィール）を作成
- 一度の起動で収集とサイト生成を実行（定期実行へ拡張可能）

## 実行時の処理
- 対象サイトをクロールして画像を解析
- altや周辺テキストによる予備判定 （ビジョン対応モデル）
- 猫画像に対してプロフィールを生成
- 画像は images、ページは pages と cats に出力
- 判定や生成のログを記録して追跡可能

## 設定
- 収集対象URLは TargetUrlsProps／TargetUrls にバインドされたプロパティで指定
- 出力先ディレクトリは cat\-collector.output\-dir で指定

## 要件
- Java／Spring Boot／Gradle
- ビジョン入力を扱えるSpring AIモデルの設定
- Windowsおよび標準JVM環境で動作

## 実行結果
著作権フリーサイトの猫画像を収集し、以下のような静的サイトを生成できました。
収集は個人の範囲でお使いください。
![cat-list.png](/img/cat-list.png)
![cat1.png](/img/cat1.png)
![cat2.png](/img/cat2.png)
![cat3.png](/img/cat3.png)
![cat4.png](/img/cat4.png)