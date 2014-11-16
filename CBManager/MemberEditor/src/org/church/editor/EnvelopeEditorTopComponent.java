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
import javax.swing.event.TableModelEvent;
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
    void updContrib(BigDecimal sumContrib) {
        balcontrib.setText(sumContrib.subtract(new BigDecimal(total_field.getText())).toString());
     }
    void modify() {
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
             return name_field.getText();
        }

        @Override
        protected void handleSave() throws IOException {
            NotifyDescriptor.Confirmation message = new NotifyDescriptor.Confirmation("Do you want to save \""
                    + name_field.getText()  + "\"?",
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
                    List<Contribution> lc = ctModel.getlc();
                    Query query = entityManager.createNamedQuery("Contribution.findAll");
                    List<Contribution> resultListContrib = query.getResultList();
                    Integer cId = resultListContrib.size()+1;
                    for (Contribution ct : lc) {
                        if(ct.getIdcontribution() == null){
                            ct.setIdcontribution(cId);
                            ct.setIdenvelope(envelope);
                            cId = cId + 1;
                        }                       
                    }
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
                    envelope.setEnvDate(Parameters.getInstance().getSabDate());
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
                    tc().ic.remove(this);
                    unregister();
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
                name_field.setText(mem.getFirstName()+"  "+mem.getLastName());
                address_field.setText(mem.getMemAddress1()+", "+mem.getMemAddress2());
                envelope = getEnvelope(mem);
                if (envelope != null){
                    total_field.setText(envelope.getTotal().toString());
                    ctModel = new ContribDataModel(envelope.getContributionList());
                    updContrib(ctModel.contribTotal());
                    contribTable.setModel(ctModel);
                    setColModel(contribTable);                   
                } else {
                    total_field.setText("0");
                    Contribution ct = new Contribution();
                    ContribTypes ctType = new ContribTypes("Tithe");
                    ct.setIdcontribTypes(ctType);
                    ct.setContribAmt(BigDecimal.ZERO);
                    List cList = new ArrayList();
                    cList.add(ct);
                    ctModel = new ContribDataModel(cList);
                    updContrib(ctModel.contribTotal());
                    contribTable.setModel(ctModel);
                    setColModel(contribTable);
                    envelope = new Envelope();
                    jt = new Joint();
                }
            }
        } else {
            name_field.setText("[no name]");            
            address_field.setText("[no address]");            
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
        name_field = new javax.swing.JTextField();
        address_field = new javax.swing.JTextField();
        jointContrib = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contribTable = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        total_field = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        offering_Field = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        church_Field = new javax.swing.JTextField();
        receipt_Req_Field = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        difference = new javax.swing.JLabel();
        balcontrib = new javax.swing.JTextField();

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        name_field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.name_field.text")); // NOI18N

        address_field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.address_field.text")); // NOI18N

        jointContrib.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(address_field, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                    .addComponent(name_field))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jointContrib, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(name_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jointContrib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(address_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        contribTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Type", "Amount"
            }
        ));
        jScrollPane1.setViewportView(contribTable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                .addGap(118, 118, 118))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.jButton3.text")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jButton3)
                .addContainerGap(48, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jButton2)
                    .addContainerGap(78, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.jLabel5.text")); // NOI18N

        total_field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.total_field.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.jLabel1.text")); // NOI18N

        offering_Field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.offering_Field.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.jLabel3.text")); // NOI18N

        church_Field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.church_Field.text")); // NOI18N

        receipt_Req_Field.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.receipt_Req_Field.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(total_field, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(offering_Field))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(receipt_Req_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(church_Field)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(total_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(receipt_Req_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(offering_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(church_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        org.openide.awt.Mnemonics.setLocalizedText(difference, org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.difference.text")); // NOI18N

        balcontrib.setText(org.openide.util.NbBundle.getMessage(EnvelopeEditorTopComponent.class, "EnvelopeEditorTopComponent.balcontrib.text")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(difference)
                .addGap(30, 30, 30)
                .addComponent(balcontrib, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 84, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(difference)
                    .addComponent(balcontrib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        int row = contribTable.getSelectedRow();
        ctModel.addRow(row);
        contribTable.tableChanged(new TableModelEvent(
                  ctModel, row, row, TableModelEvent.ALL_COLUMNS, 
                  TableModelEvent.INSERT)); 
        contribTable.repaint();
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField address_field;
    private javax.swing.JTextField balcontrib;
    private javax.swing.JTextField church_Field;
    private javax.swing.JTable contribTable;
    private javax.swing.JLabel difference;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox jointContrib;
    private javax.swing.JTextField name_field;
    private javax.swing.JTextField offering_Field;
    private javax.swing.JTextField receipt_Req_Field;
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