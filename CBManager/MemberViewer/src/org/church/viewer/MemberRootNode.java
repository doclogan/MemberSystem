/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.church.viewer;

/**
 *
 * @author derrick
 */
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import static org.church.viewer.Bundle.*;
    
public class MemberRootNode extends AbstractNode {

    @Messages("CTRL_RootName=Root")
    public MemberRootNode(Children kids) {
        super(kids);
        setDisplayName(CTRL_RootName());
}

    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> actionsForMember = Utilities.actionsForPath("Actions/Member1");
        return actionsForMember.toArray(new Action[actionsForMember.size()]);
    }
}

