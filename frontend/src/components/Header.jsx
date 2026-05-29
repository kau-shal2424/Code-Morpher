import React from 'react';
import { Code2, ArrowLeft, Terminal } from 'lucide-react';

const Header = ({ currentView, onNavigate }) => {
  return (
    <header className="header">
      <div className="header-content">
        <div 
          className="logo clickable-logo" 
          onClick={() => onNavigate('home')}
          title="Go to Home Page"
        >
          <Code2 size={32} className="logo-icon" />
          <h1>Code Morpher</h1>
        </div>
        <p>Convert Python to JavaScript, Java, or C++ instantly</p>
        
        <div className="header-nav">
          {currentView === 'editor' ? (
            <button className="btn-nav btn-back-home" onClick={() => onNavigate('home')}>
              <ArrowLeft size={16} />
              <span>Back to Home</span>
            </button>
          ) : (
            <button className="btn-nav btn-to-editor animate-pulse" onClick={() => onNavigate('editor')}>
              <Terminal size={16} />
              <span>Launch Workspace</span>
            </button>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
