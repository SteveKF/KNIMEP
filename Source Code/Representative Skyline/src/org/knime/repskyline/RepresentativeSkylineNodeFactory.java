package org.knime.repskyline;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "RepresentativeSkyline" Node.
 * An algorithm which computes a k-representative skyline based on significance and diversity. * n
 *
 * @author Stefan Wohlfart
 */
public class RepresentativeSkylineNodeFactory 
        extends NodeFactory<RepresentativeSkylineNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RepresentativeSkylineNodeModel createNodeModel() {
        return new RepresentativeSkylineNodeModel();
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
    public NodeView<RepresentativeSkylineNodeModel> createNodeView(final int viewIndex,
            final RepresentativeSkylineNodeModel nodeModel) {
        return new RepresentativeSkylineNodeView(nodeModel);
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
        return new RepresentativeSkylineNodeDialog();
    }

}

