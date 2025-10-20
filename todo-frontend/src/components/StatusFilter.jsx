export default function StatusFilter({ value, onChange }) {
  const buttons = [
    { label: '全件', value: 'all' },
    { label: '未完了', value: 'active' },
    { label: '完了',   value: 'done' },
  ];
  return (
    <div className="flex justify-center mb-4 space-x-4">
      {buttons.map(btn => (
        <button
          key={btn.value}
          onClick={() => onChange(btn.value)}
          className={`px-4 py-2 rounded ${value === btn.value ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
        >
          {btn.label}
        </button>
      ))}
    </div>
  );
}