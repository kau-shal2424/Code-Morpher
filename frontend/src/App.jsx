import React, { useState, useRef, useEffect } from 'react';
import Header from './components/Header';
import LanguageSelector from './components/LanguageSelector';
import EditorView from './components/EditorView';
import { transpileCode } from './services/api';
import ASTGraph from './components/ASTGraph.jsx';
import HomePage from './components/HomePage';
import { LayoutPanelTop, Play, Loader2, Clock, Cpu, Binary, FileCode, ShieldAlert, AlertCircle, Info, Search, ZoomIn, ZoomOut, RefreshCw, Award, GitFork, Server, ChevronRight, Check, Copy, Download } from 'lucide-react';
import './App.css';

function App() {
  const [currentView, setCurrentView] = useState('home');
  const [targetLanguage, setTargetLanguage] = useState('javascript');
  const [inputCode, setInputCode] = useState(
    'x = 10\n' +
    'y = 0\n' +
    'result = x / y\n\n' +
    'if x > 5:\n' +
    '    print("X is greater than 5")\n' +
    'else:\n' +
    '    print("X is smaller")\n\n' +
    'for i in range(1, 5):\n' +
    '    print(i)\n'
  );

  const [outputCode, setOutputCode] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [copied, setCopied] = useState(false);

  // Visualization States
  const [visualizationData, setVisualizationData] = useState(null);
  const [selectedTab, setSelectedTab] = useState('target'); // 'target' | 'tokens' | 'cst' | 'ast' | 'graph' | 'diagnostics'
  const [selectedASTNode, setSelectedASTNode] = useState(null);
  const [hoveredTokenIndex, setHoveredTokenIndex] = useState(null);
  const [showMetricsDashboard, setShowMetricsDashboard] = useState(false);

  // AST Display Mode: 'tree' or 'canvas'
  const [astDisplayMode, setAstDisplayMode] = useState('tree');

  // Canvas Zoom/Pan & Search States
  const [zoom, setZoom] = useState(1);
  const [pan, setPan] = useState({ x: 100, y: 30 });
  const [isPanning, setIsPanning] = useState(false);
  const [panStart, setPanStart] = useState({ x: 0, y: 0 });
  const [canvasSearchQuery, setCanvasSearchQuery] = useState('');

  // AST tree and search metadata states
  const [treeRoot, setTreeRoot] = useState(null);
  const [openNodes, setOpenNodes] = useState(new Set());
  const [allNodeIds, setAllNodeIds] = useState(new Set());
  const [parentMap, setParentMap] = useState(new Map());
  
  // Fullscreen state
  const [fullscreenMode, setFullscreenMode] = useState(null); // null | 'tree' | 'graph'
  
  // Tree Zoom/Pan States (for fullscreen tree zooming)
  const [treeZoom, setTreeZoom] = useState(1);
  const [treePan, setTreePan] = useState({ x: 0, y: 0 });
  const [isTreePanning, setIsTreePanning] = useState(false);
  const [treePanStart, setTreePanStart] = useState({ x: 0, y: 0 });
  
  // Tree Search States
  const [treeSearchQuery, setTreeSearchQuery] = useState('');
  const [matchingNodeIds, setMatchingNodeIds] = useState([]);
  const [currentMatchIndex, setCurrentMatchIndex] = useState(-1);

  // Helper to deep clone and assign unique recursive IDs, mapping parents
  const processAST = (node, parentId = null, counter = { id: 0 }, allIds = new Set(), pMap = new Map()) => {
    if (!node) return null;
    const id = `ast_node_${counter.id++}`;
    allIds.add(id);
    if (parentId) {
      pMap.set(id, parentId);
    }
    
    const cloned = {
      ...node,
      id
    };
    
    if (node.children && node.children.length > 0) {
      cloned.children = node.children.map(child => processAST(child, id, counter, allIds, pMap));
    } else {
      cloned.children = [];
    }
    
    return cloned;
  };

  // Compile / Process AST when visualizationData updates
  useEffect(() => {
    if (visualizationData?.ast) {
      const allIds = new Set();
      const pMap = new Map();
      const root = processAST(visualizationData.ast, null, { id: 0 }, allIds, pMap);
      setTreeRoot(root);
      setAllNodeIds(allIds);
      setOpenNodes(new Set(allIds)); // initially all expanded
      setParentMap(pMap);
      
      if (root) {
        setSelectedASTNode(root);
      }
    } else {
      setTreeRoot(null);
      setAllNodeIds(new Set());
      setOpenNodes(new Set());
      setParentMap(new Map());
      setSelectedASTNode(null);
    }
    setTreeSearchQuery('');
    setFullscreenMode(null);
    setTreeZoom(1);
    setTreePan({ x: 0, y: 0 });
  }, [visualizationData]);

  // Tree Search Matcher Effect
  useEffect(() => {
    if (!treeSearchQuery.trim() || !treeRoot) {
      setMatchingNodeIds([]);
      setCurrentMatchIndex(-1);
      return;
    }
    
    const query = treeSearchQuery.toLowerCase();
    const matches = [];
    
    const findMatches = (node) => {
      const isMatch = (node.type && node.type.toLowerCase().includes(query)) ||
                      (node.label && node.label.toLowerCase().includes(query));
      if (isMatch) {
        matches.push(node.id);
      }
      if (node.children) {
        node.children.forEach(findMatches);
      }
    };
    
    findMatches(treeRoot);
    setMatchingNodeIds(matches);
    if (matches.length > 0) {
      setCurrentMatchIndex(0);
    } else {
      setCurrentMatchIndex(-1);
    }
  }, [treeSearchQuery, treeRoot]);

  // Ancestor expander to ensure match is visible
  const expandPathToNode = (nodeId) => {
    if (!nodeId || !parentMap.size) return;
    setOpenNodes(prev => {
      const next = new Set(prev);
      let curr = nodeId;
      while (parentMap.has(curr)) {
        const pId = parentMap.get(curr);
        next.add(pId);
        curr = pId;
      }
      return next;
    });
  };

  // Scroll matching node into view on cursor change
  useEffect(() => {
    if (currentMatchIndex >= 0 && matchingNodeIds.length > 0) {
      const activeId = matchingNodeIds[currentMatchIndex];
      expandPathToNode(activeId);
      
      setTimeout(() => {
        const element = document.getElementById(`tree-node-element-${activeId}`);
        if (element) {
          element.scrollIntoView({
            behavior: 'smooth',
            block: 'center',
            inline: 'center'
          });
        }
      }, 100);
    }
  }, [currentMatchIndex, matchingNodeIds]);

  const expandAll = () => {
    setOpenNodes(new Set(allNodeIds));
  };

  const collapseAll = () => {
    if (treeRoot) {
      setOpenNodes(new Set([treeRoot.id]));
    } else {
      setOpenNodes(new Set());
    }
  };

  const prevSearchMatch = () => {
    if (matchingNodeIds.length === 0) return;
    setCurrentMatchIndex(prev => (prev - 1 + matchingNodeIds.length) % matchingNodeIds.length);
  };

  const nextSearchMatch = () => {
    if (matchingNodeIds.length === 0) return;
    setCurrentMatchIndex(prev => (prev + 1) % matchingNodeIds.length);
  };

  const handleTreeWheel = (e) => {
    if (e.ctrlKey) {
      e.preventDefault();
      const zoomFactor = 0.05;
      const nextZoom = e.deltaY < 0 ? treeZoom + zoomFactor : treeZoom - zoomFactor;
      setTreeZoom(Math.max(0.5, Math.min(2.0, nextZoom)));
    }
  };

  const handleTreeMouseDown = (e) => {
    if (e.target.classList.contains('tree-scroll-container') || e.target.classList.contains('tree-zoomable-content')) {
      setIsTreePanning(true);
      setTreePanStart({
        x: e.clientX - treePan.x,
        y: e.clientY - treePan.y
      });
    }
  };

  const handleTreeMouseMove = (e) => {
    if (!isTreePanning) return;
    setTreePan({
      x: e.clientX - treePanStart.x,
      y: e.clientY - treePanStart.y
    });
  };

  const handleTreeMouseUp = () => {
    setIsTreePanning(false);
  };

  // Monaco Reference States
  const [editor, setEditor] = useState(null);
  const [monaco, setMonaco] = useState(null);
  const decorationsRef = useRef([]);

  const handleEditorDidMount = (editorInstance, monacoInstance) => {
    setEditor(editorInstance);
    setMonaco(monacoInstance);
  };

  const highlightRange = (startIndex, stopIndex) => {
    if (!editor || !monaco || startIndex === undefined || stopIndex === undefined) return;

    const model = editor.getModel();
    if (!model) return;

    try {
      const startPos = model.getPositionAt(startIndex);
      const endPos = model.getPositionAt(stopIndex + 1);

      const range = new monaco.Range(
        startPos.lineNumber,
        startPos.column,
        endPos.lineNumber,
        endPos.column
      );

      decorationsRef.current = editor.deltaDecorations(decorationsRef.current, [
        {
          range: range,
          options: {
            className: 'monaco-line-highlight',
            inlineClassName: 'monaco-highlight-range',
            isWholeLine: false
          }
        }
      ]);

      editor.revealRangeInCenterIfOutsideViewport(range);
    } catch (e) {
      console.warn("Could not highlight range in editor:", e);
    }
  };

  const clearHighlights = () => {
    if (editor && monaco) {
      decorationsRef.current = editor.deltaDecorations(decorationsRef.current, []);
    }
  };

  // Clear decorations on cleanup
  useEffect(() => {
    return () => {
      if (editor && monaco) {
        decorationsRef.current = editor.deltaDecorations(decorationsRef.current, []);
      }
    };
  }, [editor, monaco]);

  const handleConvert = async () => {
    setIsLoading(true);
    setError(null);
    setSelectedASTNode(null);
    clearHighlights();

    try {
      const result = await transpileCode({
        sourceLanguage: 'python',
        targetLanguage: targetLanguage,
        code: inputCode,
        visualize: true // Request parallel visualization payload
      });

      setOutputCode(result.output);

      if (result.visualization) {
        setVisualizationData(result.visualization);
        // If there are diagnostics warnings, automatically switch to diagnostics tab to alert the user,
        // otherwise default to showing target code.
        if (result.visualization.errors && result.visualization.errors.length > 0) {
          setSelectedTab('diagnostics');
        } else {
          setSelectedTab('target');
        }
      } else {
        setVisualizationData(null);
        setSelectedTab('target');
      }
    } catch (err) {
      setError('Failed to transpile code. Is the Spring Boot backend server running on port 8080?');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(outputCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const downloadFile = () => {
    const extensions = { javascript: 'js', java: 'java', cpp: 'cpp', c: 'c', typescript: 'ts', ruby: 'rb' };
    const blob = new Blob([outputCode], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `transpiled.${extensions[targetLanguage] || 'txt'}`;
    link.click();
    URL.revokeObjectURL(url);
  };

  const formatNsToMs = (ns) => {
    if (ns === undefined || ns === null) return '0.00 ms';
    return `${(ns / 1000000).toFixed(3)} ms`;
  };

  // Custom Recursive AST Tree Viewer Component
  const ASTNodeTree = ({ 
    node, 
    openNodes, 
    onToggle, 
    onSelect, 
    activeNode, 
    highlightedNodes, 
    focusedNodeId 
  }) => {
    const hasChildren = node.children && node.children.length > 0;
    const isOpen = openNodes.has(node.id);
    const isSelected = activeNode?.id === node.id;
    const isHighlighted = highlightedNodes ? highlightedNodes.has(node.id) : false;
    const isFocused = focusedNodeId === node.id;

    return (
      <div 
        className="ast-tree-node"
        id={`tree-node-element-${node.id}`}
      >
        <div
          className={`ast-tree-header ${isSelected ? 'active' : ''} ${isHighlighted ? 'search-match' : ''} ${isFocused ? 'search-focus' : ''}`}
          onClick={(e) => {
            e.stopPropagation();
            onSelect(node);
            if (hasChildren) {
              onToggle(node.id);
            }
          }}
        >
          {hasChildren ? (
            <ChevronRight className={`ast-arrow ${isOpen ? 'open' : ''}`} size={16} />
          ) : (
            <span className="ast-leaf-dot"></span>
          )}
          <span className="ast-type-badge">{node.type}</span>
          <span className="ast-label-text">{node.label}</span>
        </div>

        {hasChildren && isOpen && (
          <div className="ast-tree-children">
            {node.children.map((child, idx) => (
              <ASTNodeTree
                key={child.id || idx}
                node={child}
                openNodes={openNodes}
                onToggle={onToggle}
                onSelect={onSelect}
                activeNode={activeNode}
                highlightedNodes={highlightedNodes}
                focusedNodeId={focusedNodeId}
              />
            ))}
          </div>
        )}
      </div>
    );
  };

  // Custom Recursive Compacted CST Tree Component
  const CSTNodeTree = ({ node }) => {
    const [isOpen, setIsOpen] = useState(true);
    const hasChildren = node.children && node.children.length > 0;

    return (
      <div className="cst-tree-node">
        <div
          className="cst-tree-header"
          onClick={() => { if (hasChildren) setIsOpen(!isOpen); }}
        >
          {hasChildren ? (
            <ChevronRight className={`cst-arrow ${isOpen ? 'open' : ''}`} size={14} />
          ) : (
            <span className="cst-leaf-dot"></span>
          )}
          <span className="cst-type-badge">{node.type}</span>
          {!hasChildren && <span className="cst-value-label">"{node.label}"</span>}
        </div>

        {hasChildren && isOpen && (
          <div className="cst-tree-children">
            {node.children.map((child, idx) => (
              <CSTNodeTree key={idx} node={child} />
            ))}
          </div>
        )}
      </div>
    );
  };

  // Recursive layout coordinator using the Wetherell-Shannon centered tree layout
  const calculateCanvasLayout = (node, depth = 0, state = { xOffset: 50 }) => {
    const horizontalSpacing = 160;
    const verticalSpacing = 100;

    const layoutNode = {
      type: node.type,
      label: node.label,
      startIndex: node.startIndex,
      stopIndex: node.stopIndex,
      properties: node.properties,
      depth: depth,
      y: depth * verticalSpacing + 60
    };

    if (!node.children || node.children.length === 0) {
      layoutNode.x = state.xOffset;
      state.xOffset += horizontalSpacing;
      layoutNode.children = [];
      return layoutNode;
    }

    const layoutChildren = [];
    for (const child of node.children) {
      layoutChildren.push(calculateCanvasLayout(child, depth + 1, state));
    }

    layoutNode.children = layoutChildren;

    // Center parent over its children
    const firstChildX = layoutChildren[0].x;
    const lastChildX = layoutChildren[layoutChildren.length - 1].x;
    layoutNode.x = (firstChildX + lastChildX) / 2;

    return layoutNode;
  };

  // Flatten the positioned tree nodes for rendering list + smooth Bézier SVG link collection
  const flattenLayout = (node, nodesList = [], linksList = []) => {
    nodesList.push(node);

    if (node.children) {
      for (const child of node.children) {
        linksList.push({
          fromX: node.x,
          fromY: node.y,
          toX: child.x,
          toY: child.y
        });
        flattenLayout(child, nodesList, linksList);
      }
    }
    return { nodesList, linksList };
  };

  // Layout calculations
  let graphNodes = [];
  let graphLinks = [];
  if (visualizationData?.ast) {
    try {
      const layoutRoot = calculateCanvasLayout(visualizationData.ast);
      const flat = flattenLayout(layoutRoot);
      graphNodes = flat.nodesList;
      graphLinks = flat.linksList;
    } catch (e) {
      console.error("Failed to calculate node layout:", e);
    }
  }

  // Interactive Graph Canvas handlers
  const handleCanvasMouseDown = (e) => {
    setIsPanning(true);
    setPanStart({
      x: e.clientX - pan.x,
      y: e.clientY - pan.y
    });
  };

  const handleCanvasMouseMove = (e) => {
    if (!isPanning) return;
    setPan({
      x: e.clientX - panStart.x,
      y: e.clientY - panStart.y
    });
  };

  const handleCanvasMouseUpOrLeave = () => {
    setIsPanning(false);
  };

  const handleCanvasWheel = (e) => {
    e.preventDefault();
    const zoomFactor = 0.1;
    let nextZoom = zoom + (e.deltaY < 0 ? zoomFactor : -zoomFactor);
    nextZoom = Math.max(0.3, Math.min(2.5, nextZoom));
    setZoom(nextZoom);
  };

  const resetCanvasView = () => {
    setZoom(0.8);
    setPan({ x: 80, y: 30 });
    setCanvasSearchQuery('');
  };

  return (
    <div className="app">
      <Header currentView={currentView} onNavigate={setCurrentView} />

      {currentView === 'home' ? (
        <HomePage onLaunch={() => setCurrentView('editor')} />
      ) : (
        <>
          <main className="main-content">
        <div className="controls">
          <div className="language-selector-group">
            <LanguageSelector
              selectedLanguage={targetLanguage}
              onLanguageChange={setTargetLanguage}
            />

            {visualizationData?.metrics && (
              <button
                className={`btn-metrics-badge ${showMetricsDashboard ? 'active' : ''}`}
                onClick={() => setShowMetricsDashboard(!showMetricsDashboard)}
              >
                <Clock size={16} />
                <span>Timer: {formatNsToMs(visualizationData.metrics.totalCompilationDurationNs)}</span>
              </button>
            )}
          </div>

          <button
            className={`btn-convert ${isLoading ? 'loading' : ''}`}
            onClick={handleConvert}
            disabled={isLoading || !inputCode.trim()}
          >
            {isLoading ? <Loader2 className="spinner" /> : <Play size={18} />}
            <span>{isLoading ? 'Transpiling...' : 'Convert & Analyze'}</span>
          </button>
        </div>

        {/* Expandable Timing Metrics Dashboard Panel */}
        {showMetricsDashboard && visualizationData?.metrics && (
          <div className="metrics-dashboard-panel">
            <div className="metrics-dashboard-header">
              <h4><Server size={18} /> Asynchronous Compilation Metrics Dashboard</h4>
              <button className="close-btn" onClick={() => setShowMetricsDashboard(false)}>×</button>
            </div>

            <div className="metrics-summary-grid">
              <div className="metrics-card-summary">
                <span className="metrics-summary-label">Total Latency</span>
                <span className="metrics-summary-val highlight">{formatNsToMs(visualizationData.metrics.totalCompilationDurationNs)}</span>
              </div>
              <div className="metrics-card-summary">
                <span className="metrics-summary-label">Parallel Step Sum</span>
                <span className="metrics-summary-val">
                  {formatNsToMs(
                    visualizationData.metrics.codeGenDurationNs +
                    visualizationData.metrics.astSerializationDurationNs +
                    visualizationData.metrics.tokenExtractionDurationNs +
                    visualizationData.metrics.diagnosticsDurationNs +
                    visualizationData.metrics.symbolCollectionDurationNs
                  )}
                </span>
              </div>
              <div className="metrics-card-summary">
                <span className="metrics-summary-label">Throughput Savings</span>
                <span className="metrics-summary-val green">
                  {(() => {
                    const totalParallelSum =
                      visualizationData.metrics.codeGenDurationNs +
                      visualizationData.metrics.astSerializationDurationNs +
                      visualizationData.metrics.tokenExtractionDurationNs +
                      visualizationData.metrics.diagnosticsDurationNs +
                      visualizationData.metrics.symbolCollectionDurationNs;
                    const joinDuration = visualizationData.metrics.totalParallelDurationNs;
                    if (joinDuration <= 0) return '0.0%';
                    const savings = ((totalParallelSum - joinDuration) / totalParallelSum) * 100;
                    return `${savings > 0 ? savings.toFixed(1) : '0.0'}% faster`;
                  })()}
                </span>
              </div>
            </div>

            <div className="metrics-timing-list">
              <h5><GitFork size={14} /> Parallel Pipeline Threads & Sequential Timings</h5>

              <div className="metric-row">
                <span className="metric-label">1. Lexer & Parser (Sequential)</span>
                <span className="metric-duration">{formatNsToMs(visualizationData.metrics.sequentialParseDurationNs)}</span>
                <div className="metric-progress-container">
                  <div
                    className="metric-progress-bar sequential"
                    style={{ width: `${Math.min(100, (visualizationData.metrics.sequentialParseDurationNs / visualizationData.metrics.totalCompilationDurationNs) * 100)}%` }}
                  ></div>
                </div>
              </div>

              <div className="metric-row">
                <span className="metric-label">2. Target CodeGen (Thread-1)</span>
                <span className="metric-duration">{formatNsToMs(visualizationData.metrics.codeGenDurationNs)}</span>
                <div className="metric-progress-container">
                  <div
                    className="metric-progress-bar thread"
                    style={{ width: `${Math.min(100, (visualizationData.metrics.codeGenDurationNs / visualizationData.metrics.totalCompilationDurationNs) * 100)}%` }}
                  ></div>
                </div>
              </div>

              <div className="metric-row">
                <span className="metric-label">3. AST Serialization (Thread-2)</span>
                <span className="metric-duration">{formatNsToMs(visualizationData.metrics.astSerializationDurationNs)}</span>
                <div className="metric-progress-container">
                  <div
                    className="metric-progress-bar thread"
                    style={{ width: `${Math.min(100, (visualizationData.metrics.astSerializationDurationNs / visualizationData.metrics.totalCompilationDurationNs) * 100)}%` }}
                  ></div>
                </div>
              </div>

              <div className="metric-row">
                <span className="metric-label">4. Token Extraction (Thread-3)</span>
                <span className="metric-duration">{formatNsToMs(visualizationData.metrics.tokenExtractionDurationNs)}</span>
                <div className="metric-progress-container">
                  <div
                    className="metric-progress-bar thread"
                    style={{ width: `${Math.min(100, (visualizationData.metrics.tokenExtractionDurationNs / visualizationData.metrics.totalCompilationDurationNs) * 100)}%` }}
                  ></div>
                </div>
              </div>

              <div className="metric-row">
                <span className="metric-label">5. AST Diagnostics (Thread-4)</span>
                <span className="metric-duration">{formatNsToMs(visualizationData.metrics.diagnosticsDurationNs)}</span>
                <div className="metric-progress-container">
                  <div
                    className="metric-progress-bar thread"
                    style={{ width: `${Math.min(100, (visualizationData.metrics.diagnosticsDurationNs / visualizationData.metrics.totalCompilationDurationNs) * 100)}%` }}
                  ></div>
                </div>
              </div>

              <div className="metric-row">
                <span className="metric-label">6. Symbol Collection (Thread-5)</span>
                <span className="metric-duration">{formatNsToMs(visualizationData.metrics.symbolCollectionDurationNs)}</span>
                <div className="metric-progress-container">
                  <div
                    className="metric-progress-bar thread"
                    style={{ width: `${Math.min(100, (visualizationData.metrics.symbolCollectionDurationNs / visualizationData.metrics.totalCompilationDurationNs) * 100)}%` }}
                  ></div>
                </div>
              </div>
            </div>

            <div className="metrics-footer">
              <Award size={14} color="#3fb950" />
              <span>Multi-threaded parallel engine utilizing Java's `CompletableFuture` API. Workers read AST concurrently with absolute safety.</span>
            </div>
          </div>
        )}

        {error && (
          <div className="error-banner">
            <AlertCircle size={20} />
            <span>{error}</span>
          </div>
        )}

        {/* Parallel Visualization Layout */}
        <div className="workspace-container">
          {/* Left Pane: Code Editor */}
          <div className="pane left-pane">
            <div className="pane-header">
              <h3>Python Input</h3>
            </div>
            <EditorView
              code={inputCode}
              setCode={setInputCode}
              language="python"
              onEditorMount={handleEditorDidMount}
              readOnly={false}
            />
          </div>

          {/* Right Pane: Visualization Dashboard with Pipeline Stepper */}
          <div className="pane right-pane">
            <div className="pane-header flex-header">
              <div className="pipeline-stepper">
                <button
                  className={`step-btn ${selectedTab === 'tokens' ? 'active' : ''}`}
                  onClick={() => { setSelectedTab('tokens'); clearHighlights(); }}
                >
                  <Cpu size={16} />
                  <span>Tokens</span>
                  {visualizationData?.tokens && (
                    <span className="tab-badge blue">{visualizationData.tokens.length}</span>
                  )}
                </button>

                <button
                  className={`step-btn ${selectedTab === 'cst' ? 'active' : ''}`}
                  onClick={() => { setSelectedTab('cst'); clearHighlights(); }}
                >
                  <Server size={16} />
                  <span>CST Tree</span>
                </button>

                <button
                  className={`step-btn ${selectedTab === 'ast' ? 'active' : ''}`}
                  onClick={() => { setSelectedTab('ast'); clearHighlights(); }}
                >
                  <Binary size={16} />
                  <span>AST Tree</span>
                </button>

                <button
                  className={`step-btn ${selectedTab === 'graph' ? 'active' : ''}`}
                  onClick={() => { setSelectedTab('graph'); clearHighlights(); }}
                >
                  <GitFork size={16} />
                  <span>Graph Explorer</span>
                </button>

                <button
                  className={`step-btn ${selectedTab === 'diagnostics' ? 'active' : ''}`}
                  onClick={() => { setSelectedTab('diagnostics'); clearHighlights(); }}
                >
                  <ShieldAlert size={16} />
                  <span>Diagnostics</span>
                  {visualizationData?.errors && visualizationData.errors.length > 0 && (
                    <span className="tab-badge red">{visualizationData.errors.length}</span>
                  )}
                </button>

                <button
                  className={`step-btn ${selectedTab === 'target' ? 'active' : ''}`}
                  onClick={() => { setSelectedTab('target'); clearHighlights(); }}
                >
                  <FileCode size={16} />
                  <span>Target Code</span>
                </button>
              </div>
            </div>

            <div className="pane-body">
              {/* Dynamic View Panels */}
              {selectedTab === 'target' && (
                <div className="view-panel target-panel">
                  <EditorView
                    code={outputCode || '// Transpile code to view target output...'}
                    setCode={() => { }}
                    language={targetLanguage}
                    readOnly={true}
                  />
                  {outputCode && (
                    <div className="actions">
                      <button className="btn-secondary" onClick={copyToClipboard}>
                        {copied ? <Check size={18} color="#10b981" /> : <Copy size={18} />}
                        <span>{copied ? 'Copied!' : 'Copy Results'}</span>
                      </button>
                      <button className="btn-secondary" onClick={downloadFile}>
                        <Download size={18} />
                        <span>Download File</span>
                      </button>
                    </div>
                  )}
                </div>
              )}

              {selectedTab === 'tokens' && (
                <div className="view-panel tokens-panel">
                  {!visualizationData ? (
                    <div className="empty-state">
                      <Cpu size={48} className="empty-icon" />
                      <p>Run compilation to inspect lexical token stream</p>
                    </div>
                  ) : (
                    <div className="tokens-container">
                      <div className="info-bar">
                        <Info size={16} />
                        <span>Hover over token chips to highlight their exact coordinates in the source editor!</span>
                      </div>
                      <div className="tokens-tape">
                        {visualizationData.tokens.map((token, index) => {
                          const isHovered = hoveredTokenIndex === index;
                          let typeClass = 'token-other';
                          if (token.type.includes('KEYWORD') || token.type === 'IF' || token.type === 'ELSE' || token.type === 'WHILE' || token.type === 'FOR') {
                            typeClass = 'token-keyword';
                          } else if (token.type === 'IDENTIFIER') {
                            typeClass = 'token-identifier';
                          } else if (token.type.includes('INTEGER') || token.type.includes('LITERAL') || token.type === 'STRING') {
                            typeClass = 'token-literal';
                          } else if (token.type.includes('OP') || token.type === 'ASSIGN' || token.type === 'COMP') {
                            typeClass = 'token-operator';
                          }

                          return (
                            <button
                              key={index}
                              className={`token-chip ${typeClass} ${isHovered ? 'hovered' : ''}`}
                              onMouseEnter={() => {
                                setHoveredTokenIndex(index);
                                highlightRange(token.startIndex, token.stopIndex);
                              }}
                              onMouseLeave={() => {
                                setHoveredTokenIndex(null);
                                clearHighlights();
                              }}
                            >
                              <div className="token-text">{token.text}</div>
                              <div className="token-type-label">{token.type}</div>
                              <div className="token-index-label">L{token.line}:C{token.column}</div>
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>
              )}

              {selectedTab === 'cst' && (
                <div className="view-panel cst-panel">
                  {!visualizationData || !visualizationData.cst ? (
                    <div className="empty-state">
                      <Server size={48} className="empty-icon" />
                      <p>Run compilation to explore Concrete Syntax Tree (CST) rules</p>
                    </div>
                  ) : (
                    <div className="cst-layout-container">
                      <div className="info-bar">
                        <Info size={16} />
                        <span>Concrete Syntax Tree (CST) represents exact parser rule grammar, compacted automatically by bypassing single-child intermediate layers!</span>
                      </div>
                      <div className="cst-tree-explorer">
                        <div className="cst-tree-wrapper">
                          <CSTNodeTree node={visualizationData.cst} />
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {selectedTab === 'ast' && (
                <div className="view-panel ast-panel">
                  {!visualizationData ? (
                    <div className="empty-state">
                      <Binary size={48} className="empty-icon" />
                      <p>Run compilation to explore abstract syntax tree IR</p>
                    </div>
                  ) : (
                    <div className="ast-tab-scroll-container">
                      {/* AST View Mode Selector & Search Toggle */}
                      <div className="ast-mode-selector-bar">
                        <div className="display-mode-buttons">
                          <button
                            className={`mode-btn ${astDisplayMode === 'tree' ? 'active' : ''}`}
                            onClick={() => setAstDisplayMode('tree')}
                          >
                            🌳 Tree Explorer
                          </button>
                          <button
                            className={`mode-btn ${astDisplayMode === 'canvas' ? 'active' : ''}`}
                            onClick={() => { setAstDisplayMode('canvas'); resetCanvasView(); }}
                          >
                            🎨 Interactive Canvas
                          </button>
                        </div>

                        {astDisplayMode === 'canvas' && (
                          <div className="canvas-search-box">
                            <Search size={14} className="search-icon" />
                            <input
                              type="text"
                              placeholder="Search node class/label..."
                              value={canvasSearchQuery}
                              onChange={(e) => setCanvasSearchQuery(e.target.value)}
                            />
                            {canvasSearchQuery && (
                              <button className="clear-search-btn" onClick={() => setCanvasSearchQuery('')}>×</button>
                            )}
                          </div>
                        )}
                      </div>

                      {/* Rendering Conditional Display Mode */}
                      {astDisplayMode === 'tree' ? (
                        <div className="ast-layout">
                          {/* Left: Collapsible Node Tree */}
                          <div className="ast-tree-explorer">
                            <div className="ast-tree-toolbar">
                              <div className="ast-tree-search">
                                <Search size={14} className="search-icon" />
                                <input
                                  type="text"
                                  placeholder="Search tree nodes..."
                                  value={treeSearchQuery}
                                  onChange={(e) => setTreeSearchQuery(e.target.value)}
                                />
                                {matchingNodeIds.length > 0 && (
                                  <span className="search-count">
                                    {currentMatchIndex + 1}/{matchingNodeIds.length}
                                  </span>
                                )}
                              </div>
                              <div className="ast-tree-actions">
                                <button className="mini-action-btn" onClick={expandAll} title="Expand All">📂</button>
                                <button className="mini-action-btn" onClick={collapseAll} title="Collapse All">📁</button>
                                <button className="mini-action-btn" onClick={() => setFullscreenMode('tree')} title="Fullscreen"><LayoutPanelTop size={14} /></button>
                              </div>
                            </div>
                            <div className="ast-tree-wrapper">
                              {treeRoot && (
                                <ASTNodeTree
                                  node={treeRoot}
                                  openNodes={openNodes}
                                  onToggle={(id) => {
                                    setOpenNodes(prev => {
                                      const next = new Set(prev);
                                      if (next.has(id)) next.delete(id);
                                      else next.add(id);
                                      return next;
                                    });
                                  }}
                                  onSelect={(node) => {
                                    setSelectedASTNode(node);
                                    highlightRange(node.startIndex, node.stopIndex);
                                  }}
                                  activeNode={selectedASTNode}
                                  highlightedNodes={new Set(matchingNodeIds)}
                                  focusedNodeId={matchingNodeIds[currentMatchIndex]}
                                />
                              )}
                            </div>
                          </div>

                          {/* Right: Key-Value Properties Inspector */}
                          <div className="ast-properties-inspector">
                            <h4>Properties Inspector</h4>
                            {selectedASTNode ? (
                              <div className="properties-wrapper">
                                <div className="property-group">
                                  <span className="prop-key">Class</span>
                                  <span className="prop-value strong">{selectedASTNode.type}</span>
                                </div>
                                <div className="property-group">
                                  <span className="prop-key">Label</span>
                                  <span className="prop-value">{selectedASTNode.label}</span>
                                </div>
                                <div className="property-group">
                                  <span className="prop-key">Range</span>
                                  <span className="prop-value code">
                                    {selectedASTNode.startIndex} ➔ {selectedASTNode.stopIndex} ({selectedASTNode.stopIndex - selectedASTNode.startIndex + 1} chars)
                                  </span>
                                </div>

                                {selectedASTNode.properties && Object.keys(selectedASTNode.properties).length > 0 ? (
                                  <div className="dynamic-properties">
                                    <h5>Semantic Attributes</h5>
                                    {Object.entries(selectedASTNode.properties).map(([key, val]) => (
                                      <div className="property-group" key={key}>
                                        <span className="prop-key">{key}</span>
                                        <span className="prop-value italic">
                                          {typeof val === 'object' ? JSON.stringify(val) : String(val)}
                                        </span>
                                      </div>
                                    ))}
                                  </div>
                                ) : (
                                  <div className="no-attributes">
                                    <p>No extra attributes for this node class.</p>
                                  </div>
                                )}
                              </div>
                            ) : (
                              <div className="properties-empty">
                                <p>No node selected.</p>
                                <p className="subtext">Click any AST node in the tree list to view its semantic attributes and precise span range.</p>
                              </div>
                            )}
                          </div>
                        </div>
                      ) : (
                        /* Phase 4: Recruiter Grade Dynamic Graph Canvas */
                        <div className="ast-canvas-layout">
                          <div className="ast-canvas-container">
                            <div className="canvas-controls-overlay">
                              <button title="Zoom In" onClick={() => setZoom(z => Math.min(2.5, z + 0.1))}><ZoomIn size={14} /></button>
                              <button title="Zoom Out" onClick={() => setZoom(z => Math.max(0.3, z - 0.1))}><ZoomOut size={14} /></button>
                              <button title="Reset View" onClick={resetCanvasView}><RefreshCw size={14} /></button>
                            </div>

                            <div className="canvas-instructions-overlay">
                              <Info size={12} />
                              <span>Drag to Pan • Wheel to Zoom • Click Node to Inspect</span>
                            </div>

                            <svg
                              className={`ast-svg-canvas ${isPanning ? 'grabbing' : 'grab'}`}
                              width="100%"
                              height="440"
                              onMouseDown={handleCanvasMouseDown}
                              onMouseMove={handleCanvasMouseMove}
                              onMouseUp={handleCanvasMouseUpOrLeave}
                              onMouseLeave={handleCanvasMouseUpOrLeave}
                              onWheel={handleCanvasWheel}
                              style={{ background: 'rgba(8, 12, 16, 0.5)', borderRadius: '12px' }}
                            >
                              <g transform={`translate(${pan.x}, ${pan.y}) scale(${zoom})`}>
                                {/* Bézier Curves Links between parent and child nodes */}
                                {graphLinks.map((link, idx) => (
                                  <path
                                    key={idx}
                                    d={`M ${link.fromX} ${link.fromY} C ${link.fromX} ${(link.fromY + link.toY) / 2}, ${link.toX} ${(link.fromY + link.toY) / 2}, ${link.toX} ${link.toY}`}
                                    fill="none"
                                    stroke="rgba(99, 102, 241, 0.25)"
                                    strokeWidth="2.5"
                                  />
                                ))}

                                {/* Positioned AST Nodes */}
                                {graphNodes.map((node, idx) => {
                                  const isSelected = selectedASTNode && selectedASTNode.startIndex === node.startIndex && selectedASTNode.stopIndex === node.stopIndex;

                                  // Highlight matching nodes based on search query
                                  const matchesSearch = canvasSearchQuery && (
                                    node.type.toLowerCase().includes(canvasSearchQuery.toLowerCase()) ||
                                    node.label.toLowerCase().includes(canvasSearchQuery.toLowerCase())
                                  );

                                  return (
                                    <g
                                      key={idx}
                                      transform={`translate(${node.x}, ${node.y})`}
                                      className={`canvas-node-group ${isSelected ? 'active' : ''} ${matchesSearch ? 'searched-glow' : ''}`}
                                      onClick={(e) => {
                                        e.stopPropagation(); // Avoid triggering parent event
                                        setSelectedASTNode(node);
                                        highlightRange(node.startIndex, node.stopIndex);
                                      }}
                                    >
                                      {/* Outer Node rect container */}
                                      <rect
                                        x="-70"
                                        y="-24"
                                        width="140"
                                        height="48"
                                        rx="10"
                                        fill="#0d1117"
                                        stroke={isSelected ? '#6366f1' : (matchesSearch ? '#ff9b50' : '#21262d')}
                                        strokeWidth={isSelected || matchesSearch ? '2.5' : '1.5'}
                                        style={{ transition: 'all 0.2s ease' }}
                                      />
                                      {/* Color-coded type top line banner */}
                                      <line
                                        x1="-70"
                                        y1="-23"
                                        x2="70"
                                        y2="-23"
                                        stroke={node.type.includes('Node') ? '#6366f1' : '#ff9b50'}
                                        strokeWidth="3.5"
                                      />
                                      {/* Text fields */}
                                      <text
                                        y="-6"
                                        textAnchor="middle"
                                        fill="#818cf8"
                                        fontWeight="800"
                                        fontSize="10"
                                        fontFamily="'Fira Code', monospace"
                                      >
                                        {node.type.length > 20 ? node.type.substring(0, 17) + '...' : node.type}
                                      </text>
                                      <text
                                        y="12"
                                        textAnchor="middle"
                                        fill="#f0f6fc"
                                        fontWeight="600"
                                        fontSize="9.5"
                                        fontFamily="'Fira Code', monospace"
                                      >
                                        {node.label.length > 20 ? node.label.substring(0, 17) + '...' : node.label}
                                      </text>
                                    </g>
                                  );
                                })}
                              </g>
                            </svg>
                          </div>

                          {/* Canvas Side Properties Inspector Card */}
                          <div className="ast-properties-inspector">
                            <h4>Canvas Node Inspector</h4>
                            {selectedASTNode ? (
                              <div className="properties-wrapper">
                                <div className="property-group">
                                  <span className="prop-key">Class</span>
                                  <span className="prop-value strong">{selectedASTNode.type}</span>
                                </div>
                                <div className="property-group">
                                  <span className="prop-key">Label</span>
                                  <span className="prop-value">{selectedASTNode.label}</span>
                                </div>
                                <div className="property-group">
                                  <span className="prop-key">Source span</span>
                                  <span className="prop-value code">
                                    {selectedASTNode.startIndex} ➔ {selectedASTNode.stopIndex}
                                  </span>
                                </div>

                                {selectedASTNode.properties && Object.keys(selectedASTNode.properties).length > 0 ? (
                                  <div className="dynamic-properties">
                                    <h5>Semantic Attributes</h5>
                                    {Object.entries(selectedASTNode.properties).map(([key, val]) => (
                                      <div className="property-group" key={key}>
                                        <span className="prop-key">{key}</span>
                                        <span className="prop-value italic">
                                          {typeof val === 'object' ? JSON.stringify(val) : String(val)}
                                        </span>
                                      </div>
                                    ))}
                                  </div>
                                ) : (
                                  <div className="no-attributes">
                                    <p>No extra attributes for this node class.</p>
                                  </div>
                                )}
                              </div>
                            ) : (
                              <div className="properties-empty">
                                <p>Click any node inside the dynamic canvas view to inspect details instantly!</p>
                              </div>
                            )}
                          </div>
                        </div>
                      )}

                      {/* Phase 3: Symbol Table Grid */}
                      {visualizationData.symbols && visualizationData.symbols.length > 0 && (
                        <div className="symbol-table-container">
                          <h4>Static Analysis Symbol Table Grid</h4>
                          <div className="symbol-table-scroll">
                            <table className="symbol-table">
                              <thead>
                                <tr>
                                  <th>Identifier</th>
                                  <th>Kind</th>
                                  <th>Type</th>
                                  <th>Declared Scope</th>
                                  <th>Span Range</th>
                                </tr>
                              </thead>
                              <tbody>
                                {visualizationData.symbols.map((sym, idx) => (
                                  <tr
                                    key={idx}
                                    className="symbol-row"
                                    onClick={() => highlightRange(sym.startIndex, sym.stopIndex)}
                                  >
                                    <td className="sym-name">{sym.name}</td>
                                    <td className="sym-kind">
                                      <span className={`sym-badge ${sym.kind.toLowerCase()}`}>{sym.kind}</span>
                                    </td>
                                    <td className="sym-type"><code>{sym.type}</code></td>
                                    <td className="sym-scope">{sym.scope}</td>
                                    <td className="sym-range"><code>{sym.startIndex} ➔ {sym.stopIndex}</code></td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}

              {selectedTab === 'graph' && (
                <div className="view-panel graph-panel">
                  {!visualizationData ? (
                    <div className="empty-state">
                      <GitFork size={48} className="empty-icon" />
                      <p>Run compilation to explore interactive React Flow graph</p>
                    </div>
                  ) : (
                    <div className="graph-panel-content" style={{ display: 'flex', flexDirection: 'column', height: '500px', position: 'relative' }}>
                      <div className="ast-tree-toolbar" style={{ borderBottomLeftRadius: 0, borderBottomRightRadius: 0, marginBottom: 0 }}>
                        <div className="ast-tree-search">
                          <Search size={14} className="search-icon" />
                          <input
                            type="text"
                            placeholder="Search graph nodes..."
                            value={canvasSearchQuery}
                            onChange={(e) => setCanvasSearchQuery(e.target.value)}
                          />
                          {canvasSearchQuery && (
                            <button className="clear-search-btn" onClick={() => setCanvasSearchQuery('')} style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }}>×</button>
                          )}
                        </div>
                        <div className="ast-tree-actions">
                          <button className="mini-action-btn" onClick={() => setFullscreenMode('graph')} title="Fullscreen"><LayoutPanelTop size={14} /></button>
                        </div>
                      </div>
                      <div style={{ flex: 1, position: 'relative', border: '1px solid var(--border-color)', borderTop: 'none', borderRadius: '0 0 12px 12px', overflow: 'hidden' }}>
                        <ASTGraph 
                          visualizationData={visualizationData} 
                          searchQuery={canvasSearchQuery}
                          onSearchQueryChange={setCanvasSearchQuery}
                          selectedASTNode={selectedASTNode}
                          onSelectNode={(node) => {
                            setSelectedASTNode(node);
                            highlightRange(node.startIndex, node.stopIndex);
                          }}
                          isFullscreen={false}
                        />
                      </div>
                    </div>
                  )}
                </div>
              )}

              {selectedTab === 'diagnostics' && (
                <div className="view-panel diagnostics-panel">
                  {!visualizationData ? (
                    <div className="empty-state">
                      <ShieldAlert size={48} className="empty-icon" />
                      <p>Run compilation to scan for compiler warnings & diagnostics</p>
                    </div>
                  ) : (
                    <div className="diagnostics-container">
                      <div className="info-bar">
                        <Info size={16} />
                        <span>Click on any error or warning to locate the source code ranges and squiggly lines instantly!</span>
                      </div>

                      {visualizationData.errors && visualizationData.errors.length > 0 ? (
                        <div className="errors-list">
                          {visualizationData.errors.map((err, idx) => {
                            const isWarning = err.severity === 'WARNING';
                            return (
                              <div
                                key={idx}
                                className={`error-card ${isWarning ? 'warning' : 'danger'}`}
                                onClick={() => highlightRange(err.startIndex, err.stopIndex)}
                              >
                                <div className="error-card-header">
                                  <span className={`severity-badge ${isWarning ? 'yellow' : 'red'}`}>
                                    {err.severity}
                                  </span>
                                  <span className="error-card-pos">
                                    Line {err.line > 0 ? err.line : 'Unknown'}, Column {err.column >= 0 ? err.column : 'Unknown'}
                                  </span>
                                </div>
                                <div className="error-card-body">
                                  <p className="error-msg">{err.message}</p>
                                  {err.startIndex !== undefined && err.stopIndex !== undefined && (
                                    <div className="error-range">
                                      Span Range: <code>{err.startIndex} ➔ {err.stopIndex}</code>
                                    </div>
                                  )}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      ) : (
                        <div className="clean-diagnostics">
                          <Check size={48} className="success-icon" />
                          <h4>No Issues Found</h4>
                          <p>The parser built the AST successfully and semantic diagnostics completed with a clean bill of health!</p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </main>

      {/* Fullscreen Overlay for Tree Explorer */}
      {fullscreenMode === 'tree' && (
        <div className="tree-view-fullscreen-overlay">
          <div className="fullscreen-toolbar">
            <div className="toolbar-left">
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Binary size={18} className="logo-icon" />
                AST Tree Explorer (Fullscreen)
              </h3>
            </div>
            <div className="toolbar-middle">
              <div className="fullscreen-search-box">
                <Search size={16} className="search-icon" />
                <input
                  type="text"
                  placeholder="Search tree nodes..."
                  value={treeSearchQuery}
                  onChange={(e) => setTreeSearchQuery(e.target.value)}
                />
                {matchingNodeIds.length > 0 && (
                  <span className="search-results-count">
                    {currentMatchIndex + 1} of {matchingNodeIds.length}
                  </span>
                )}
                <button 
                  className="search-nav-btn" 
                  onClick={prevSearchMatch}
                  disabled={matchingNodeIds.length === 0}
                  title="Previous match"
                >
                  ▲
                </button>
                <button 
                  className="search-nav-btn" 
                  onClick={nextSearchMatch}
                  disabled={matchingNodeIds.length === 0}
                  title="Next match"
                >
                  ▼
                </button>
                {treeSearchQuery && (
                  <button className="clear-search-btn" onClick={() => setTreeSearchQuery('')}>×</button>
                )}
              </div>
            </div>
            <div className="toolbar-right">
              <button className="toolbar-btn" onClick={expandAll} title="Expand All">📂 Expand All</button>
              <button className="toolbar-btn" onClick={collapseAll} title="Collapse All">📁 Collapse All</button>
              <button className="toolbar-btn" onClick={() => setTreeZoom(z => Math.min(2.5, z + 0.1))} title="Zoom In"><ZoomIn size={16} /></button>
              <button className="toolbar-btn" onClick={() => setTreeZoom(z => Math.max(0.5, z - 0.1))} title="Zoom Out"><ZoomOut size={16} /></button>
              <button className="toolbar-btn" onClick={() => { setTreeZoom(1); setTreePan({ x: 0, y: 0 }); }} title="Reset Zoom">Reset View</button>
              <button className="toolbar-btn exit-btn" onClick={() => setFullscreenMode(null)} title="Exit Fullscreen">Exit</button>
            </div>
          </div>
          
          <div className="fullscreen-content-split">
            <div 
              className="tree-scroll-container"
              onWheel={handleTreeWheel}
              onMouseDown={handleTreeMouseDown}
              onMouseMove={handleTreeMouseMove}
              onMouseUp={handleTreeMouseUp}
              onMouseLeave={handleTreeMouseUp}
              style={{
                cursor: isTreePanning ? 'grabbing' : 'grab'
              }}
            >
              <div 
                className="tree-zoomable-content"
                style={{ 
                  transform: `translate(${treePan.x}px, ${treePan.y}px) scale(${treeZoom})`,
                  transformOrigin: 'top left'
                }}
              >
                {treeRoot && (
                  <ASTNodeTree
                    node={treeRoot}
                    openNodes={openNodes}
                    onToggle={(id) => {
                      setOpenNodes(prev => {
                        const next = new Set(prev);
                        if (next.has(id)) next.delete(id);
                        else next.add(id);
                        return next;
                      });
                    }}
                    onSelect={(node) => {
                      setSelectedASTNode(node);
                      highlightRange(node.startIndex, node.stopIndex);
                    }}
                    activeNode={selectedASTNode}
                    highlightedNodes={new Set(matchingNodeIds)}
                    focusedNodeId={matchingNodeIds[currentMatchIndex]}
                  />
                )}
              </div>
            </div>
            
            <div className="ast-properties-inspector fullscreen-inspector">
              <h4>Properties Inspector</h4>
              {selectedASTNode ? (
                <div className="properties-wrapper">
                  <div className="property-group">
                    <span className="prop-key">Class</span>
                    <span className="prop-value strong">{selectedASTNode.type}</span>
                  </div>
                  <div className="property-group">
                    <span className="prop-key">Label</span>
                    <span className="prop-value">{selectedASTNode.label}</span>
                  </div>
                  <div className="property-group">
                    <span className="prop-key">Range</span>
                    <span className="prop-value code">
                      {selectedASTNode.startIndex} ➔ {selectedASTNode.stopIndex} ({selectedASTNode.stopIndex - selectedASTNode.startIndex + 1} chars)
                    </span>
                  </div>

                  {selectedASTNode.properties && Object.keys(selectedASTNode.properties).length > 0 ? (
                    <div className="dynamic-properties">
                      <h5>Semantic Attributes</h5>
                      {Object.entries(selectedASTNode.properties).map(([key, val]) => (
                        <div className="property-group" key={key}>
                          <span className="prop-key">{key}</span>
                          <span className="prop-value italic">
                            {typeof val === 'object' ? JSON.stringify(val) : String(val)}
                          </span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="no-attributes">
                      <p>No extra attributes for this node class.</p>
                    </div>
                  )}
                </div>
              ) : (
                <div className="properties-empty">
                  <p>No node selected.</p>
                  <p className="subtext">Click any AST node in the tree list to view its semantic attributes and precise span range.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Fullscreen Overlay for Graph Explorer */}
      {fullscreenMode === 'graph' && visualizationData && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', zIndex: 9999 }}>
          <ASTGraph 
            visualizationData={visualizationData} 
            searchQuery={canvasSearchQuery}
            onSearchQueryChange={setCanvasSearchQuery}
            selectedASTNode={selectedASTNode}
            onSelectNode={(node) => {
              setSelectedASTNode(node);
              highlightRange(node.startIndex, node.stopIndex);
            }}
            isFullscreen={true}
            onExitFullscreen={() => setFullscreenMode(null)}
          />
        </div>
      )}

      <footer className="footer">
        <p>by- SUPREME</p>
        <p>Asmit Bhandari • Harshit Gahlot • Kaushal Thakur • Srishti Rana</p>
      </footer>
        </>
      )}
    </div>
  );
}

export default App;
