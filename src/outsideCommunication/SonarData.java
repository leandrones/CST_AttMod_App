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
package outsideCommunication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author leandro
 */
public class SonarData implements Serializable {
    public List<Float> sonar_readings;
    public List<Boolean> detect_state;
    
    public SonarData(){
        sonar_readings = Collections.synchronizedList(new ArrayList<>(8));
        detect_state = Collections.synchronizedList(new ArrayList<>(8));
        
        for (int i = 0; i < 8; i++) {
            sonar_readings.add(Float.NaN);
            detect_state.add(Boolean.FALSE);
        }
    }
    
    /*
    @Override
    public Object clone(){
        SonarData sonarData = null;
        try {
            return (SonarData) super.clone();
        } catch (CloneNotSupportedException ex) {
            sonarData = new SonarData();
            sonarData.detect_state = this.detect_state;
            sonarData.sonar_readings = this.sonar_readings;
            Logger.getLogger(SonarData.class.getName()).log(Level.SEVERE, null, ex);
            return sonarData;
        }
    }*/
}
