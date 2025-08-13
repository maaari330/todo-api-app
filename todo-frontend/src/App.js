import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import LoadingSpinner from './components/LoadingSpinner';
import { AuthProvider } from './context/AuthContext';
import { useAuth } from './hooks/useAuth';
import TodosPage from './pages/TodosPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import CategoryPage from './pages/CategoryPage';
import TagPage from './pages/TagPage';
import CalendarPage from './pages/CalendarPage';

// AppWithAuth 以下どこからでも認証状態を AuthContext で参照できる
export default function App() {
  return (
    <AuthProvider>
      <AppWithAuth />
    </AuthProvider>
  );
}

function AppWithAuth() {
  const { user, loading } = useAuth(); // useAuth（useContext） を使用するには上記の AuthProvider ラップが必須。

  // AuthProvider で取得中ならスピナーを表示
  if (loading) return <LoadingSpinner />;

  // URL に応じて表示コンポーネントを切り替える（React Routerを活用）
  return (
    <Router>
      <div className="min-h-screen flex flex-col bg-gray-50">
        <Header />
        <main className="flex-1 container mx-auto px-4 py-6">
          <Routes>
            {/* デフォルトはログイン or Todo にリダイレクト */}
            <Route path="/" element={<Navigate to={user ? "/todos" : "/login"} replace />} />
            {/* ログイン画面 */}
            <Route path="/login" element={user ? <Navigate to="/todos" replace /> : <LoginPage />} />
            {/* サインアップ画面 */}
            <Route path="/signup" element={user ? <Navigate to="/todos" replace /> : <SignupPage />} />

            {/* プライベートルート */}
            {/* Todo一覧、カテゴリ一覧、タグ一覧ページ */}
            <Route path="/todos" element={user ? <TodosPage /> : <Navigate to="/login" replace />} />
            <Route path="/categories" element={user ? <CategoryPage /> : <Navigate to="/login" replace />} />
            <Route path="/tags" element={user ? <TagPage /> : <Navigate to="/login" replace />} />
            <Route path="/calendar" element={user ? <CalendarPage /> : <Navigate to="/login" replace />} />
            {/* 存在しないパスはホームへ */}
            <Route path="*" element={<Navigate to={user ? "/todos" : "/login"} replace />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}