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
import demo.Member1;
import java.beans.IntrospectionException;
import java.util.List;
import org.openide.nodes.BeanNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

public class MemberChildFactory extends ChildFactory<Member1> {

    private List<Member1> resultList;

    public MemberChildFactory(List<Member1> resultList) {
        this.resultList = resultList;
    }

    @Override
    protected boolean createKeys(List<Member1> list) {
        for (Member1 memlist : resultList) {
            list.add(memlist);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(Member1 m) {
        try {
            BeanNode bn = new MemberBeanNode(m);
            bn.setDisplayName(m.getFirstName()+" "+m.getLastName());
            return bn;
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    private class MemberBeanNode extends BeanNode {
    public MemberBeanNode(Member1 bean) throws IntrospectionException {
        super(bean, Children.LEAF, Lookups.singleton(bean));
    }
}
}