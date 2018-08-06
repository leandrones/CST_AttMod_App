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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;

/**
 *
 * @author leandro
 */
public class DistanceFeatMapCodelet extends FeatMapCodelet {
    private float mr = 10;
    
    private  int time_graph;

    public DistanceFeatMapCodelet(int nsensors, ArrayList<String> sens_names, String featmapname,int timeWin, int mapDim) {
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
        
        MemoryObject laser_bufferMO = (MemoryObject) sensor_buffers.get(0);
        
        List laserData_buffer;
        
        laserData_buffer = (List) laser_bufferMO.getI();
        
//        System.out.println("laser buff MO "+laser_bufferMO);
        
        List distFM = (List) featureMap.getI();
        
//        System.out.println("LASER DATA BUFF SIZE "+laserData_buffer.size());
        
        
        if(distFM.size() == timeWindow){
            distFM.remove(0);
        }
        
        distFM.add(new ArrayList<>());
        
        int t = distFM.size()-1;

        ArrayList<Float> distFM_t = (ArrayList<Float>) distFM.get(t);
        
        for (int j = 0; j < mapDimension; j++) {
            distFM_t.add(new Float(0));
        }
        
        MemoryObject laserDataMO;
        
        if(laserData_buffer.size() < 1){
            return;
        }

        laserDataMO = (MemoryObject)laserData_buffer.get(laserData_buffer.size()-1);

        List laserData;

        laserData = (List) laserDataMO.getI();

//            System.out.println("LASER DATA SIZE "+laserData.size());

        for (int j = 0; j < laserData.size(); j++) {
            double function_value;
            function_value = 0;

            Float Fvalue;

            for (int k = 0; k < j; k++) {
                Fvalue = (Float) laserData.get(k);
                function_value += Fvalue.doubleValue();
            }
            Fvalue = (Float) laserData.get(j);
            function_value /= j+1;
            function_value = abs((Fvalue.doubleValue() - function_value)) / mr;

            distFM_t.set(j, new Float(function_value));

//                System.out.println("dist fm "+function_value+" t="+t+" j="+j);
        }   
        
        printToFile(distFM_t);
    }
    private void printToFile(ArrayList<Float> arr){
        if(time_graph < 50){
            try(FileWriter fw = new FileWriter("distanceFM.txt", true);
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
    

