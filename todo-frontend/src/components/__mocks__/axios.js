// Jest の manual mock として振る舞うため、jestのspy(jest.fn) として実装し、呼び出し履歴を残す。

const axios = {
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn(),
  patch: jest.fn(),
};

module.exports = axios;