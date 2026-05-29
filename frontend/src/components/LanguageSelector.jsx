import React from 'react';
import './LanguageSelector.css';

const LanguageSelector = ({ selectedLanguage, onLanguageChange }) => {
  const languages = [
    { label: 'JavaScript', value: 'javascript' },
    { label: 'Java', value: 'java' },
    { label: 'C++', value: 'cpp' },
    { label: 'C', value: 'c' },
  ];

  return (
    <div className="language-selector">
      <div className="selector-group source-group">
        <label className="selector-label">Source Language</label>
        <div className="fixed-language">Python</div>
      </div>

      <div className="selector-group target-group">
        <label className="selector-label">Target Language</label>
        <select
          value={selectedLanguage}
          onChange={(e) => onLanguageChange(e.target.value)}
          className="lang-select"
        >
          {languages.map((lang) => (
            <option key={lang.value} value={lang.value}>
              {lang.label}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
};

export default LanguageSelector;
