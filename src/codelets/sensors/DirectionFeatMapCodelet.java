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

import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.sensorial.FeatMapCodelet;
import cst_attmod_app.AgentMind;
import static cst_attmod_app.AgentMind.buffersize;
import static cst_attmod_app.AgentMind.laserdimension;
import static cst_attmod_app.AgentMind.sonardimension;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import outsideCommunication.SonarData;

/**
 *
 * @author leandro
 */
public class DirectionFeatMapCodelet extends FeatMapCodelet{
    private  int time_graph;
    
    
    public DirectionFeatMapCodelet(int nsensors, ArrayList<String> sens_names, String featmapname,int timeWin, int mapDim) {
        super(nsensors, sens_names, featmapname,timeWin,mapDim);
        this.time_graph = 0;
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
        
        MemoryObject laser_bufferMO = (MemoryObject) sensor_buffers.get(1);
        MemoryObject sonar_bufferMO = (MemoryObject) sensor_buffers.get(0);
        
        List laserDataBuffer,sonarDataBuffer;
        
//        System.out.println("sensor buffers  "+sensor_buffers);
        
        laserDataBuffer = (List) laser_bufferMO.getI();
        sonarDataBuffer= (List) sonar_bufferMO.getI();
        
        List directionFM = (List) featureMap.getI();
        
        if(directionFM.size() == timeWindow){
            directionFM.remove(0);
        }
        
        directionFM.add(new ArrayList<>());
        
        int t = directionFM.size()-1;
        
        ArrayList<Float> directionFM_t = (ArrayList<Float>) directionFM.get(t);
        
        for (int j = 0; j < mapDimension; j++) {
            directionFM_t.add(new Float(0));
        }
        
        float[][] temp_direction_laser = new float[buffersize][laserdimension];
        float[][] temp_direction_sonar = new float[buffersize][sonardimension];

            
//        for (int t = 0; t < laserDataBuffer.size()-1 && t < sonarDataBuffer.size()-1; t++) {
            
//            System.out.println("buffer size "+laserDataBuffer.size());
            
        //Calculating the function for the laser
        MemoryObject laserDataMO1,laserDataMO2;
        
        if(laserDataBuffer.size() < 2){
            return;
        }
        laserDataMO1 = (MemoryObject)laserDataBuffer.get(laserDataBuffer.size()-2);
        laserDataMO2 = (MemoryObject)laserDataBuffer.get(laserDataBuffer.size()-1);

        List laserData1, laserData2;

        laserData1 = (List) laserDataMO1.getI();
        laserData2 = (List) laserDataMO2.getI();

        int[] count = new int[3];
        for (int j = 0; j < 3; j++) {
            count[j] = 0;
        }

        /*for(int t2 = 0; t2 < laserDataBuffer.size(); t2++){
            MemoryObject mo;
            List l;
            Float f;

            mo = (MemoryObject) laserDataBuffer.get(t2);
            l = (List) mo.getI();
            f = (Float) l.get(90);

            System.out.print("t2= "+t2+ " f= " +f+" ");
        }
        System.out.println("");*/

        for (int j = 0; j < laserData1.size(); j++) {
            Float f1,f2;

            f1 = (Float) laserData1.get(j);
            f2 = (Float) laserData2.get(j);

//                if(j == 90)
//                    System.out.println("\u001B[32m"+"f1= "+f1+" f2= "+f2+" t= "+t);

            temp_direction_laser[t][j] = f2-f1;                
            temp_direction_laser[t][j] /= dt;
            if (temp_direction_laser[t][j] > 0) {
//                    System.out.println("\u001B[32m"+"f1= "+f1+" f2= "+f2+" t= "+t+" j = "+j);
                temp_direction_laser[t][j] = 1;
                count[2] += 1;
            }
            else if (temp_direction_laser[t][j] < 0) {
//                    System.out.println("\u001B[34m"+"f1= "+f1+" f2= "+f2+" t= "+t+" j = "+j);
                temp_direction_laser[t][j] = -1;
                count[0] += 1;
            }
            else{
                temp_direction_laser[t][j] = 0;
                count[1] += 1;
            }
        }

//            System.out.println("\u001B[32m"+"count= "+Arrays.toString(count));


        for (int j = 0; j < laserData1.size(); j++) {
            temp_direction_laser[t][j] = laserData1.size()/ count[1+(int)temp_direction_laser[t][j]];
//                System.out.println("\u001B[32m"+"temp direc laser j "+temp_direction_laser[t][j]+" t "+t+" j "+j);
        }

        //Calculating the function for the sonar
        MemoryObject sonarDataMO1,sonarDataMO2;
        
        if(sonarDataBuffer.size() < 2){
            return;
        }
        sonarDataMO1 = (MemoryObject)sonarDataBuffer.get(sonarDataBuffer.size()-2);
        sonarDataMO2 = (MemoryObject)sonarDataBuffer.get(sonarDataBuffer.size()-1);

        SonarData sonarData1, sonarData2;

        sonarData1 =  (SonarData) sonarDataMO1.getI();
        sonarData2 =  (SonarData) sonarDataMO2.getI();

        for (int j = 0; j < 3; j++) {
            count[j] = 0;
        }

        for (int j = 0; j < sonarData1.sonar_readings.size(); j++) {
            Float f1,f2;

            f1 = sonarData1.sonar_readings.get(j);
            f2 = sonarData2.sonar_readings.get(j);

//                System.out.println("sonar data det state 1 = "+sonarData1.detect_state.get(j));
//                System.out.println("sonar data det state 2 = "+sonarData2.detect_state.get(j));

            if(sonarData1.detect_state.get(j) == true && 
                    sonarData2.detect_state.get(j) == true){
                temp_direction_sonar[t][j] = f2-f1;
                temp_direction_sonar[t][j] /= dt;

//                    System.out.println("f 2 = "+f2+" f 1 = "+f1);


                if (temp_direction_sonar[t][j] > 0) {
//                        System.out.println("\u001B[32m"+"TO NO IF 1 "+temp_direction_sonar[t][j]+" T "+t+" J "+j);
//                        System.out.println("f 2 = "+f2+" f 1 = "+f1);
                    temp_direction_sonar[t][j] = 1;
                    count[2] += 1;
                }
                else if (temp_direction_sonar[t][j] < 0) {
//                        System.out.println("\u001B[31m"+"TO NO IF 2 "+temp_direction_sonar[t][j]+" T "+t+" J "+j);
//                    System.out.println("f 2 = "+f2+" f 1 = "+f1);
                    temp_direction_sonar[t][j] = -1;
                    count[0] += 1;
                }
                else{
                    temp_direction_sonar[t][j] = 0;
                    count[1] += 1;
                }
            }
            else{
                temp_direction_sonar[t][j] = 0;
                count[1] += 1;
            }
        }

//            System.out.println("\u001B[31m"+"count= "+Arrays.toString(count));

        for (int j = 0; j < sonarData1.sonar_readings.size(); j++) {
            temp_direction_sonar[t][j] = sonarData1.sonar_readings.size()/ count[1+(int)temp_direction_sonar[t][j]];
//                System.out.println("\u001B[31m"+"temp direc sonar j "+temp_direction_sonar[t][j]+" t "+t+" j "+j);
        }

        //Composing functions 
        //Composing here means taking 22 or 23 sonar values and getting the
        //max with a sonar value

        //First and last sonars
        ArrayList<Float> maximum = new ArrayList<>();

        //first
        for (int j = 0; j < 22; j++) {
            maximum.add(temp_direction_laser[t][j]);
        }
        maximum.add(temp_direction_sonar[t][0]);

        float[] max_array = new float[sonardimension];
        max_array[0] = Collections.max(maximum);

        //last
        for (int j = 160,k = 0; j < laserdimension; j++,k++) {
            maximum.set(k,temp_direction_laser[t][j]);
        }
        maximum.set(22,temp_direction_sonar[t][7]);

        max_array[7] = Collections.max(maximum);

        //the other 6
        maximum.add(Float.NaN);
        for (int k = 1; k < 7; k++) {
            for (int i = 0, j = 23*(k-1)+22; j < 23*k + 22; i++,j++) {
                maximum.set(i,temp_direction_laser[t][j]);
            }
            maximum.set(23,temp_direction_sonar[t][0]);

            max_array[k] = Collections.max(maximum);
        }

        for (int j = 0; j < sonardimension; j++) {
            directionFM_t.set(j, max_array[j]);
//                System.out.println("\u001B[31m"+"direct fm "+directionFM_t.get(j)+" t="+t+" j="+j);
        }
        
        printToFile(directionFM_t);
    }
    
    private void printToFile(ArrayList<Float> arr){
        if(time_graph < 50){
            try(FileWriter fw = new FileWriter("directionFM.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(time_graph+" "+ arr);
                time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }    
}
