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
    
package codelets.motor;

/**
 *
 * @author leandro
 */

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import CommunicationInterface.MotorI;

public class MotorCodelet extends Codelet {
    
    private MemoryObject motorActionMO;
    private MemoryObject rm_speed_MO, lm_speed_MO; 
    private MotorI rm, lm;
    
    private int MOVEMENT_TIME = 2000; // 2 seconds
    
    public MotorCodelet(MotorI rmo, MotorI lmo){
    	super();
        rm = rmo;
        lm = lmo;
    }

    @Override
    public void accessMemoryObjects() {
        motorActionMO = (MemoryObject) this.getInput("MOTOR");
        rm_speed_MO = (MemoryObject) this.getInput("R_M_SPEED");
        lm_speed_MO = (MemoryObject) this.getInput("L_M_SPEED");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
    	try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    	
    	String action = (String) motorActionMO.getI();
    	rm.setSpeed((float) rm_speed_MO.getI());
        lm.setSpeed((float) lm_speed_MO.getI());
    	
    }
    
}
