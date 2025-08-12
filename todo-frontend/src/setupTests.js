import '@testing-library/jest-dom';

// テスト環境の初期化

// 完全な Notification モック
class NotificationMock {
  // 呼び出しログ
  static calls = [];
  // permission の状態
  static permission = 'default';
  // requestPermission を常に Promise で返す spy
  static requestPermission = jest.fn(() => {
    NotificationMock.permission = 'granted';
    return Promise.resolve('granted');
  });

  constructor(title, options) {
    NotificationMock.calls.push({ title, options });
  }
}

// グローバルに張り替え
global.Notification = NotificationMock;