todo-api-app サイト解説

      このリポジトリは Todo タスク管理アプリケーション の実装です。バックエンドは Spring Boot、フロントエンドは React/TypeScript で実装しており、Docker Compose で起動できます。
    ユーザー登録・JWT 認証、カテゴリ／タグ管理、繰り返しタスク、期日・リマインダー、カレンダー表示、Web Push/In App 通知などの機能を備えています。
    以下では構成やセットアップ方法、主な API エンドポイント、フロントエンドの機能について解説します。

    リポジトリ構成
    ├─ todo-api/            # Spring Boot バックエンド
    │  ├─ src/main/java/com/example/todoapi/…
    │  ├─ src/main/resources/application.yml
    │  └─ Dockerfile
    ├─ todo-frontend/       # React フロントエンド
    │  ├─ src/
    │  ├─ public/
    │  └─ package.json
    ├─ mysql/               # MySQL 用 Dockerfile
    ├─ docker-compose.yml   # DB/API/フロントの統合起動
    └─ .github/workflows/   # CI 設定

 バックエンド (todo-api) 
 
    Spring Boot 3 (Java 17) と Spring Data JPA を用いた RESTful API。タスクやカテゴリ、タグを管理するエンドポイントを提供します。
    
    Todo エンティティはタイトル、完了フラグ、期限、繰り返し種別、カテゴリ、タグ、リマインダー時刻等を保持。
    繰り返し種別は NONE/DAILY/WEEKLY/MONTHLY から選択できます。
    
    ユーザー認証は JWT を使用しており、/auth/signup で登録、/auth/login でトークン取得、/auth/me で現在のユーザー情報を取得します。
    ユーザーは ROLE_USER または ROLE_ADMIN のロールを持ちます。
    
    Flyway による DB マイグレーション、Spring Security、OpenAPI/Swagger UI などを利用しています。
    環境変数は application.yml で .env ファイルから取り込まれます。
    
    通知機能では、スケジュールジョブが一定間隔で期日の近いタスクを検出し、Web Push（VAPID）とアプリ内通知（In App）を送信します。
    ユーザー別に最新通知を取得する /notifications/in-app/recent エンドポイントが用意されています。
    
    ・　主な API エンドポイント
    パス	　　　　　　　　　メソッド	　　　　　　　概要
    /todos	　　　　　　　　　　　GET	　　　　　　　　　  タスク一覧の取得。タイトル検索や完了状態、カテゴリID、タグID 等でフィルタリング可能。
    /todos	　　　　　　　　　　　POST	　　　　　　　タスクの作成。タイトル、期限、繰り返し種別、カテゴリ・タグ ID、リマインダー などを JSON で送信。
    /todos/{id}	　　　　　　　 PUT	　　　　　　　　　   既存タスクの更新。
    /todos/{id}	　　　　　　　 DELETE	　　　　　　　タスクの削除。
    /todos/{id}	　　　　　　　 PATCH	　　　　　　　完了フラグのトグル。
    /categories	　　　　　　　 GET/POST/PUT/DELETE	 カテゴリの一覧取得・作成・更新・削除。タスクに紐づくカテゴリは削除不可。
    /tags	　　　　　　　　　　 GET/POST/PUT/DELETE	 タグの一覧取得・作成・更新・削除。タスクに紐づくタグは削除不可。
    /auth/signup	　　　　　　   POST	　　　　　　　新規ユーザー登録。
    /auth/login	　　　　　　　 POST	　　　　　　　ユーザー名・パスワードから JWT トークンを取得。
    /auth/me	　　　　　　　 GET	　　　　　　　　    現在のユーザー情報を返却。※Authorization ヘッダーに Bearer <token> を付与する必要がある。
    /api/push/public-key	　GET	  　　　　　　　    Web Push の公開鍵 (VAPID) を取得。
    /api/push/subscribe	        POST/DELETE	       Push 購読を登録・解除。
    /notifications/in-app/recent  GET	             最新のアプリ内通知を取得。


 フロントエンド (todo-frontend) 
 
    React 18 と Tailwind CSS を用いた SPA。
    create-react-app で作成しており、エントリポイントは src/App.js です。
    AuthContext を通じて認証状態を管理し、React Router DOM で画面遷移します。
    
    ログイン／サインアップページ、タスク一覧ページ、カテゴリ管理ページ、タグ管理ページ、カレンダー表示ページ、通知設定ページがあります。
    タスク一覧ページ では、タイトル・ステータス・カテゴリ・タグ別にタスクをフィルターでき、ページネーション表示となっています。編集・削除の権限チェックも入ります。
    
    通知設定ページでは購読を切り替えるコンポーネントを提供します。
    
    src/utils/axiosConfig.ts で Axios インスタンスを生成し、API のベース URL を .envで管理し、 /api に自動設定して JWT トークンを自動付与します。
    Docker 環境では Nginx が /api パスをバックエンドにリバースプロキシします。

