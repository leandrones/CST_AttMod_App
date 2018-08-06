/*
 * Copyright (C) 2018 leandro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package codelets.sensors;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import outsideCommunication.SonarData;

/**
 *
 * @author leandro
 */
public class SonarCodelet extends Codelet {
    private MemoryObject sonar_read;
    private SensorI sonar;
    
    public SonarCodelet(SensorI sonar){
        this.sonar = sonar;
    }

    @Override
    public void accessMemoryObjects() {
        sonar_read = (MemoryObject) this.getOutput("SONARS");
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        SonarData sonarData = (SonarData) sonar.getData();
        
        try {
            Thread.sleep(00);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        sonar_read.setI(sonarData);
        
        //System.out.println(sonar_read.toString());
        SonarData a = (SonarData) sonar_read.getI();
//        System.out.println("Sonar detect state = "+a.detect_state.toString());
//        System.out.println("Sonar value = "+a.sonar_readings);
    }
    
}
