package com.transpiler.ir;

/**
 * Base abstract class for all Intermediate Representation (IR) nodes.
 */
public abstract class IRNode {
    private int startIndex;
    private int stopIndex;

    /**
     * Default constructor for IRNode.
     */
    protected IRNode() {
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getStopIndex() {
        return stopIndex;
    }

    public void setStopIndex(int stopIndex) {
        this.stopIndex = stopIndex;
    }

    /**
     * Provides a string representation of the node for debugging and logging.
     *
     * @return String representation of the node.
     */
    @Override
    public abstract String toString();
}
