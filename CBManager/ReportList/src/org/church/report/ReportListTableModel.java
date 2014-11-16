/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.church.report;

import demo.ContribTypes;
import demo.Contribution;
import demo.Memenv;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.windows.WindowManager;

/**
 *
 * @author derrick
 */
public class ReportListTableModel extends AbstractTableModel{
    protected int start_num_rows;
    protected int nextEmptyRow;
    protected int numRows;
    
    String[] colNames = {
                            "First Name",
                            "Last Name",
                            "Envelope No.",
                        };

    int NUM_COLUMNS = colNames.length;
    List<Memenv> lm;
    
    ReportListTableModel(List<Memenv> lm) {
        this.lm = lm;
        start_num_rows = lm.size();
        nextEmptyRow = lm.size();
        numRows      = lm.size();
    }

    public List<Memenv> getm() {
        return lm;
    }
    
    @Override
    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    @Override
    public int getRowCount() {
        return lm.size();
    }
    @Override
    public String getColumnName(int col) {
        return colNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        try{
            Memenv m = (Memenv) lm.get(row);
            switch (col) {
             case 0:
               return m.getFirstName();
             case 1:
               return m.getLastName();
             case 2:
               return m.getIdenvelope();    
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
        return false;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
    }
}
