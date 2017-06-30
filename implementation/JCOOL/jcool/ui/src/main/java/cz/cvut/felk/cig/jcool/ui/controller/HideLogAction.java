/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cvut.felk.cig.jcool.ui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.apache.commons.lang.SystemUtils;

/**
 *
 * @author ytoh
 */
public class HideLogAction extends AbstractAction {
    private static final String SHOW = "Show log";
    private static final String HIDE = "Hide log";
    public static final String COMMAND_KEY = HideLogAction.class + " [toggle]";

    private boolean hidden;

    public HideLogAction() {
        super.putValue(AbstractAction.ACTION_COMMAND_KEY, COMMAND_KEY);
        if(SystemUtils.IS_OS_MAC) {
            super.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.META_DOWN_MASK));
        } else {
            super.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        }
        hidden = false;
        update();
    }

    private void update() {
        super.putValue(AbstractAction.NAME, hidden ? SHOW : HIDE);
    }

    public void actionPerformed(ActionEvent e) {
        if(COMMAND_KEY.equals(e.getActionCommand())) {
            boolean old = hidden;
            hidden = !hidden;
            update();
            firePropertyChange(COMMAND_KEY, old, hidden);
        }
    }
}
