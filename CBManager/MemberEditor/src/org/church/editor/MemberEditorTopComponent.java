/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.church.editor;

import demo.Member1;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.Icon;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.UndoRedo;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.church.editor//MemberEditor//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MemberEditorTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "org.church.editor.MemberEditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MemberEditorAction",
        preferredID = "MemberEditorTopComponent"
)
@Messages({
    "CTL_MemberEditorAction=MemberEditor",
    "CTL_MemberEditorTopComponent=MemberEditor Window",
    "HINT_MemberEditorTopComponent=This is a MemberEditor window"
})
public final class MemberEditorTopComponent extends TopComponent implements LookupListener{
    private Lookup.Result result = null;
    private UndoRedo.Manager manager = new UndoRedo.Manager();
    InstanceContent ic = new InstanceContent();
    Member1 member;
    public MemberEditorTopComponent() {
        initComponents();
        setName(Bundle.CTL_MemberEditorTopComponent());
        setToolTipText(Bundle.HINT_MemberEditorTopComponent());
        associateLookup(new AbstractLookup(ic));
        firstName_field.getDocument().addUndoableEditListener(manager);
        lastName_field.getDocument().addUndoableEditListener(manager);
        address1_field.getDocument().addUndoableEditListener(manager);
        address2_field.getDocument().addUndoableEditListener(manager);
        sex_field.getDocument().addUndoableEditListener(manager);
        position_field.getDocument().addUndoableEditListener(manager);
        firstName_field.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            modify();
        }
        });
        lastName_field.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                modify();
            }
        });
        address1_field.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                modify();
            }
        });
        address2_field.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                modify();
            }
        });
        sex_field.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                modify();
            }
        });
        position_field.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                modify();
            }
        });
    }
    private void modify() {
        if (getLookup().lookup(MySavable.class) == null) {
            ic.add(new MySavable());
        }
    }
    private static final Icon ICON = ImageUtilities.loadImageIcon("org/shop/editor/Icon.png", true);

    void resetFields() {
        member = new Member1();
        firstName_field.setText("");
        lastName_field.setText("");
        address1_field.setText("");
        address2_field.setText("");
        position_field.setText("");
        sex_field.setText("");

    }

    private class MySavable extends AbstractSavable implements Icon {

        MySavable() {
            register();
        }

        @Override
        protected String findDisplayName() {
            String fname = firstName_field.getText();
            String lname = lastName_field.getText();
            return fname + " " + lname;
        }

        @Override
        protected void handleSave() throws IOException {
            Confirmation message = new NotifyDescriptor.Confirmation("Do you want to save \""
                    + firstName_field.getText() + " (" + lastName_field.getText() + ")\"?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(message);
            //When user clicks "Yes", indicating they really want to save,
            //we need to disable the Save action,
            //so that it will only be usable when the next change is made
            //to the JTextArea:
            if (NotifyDescriptor.YES_OPTION.equals(result)) {
                //Handle the save here...
                EntityManager entityManager = Persistence.createEntityManagerFactory("MemberLibraryPU").createEntityManager();
                entityManager.getTransaction().begin();
                if (member.getIdmember() != null) {
                    Member1 m = entityManager.find(Member1.class, member.getIdmember());
                    m.setFirstName(firstName_field.getText());
                    m.setLastName(lastName_field.getText());
                    m.setMemAddress1(address1_field.getText());
                    m.setMemAddress2(address2_field.getText());
                    m.setMemPosition(position_field.getText());
                    m.setSex(sex_field.getText());
                    entityManager.getTransaction().commit();
                    tc().ic.remove(this);
                    unregister();
                } else {
                    Query query = entityManager.createNamedQuery("Member1.findAll");
                    List<Member1> resultList = query.getResultList();
                    member.setIdmember(resultList.size()+1);
                    member.setFirstName(firstName_field.getText());
                    member.setLastName(lastName_field.getText());
                    member.setMemAddress1(address1_field.getText());
                    member.setMemAddress2(address2_field.getText());
                    member.setMemPosition(position_field.getText());
                    member.setSex(sex_field.getText());
                    //add more fields that will populate all the other columns in the table!
                    entityManager.persist(member);
                    entityManager.getTransaction().commit();
                }
                
            }
        }

        MemberEditorTopComponent tc() {
            return MemberEditorTopComponent.this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MySavable) {
                MySavable m = (MySavable) obj;
                return tc() == m.tc();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return tc().hashCode();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            ICON.paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return ICON.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return ICON.getIconHeight();
        }

    }
    @Override
    public void resultChanged(LookupEvent lookupEvent) {
        Lookup.Result r = (Lookup.Result) lookupEvent.getSource();
        Collection<Member1> coll = r.allInstances();
        if (!coll.isEmpty()) {
            for (Member1 mem : coll) {
                member = mem;
                firstName_field.setText(mem.getFirstName());
                lastName_field.setText(mem.getLastName());
                address1_field.setText(mem.getMemAddress1());
                address2_field.setText(mem.getMemAddress2());
                sex_field.setText(mem.getSex());
                position_field.setText(mem.getMemPosition());
            }
        } else {
            firstName_field.setText("[no name]");
            lastName_field.setText("[no city]");
            address1_field.setText("[no address1]");
            address2_field.setText("[no address2]");
            sex_field.setText("[no sex]");
            position_field.setText("[no position]");
        }
    }
    @Override
    public UndoRedo getUndoRedo() {
        return manager;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCalendar1 = new com.toedter.calendar.JCalendar();
        jCalendar2 = new com.toedter.calendar.JCalendar();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        firstName_field = new javax.swing.JTextField();
        lastName_field = new javax.swing.JTextField();
        address1_field = new javax.swing.JTextField();
        address2_field = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        sex_field = new javax.swing.JTextField();
        position_field = new javax.swing.JTextField();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.jLabel4.text")); // NOI18N

        firstName_field.setText(org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.firstName_field.text")); // NOI18N

        lastName_field.setText(org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.lastName_field.text")); // NOI18N

        address1_field.setText(org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.address1_field.text")); // NOI18N

        address2_field.setText(org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.address2_field.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(firstName_field, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lastName_field)
                            .addComponent(address2_field)
                            .addComponent(address1_field)))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(firstName_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lastName_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(address1_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(address2_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(220, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.jLabel7.text")); // NOI18N

        sex_field.setText(org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.sex_field.text")); // NOI18N

        position_field.setText(org.openide.util.NbBundle.getMessage(MemberEditorTopComponent.class, "MemberEditorTopComponent.position_field.text")); // NOI18N
        position_field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                position_fieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(42, 42, 42)
                                .addComponent(sex_field, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(position_field)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(22, 22, 22)
                        .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(sex_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(position_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(107, 107, 107)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void position_fieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_position_fieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_position_fieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField address1_field;
    private javax.swing.JTextField address2_field;
    private javax.swing.JTextField firstName_field;
    private com.toedter.calendar.JCalendar jCalendar1;
    private com.toedter.calendar.JCalendar jCalendar2;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField lastName_field;
    private javax.swing.JTextField position_field;
    private javax.swing.JTextField sex_field;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        result = WindowManager.getDefault().findTopComponent("MemberViewerTopComponent").getLookup().lookupResult(Member1.class);
        result.addLookupListener(this);
        resultChanged(new LookupEvent(result));
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        result.removeLookupListener(this);
        result = null;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
