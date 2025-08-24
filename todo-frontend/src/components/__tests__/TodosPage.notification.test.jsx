import React from 'react';
import { render, screen, act, waitFor } from '@testing-library/react';
import TodosPage from '../TodosPage';
import { advanceTo, advanceBy, clear } from 'jest-date-mock';
import api from '../../utils/axiosConfig';

jest.mock('../utils/axiosConfig'); // axiosConfig モジュール（APIモジュール）をモック
jest.useFakeTimers();

describe('通知機能', () => {
  beforeEach(() => {
    const { default: api } = require('../utils/axiosConfig');
    api.get.mockReset();
    api.get.mockResolvedValue({ data: [] });
    // テスト間で記録をクリア
    Notification.calls.length = 0;
    Notification.permission = 'default';
    Notification.requestPermission.mockClear();
  });

  afterAll(() => {
    jest.useRealTimers();
    clear();
  });

  it('マウント時に requestPermission を呼ぶ', async () => {
    await act(async () => {
      render(<TodosPage />);
    });
    await waitFor(() => {
      expect(Notification.requestPermission).toHaveBeenCalled();
    });
  });


  it('granted のとき、33時間後に通知が出る', async () => {
    jest.setSystemTime(new Date('2025-07-01T00:00:00Z'));
    Notification.permission = 'granted';
    // テスト環境の「現在時刻」を強制的に指定の日付に合わせる
    api.get.mockResolvedValue( {data: {content: [{ id: 1, title: 'Test', dueDate: '2025-07-03', done: false }], number: 0, size: 10, totalPages: 1, totalElements: 1} });
    await act(async () => {
      render(<TodosPage />);
    });
    await screen.findByText('Test');
    act(() => {
      // advanceToの時刻から33時間分進める
      jest.advanceTimersByTime(57 * 60 * 60 * 1000);
    });
    // setupTests.jsの呼び出しログ確認　title,bodyはTodosPage.jsxで定義済
    expect(Notification.calls).toHaveLength(1);
    expect(Notification.calls[0]).toEqual({
      title: '期限通知',
      options: { body: '「Test」の期限は本日です！' }
    });
  });


  it('denied のときは通知を出さない', async () => {
    Notification.permission = 'denied';
    advanceTo(new Date('2025-07-02T08:00:00Z'));
    api.get.mockResolvedValue({
      data: [{ id:2, title:'NoNotify', dueDate:'2025-07-02', completed:false }]
    });

    await act(async () => {
      render(<TodosPage />);
    });

    act(() => {
      advanceBy(2 * 60 * 60 * 1000);
      jest.runOnlyPendingTimers();
    });
    // もしNotification.permissionがgrantedだったら本来は期限当日の通知がログに残る
    // 今回はdeniedなのでログは残らない
    expect(Notification.calls).toHaveLength(0);
  });
});