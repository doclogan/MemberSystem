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
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.*;

public class CtTypeComboDataModel extends JComboBox {
   public CtTypeComboDataModel() {
       EntityManager em = Persistence.createEntityManagerFactory("MemberLibraryPU").createEntityManager();
       Query query = em.createNamedQuery("ContribTypes.findAll");
       List<ContribTypes> resultList = query.getResultList();
       for (ContribTypes c : resultList) {
           addItem(c.getIdcontribTypes());
       }       
   }
}
