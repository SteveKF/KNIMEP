package org.knime.preferences.bnl;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "BlockNestedLoop" Node.
 * Uses a Block Nested Loop to get the skyline points of a specific database table
 *
 * @author Stefan Wohlfart
 */
public class BlockNestedLoopNodeFactory 
        extends NodeFactory<BlockNestedLoopNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockNestedLoopNodeModel createNodeModel() {
        return new BlockNestedLoopNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<BlockNestedLoopNodeModel> createNodeView(final int viewIndex,
            final BlockNestedLoopNodeModel nodeModel) {
        return new BlockNestedLoopNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new BlockNestedLoopNodeDialog();
    }

}

