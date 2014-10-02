/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.church.editor;

/**
 *
 * @author derrick
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(id="org.church.editor.MemberNewActionListener", category="File")
@ActionRegistration(displayName="#CTL_MemberNewActionListener")
@ActionReference(path="Menu/File", position=10)
@Messages("CTL_MemberNewActionListener=New")
public final class MemberNewActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        MemberEditorTopComponent tc = (MemberEditorTopComponent) WindowManager.getDefault().findTopComponent("MemberEditorTopComponent");
        tc.resetFields();
        tc.open();
        tc.requestActive();
    }

}
