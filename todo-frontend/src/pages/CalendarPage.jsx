import React, { useEffect, useState, useContext } from 'react'
import FullCalendar from '@fullcalendar/react' // カレンダー本体
import dayGridPlugin from '@fullcalendar/daygrid' // 月間ビュー
import timeGridPlugin from '@fullcalendar/timegrid' // 週間／日間のタイムグリッドビュー
import interactionPlugin from '@fullcalendar/interaction'
import { useTodos } from '../hooks/useTodos' // 既存の Hook 層から
import { useCategories } from '../hooks/useCategories'
import { useTags } from '../hooks/useTags'
import TodoDrawer from '../components/TodoDrawer'
import LoadingSpinner from '../components/LoadingSpinner'
import { AuthContext } from '../context/AuthContext'
import { canModifyTodo } from '../utils/permissionUtils'

export default function CalendarPage() {
  const { user } = useContext(AuthContext);  // 現在のログインユーザー情報
  // Hook から必要なメソッドを取得
  const { todos, loading: todosLoading, error: todosError, create, update } = useTodos()
  const { categories, loading: catsLoading, error: catsError } = useCategories()
  const { tags, loading: tagsLoading, error: tagsError } = useTags()

  const [events, setEvents] = useState([])
  // Drawer の制御
  const [openDrawer, setOpenDrawer] = useState(false)
  const [drawerInitialValues, setDrawerInitialValues] = useState({})


  // todos → FullCalendar イベントオブジェクト（1つの予定を表す JavaScript オブジェクト）にマッピング
  useEffect(() => {
    if (!todosLoading && todos) {
      const evts = todos
        .filter(t => t.dueDate) // 期限日があるタスクだけ
        .map(t => ({
          id:       String(t.id),
          title:    t.title,
          start:    t.dueDate,  // イベント開始日時：タスクの期日（dueDate）
          allDay:   true,       // 24時間（終日）イベントとしてカレンダーの「上部の全日スペース」に表示
          backgroundColor: t.done ? '#ccc' : undefined, 
        }))
      setEvents(evts)
    }
  }, [todos, todosLoading])

  if (todosLoading || catsLoading || tagsLoading) return <LoadingSpinner />
  if (todosError || catsError || tagsError)   return <div className="text-red-500">{todosError?.message || catsError?.message || tagsError?.message}</div>

   // カレンダーの日付クリック（新規作成モード）
  const handleDateClick = info => { // infoは FullCalendar が dateClick イベントを発火させたときに自動で渡すオブジェクト（クリックした日付や関連情報を含む）
    setDrawerInitialValues({ 
      dueDate: info.dateStr,
      repeatType: 'NONE',
     }) // クリックした日付を dueDate に設定
    setOpenDrawer(true)
  }

  // イベントクリック（編集モード）
  const handleEventClick = info => {  
    const todo = todos.find(t => String(t.id) === info.event.id) // info.event.id: カレンダー上でクリックしたタスクの ID
    if (!todo) return
    // 権限チェック
    if (!canModifyTodo(todo, user)) {
      alert('編集権限がありません');
      return;
    }
    setDrawerInitialValues(todo)
    setOpenDrawer(true)
  }

  // Drawer 内フォームの submit ハンドラ
  const handleSubmit = async payload => {
    if (drawerInitialValues.id) {
      // 編集モード
      await update(drawerInitialValues.id, payload)
    } else {
      // 新規作成モード
      await create(payload)
    }
  }

  return (
    <div className="max-w-4xl mx-auto">
      <h2 className="text-2xl mb-4">タスクカレンダー</h2>
      <FullCalendar
        plugins={[ dayGridPlugin, timeGridPlugin, interactionPlugin ]}
        initialView="dayGridMonth"
        // 左に前後移動／今日、右に「月間」「週間」切替ボタン
        headerToolbar={{
          left:  'prev,next today',
          center:'title',
          right: 'dayGridMonth,timeGridWeek'
        }}
        views={{
          dayGridMonth: { buttonText: '月間' },
          timeGridWeek: { buttonText: '週間' },
        }}
        events={events}  // 先ほど作った配列を渡す
        dateClick={handleDateClick} // 日付クリックで新規作成フォームに遷移
        eventClick={handleEventClick} // タスク編集フォームに遷移
      />
      {/* Drawer（新規 or 編集） */}
      <TodoDrawer
        open={openDrawer}
        onClose={() => setOpenDrawer(false)}
        initialValues={drawerInitialValues}
        categories={categories}
        tags={tags}
        onSubmit={handleSubmit}
      />
    </div>
  )
}