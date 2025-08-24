import React from 'react';

export default function Footer() {
  return (
    <footer className="mt-8 py-4 text-center text-sm text-gray-500">
      © {new Date().getFullYear()} My ToDo App. All rights reserved.
    </footer>
  );
}