package org.nlogo.deltatick;

import org.nlogo.agent.TreeAgentSet;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/28/13
 * Time: 9:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitState extends Trait {

    public HashMap<String, String> selectedVariationsPercent = new HashMap<String, String>();

    public TraitState(TraitState ts) {
        super(ts);
        this.selectedVariationsPercent.clear();
        for (Map.Entry <String, String> entry : ts.selectedVariationsPercent.entrySet()) {
            this.selectedVariationsPercent.put(entry.getKey(), new String(entry.getValue()));
        }
        //selectedVariationsPercent = new HashMap<String, String>(ts.selectedVariationsPercent);
    }

    public TraitState(Trait t, HashMap<String, String> hm) {
        super(t);

        for (Map.Entry<String, String> entry : hm.entrySet()) {
            int percent = (int) Math.round(Double.parseDouble(entry.getValue()));
            if (percent > 0) {
                Variation variation = this.getVariationHashMap().get(entry.getKey());
                variation.percent = percent;
            }
            //this.getVariationHashMap().get(entry.getKey()).percent = (int) Double.parseDouble(hm.get(entry.getKey()));
        }

        for (Map.Entry <String, String> entry : hm.entrySet()) {
            this.selectedVariationsPercent.put(entry.getKey(), new String(entry.getValue()));
        }
        //selectedVariationsPercent = new HashMap<String, String>(hm);
    }


//    @Override
//    public HashMap<String, String> getValuesPercentList() {
//        return selectedVariationsPercent;
////        HashMap<String, String> valuesPercentList = new HashMap<String, String>();
////        for (Variation variation : variationHashMap.values()) {
////            String percent = Integer.toString(variation.percent);
////            valuesPercentList.put(variation.value, percent);
////        }
////        return valuesPercentList;
//    }


}
