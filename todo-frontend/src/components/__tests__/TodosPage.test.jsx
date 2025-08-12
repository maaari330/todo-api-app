import React from 'react';
import { render, screen, waitFor, act, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import api from '../../utils/axiosConfig';
import TodosPage from '../TodosPage';
import { normalizeLocalDateTime } from '../../utils/dateUtils';
import { ApiLevel } from '@testing-library/user-event/dist/cjs/utils/index.js';

jest.mock('../utils/axiosConfig'); // axiosConfig モジュール（APIモジュール）をモック

async function setupWithNewTask(title = 'new_task', dueDate= '2025-07-01') {
  const user = userEvent.setup();
  let getCount = 0;
  // URL・回数に関わらず全ての GET をフックして返り値を動的に切り替える
  api.get.mockImplementation((url, { params }) => {
    getCount += 1;
    // マウント時の1回目 → 空リスト
    if (getCount <= 1) {return Promise.resolve( {data: { content: [], number: 0, size: params.size, totalPages: 0, totalElements: 0 }} );}
    // 2回目以降（＝POST のあと、fetchTodos が走るタイミング） → 追加したタイトルと日付を返す
    return Promise.resolve({ data: { content: [{ id: 1, title, dueDate, done: false }], number: params.page, size: params.size, totalPages: 1, totalElements: 1 }});
  });
  await act(async () => { render(<TodosPage />);});
  // title,dueDateなしでgetが呼ばれる（全件検索）のを待つ
  await waitFor(() => expect(api.get).toHaveBeenCalledWith('/todos', { params: { page: 0, size: 10, sort: 'dueDate,asc' } }));
  // POST モック ＆ 再度 GET モック（ピンポイントでの上書きのため）を準備（新規タスク追加後に使用される）
  api.post.mockResolvedValueOnce({ data: [{ id: 1, title, dueDate, done: false }] });
  api.get.mockResolvedValueOnce({data: { content: [{ id: 1, title, dueDate, done: false }], number: 0, size: 10, totalPages: 1, totalElements: 1},});
  const normalized = normalizeLocalDateTime(dueDate);
  // 新規タスクを追加
  const input =await screen.findByPlaceholderText('新しいタスクを入力');
  await user.type(input, title);
  const dateInput = screen.getByTestId('due-date-input');
  await user.type(dateInput, normalized);
  const addBtn =await screen.findByRole('button', { name: '追加' });
  await user.click(addBtn);
  // 画面に追加結果が反映されるのを待つ
  await screen.findByText(title);
  return { user };
}


describe('TodosPage', () => {
  beforeEach(() => {
    // jest.fn()は呼び出し履歴を残すので、毎テスト前にクリアする
    jest.clearAllMocks();
    // setupTests.js内のglobal変数は、テストファイルごとに１回実行されるため、
    // １つのテストファイルに複数のテストがあっても以降は同じglobal変数が共有される
    Notification.calls = [];
    Notification.permission = 'default';
    Notification.requestPermission = jest.fn().mockResolvedValue('granted');
  });

  test('ローディング中は Loading... が表示される', async () => {
    // Arrange: GET が resolve しないようにしてローディング状態を再現
    api.get.mockImplementation(() => new Promise(() => {}));
    render(<TodosPage />);
    // Assert
    expect(await screen.findByText('Loading...')).toBeInTheDocument();
  });

  test('初回ロード時に /todos を呼び出し、結果を表示する', async () => {
    // Arrange
    const mockData = [
      { id: 1, title: 'Task A', dueDate: '2025-07-01', done: false },
      { id: 2, title: 'Task B', dueDate: '2025-07-02', done: true },
    ];
    api.get.mockResolvedValue({ data: {content: mockData, number: 0, size: 10, totalPages: 1, totalElements: mockData.length } });
    // Act
    await act(async () => { render(<TodosPage />); });
    // Assert
    // Actの中でtitle,dueDateなしでgetが呼ばれたかを検証（全件検索）→ Arrangeのモックデータが返却される
    expect(api.get).toHaveBeenCalledWith('/todos',{ params: { page: 0, size: 10, sort: 'dueDate,asc' } });
    // “Task A” が現れるまでデフォルト timeout (1000ms) リトライしてくれる
    expect(await screen.findByText('Task A')).toBeInTheDocument();
    // “Task B” が現れるまでデフォルト timeout (1000ms) リトライしてくれる
    expect(await screen.findByText('Task B')).toBeInTheDocument();
  });


  test('検索ボックス入力後、keyword パラメータ付き GET を呼び出す', async () => {
    // Arrange
    api.get.mockResolvedValue({ data: { content: [], number: 0, size: 10, totalPages: 0, totalElements: 0 } });
    const user=userEvent.setup();
    // Act
    // まず検索語無しで初回が呼ばれるのを待つ
    await act(async () => render(<TodosPage />));
    const input = await screen.findByPlaceholderText('検索キーワードを入力');
    await user.type(input, 'foo');
    // Assert
    // デバウンス 300ms をまたいで待つ
    await waitFor(() => expect(api.get).toHaveBeenLastCalledWith('/todos', { params: { page: 0, size: 10, sort: 'dueDate,asc', keyword: 'foo' } }),{ timeout: 500 });
  });


  test('「完了」ボタン押下で done パラメータ付き GET を呼び出す', async () => {
    // Arrange
    api.get.mockResolvedValue({ data: { content: [], number: 0, size: 10, totalPages: 0, totalElements: 0 } });
    const user=userEvent.setup();
    // Act
    // まずステータスフィルタ無しで初回が呼ばれるのを待つ
    await act(async () => render(<TodosPage />));
    // 「完了」ボタンをクリック
    const doneBtn = await screen.findByRole('button', { name: '完了' });
    await user.click(doneBtn);
    // Assert
    await waitFor(() => expect(api.get).toHaveBeenLastCalledWith('/todos', { params: { page: 0, size: 10, sort: 'dueDate,asc', done: true } }),{ timeout: 500 });
  });


  test('タスクを追加すると postを呼び出し、結果を画面表示する', async () => {
    await setupWithNewTask('new_task', '2025-07-01');
    const normalized = normalizeLocalDateTime('2025-07-01');
    // Assert
    expect(api.post).toHaveBeenCalledWith('/todos',{title:'new_task',done:false, dueDate:normalized});
    expect(api.get).toHaveBeenLastCalledWith('/todos', { params: { page: 0, size: 10, sort: 'dueDate,asc' } });
    // 新しいタスクが画面に描画されるのを待って検証
    expect(await screen.findByText('new_task')).toBeInTheDocument();
  });


  test('タスクを編集すると putを呼び出し、結果を画面表示する', async () => {
    const { user } = await setupWithNewTask('new_task','2025-07-01');
    // 新しいタスクが画面に描画される
    await screen.findByText('new_task');
    const editBtn = screen.getByTitle('タスクを編集');
    await user.click(editBtn);
    // 編集用 input が出現するのを待ち、タイトルを変更
    const titleInput = await screen.findByDisplayValue('new_task');
    await user.clear(titleInput);
    await user.type(titleInput, 'fixed_task');
    // 期限を変更
    const title = await screen.findByDisplayValue('fixed_task')
    const li = title.closest('li');
    const dateInput =  li.querySelector('input[type="datetime-local"]');
    await user.clear(dateInput);
    const normalizedNew = normalizeLocalDateTime('2025-07-30');
    await user.type(dateInput, normalizedNew);
    // 保存ボタンをクリック
    // PUT 後に再度 GET される想定のモック
    api.put.mockResolvedValueOnce({ data: { id: 1, title: 'fixed_task', dueDate: normalizedNew, done: false } });
    api.get.mockResolvedValueOnce({ data: { content: [{ id: 1, title: 'fixed_task', dueDate: normalizedNew, done: false }], number: 0, size: 10, totalPages: 1, totalElements: 1}});
    const saveBtn = screen.getByTitle('変更を保存');
    await act(async () => user.click(saveBtn));
    // Assert    
    expect(api.put).toHaveBeenCalledWith('/todos/1',{ title: 'fixed_task', dueDate: normalizedNew });
    expect(api.get).toHaveBeenLastCalledWith('/todos', { params: { page: 0, size: 10, sort: 'dueDate,asc' } });
    expect(await screen.findByText('fixed_task')).toBeInTheDocument();
    expect(await screen.findByText(normalizedNew)).toBeInTheDocument();
  });


  test('タスクを削除すると deleteを呼び出し、結果を画面表示する', async () => {
    const { user }= await setupWithNewTask('footask');
    // 編集用 input が出現するのを待ち、その中身を削除
    const newItem = await screen.findByText('footask');
    expect(newItem).toBeInTheDocument();
    // 削除前のモック準備
    api.delete.mockResolvedValueOnce({ data: { id: 1, title: 'footask', dueDate: '2025-08-01', done: false } });
    // 削除後は空配列
    api.get.mockResolvedValueOnce({ data: { content: [], number: 0, size: 10, totalPages: 0, totalElements: 0 } });
    const deleteBtn = screen.getByTitle('タスクを削除');
    await user.click(deleteBtn);
    // Assert
    expect(api.delete).toHaveBeenCalledWith('/todos/1');
    expect(api.get).toHaveBeenLastCalledWith('/todos', { params: { page: 0, size: 10, sort: 'dueDate,asc' } });
    await waitFor(() => {expect(screen.queryByText('footask')).toBeNull();});
  });


  test('タスクを完了に変更すると patchを呼び出し、結果を画面表示する', async () => {
    const { user }= await setupWithNewTask('footask', '2025-08-01');
    const newItem = await screen.findByText('footask');
    expect(newItem).toBeInTheDocument();
    // 更新前のモック準備
    api.patch.mockResolvedValueOnce({ data: { id: 1, title: 'footask', dueDate: '2025-08-01', done: true } });
    api.get.mockResolvedValueOnce({ data: { content: [{ id: 1, title: 'footask', dueDate: '2025-08-01', done: true }], number: 0, size: 10, totalPages: 1, totalElements: 1 } });
    const checkbox = await screen.findByRole('checkbox');
    await act(async () => user.click(checkbox));
    // Assert
    expect(api.patch).toHaveBeenCalledWith('/todos/1',{ done: true });
    expect(api.get).toHaveBeenLastCalledWith('/todos', { params: { page: 0, size: 10, sort: 'dueDate,asc' } });
    await screen.findByRole('checkbox', { checked: true })
  });
});