package jsyntaxpane.actions;

import javax.swing.text.JTextComponent;

/**
 *
 * @author JPEXS
 */
public interface QuickFindHandler {
    public void showQuickFind(final JTextComponent target, DocumentSearchData dsd);
}
