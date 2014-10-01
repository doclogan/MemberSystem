/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.church.toolbar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "File",
        id = "org.church.toolbar.CustomActionListener"
)
@ActionRegistration(
        lazy = false,        
        displayName = "Not Used"
)
@ActionReference(path = "Toolbars/File", position = 500)
@Messages("CTL_CustomActionListener=CustomToolbar")
public final class CustomActionListener extends AbstractAction implements Presenter.Toolbar {
    @Override
    public Component getToolbarPresenter() {
        return new SabbathDatePanel();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }
}
