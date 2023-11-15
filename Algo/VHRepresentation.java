package Algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Copyright (C), HITSZ
 * @Author Maohua Lyu, Wensheng, gan
 * @since 2022/11/7
 **/
class VHElement{
    int event;
    int utility;
    int ru;     //剩余效用
    int st;     //开始时间
    int ft;     //结束时间

    int sameStPos;

    public VHElement(int e, int u, int r, int s, int f, int sameStPos){
        event = e;
        utility = u;
        ru = r;
        st = s;
        ft = f;
        this.sameStPos = sameStPos;
    }
}

public class VHRepresentation {
    //verticalMap存储每个item在数据序列中的位置
    HashMap<Integer, Integer> verticalMap = new HashMap<>();

    ArrayList<VHElement> horizontalList = new ArrayList<>();

    int seqID = 0;

    public int seqUtility = 0;

    VHRepresentation(){
    }
    VHRepresentation(int seqID, int seqUtility) {
        this.seqID = seqID;
        this.seqUtility = seqUtility;
    }

    public List<VHElement> getEvents() {
        return horizontalList;
    }

    public int getSeqUtility() {return this.seqUtility;}

    public int getSeqID() {return this.seqID;}

    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
//        for(Map.Entry<Integer, ArrayList<Integer>> entry : verticalMap.entrySet()){
//            buffer.append(entry.getKey()+": [");
//            for(Integer i : entry.getValue()){
//                buffer.append(i + ",");
//            }
//            buffer.append("]\n");
//        }
        for(VHElement tempElement : horizontalList){
            buffer.append("item: " + tempElement.event + ", utility: "+ tempElement.utility+", ru: "+tempElement.ru +
                   ", sTime: "+tempElement.st+", eTime: "+tempElement.ft+" samePos: " + tempElement.sameStPos + "\n");
        }
        return buffer.toString();
    }

}