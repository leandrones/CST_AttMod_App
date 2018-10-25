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
 
package codelets.sensors;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

/**
 *
 * @author leandro
 */
public class LaserCodelet extends Codelet {
    private SensorI laser;
    private MemoryObject laser_read;
    
    public LaserCodelet(SensorI lsr){
        laser = lsr;
    }

    @Override
    public void accessMemoryObjects() {
        laser_read = (MemoryObject) this.getOutput("LASER");
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        laser_read.setI(laser.getData());
        
        //// System.out.println("laser read: "+laser_read.toString());
    }
    
}
