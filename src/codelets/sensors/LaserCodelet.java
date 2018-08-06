/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        
        //System.out.println("laser read: "+laser_read.toString());
    }
    
}
