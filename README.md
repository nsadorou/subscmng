# サブスクリプション管理アプリ

## 概要
このアプリは、各種サブスクリプションサービスを管理するためのAndroidアプリケーションです。月額・年額の支払いサイクルを管理し、支払日や有効期限の通知機能を提供します。

## 主な機能

### ✅ 完実装済み機能
- **データベース管理**: Room を使用したローカルデータベース
- **CRUD操作**: サブスクリプションの作成、読取、更新、削除
- **UI画面**: Jetpack Compose による5つの主要画面
  - ホーム画面：サブスクリプション一覧表示
  - 追加・編集画面：新規作成・既存編集
  - 詳細画面：個別サブスクリプション詳細
  - 設定画面：アプリ設定
- **通知機能**: WorkManagerを使用した期限切れ・支払日通知
- **依存性注入**: Hilt による DI 
- **ナビゲーション**: Navigation Compose

### 📊 データモデル
```kotlin
data class Subscription(
    val id: Long = 0,
    val serviceName: String,     // サービス名
    val amount: Double,          // 金額
    val paymentCycle: PaymentCycle, // 支払いサイクル（月額/年額）
    val paymentDay: Int,         // 支払日
    val expirationDate: Date?,   // 有効期限
    val memo: String = "",       // メモ
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
```

### 🛠 技術スタック
- **言語**: Kotlin
- **UIフレームワーク**: Jetpack Compose + Material Design 3
- **データベース**: Room
- **依存性注入**: Hilt
- **バックグラウンド処理**: WorkManager
- **ナビゲーション**: Navigation Compose
- **アーキテクチャ**: MVVM

### 📱 画面構成
1. **ホーム画面** (`HomeScreen`)
   - サブスクリプション一覧表示
   - 月別・年別・タイプ別支出サマリー
   - 検索・フィルタ機能

2. **追加・編集画面** (`AddEditScreen`)
   - 新規サブスクリプション追加
   - 既存サブスクリプション編集
   - フォームバリデーション

3. **詳細画面** (`DetailScreen`)
   - サブスクリプション詳細表示
   - 編集・削除ボタン

4. **設定画面** (`SettingsScreen`)
   - 通知設定
   - アプリ設定

### 🔔 通知機能
- WorkManagerによる定期通知
- 支払日前の事前通知
- 有効期限切れ通知
- カスタマイズ可能な通知設定

## ビルド方法

### 前提条件
- Android Studio Arctic Fox 以降
- Android SDK 24 以降
- Kotlin 1.9.20

### ビルド手順
```bash
# リポジトリをクローン
git clone [リポジトリURL]
cd subscmng

# デバッグAPKをビルド
./gradlew assembleDebug

# リリースAPKをビルド  
./gradlew assembleRelease

# テスト実行
./gradlew check
```

### 生成されるAPK
- デバッグ版: `app/build/outputs/apk/debug/app-debug.apk`
- リリース版: `app/build/outputs/apk/release/app-release.apk`

## 開発状況

### ✅ 完了項目
- [x] プロジェクト基盤設定
- [x] Room データベース実装
- [x] Hilt DI設定
- [x] 全UI画面実装
- [x] ViewModels実装
- [x] 通知システム実装
- [x] ナビゲーション設定
- [x] ビルド設定完了
- [x] APK生成成功

### ⚠️ 改善事項
- アイコンの非推奨警告修正
- WorkManagerの非推奨API更新
- 日付選択UIの改善
- テストケース追加

## ライセンス
このプロジェクトはMITライセンスの下で公開されています。

## 作成者
GitHub Copilot による自動生成プロジェクト
