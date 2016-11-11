package org.knime.preferences.sql;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PreferenceSQL" Node.
 * Output for Preference SQL
 *
 * @author Stefan Wohlfart
 */
public class PreferenceSQLNodeFactory 
        extends NodeFactory<PreferenceSQLNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PreferenceSQLNodeModel createNodeModel() {
        return new PreferenceSQLNodeModel();
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
    public NodeView<PreferenceSQLNodeModel> createNodeView(final int viewIndex,
            final PreferenceSQLNodeModel nodeModel) {
        return new PreferenceSQLNodeView(nodeModel);
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
        return new PreferenceSQLNodeDialog();
    }

}

