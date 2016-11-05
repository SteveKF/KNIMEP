package org.knime.preferences.prefCreator;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PreferenceCreator" Node.
 * Sets up the Scores for the Preference SQL node.
 *
 * @author Stefan Wohlfart
 */
public class PreferenceCreatorNodeFactory 
        extends NodeFactory<PreferenceCreatorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PreferenceCreatorNodeModel createNodeModel() {
        return new PreferenceCreatorNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<PreferenceCreatorNodeModel> createNodeView(final int viewIndex,
            final PreferenceCreatorNodeModel nodeModel) {
        return new PreferenceCreatorNodeView(nodeModel);
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
        return new PreferenceCreatorNodeDialog();
    }

}

