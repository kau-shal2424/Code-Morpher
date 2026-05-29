import React from 'react';
import { 
  ArrowRight, 
  Terminal, 
  Cpu, 
  Binary, 
  GitFork, 
  Code2, 
  ShieldAlert, 
  Layers, 
  Zap, 
  Sparkles,
  ChevronRight,
  Database
} from 'lucide-react';

const HomePage = ({ onLaunch }) => {
  const teamMembers = [
    { name: 'Asmit Bhandari', initial: 'A', gradient: 'from-pink-500 to-rose-500' },
    { name: 'Harshit Gahlot', initial: 'H', gradient: 'from-purple-500 to-indigo-500' },
    { name: 'Kaushal Thakur', initial: 'K', gradient: 'from-blue-500 to-cyan-500' },
    { name: 'Srishti Rana', initial: 'S', gradient: 'from-emerald-500 to-teal-500' }
  ];

  const features = [
    {
      icon: <Terminal size={24} className="feature-icon-svg text-indigo-400" />,
      title: "Multi-Language Transpilation",
      description: "Instantly translate Python source files into clean, semantic JavaScript, Java, C++, and C with dynamic output code formatting."
    },
    {
      icon: <Cpu size={24} className="feature-icon-svg text-purple-400" />,
      title: "Interactive Token Streams",
      description: "Inspect raw lexical elements token by token. Hovering over a token highlights its exact coordinates and line/column spans in the source editor."
    },
    {
      icon: <Binary size={24} className="feature-icon-svg text-blue-400" />,
      title: "Dual-Engine AST/CST Explorer",
      description: "Visualize parse trees in real-time. Switch between Wetherell-Shannon tree canvas layouts and fully-compacted Concrete Syntax Trees."
    },
    {
      icon: <GitFork size={24} className="feature-icon-svg text-teal-400" />,
      title: "Dependency Graph & Symbol Grid",
      description: "Navigate fully-scalable React Flow graphs representing scope hierarchies and search/filter global and local symbol variables."
    },
    {
      icon: <Zap size={24} className="feature-icon-svg text-amber-400" />,
      title: "Parallel Spring Boot Diagnostics",
      description: "Powered by a multi-threaded parallel compilation engine utilizing Java's CompletableFuture for thread-safe asynchronous analysis."
    },
    {
      icon: <ShieldAlert size={24} className="feature-icon-svg text-rose-400" />,
      title: "Compiler Diagnostics & Linting",
      description: "Catch errors at the lexical and syntactic levels with instant warning cards linked directly to source positions."
    }
  ];

  return (
    <div className="home-container">
      {/* Decorative Glows */}
      <div className="home-glow glow-1"></div>
      <div className="home-glow glow-2"></div>

      {/* Hero Section */}
      <section className="home-hero">
        <div className="home-hero-badge">
          <Sparkles size={14} className="badge-icon" />
          <span>Semantic Source-to-Source Compiler Engine</span>
        </div>
        
        <h1 className="home-hero-title">
          Morph Python Code into <br />
          <span className="gradient-text">Target Languages Semantically</span>
        </h1>
        
        <p className="home-hero-subtitle">
          Code-Morpher is a highly sophisticated compiler frontend and transpilation dashboard. 
          Write pythonic algorithms and instantly analyze lexical tokens, compacted CST paths, 
          AST graphs, scope symbol grids, and compile-time telemetry.
        </p>

        <div className="home-hero-actions">
          <button className="home-btn-primary" onClick={onLaunch}>
            <span>Launch Compiler Workspace</span>
            <ArrowRight size={20} className="arrow-icon" />
          </button>
        </div>
      </section>

      {/* About / Features Section */}
      <section className="home-section">
        <div className="section-header">
          <h2 className="section-title">Deep Pipeline Visualization Suite</h2>
          <p className="section-subtitle">Code-Morpher does more than translate code. Explore what happens under the hood during the compilation lifecycle.</p>
        </div>

        <div className="features-grid">
          {features.map((feat, idx) => (
            <div key={idx} className="feature-card">
              <div className="feature-icon-container">
                {feat.icon}
              </div>
              <h3 className="feature-card-title">{feat.title}</h3>
              <p className="feature-card-desc">{feat.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Team Section */}
      <section className="home-section home-team-section">
        <div className="section-header">
          <span className="team-accent-badge">PROJECT CREATORS</span>
          <h2 className="section-title">Team <span className="team-highlight">SUPREME</span></h2>
          <p className="section-subtitle">Meet the innovators, architects, and developers who brought Code-Morpher to life.</p>
        </div>

        <div className="team-grid">
          {teamMembers.map((member, idx) => (
            <div key={idx} className="team-card">
              <div className="team-avatar-wrapper">
                <div className={`team-avatar-gradient bg-gradient-to-br ${member.gradient}`}>
                  {member.initial}
                </div>
              </div>
              <h3 className="team-member-name">{member.name}</h3>
            </div>
          ))}
        </div>
      </section>

      {/* Footer Info */}
      <div className="home-footer-info">
        <div className="footer-logo">
          <Code2 size={24} className="footer-logo-icon" />
          <span>Code Morpher</span>
        </div>
        <p className="footer-copyright">© 2026 Team SUPREME. Built for extreme compiler fidelity.</p>
      </div>
    </div>
  );
};

export default HomePage;
