package org.knime.preferences.maximizer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DominationMaximizer" Node.
 * 
 *
 * @author Stefan Wohlfart
 */
public class DominationMaximizerNodeFactory 
        extends NodeFactory<DominationMaximizerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DominationMaximizerNodeModel createNodeModel() {
        return new DominationMaximizerNodeModel();
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
    public NodeView<DominationMaximizerNodeModel> createNodeView(final int viewIndex,
            final DominationMaximizerNodeModel nodeModel) {
        return new DominationMaximizerNodeView(nodeModel);
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
        return new DominationMaximizerNodeDialog();
    }

}

