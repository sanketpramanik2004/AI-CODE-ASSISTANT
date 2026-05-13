import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './styles.css';

const ignoredRuntimeErrors = [
  'ResizeObserver loop completed with undelivered notifications.',
  'ResizeObserver loop limit exceeded'
];

window.addEventListener(
  'error',
  (event) => {
    if (ignoredRuntimeErrors.includes(event.message)) {
      event.stopImmediatePropagation();
    }
  },
  true
);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
