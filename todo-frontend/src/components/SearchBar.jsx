import React, { useState, useEffect } from 'react';

export default function SearchBar({ value, onChange }) {
  const [innerValue, setInnerValue] = useState(value ?? ''); // 検索欄への文字表示用
  const [isComposing, setIsComposing] = useState(false);

  useEffect(() => {
    setInnerValue(value ?? '');
  }, [value]);

  const handleChange = (e) => {
    const v = e.target.value;
    setInnerValue(v);
    if (!isComposing) {
      onChange(v);  // かな入力が確定したらtodoタスク検索結果表示に渡す
    }
  };

  const handleCompositionStart = () => {
    setIsComposing(true);
  };

  const handleCompositionEnd = (e) => {
    const v = e.target.value;
    setIsComposing(false);
    setInnerValue(v);
    onChange(v);
  };

  return (
    <div className="flex justify-center mb-4">
      <input
        type="text"
        placeholder="検索キーワードを入力"
        value={innerValue}
        onChange={handleChange}
        onCompositionStart={handleCompositionStart}
        onCompositionEnd={handleCompositionEnd}
        className="w-1/2 max-w-xs border rounded px-3 py-2"
      />
    </div>
  );
}