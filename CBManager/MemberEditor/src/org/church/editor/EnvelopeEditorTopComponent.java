/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.church.editor;

import demo.ContribTypes;
import demo.Contribution;
import demo.Envelope;
import demo.Joint;
import demo.Member1;
import demo.Memenv;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import org.church.parameters.Parameters;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
        dtd = "-//org.church.editor//EnvelopeEditor//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "EnvelopeEditorTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "org.church.editor.EnvelopeEditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_EnvelopeEditorAction",
        preferredID = "EnvelopeEditorTopComponent"
)
@Messages({
    "CTL_EnvelopeEditorAction=EnvelopeEditor",
    "CTL_EnvelopeEditorTopComponent=EnvelopeEditor Window",
    "HINT_EnvelopeEditorTopComponent=This is a EnvelopeEditor window"
})
public final class EnvelopeEditorTopComponent extends TopComponent implements LookupListener{
    private Lookup.Result result = null;
    private UndoRedo.Manager manager = new UndoRedo.Manager();
    InstanceContent ic = new InstanceContent();
    Envelope envelope;
     Member1 member;
     Joint jt;
    ContribDataModel ctModel;
    public EnvelopeEditorTopComponent() {
        initComponents();
        setName(Bundle.CTL_EnvelopeEditorTopComponent());
        setToolTipText(Bundle.HINT_EnvelopeEditorTopComponent());
        associateLookup(new AbstractLookup(ic));
        total_field.getDocument().addUndoableEditListener(manager);
        total_field.addKeyListener(new KeyAdapter() {
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
    void resetFields() {
        envelope = new Envelope();
        total_field.setText("");
    }
    
    private static final Icon ICON = ImageUtilities.loadImageIcon("org/shop/editor/Icon.png", true);
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
            NotifyDescriptor.Confirmation message = new NotifyDescriptor.Confirmation("Do you want to save \""
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
                if (envelope.getIdenvelope() != null) {
                    Envelope env = entityManager.find(Envelope.class, envelope.getIdenvelope());
                    env.setTotal(new Integer(total_field.getText()));
                    env.setContributionList(ctModel.getlc());
                    entityManager.merge(env);
                    entityManager.getTransaction().commit();
                    tc().ic.remove(this);
                    unregister();
                } else {
                    Query query = entityManager.createNamedQuery("Envelope.findAll");
                    List<Envelope> resultList = query.getResultList();
                    envelope.setIdenvelope(resultList.size()+1);
                    envelope.setTotal(new Integer(total_field.getText()));
                    query = entityManager.createNamedQuery("Contribution.findAll");
                    List<Contribution> resultListContrib = query.getResultList();
                    Integer cId = resultListContrib.size()+1;
                    List<Contribution> lc = ctModel.getlc();                    
                    for (Contribution ct : lc) {
                        ct.setIdcontribution(cId);
                        ct.setIdenvelope(envelope);
                        cId = cId + 1;
                    }
                    envelope.setContributionList(lc);
                    query = entityManager.createNamedQuery("Joint.findAll");
                    List<Joint> resultListJoint = query.getResultList();
                    Integer jId = resultListJoint.size()+1;
                    jt.setIdjoint(jId);
                    jt.setIdenvelope(envelope);
                    jt.setIdmember(member);
                    List<Joint> jtList = new ArrayList();
                    jtList.add(jt);
                    envelope.setJointList(jtList);
                    jtList = member.getJointList();
                    jtList.add(jt);
                    //add more fields that will populate all the other columns in the table!
                    entityManager.persist(envelope);
                    entityManager.merge(member);
                    entityManager.getTransaction().commit();
                }               
            }
        }
        EnvelopeEditorTopComponent tc() {
            return EnvelopeEditorTopComponent.this;
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
                envelope = getEnvelope(mem);
                if (envelope != null){
                    total_field.setText(envelope.getTotal().toString());
                    ctModel = new ContribDataModel(envelope.getContributionList());
                    contribTable.setModel(ctModel);
                    setColModel(contribTable);                   
                } else {
                    total_field.setText("0.00");
                    Contribution ct = new Contribution();
                    ContribTypes ctType = new ContribTypes("Tithe");
                    ct.setIdcontribTypes(ctType);
                    ct.setContribAmt(BigDecimal.ZERO);
                    List cList = new ArrayList();
                    cList.add(ct);
                    ctModel = new ContribDataModel(cList);
                    contribTable.setModel(ctModel);
                    setColModel(contribTable);
                    envelope = new Envelope();
                    jt = new Joint();
                }
            }
        } else {
            firstName_field.setText("[no name]");
            lastName_field.setText("[no city]");
            address1_field.setText("[no address1]");
            address2_field.setText("[no address2]");
        }
    }
    @Override
    public UndoRedo getUndoRedo() {
        return manager;
    }
    public Envelope getEnvelope(Member1 mem){
        EntityManager em = Persistence.createEntityManagerFactory("MemberLibraryPU").createEntityManager();
        try {
            TypedQuery<Memenv> tquery = em.createQuery(
            "SELECT m FROM Memenv m WHERE m.envDate = :envDate and m.idmember = :idmember", Memenv.class);
            Date date_param = Parameters.getInstance().getSabDate();
            Memenv menv = tquery.setParameter("idmember", mem.getIdmember()).setParameter("envDate",date_param).getSingleResult();
            Query query = em.createNamedQuery("Envelope.findByIdenvelope");
            return (Envelope) query.setParameter("idenvelope", menv.getIdenvelope()).getSingleResult();
        } catch(NoResultException e) {
            return null;
        }
    }
    public javax.swing.table.TableColumnModel setColModel(javax.swing.JTable table) {
        javax.swing.table.TableColumn ctCol = 
                           table.getColumnModel().getColumn(0);        
        javax.swing.JComboBox ctCombo = new CtTypeComboDataModel();
        ctCol.setCellEditor(new DefaultCellEditor(ctCombo));
        return table.getColumnModel();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        firstName_field = new javax.swing.JTextField();
        lastName_field = new javax.swing.JTextField();
        address1_field = new javax.swing.JTextField();
        address2_field = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        total_field = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contribTable = new javax.swing.JTable();

        firstName_field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.firstName_field.text")); // NOI18N

        lastName_field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.lastName_field.text")); // NOI18N

        address1_field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.address1_field.text")); // NOI18N

        address2_field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.address2_field.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(firstName_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lastName_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(address1_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(address2_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(210, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firstName_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastName_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(address1_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(address2_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.jLabel5.text")); // NOI18N

        total_field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.total_field.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(total_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(total_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        contribTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(contribTable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField address1_field;
    private javax.swing.JTextField address2_field;
    private javax.swing.JTable contribTable;
    private javax.swing.JTextField firstName_field;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField lastName_field;
    private javax.swing.JTextField total_field;
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