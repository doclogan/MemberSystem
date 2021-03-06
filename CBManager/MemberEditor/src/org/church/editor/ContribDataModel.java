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
import demo.ContribTypes;
import demo.Contribution;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.windows.WindowManager;

public class ContribDataModel extends AbstractTableModel{
/*
 * First row of array data contains column names
*/
    protected int start_num_rows;
    protected int nextEmptyRow;
    protected int numRows;
    
    String[] colNames = {
                            "Type",
                            "Amount",
                        };

    int NUM_COLUMNS = colNames.length;
    List<Contribution> lc;
    
    ContribDataModel(List<Contribution> lc) {
        this.lc = lc;
        start_num_rows = lc.size();
        nextEmptyRow = lc.size();
        numRows      = lc.size();
    }

    public List<Contribution> getlc() {
        return lc;
    }
    
    public BigDecimal contribTotal() {
        BigDecimal sumContrib = BigDecimal.ZERO;
        for (Contribution ct : lc) {
            sumContrib = sumContrib.add(ct.getContribAmt());
        }
        return sumContrib;
    }
    
    @Override
    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    @Override
    public int getRowCount() {
        return lc.size();
    }
    @Override
    public String getColumnName(int col) {
        return colNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        try{
            Contribution c = (Contribution) lc.get(row);
            switch (col) {
             case 0:
               return c.getIdcontribTypes().getIdcontribTypes();
             case 1:
               return c.getContribAmt();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.                  
        return true;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
      Contribution c = (Contribution) lc.get(row);
      if(col == 0)
        c.setIdcontribTypes(new ContribTypes((String) value));
      if(col == 1)
        c.setContribAmt((BigDecimal) value);       
      lc.set(row, c);
      EnvelopeEditorTopComponent tc = (EnvelopeEditorTopComponent) WindowManager.getDefault().findTopComponent("EnvelopeEditorTopComponent");
      tc.updContrib(contribTotal());
      tc.modify();
      fireTableCellUpdated(row, col);
    }

    public void addRow(int row) {
        Contribution c =  new Contribution();
        c.setIdcontribTypes(new ContribTypes((String) "Local"));
        c.setContribAmt(BigDecimal.ZERO);
        lc.add(c);
    }
    public boolean delRow(int row) {
        if (row < 0 || row >=  lc.size())
            return false;
        else {
            return true;
        }
    }
}