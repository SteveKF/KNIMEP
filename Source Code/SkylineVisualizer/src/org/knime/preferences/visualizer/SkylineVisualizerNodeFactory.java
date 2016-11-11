package org.knime.preferences.visualizer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SkylineVisualizer" Node.
 * A node to visualize skylines and representative skylines.
 *
 * @author Stefan Wohlfart
 */
public class SkylineVisualizerNodeFactory 
        extends NodeFactory<SkylineVisualizerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SkylineVisualizerNodeModel createNodeModel() {
        return new SkylineVisualizerNodeModel();
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
    public NodeView<SkylineVisualizerNodeModel> createNodeView(final int viewIndex,
            final SkylineVisualizerNodeModel nodeModel) {
        return new SkylineVisualizerNodeView(nodeModel);
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
        return new SkylineVisualizerNodeDialog();
    }

}

