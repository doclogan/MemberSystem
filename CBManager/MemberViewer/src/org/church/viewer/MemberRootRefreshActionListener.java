/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.church.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author derrick
 */
    
@ActionID(id="org.shop.viewer.MemberRootRefreshActionListener", category="Member1")
@ActionRegistration(displayName="#CTL_MemberRootRefreshActionListener")
@Messages("CTL_MemberRootRefreshActionListener=Refresh")
public class MemberRootRefreshActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        MemberViewerTopComponent.refreshNode();
}
    
}
