import React from "react";

// page:；現在ページ (0-based)、totalPages：総ページ数、onPageChange：(newPage: number) => void
export default function Pagination( {page, totalPages, onPageChange,}) {
  if (totalPages === 0) return null;
  const prev = () => onPageChange(Math.max(0, page - 1));
  const next = () => onPageChange(Math.min(totalPages - 1, page + 1));

  return (
    <div className="flex justify-center items-center space-x-2 mt-4">
      <button
        onClick={prev}
        disabled={page === 0} //先頭ページ）のときに true を返し、その場合ボタンが disabled 状態になる
        className="px-2 py-1 bg-gray-200 rounded disabled:opacity-50"
      >
        « 前へ
      </button>
      {[...Array(totalPages)].map((_, i) => (
        <button
          key={i}
          onClick={() => onPageChange(i)}
          className={`px-2 py-1 rounded ${i === page ? "bg-blue-500 text-white" : "bg-gray-100"}`}
        >
          {i + 1}
        </button>
      ))}
      <button
        onClick={next}
        disabled={page + 1 >= totalPages}
        className="px-2 py-1 bg-gray-200 rounded disabled:opacity-50"
      >
        次へ »
      </button>
    </div>
  );
}