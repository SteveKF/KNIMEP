package org.knime.preferences.distance;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DistanceBasedResolver" Node.
 * 
 *
 * @author Stefan Wohlfart
 */
public class DistanceBasedResolverNodeFactory 
        extends NodeFactory<DistanceBasedResolverNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DistanceBasedResolverNodeModel createNodeModel() {
        return new DistanceBasedResolverNodeModel();
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
    public NodeView<DistanceBasedResolverNodeModel> createNodeView(final int viewIndex,
            final DistanceBasedResolverNodeModel nodeModel) {
        return new DistanceBasedResolverNodeView(nodeModel);
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
        return new DistanceBasedResolverNodeDialog();
    }

}

