import React, { useEffect, useState, useCallback, useRef, useMemo } from 'react';
import ReactFlow, {
  Controls,
  MiniMap,
  Handle,
  Background,
  useNodesState,
  useEdgesState,
  useReactFlow,
  ReactFlowProvider
} from 'reactflow';
import dagre from 'dagre';
import { ZoomIn, ZoomOut, RefreshCw, LayoutPanelTop, Search, GitFork } from 'lucide-react';
import 'reactflow/dist/style.css';

// Semantic colors for node types (matches dark theme)
const NODE_COLORS = {
  FunctionNode: '#3b82f6',     // blue
  FunctionCallNode: '#6366f1', // indigo
  ReturnNode: '#ec4899',       // pink
  AssignmentNode: '#06b6d4',   // cyan
  IfNode: '#f97316',          // orange
  LoopNode: '#8b5cf6',        // purple
  LiteralNode: '#10b981',     // green
  VariableDeclNode: '#a855f7', // purple-magenta
  VariableNode: '#6b7280',    // gray
  default: '#374151'          // dark gray
};

// Custom node component with glassmorphism styling
const ASTNode = ({ data, selected }) => {
  const color = NODE_COLORS[data.nodeType] || NODE_COLORS.default;
  const isSelected = selected;
  return (
    <div style={{
      padding: '8px 12px',
      backgroundColor: 'rgba(13, 17, 23, 0.85)',
      border: isSelected ? '2px solid #6366f1' : '1px solid rgba(255, 255, 255, 0.08)',
      boxShadow: isSelected ? '0 0 15px rgba(99, 102, 241, 0.4)' : '0 4px 12px rgba(0, 0, 0, 0.15)',
      borderRadius: '8px',
      color: '#f0f6fc',
      fontSize: '11px',
      textAlign: 'center',
      minWidth: '130px',
      backdropFilter: 'blur(8px)',
      transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
      borderTop: `4px solid ${color}`
    }} title={`${data.nodeType}: ${data.label}`}
    >
      {/* Handles for React Flow edges */}
      <Handle type="target" position="left" />
      <strong style={{
        color: '#818cf8',
        display: 'block',
        marginBottom: '2px',
        fontFamily: "'Fira Code', monospace",
        fontSize: '9px',
        fontWeight: '800'
      }}>
        {data.nodeType}
      </strong>
      <div style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', fontWeight: '600' }}>
        {data.label || 'empty'}
      </div>
      <Handle type="source" position="right" />
    </div>
  );
};

const nodeTypes = {
  astNode: ASTNode
};

// Helper to flatten AST into nodes/edges using pre-assigned IDs
let nodeIdCounter = 0;
function buildGraph(astNode, parentId = null, nodes = [], edges = []) {
  const nodeId = astNode.id || `node_${nodeIdCounter++}`;
  const type = astNode.type || 'Unknown';
  nodes.push({
    id: nodeId,
    type: 'astNode',
    data: {
      label: astNode.label || '',
      nodeType: type,
      properties: astNode.properties || {},
      astNode: astNode
    },
    position: { x: 0, y: 0 },
    style: { width: 130, height: 50 }
  });

  if (parentId) {
    edges.push({
      id: `e_${parentId}_${nodeId}`,
      source: parentId,
      target: nodeId,
      animated: false,
      style: { stroke: 'rgba(99, 102, 241, 0.3)', strokeWidth: 2 }
    });
  }

  if (astNode.children && astNode.children.length) {
    for (const child of astNode.children) {
      buildGraph(child, nodeId, nodes, edges);
    }
  }
  return { nodes, edges };
}

// Dagre layout helper
function getLayoutedElements(nodes, edges, direction = 'LR') {
  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));
  dagreGraph.setGraph({ rankdir: direction, marginx: 40, marginy: 40, nodesep: 60, ranksep: 80 });

  nodes.forEach(node => {
    dagreGraph.setNode(node.id, { width: 150, height: 60 });
  });

  edges.forEach(edge => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  dagre.layout(dagreGraph);

  const layoutedNodes = nodes.map(node => {
    const position = dagreGraph.node(node.id);
    return {
      ...node,
      position: {
        x: position.x - 75,
        y: position.y - 30
      }
    };
  });

  return { nodes: layoutedNodes, edges };
}

