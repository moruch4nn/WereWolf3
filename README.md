 # WereWolf3
マインクラフトで人狼イベントを開催するために作成された人狼プラグインです。

[Notion](https://moruch4nn.notion.site/Ver-3-RedTownServer-29c1f0d44cf34ebfb36d1ba08b57a0dc) で詳細を見る。

---

## 推奨環境について
このプラグインは1.19.3のSpigotサーバーに導入することを想定して作成されました。

それ以外のバージョンでも動作する可能性はありますがあくまで推奨環境は1.19.3のspigotサーバーです。
## 前提プラグインについて
このプラグインは保守管理を容易にするため複数のプラグイン/ライブラリを使用しています。

推奨のプラグインの導入は必須ではありませんが導入することでスムーズな進行が可能になります。
### 必須プラグイン
1. ProtocolLib: パケット関連のライブラリです。このライブラリを使用することでプラグインは特定のバージョンにとらわれず様々なバージョンに対応で来ます。

### 技術的なお話
このプラグインは保守を楽にするために互換性が失われる可能性があるパケット部分を[src/main/kotlin/dev/mr3n/werewolf3/protocol](src%2Fmain%2Fkotlin%2Fdev%2Fmr3n%2Fwerewolf3%2Fprotocol)にまとめています。<br>
サーバーバージョンを上げた際、互換性に問題がある場合はそのパッケージ内のファイルを修正してください。

---

inspired by TCT(Trouble in crafter town)