セットアップ方法
      0. 前提条件
      Docker Desktop　と Docker Compose が動作する環境
      
      1. リポジトリをクローンします。
      git clone https://github.com/maaari330/todo-api-app.git
      cd todo-api-app
      
      2. .env の作成
      アプリを起動する前にリポジトリのルートに .env ファイルを作成し、次のような環境変数を設定します（必要に応じて変更してください）。
            MYSQL_ROOT_PASSWORD=your_root_password
            MYSQL_DATABASE=todo
            SPRING_DATASOURCE_USERNAME=root
            SPRING_DATASOURCE_PASSWORD=your_root_password
      JWT 秘密鍵と有効期限（ミリ秒）。例では 24 時間を指定
            JWT_SECRETKEY=YourRandomJwtSecretKey
            JWT_EXPIRATION=86400000
      Actuator 用 Basic 認証（Swagger/健康チェック用）
            ACTUATOR_USER=admin
            ACTUATOR_PASSWORD=adminpass
      スケジューラのスレッドプールサイズと通知ジョブ間隔（ミリ秒）
            SCHEDULER_POOL_SIZE=5
            NOTIFY_SCAN_MS=60000
      Web Push (VAPID) キー。`web-push` パッケージ等で生成できます
            VAPID_PUBLIC_KEY=YourPublicKey
            VAPID_PRIVATE_KEY=YourPrivateKey
            VAPID_SUBJECT=mailto:you@example.com
       (オプション) フロントエンドが直接 API にアクセスする場合の URL
      　　※Nginxが/api→app:8080へプロキシする構成となっているため、オプションにしています。
            REACT_APP_API_URL=http://localhost:8080
      
      3. Docker Compose で起動する
      上記 .env ファイルを用意したら、Docker Compose でサービスを起動します。
      docker compose up -d
      
      docker-compose.yml では MySQL とバックエンド、フロントエンドをそれぞれ db、app、web サービスとして定義し、.env で設定した環境変数を読み込みます。
      バックエンドは、MySQL がヘルスチェックを通過して正常稼働になるまで起動を待機し、フロントエンドは Nginx でポート 80 から配信されます。
      
      4. 起動後、次の URL にアクセスして動作を確認します。
            フロントエンド: http://localhost:3000
            バックエンド API: http://localhost:8080
            Swagger UI: http://localhost:8080/swagger-ui.html

個別にビルド／実行する場合

Docker を使わずにローカルで動かすことも可能です。

バックエンド

Java 17 と Maven を用意します。

todo-api ディレクトリで依存関係を解決し JAR を作成します。

cd todo-api
mvn clean package


.env で指定したデータベースに接続できるよう環境変数を設定し、以下のように JAR を起動します。

export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/todo
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=your_root_password
# その他 JWT_SECRETKEY 等も同様に環境変数として設定
java -jar target/todo-api-*.jar

フロントエンド

Node.js 18+ と npm を用意します。

todo-frontend ディレクトリで依存パッケージをインストールします。

cd todo-frontend
npm ci


開発サーバを起動してブラウザで確認します。

npm start


REACT_APP_API_URL 環境変数を .env またはコマンドラインで指定すると、API のベース URL を変更できます。

開発・テスト

バックエンドは JUnit でテストされています。mvn test で実行できます。

フロントエンドは npm test で Jest/React Testing Library のテストを実行できます。CI では JSON/YAML の lint やユニットテストを行い、Docker イメージをビルドして GitHub Container Registry へプッシュするワークフローが設定されています
github.com
。

注意点

本リポジトリは学習用のサンプルです。JWT 秘密鍵や DB パスワードなどは外部に公開しないでください。

.env ファイルには多くの変数がありますが、不要な変数は削除しても動作に影響しません。通知機能を使用する場合は Web Push (VAPID) キーを必ず設定してください。


以上がこのリポジトリの概要とセットアップ手順です。Docker を使えば簡単に環境構築ができるので、ぜひ試してみてください。問題があれば Issues 等で報告してください。
