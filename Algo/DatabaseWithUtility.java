package Algo;

import java.io.*;
import java.util.*;

/**
 * @Copyright (C), HITSZ
 * @Author Maohua Lyu, Wensheng, gan
 * @since 2022/11/7
 **/
public class DatabaseWithUtility {
    protected ArrayList<VHRepresentation> sequences = new ArrayList<>();

    long totalUtility = 0;

    HashMap<Integer, Integer> mapItemSWU = new HashMap<>();

    public void loadfile(String path, int maxNumberOfSequence) throws IOException {
        String thisLine;
        BufferedReader myInput = null;
        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
            int sid = -1;

            //utility of current sequence
            int seqUtility = 0;
            //items contained in current sequence
            HashMap<Integer, int[]> seqItem = new HashMap<>();
            int idx = 0;
            //ordered event set of current sequence
//            ArrayList<Integer> orderEventSet = new ArrayList<>();
            //utility of items contained in current sequence
            ArrayList<Integer> seqItemUtility = new ArrayList<>();
            //start time of items contained in current sequence
            ArrayList<Integer> seqStartTime = new ArrayList<>();
            //finish time of items contained in current sequence
            ArrayList<Integer> seqFinishTime = new ArrayList<>();
            //id of current sequence
            int curSid;

            int NumberOfSequence = 0;
            // for each line until the end of file
            while ((thisLine = myInput.readLine()) != null) {
                // split the sequence according to the "," separator
                String[] tokens = thisLine.split(" |,");
                curSid = Integer.parseInt(tokens[0]);
                int item = Integer.parseInt(tokens[1]);     // item
                int startTime = Integer.parseInt(tokens[2]);
                int finishTime = Integer.parseInt(tokens[3]);
                int itemUtility = Integer.parseInt(tokens[4]); //utility 含效用的数据集
                if(sid == -1) {
                    seqItem.put(item, new int[]{idx++, itemUtility});
//                    orderEventSet.add(item);
                    seqItemUtility.add(itemUtility);
                    seqStartTime.add(startTime);
                    seqFinishTime.add(finishTime);
                    seqUtility += itemUtility;
                }

                //if this is the first item of a new sequence
                //登记前一条序列的信息
                if (curSid != sid) {
                    //sequence number ++
                    if (sid == -1) {
                        sid = curSid;
                        continue;
                    }
                    idx = 0;
                    NumberOfSequence++;
                    totalUtility += seqUtility;
                    if (NumberOfSequence == maxNumberOfSequence) break;
                    addSequences(NumberOfSequence, seqItem, seqItemUtility, seqStartTime, seqFinishTime, seqUtility);
                    //重置seqUtility和seqItem
                    seqItem.clear();
//                    orderEventSet.clear();
                    seqItemUtility.clear();
                    seqStartTime.clear();
                    seqFinishTime.clear();
                    seqUtility = 0;
                    //更新sid
                    sid = curSid;
                }
                //记录当前item信息
                if (seqItem.get(item) != null) {
                    int u = seqItem.get(item)[1];
                    if(itemUtility > u) {
                        seqItem.put(item, new int[]{idx, itemUtility});
                        seqUtility -= u;    // delete the former utility of the same event
                        seqUtility += itemUtility;
                    }
                } else {
                    seqItem.put(item, new int[]{idx, itemUtility});
                    seqUtility += itemUtility;  // add the event utility
                }
//                orderEventSet.add(item);
                idx++;
                seqItemUtility.add(itemUtility);
                seqStartTime.add(startTime);
                seqFinishTime.add(finishTime);
            }
            //处理最后一个序列

            //sequence number ++
            NumberOfSequence++;
            //add seqUtility to totalUtility
            totalUtility += seqUtility;
            addSequences(NumberOfSequence, seqItem, seqItemUtility, seqStartTime, seqFinishTime, seqUtility);
            seqItem.clear();
//            orderEventSet.clear();
            seqFinishTime.clear();
            seqStartTime.clear();
            seqItemUtility.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(myInput != null) {
                myInput.close();
            }
        }
    }

    public ArrayList<VHRepresentation> getDatabase() {
        return sequences;
    }

    public ArrayList<VHRepresentation> getDatabase(double minUtil, int maximumRemoveTimes) {
        int removeCount = 0;
        int removeTimes = 0;
        // SWU
        HashMap<Integer, Integer> mapItemSWU = this.mapItemSWU;
        // we create an iterator to loop over all items
        Iterator<Map.Entry<Integer, Integer>> iterator = mapItemSWU.entrySet().iterator();

        // for each item
        while (iterator.hasNext()) {
            // we obtain the entry in the map
            Map.Entry<Integer, Integer> entryMapItemSWU = iterator.next();
            Integer itemSWUValue = entryMapItemSWU.getValue();

            // if the estimated utility of the current item is less than minutil
            if (itemSWUValue < minUtil) {
                removeCount ++;
//                System.out.println("remove item: " + entryMapItemSWU.getKey());
                iterator.remove();
            }
        }

        int removeUtility = 0;
        while (removeTimes < maximumRemoveTimes) {
            // If not item can be removed
            if(removeCount == 0) {
                break;
            }

            // update removeTimes
            removeTimes++;
            removeCount = 0;
            // scan the database again.
            // For each sequence
            Iterator<VHRepresentation> iteratorSequence = sequences.iterator();
            while (iteratorSequence.hasNext()) {
                VHRepresentation sequence = iteratorSequence.next();
                // Initialize to 0
                removeUtility = 0;

                // For each item
                Iterator<VHElement> iteratorItems = sequence.getEvents().iterator();
//                int cnt1 = 0, cnt2 = 0;
//                boolean flag = false;
                while (iteratorItems.hasNext()) {
                    // get each event
                    VHElement event = iteratorItems.next();
                    int utility = event.utility;
//                    if (event.event == 196) {
//                        cnt1 ++;
//                        flag = true;
//                    }
                    if (mapItemSWU.get(event.event) == null) {
                        // remove event
                        iteratorItems.remove();
                        // subtract the item utility value from the sequence utility.
                        sequence.seqUtility -= utility;
                        // update removeUtility
                        removeUtility += utility;
                    }
                }
//                if (flag) System.out.println(cnt1 + " " + cnt2);
                // If the sequence has become empty, we remove the sequences from the database
                if (sequence.getEvents().size() < 2) {
                    iteratorSequence.remove();
                } else {
                    // update the SWU of all items
                    iteratorItems = sequence.getEvents().iterator();
                    // for each event still in sequence
                    while (iteratorItems.hasNext()) {
                        // get event type
                        VHElement item = iteratorItems.next();
                        if (mapItemSWU.get(item.event) == null) {
//                            System.out.println("item " + item.event + " still in seq");
                            continue;
                        }
                        if(mapItemSWU.get(item.event) - removeUtility < minUtil) {
                            removeCount++;
//                            System.out.println("remove item: " + item.event);
                            mapItemSWU.remove(item.event);
                        } else {
                            mapItemSWU.put(item.event, mapItemSWU.get(item.event) - removeUtility);
                        }
                    }
                }
            }
        }

        // get ordered e-seq database and reset the remaining utility of each event
        ArrayList<VHRepresentation> newSequences = new ArrayList<>();
        for (int i = 0; i < sequences.size(); i++) {
            VHRepresentation orderedSeq = sort(sequences.get(i), i + 1);
            newSequences.add(orderedSeq);
        }
        this.mapItemSWU = mapItemSWU;
        this.sequences = newSequences;
        return sequences;
    }

    // compare the ordering of two events
    public boolean compareEvent(VHElement a, VHElement b) {
        if(a.st < b.st) {
            return true;
        } else if(a.st == b.st && a.ft < b.ft) {
            return true;
        } else return a.st == b.st && a.ft == b.ft && a.event < b.event;
    }

    // sort the events in a sequence using quicksort
    public void quicksort(VHElement[] events, int l, int r) {
        if(l >= r) return;
        int i = l - 1, j = r + 1;
        VHElement x = events[l];
        while(i < j) {
            do i++; while (compareEvent(events[i], x));
            do j--; while (compareEvent(x, events[j]));
            if (i < j) {
                VHElement temp = events[i];
                events[i] = events[j];
                events[j] = temp;
            }
            else break;
        }
        quicksort(events, l, j);
        quicksort(events, j + 1, r);
    }

    // sort the database
    public VHRepresentation sort(VHRepresentation seq, int seqID) {
        VHElement[] events = new VHElement[seq.horizontalList.size()];

        for(int i = 0; i < events.length; i++) {
            events[i] = seq.horizontalList.get(i);
        }

        quicksort(events, 0, events.length - 1);
        VHRepresentation orderedSeq = new VHRepresentation(seqID, seq.seqUtility);
        int seqUtility = seq.seqUtility;
        int idx = 0;
        int st = -1;
        int samePos = -1;
//        System.out.println("Seq: " + seqID);
        for(VHElement e: events) {
            int utility = e.utility;
            seqUtility -= utility;
            e.ru = seqUtility;
            if(st == e.st) {
                e.sameStPos = samePos;
            } else {
                st = e.st;
                e.sameStPos = idx;
                samePos = idx;
            }
            orderedSeq.verticalMap.put(e.event, idx++);
            orderedSeq.horizontalList.add(e);
//            System.out.println("item: " + e.event + ", st: " + e.st + ", ed: " + e.ft + " event pos: " + (idx - 1));
        }

        return orderedSeq;
    }

    private void addSequences(int seqID, HashMap<Integer, int[]> seqItem, ArrayList<Integer> seqItemUtility, ArrayList<Integer> seqStartTime, ArrayList<Integer> seqFinishTime, int seqUtility) {
        VHRepresentation seq = new VHRepresentation(seqID, seqUtility);
//        System.out.println("eventSet: " + orderEventSet.size() + " hash: " + seqItem.size());
        int seqU = seqUtility;
        for (Map.Entry<Integer, int[]> entry: seqItem.entrySet()) {
            int event = entry.getKey();
            if(mapItemSWU.get(event) == null) {
                mapItemSWU.put(event, seqU);
            } else {
                mapItemSWU.put(event, mapItemSWU.get(event) + seqU);
            }
            int idx = entry.getValue()[0];
            int curItemUtility = seqItemUtility.get(idx);
            seqUtility -= curItemUtility;
            VHElement element = new VHElement(event, curItemUtility, seqUtility, seqStartTime.get(idx), seqFinishTime.get(idx), 0);
            seq.horizontalList.add(element);
        }

//        for (int i = 0; i < seqItem.size(); i++) {
//            int curItem = orderEventSet.get(i);
//            int curItemUtility = seqItemUtility.get(i);
//            seqUtility -= curItemUtility;
//            VHElement element = new VHElement(curItem, curItemUtility, seqUtility, seqStartTime.get(i), seqFinishTime.get(i), 0);

            // horizontalList
//            seq.horizontalList.add(element);

//            if (seq.verticalMap.get(curItem) == null) {
//                ArrayList<Integer> posList = new ArrayList<>();
//                posList.add(i);
//                seq.verticalMap.put(curItem, posList);  // 若当前event没有出现过，新建一个list记录当前事件出现的位置
//            } else {
//                seq.verticalMap.get(curItem).add(i);    // 若当前事件已经出现了，则将其位置加入到对应的list中
//            }
//        }
//        for(Integer item: seqItem) {
//            if (mapItemSWU.get(item) == null) {
//                mapItemSWU.put(item, seqUtility);
//            } else {
//                mapItemSWU.put(item, mapItemSWU.get(item) + seqUtility);
//            }
//        }
        sequences.add(seq);
    }

    public List<VHRepresentation> getSequences() {
        return sequences;
    }
    public void print() {
        System.out.println("============  SEQUENCE DATABASE ==========");
        // for each sequence
        int i = 1;
        for (VHRepresentation sequence : sequences) {
            System.out.print("Seq: " + i++ + ", seqUtility: " + sequence.seqUtility + "\n");
            System.out.print(sequence);
        }
//        System.out.print("Seq: " + i++ + ", seqUtility: " + sequences.get(0).seqUtility + "\n");
//        System.out.print(sequences.get(0));
    }
}