const ASTGraphInner = ({
  visualizationData,
  searchQuery,
  onSearchQueryChange,
  selectedASTNode,
  onSelectNode,
  isFullscreen,
  onExitFullscreen
}) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const { fitView, zoomIn, zoomOut, setViewport } = useReactFlow();
  const hasLoadedRef = useRef(false);

  // Memoize nodeTypes to ensure absolute reference stability across renders (fixes React Flow Warning #002)
  const memoizedNodeTypes = useMemo(() => nodeTypes, []);

  // Effect 1: Generate layout when visualizationData changes
  useEffect(() => {
    if (!visualizationData?.ast) {
      setNodes([]);
      setEdges([]);
      hasLoadedRef.current = false;
      return;
    }
    nodeIdCounter = 0;
    const { nodes: rawNodes, edges: rawEdges } = buildGraph(visualizationData.ast);
    const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(rawNodes, rawEdges);
    setNodes(layoutedNodes);
    setEdges(layoutedEdges);
    hasLoadedRef.current = false;
  }, [visualizationData, setNodes, setEdges]);

  // Effect 2: Apply selection and search query styling. Decoupled from `nodes` state updates to prevent recursive rendering loops.
  useEffect(() => {
    setNodes(currentNodes => {
      if (currentNodes.length === 0) return currentNodes;

      const q = searchQuery?.toLowerCase() || '';
      return currentNodes.map(node => {
        const isSelected = selectedASTNode && node.id === selectedASTNode.id;
        const { label, nodeType, properties } = node.data;
        const propString = JSON.stringify(properties).toLowerCase();
        const isMatch = q && (
          (label && label.toLowerCase().includes(q)) ||
          (nodeType && nodeType.toLowerCase().includes(q)) ||
          propString.includes(q)
        );

        return {
          ...node,
          selected: isSelected,
          style: {
            ...node.style,
            boxShadow: isMatch ? '0 0 15px #ff9b50' : undefined,
            border: isMatch ? '2px dashed #ff9b50' : undefined
          }
        };
      });
    });
  }, [selectedASTNode, searchQuery, visualizationData, setNodes]);

  // Effect 3: Automatic fitView on load
  useEffect(() => {
    if (nodes.length > 0 && !hasLoadedRef.current) {
      const timer = setTimeout(() => {
        fitView({ padding: 0.2, duration: 400 });
        hasLoadedRef.current = true;
      }, 150);
      return () => clearTimeout(timer);
    }
  }, [nodes, fitView]);

  // Effect 4: Camera focus/centering on selected or searched node
  useEffect(() => {
    if (nodes.length === 0) return;

    // Center on selected node
    if (selectedASTNode) {
      const matched = nodes.find(n => n.id === selectedASTNode.id);
      if (matched) {
        const timer = setTimeout(() => {
          fitView({ nodes: [matched], duration: 400, maxZoom: 1.2 });
        }, 50);
        return () => clearTimeout(timer);
      }
    }

    // Otherwise center on first search match
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      const firstMatch = nodes.find(node => {
        const { label, nodeType, properties } = node.data;
        const propString = JSON.stringify(properties).toLowerCase();
        return (label && label.toLowerCase().includes(q)) ||
          (nodeType && nodeType.toLowerCase().includes(q)) ||
          propString.includes(q);
      });

      if (firstMatch) {
        const timer = setTimeout(() => {
          fitView({ nodes: [firstMatch], duration: 400, maxZoom: 1.2 });
        }, 100);
        return () => clearTimeout(timer);
      }
    }
  }, [selectedASTNode, searchQuery, nodes, fitView]);

  // Custom wheel handler for faster zoom
  const handleWheel = useCallback((event) => {
    event.preventDefault();
    const { deltaY } = event;
    const zoomFactor = deltaY > 0 ? 0.9 : 1.1; // faster step
    setViewport(prev => ({ ...prev, zoom: prev.zoom * zoomFactor }), { duration: 200 });
  }, [setViewport]);

  const onNodeClick = useCallback((event, node) => {
    if (onSelectNode) {
      onSelectNode(node.data.astNode);
    }
  }, [onSelectNode]);

  const resetView = useCallback(() => {
    setViewport({ x: 100, y: 50, zoom: 0.85 }, { duration: 400 });
  }, [setViewport]);

  const selectedNode = nodes.find(n => selectedASTNode && n.id === selectedASTNode.id);

  return (
    <div className={`graph-view-container ${isFullscreen ? 'fullscreen' : ''}`} style={{ display: 'flex', height: '100%', flexDirection: 'column', width: '100%', position: 'relative' }} onWheel={handleWheel}>
      {isFullscreen && (
        <div className="fullscreen-toolbar">
          <div className="toolbar-left">
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <GitFork size={18} className="logo-icon" />
              AST Graph Explorer (Fullscreen)
            </h3>
          </div>
          <div className="toolbar-middle">
            <div className="fullscreen-search-box">
              <Search size={16} className="search-icon" />
              <input
                type="text"
                placeholder="Search graph nodes..."
                value={searchQuery}
                onChange={(e) => onSearchQueryChange && onSearchQueryChange(e.target.value)}
              />
              {searchQuery && (
                <button className="clear-search-btn" onClick={() => onSearchQueryChange && onSearchQueryChange('')}>×</button>
              )}
            </div>
          </div>
          <div className="toolbar-right">
            <button className="toolbar-btn" onClick={() => zoomIn({ duration: 200 })} title="Zoom In"><ZoomIn size={16} /></button>
            <button className="toolbar-btn" onClick={() => zoomOut({ duration: 200 })} title="Zoom Out"><ZoomOut size={16} /></button>
            <button className="toolbar-btn" onClick={() => fitView({ duration: 400 })} title="Fit View">Fit View</button>
            <button className="toolbar-btn" onClick={resetView} title="Reset View">Reset View</button>
            <button className="toolbar-btn exit-btn" onClick={onExitFullscreen} title="Exit Fullscreen">Exit</button>
          </div>
        </div>
      )}

      <div style={{ display: 'flex', flex: 1, position: 'relative', height: isFullscreen ? 'calc(100vh - 70px)' : '100%' }}>
        <div style={{ flex: 1, height: '100%', position: 'relative' }}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            nodeTypes={memoizedNodeTypes}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onNodeClick={onNodeClick}
            attributionPosition="bottom-right"
            style={{ width: '100%', height: '100%', background: 'transparent' }}
          >
            <Controls showInteractive={false} />
            <MiniMap nodeColor={n => NODE_COLORS[n.data?.nodeType] || NODE_COLORS.default} />
            <Background color="#444" gap={16} />
          </ReactFlow>
        </div>

        {selectedNode && (
          <div className="graph-inspector ast-properties-inspector" style={{
            width: '320px',
            display: 'flex',
            flexDirection: 'column',
            background: 'rgba(13, 17, 23, 0.9)',
            backdropFilter: 'blur(10px)',
            borderLeft: '1px solid var(--border-color)',
            height: '100%',
            padding: '1.25rem',
            overflowY: 'auto'
          }}>
            <h4>Node Inspector</h4>
            <div className="properties-wrapper">
              <div className="property-group">
                <span className="prop-key">Class</span>
                <span className="prop-value strong">{selectedNode.data.nodeType}</span>
              </div>
              <div className="property-group">
                <span className="prop-key">Label</span>
                <span className="prop-value">{selectedNode.data.label}</span>
              </div>
              <div className="property-group">
                <span className="prop-key">Range</span>
                <span className="prop-value code">
                  {selectedNode.data.astNode.startIndex} ➔ {selectedNode.data.astNode.stopIndex} ({selectedNode.data.astNode.stopIndex - selectedNode.data.astNode.startIndex + 1} chars)
                </span>
              </div>

              {selectedNode.data.properties && Object.keys(selectedNode.data.properties).length > 0 ? (
                <div className="dynamic-properties">
                  <h5>Semantic Attributes</h5>
                  {Object.entries(selectedNode.data.properties).map(([key, val]) => (
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
          </div>
        )}
      </div>
    </div>
  );
};

const ASTGraph = (props) => {
  return (
    <ReactFlowProvider>
      <ASTGraphInner {...props} />
    </ReactFlowProvider>
  );
};

export default ASTGraph;
