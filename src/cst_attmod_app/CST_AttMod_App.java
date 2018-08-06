/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
 
package cst_attmod_app;

import outsideCommunication.OutsideCommunication;

/**
 *
 * @author leandro
 */
public class CST_AttMod_App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OutsideCommunication oc = new OutsideCommunication();
        oc.start();
        AgentMind am = new AgentMind(oc);
        
        
    }
    
}
