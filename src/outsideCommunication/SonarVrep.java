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

import CommunicationInterface.SensorI;
import coppelia.BoolW;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
        
/**
 *
 * @author leandro
 */
public class SonarVrep implements SensorI{
    private SonarData sonarData;
    private IntW sonar_handles[];
    private remoteApi vrep;
    private int clientID;
    
    private  int time_graph;
    
    public SonarVrep(remoteApi vrep, int clientid, IntW sonar_handles[]){
        this.time_graph = 0;
        sonarData = new SonarData();
        this.vrep = vrep;
        this.sonar_handles = sonar_handles;
        clientID = clientid;
    }

    @Override
    public Object getData() {
        FloatWA xyz_read = new FloatWA(3);
        
        try {
            Thread.sleep(50);
//            System.out.println("\u001B[31m"+"TRY CATCH");
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        for(int i = 0; i < 8;i++) {
            float[] temp;
            BoolW detect_state_i = new BoolW(false);
            int ret;
            
            ret = vrep.simxReadProximitySensor(clientID, sonar_handles[i].getValue(), detect_state_i, xyz_read, null, null, remoteApi.simx_opmode_buffer);
            if(ret != 0)
                sonarData.sonar_readings.set(i, Float.NaN);
            temp = xyz_read.getArray();

            sonarData.detect_state.set(i, detect_state_i.getValue());
            
            if(detect_state_i.getValue() == false){
                sonarData.sonar_readings.set(i, new Float(0));
            }
            else{
                sonarData.sonar_readings.set(i, temp[2]);
            }
                
        }
        System.out.println("\u001B[36m"+"sonar data\n"+sonarData.sonar_readings);
        
        printToFile(sonarData.sonar_readings);
        
        return  sonarData;
    }
    
    private void printToFile(Object object){
        if(time_graph < 50){
            try(FileWriter fw = new FileWriter("sonar.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(time_graph+" "+ object);
                time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
