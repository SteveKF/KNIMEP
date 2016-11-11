package org.knime.preference.extract;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PreferenceSQLExtract" Node.
 * Extracts Preference SQL Queries
 *
 * @author Stefan Wohlfart
 */
public class PreferenceSQLExtractNodeFactory 
        extends NodeFactory<PreferenceSQLExtractNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PreferenceSQLExtractNodeModel createNodeModel() {
        return new PreferenceSQLExtractNodeModel();
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
    public NodeView<PreferenceSQLExtractNodeModel> createNodeView(final int viewIndex,
            final PreferenceSQLExtractNodeModel nodeModel) {
        return new PreferenceSQLExtractNodeView(nodeModel);
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
        return new PreferenceSQLExtractNodeDialog();
    }

}

