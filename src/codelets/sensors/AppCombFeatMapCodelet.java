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
import br.unicamp.cst.sensorial.CombFeatMapCodelet;
import cst_attmod_app.AgentMind;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author leandro
 */
public class AppCombFeatMapCodelet extends CombFeatMapCodelet {
    
    private  int time_graph;
    
    public AppCombFeatMapCodelet(int numfeatmaps, ArrayList<String> featmapsnames, int timeWin, int CFMdim) {
        super(numfeatmaps, featmapsnames,timeWin,CFMdim);
        this.time_graph = 0;
    }

    @Override
    public void calculateCombFeatMap() {
        /*ArrayList<Integer> sizes = new ArrayList<>();
        
        for (int i = 0; i < num_feat_maps; i++) {
            MemoryObject mo = (MemoryObject)feature_maps.get(i);
            List fm = (List) mo.getI();
            sizes.add(fm.size());
        }
        int min = Collections.min(sizes);
        */
        
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        List weight_values = (List) weights.getI();
        
        List combinedFM = (List) comb_feature_mapMO.getI();
        
        ArrayList<Float> maximum = new ArrayList<>(Collections.nCopies(23, new Float(0)));
        if(combinedFM.size() == timeWindow){
            combinedFM.remove(0);
        }
        combinedFM.add(new ArrayList<>());
        int t = combinedFM.size()-1;

        List CFMrow;
        CFMrow = (List) combinedFM.get(t);

        for(int j = 0; j < CFMdimension; j++){
            CFMrow.add(new Float(0));
        }
        
//        for (int t = 0; t < combinedFM.size(); t++) {
        

        for (int j = 0; j < CFMrow.size(); j++) {
            float ctj;
            ctj = 0;
            for (int k = 0; k < num_feat_maps; k++) {
                MemoryObject FMkMO;
                FMkMO = (MemoryObject) feature_maps.get(k);

                List FMk;
                FMk = (List) FMkMO.getI();
                List FMk_t;
                
                if(FMk.size() < 1){
                    return;
                }
                
                FMk_t = (List) FMk.get(FMk.size()-1);

                Float weight_val, fmkt_val;

//                    System.out.println("FMKT SIZE "+FMk_t.size());
                if(k == 1){
                    switch (j) {
                        case 0:
                            //first
                            for (int l = 0; l < 22; l++) {
                                maximum.set(l,(Float) FMk_t.get(l));
                            }   break;
                        case AgentMind.sonardimension-1:
                            for (int l = 160,i = 0; l < AgentMind.laserdimension; l++,i++) {
                                maximum.set(i, (Float)FMk_t.get(l));
                            }   break;
                        default:
                            for(int i = 0, l = 23*(j-1)+22; l < 23*j + 22; l++,i++){
//                                    System.out.println("I "+i + " L "+l+" J "+j);
                                maximum.set(i, (Float)FMk_t.get(l));
                            }   break;
                    }
                    fmkt_val = Collections.max(maximum);
                }
                else{
                    fmkt_val = (Float) FMk_t.get(j); 
                }

                System.out.println("\u001B[34m"+"FMK_t");
                System.out.println("\u001B[34m"+FMk_t);                    

                weight_val = (Float) weight_values.get(k);
                ctj += weight_val*fmkt_val;
//                    System.out.println("ctj="+ctj+" fmktval= "+fmkt_val+" k = "+k);

            }
//                System.out.println("ctj="+ctj);
            CFMrow.set(j, ctj);
            System.out.println("CFM ROW");
            System.out.println(CFMrow);
        }
        printToFile((ArrayList<Float>) CFMrow);
    }
    
    private void printToFile(ArrayList<Float> arr){
        if(time_graph < 50){
            try(FileWriter fw = new FileWriter("CFM.txt", true);
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
