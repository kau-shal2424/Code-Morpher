import React from 'react';
import Editor from '@monaco-editor/react';

const EditorView = ({ code, setCode, language, onEditorMount, readOnly }) => {
  return (
    <div className="editor-container" style={{ height: '520px' }}>
      <Editor
        height="100%"
        language={language}
        theme="vs-dark"
        value={code}
        onChange={(val) => {
          if (!readOnly && setCode) {
            setCode(val || '');
          }
        }}
        onMount={onEditorMount}
        options={{
          readOnly: readOnly,
          minimap: { enabled: false },
          fontSize: 14,
          fontFamily: "'Fira Code', 'Cascadia Code', Consolas, Monaco, monospace",
          fontLigatures: true,
          scrollBeyondLastLine: false,
          automaticLayout: true,
          cursorBlinking: 'smooth',
          cursorSmoothCaretAnimation: 'on',
          lineNumbersMinChars: 3,
          padding: { top: 12, bottom: 12 },
          renderLineHighlight: 'all',
          scrollbar: {
            verticalScrollbarSize: 10,
            horizontalScrollbarSize: 10
          }
        }}
      />
    </div>
  );
};

export default EditorView;
