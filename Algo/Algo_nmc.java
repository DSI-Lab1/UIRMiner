package Algo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @Copyright (C), HITSZ
 * @Author Maohua Lyu, Wensheng, gan
 * @since 2022/11/24
 **/
public class Algo_nmc {
    /**
     *  record the starting time of the algorithm
     */
    long startTime = 0;

    /**
     * record the number of patterns
     */
    long ruleCount = 0;


    /**
     * writer to write the output file
     **/
    BufferedWriter writer = null;

    /**
     * the minUtility threshold
     **/
    double threshold = 0;

    double confidence = 0;

    /**
     * candidate TR
     */

    long candidate = 0;

    final boolean printOutput = true;

    final boolean databseDEBUG = false;

    final boolean complementRSU = true;

    private int maximumRemoveTimes = Integer.MAX_VALUE;

    /**
     * the input file path
     **/
    String input;

    //database
    DatabaseWithUtility database;

    // the number of Candidate
    long NumOfCandidate = 0;


    class LastEvt {
        public int RSU;

        ArrayList<ProjectSequence> projectDB;

        public LastEvt(int RSU, ArrayList<ProjectSequence> projectDB) {
            this.RSU = RSU;
            this.projectDB = projectDB;
        }
    }

    /**
     * Default constructor
     */
    public Algo_nmc() {
    }

    public void runAlgo(String dataset, String output, double utilityRatio, double confidence, int maxAntecedent, int maxConsequent, int maximumNumberOfSequences) throws IOException {
        // reset maximum
        MemoryLogger.getInstance().reset();

        // input path
        this.input = dataset;

        // record the start time of the algorithm
        startTime = System.currentTimeMillis();

        // create a writer object to write results to file

        //VH database
        if (database == null) {
            try {
                database = new DatabaseWithUtility();
                database.loadfile(dataset, maximumNumberOfSequences);
                MemoryLogger.getInstance().checkMemory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (databseDEBUG) {
            System.out.println("origin database");
            database.print();
            System.out.println("total sequences is: " + database.sequences.size() + '\n');
        }

        // set the utility threshold
        if (utilityRatio >= 1) this.threshold = utilityRatio;
        else this.threshold = utilityRatio * database.totalUtility;
        this.confidence = confidence;

        if (threshold == 0.0) this.threshold = 0.1;
        if (this.confidence == 0.0) this.confidence = 0.0;  // set the default confidence threshold.

        database.sequences = database.getDatabase(threshold, maximumRemoveTimes);

        output = "Algo_nmc " + output;

        writer = new BufferedWriter(new FileWriter(output));


        /**
         * print the database after performing SWU
         */
        if (databseDEBUG) {
            System.out.println("After SWU");
            database.print();
            System.out.println("total sequences is: " + database.sequences.size());
        }

        antecedentGenerate(database.sequences, database.mapItemSWU, maxAntecedent, maxConsequent);

        long runtime = System.currentTimeMillis() - startTime;
        // print the basic info
        System.out.println("=============TRule Miner STATS=============");
        System.out.println(" dataset: " + dataset);
        System.out.print(" Utility threshold ratio: " + utilityRatio+ " \n");
        System.out.print(" Utility threshold: " + this.threshold + " \n");
        System.out.print(" Confidence threshold ratio: " + confidence+ " \n");
        System.out.println(" rule count: " + ruleCount);
        System.out.println(" Total time: " + runtime / 1000d + " s");
        System.out.print(" Max Memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB\n");
        System.out.println(" Candidate rule count: " + candidate);
        // write the info
        String buffer = "\n" +
                "=============TRule Miner STATS=============\n" +
                " dataset: " + dataset.substring(15) + '\n' +
                " Utility threshold ratio: " + utilityRatio + " \n" +
                " Utility threshold: " + this.threshold + " \n" +
                " Confidence threshold ratio: " + confidence + " \n" +
                " rule count: " + ruleCount + " \n" +
                " Total time: " + runtime / 1000d + " s\n" +
                " Max Memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB\n" +
                " Candidate rule count: " + candidate + "\n";
        writer.write(buffer);
        MemoryLogger.getInstance().checkMemory();
        writer.close();
        database = null;
    }

    public void save(ArrayList<Integer> antecedent, ArrayList<Integer> consequent, ArrayList<ArrayList<Integer>> anteRelation,
                     ArrayList<ArrayList<Integer>> conRelation, int utility, int ruleSup, double ruleConfidence) throws IOException {
        // create a string buffer
        StringBuilder buffer = new StringBuilder();
        // write the left side of the rule (the antecedent)
        buffer.append("[");
        for (int i = 0; i < antecedent.size(); i++) {
            if (i != antecedent.size() - 1) {
                buffer.append(antecedent.get(i)).append(", ");
            } else {
                buffer.append(antecedent.get(i));
            }
        }
        buffer.append("]");
        // write separator
        buffer.append("	--> [");
        for (int i = 0; i < consequent.size(); i++) {
            if(i != consequent.size() - 1) {
                buffer.append(consequent.get(i)).append(", ");
            } else {
                buffer.append(consequent.get(i));
            }
        }
        buffer.append("]");
        // write relation
        buffer.append(" , with relation [");
        for (int i = 0; i < anteRelation.size(); i++) {
            if (i != anteRelation.size() - 1) {
                buffer.append(anteRelation.get(i).toString()).append(", ");
            } else {
                buffer.append(anteRelation.get(i).toString());
            }
        }
        buffer.append("] and [");
        for (int i = 0; i < conRelation.size(); i++) {
            if (i != conRelation.size() - 1) {
                buffer.append(conRelation.get(i).toString()).append(", ");
            } else {
                buffer.append(conRelation.get(i).toString());
            }
        }
        buffer.append("]");
        buffer.append("\t#UTIL: ");
        buffer.append(utility);
        // write confidence
        buffer.append("\t#CONF: ");
        buffer.append(ruleConfidence);
        // write support
        buffer.append("\t#SUP: ");
        buffer.append(ruleSup);
        writer.write(buffer.toString());
        writer.newLine();
    }


    /**
     *  1. generate the antecedent utilityList
     *  2. call the consequent utilityList generator
     * @param seqDatabase   the origin database
     * @param mapEventSwu   all promising events
     */
    public void antecedentGenerate(ArrayList<VHRepresentation> seqDatabase, HashMap<Integer, Integer> mapEventSwu, int maxAntecedent, int maxConsequent) throws IOException {
        for(Map.Entry<Integer, Integer> entry: mapEventSwu.entrySet()) {
            int event = entry.getKey();
            int upperBound = 0;
            ArrayList<ProjectSequence> newProjectSeqDB = new ArrayList<>();

            for (VHRepresentation seq: seqDatabase) {
                Integer pos = seq.verticalMap.get(event);
                if(pos != null) {   // event in this sequence
                    int ubInSeq = 0;
                    int idx = pos;
                    VHElement e = seq.horizontalList.get(idx);
                    int curUtility = e.utility;
                    ubInSeq = curUtility + e.ru;
                    EPosition newPosition = new EPosition(pos, e.utility);
                    newPosition.setValid(true);

                    // update the sumUtility and ub
                    newProjectSeqDB.add(new ProjectSequence(seq, newPosition));
                    upperBound += ubInSeq;
                }
            }

            if (upperBound >= threshold) {  // for event utility and its remaining utility
                ArrayList<Integer> antecedent = new ArrayList<>();
                antecedent.add(event);
                // generate the consequent
                consequentGenerate(newProjectSeqDB, antecedent, maxAntecedent, maxConsequent);

                // for next promising event
                antecedent.remove(antecedent.size() - 1);
            }
        }
    }

    /**
     *  1. generate the consequent utilityList according to the antecedent projected database √
     *  2. extend the temporal rule
     *  3. confirm the relation of the antecedent and the consequent
     *  4. extend them in terms of the order b, m, o, e, f, c, s
     * @param projectedAnteDB   projected database of the antecedent
     * @param antecedent    the event in the antecedent
     */
    public void consequentGenerate(ArrayList<ProjectSequence> projectedAnteDB, ArrayList<Integer> antecedent, int maxAntecedent, int maxConsequent) throws IOException {
        // get the before relation PDB of the consequent
        HashSet<Integer> allConsequentEvents = getAllConsequentEvents(projectedAnteDB, antecedent);
        for (Integer ent: allConsequentEvents) {
            for (int r = 0; r < 7; r++) {
                int[] mapEventWithRSU = getMapEvtbRSU(projectedAnteDB, antecedent.get(0), ent, r);

                if(mapEventWithRSU[2] >= threshold && (double)mapEventWithRSU[1] / projectedAnteDB.size() >= confidence) {
                    ruleCount++;
                    ArrayList<Integer> consequent = new ArrayList<>();
                    consequent.add(ent);
                    ArrayList<ArrayList<Integer>> conRelation = new ArrayList<>();
                    ArrayList<Integer> conRelation0 = new ArrayList<>();
                    conRelation0.add(r);
                    conRelation.add(conRelation0);
//                    save(antecedent, consequent, new ArrayList<>(), conRelation, mapEventWithRSU[2], mapEventWithRSU[1], (double)mapEventWithRSU[1] / projectedAnteDB.size());
                    if (printOutput) {
                        System.out.println("rule: " + antecedent.get(0) + "-->" + ent + " with relation: " + r + " utility: " +
                                mapEventWithRSU[2] + " confidence: " + (double)mapEventWithRSU[1] / projectedAnteDB.size() + " ante sup: " + mapEventWithRSU[1] +
                                " con sup: " + projectedAnteDB.size());
                    }
                }

                // extend ante
                if (antecedent.size() < maxAntecedent) {
                    if(mapEventWithRSU[0] >= threshold) {
                        HashSet<Integer> setEvtExtendAnte = getSetEvtExtendAnte(projectedAnteDB, antecedent.get(0), ent);  // get all extendable events about ante
                        for (int evt: setEvtExtendAnte) {
                            // extend ext into antecedent
                            // for a specific event, get its project database and extend it.
                            LastEvt lastEvt = getEvtProjectDB(evt, projectedAnteDB, antecedent, ent, r);    // first expand
                            int RSU = lastEvt.RSU;
                            if (RSU < threshold) continue;
                            ArrayList<ProjectSequence> newProjectedDB = lastEvt.projectDB;

                            ArrayList<ArrayList<Integer>> anteRelation = new ArrayList<>();
                            ArrayList<ArrayList<Integer>> ruleRelation = new ArrayList<>();
                            ArrayList<Integer> localRelation = new ArrayList<>();
                            localRelation.add(r);
                            ruleRelation.add(localRelation);
                            // extend evt as the order of b, m, o, e, f, c, s
                            for (int i = 0; i < 7; i++) {
                                MemoryLogger.getInstance().checkMemory();
                                RSU -= extendAntecedent(newProjectedDB, evt, antecedent, anteRelation, ent, ruleRelation, i, maxAntecedent, maxConsequent);
                                if (complementRSU) {
                                    if (RSU < threshold) break;
                                }
                            }
                            ruleRelation.remove(ruleRelation.size() - 1);
                        }
                    }
                }

                // extend consequent
                if(mapEventWithRSU[3] >= threshold && (double)mapEventWithRSU[1] / projectedAnteDB.size() >= confidence) {
                    HashSet<Integer> setOfEvtExtendConsequent = getSetEventExtendConsequent(projectedAnteDB, antecedent.get(0), ent, r);
                    for (Integer evt: setOfEvtExtendConsequent) {
                        ArrayList<Integer> consequent = new ArrayList<>();
                        consequent.add(ent);
                        ArrayList<Integer> relation = new ArrayList<>();
                        relation.add(r);
                        LastEvt lastEvt = getEvtProjectDBC(evt, projectedAnteDB, antecedent, consequent, relation);    // first expand
                        int RSU = lastEvt.RSU;
                        if (RSU < threshold) continue;
                        ArrayList<ProjectSequence> newProjectedDB = lastEvt.projectDB;
                        ArrayList<ArrayList<Integer>> ruleRelation = new ArrayList<>();
                        ArrayList<Integer> localRelation = new ArrayList<>();
                        localRelation.add(r);
                        ruleRelation.add(localRelation);

                        for (int i = 0; i < 7; i++) {
                            MemoryLogger.getInstance().checkMemory();
                            RSU -= extendConsequent(newProjectedDB, evt, antecedent, new ArrayList<>(), consequent, ruleRelation, i, (double)projectedAnteDB.size(), maxConsequent);
                            if (complementRSU) {
                                if (RSU < threshold) break;
                            }
                        }

                        consequent.remove(consequent.size() - 1);
                    }
                }
            }
        }
    }

    private int extendConsequent(ArrayList<ProjectSequence> projectedDB, int evt, ArrayList<Integer> antecedent, ArrayList<ArrayList<Integer>> anteRelation
            , ArrayList<Integer> consequent, ArrayList<ArrayList<Integer>> ruleRelation, int relation, double anteSup, int maxConsequent) throws IOException {
        candidate++;
        int rsu = 0;
        for (ProjectSequence projectSequence: projectedDB) {
            VHRepresentation seq = projectSequence.getSequence();
            ArrayList<VHElement> elements = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            int evtPos = idxs.get(evt);
            if (relation != getRelation(elements.get(evtPos), elements.get(idxs.get(consequent.get(consequent.size() - 1))))) continue;
            rsu += projectSequence.getEPositions().getUtility() + elements.get(idxs.get(evt)).ru + elements.get(idxs.get(evt)).utility;
        }

        if (rsu >= threshold) {
            HashMap<ArrayList<Integer>, Integer> relationToRSU = new HashMap<>(); // <ruleRelation: rsu>
            HashMap<ArrayList<Integer>, Integer> relationToUtility = new HashMap<>();   // <ruleRelation: utility>
            HashMap<ArrayList<Integer>, Integer> relationToSupport = new HashMap<>();   // <ruleRelation: support>
            HashMap<ArrayList<Integer>, ArrayList<ProjectSequence>> relationToProjectedDB = new HashMap<>();   // <ruleRelation: DB>

            for (ProjectSequence projectSequence : projectedDB) {
                VHRepresentation seq = projectSequence.getSequence();
                ArrayList<VHElement> elements = seq.horizontalList;
                HashMap<Integer, Integer> idxs = seq.verticalMap;
                int eventPos = idxs.get(evt);
                if (relation != getRelation(elements.get(eventPos), elements.get(idxs.get(consequent.get(consequent.size() - 1)))))
                    continue;
                ArrayList<Integer> relations = getAllRelations(elements, idxs, antecedent, consequent, evt);
                VHElement event = elements.get(eventPos);
                if (projectSequence.getEPositions().getValid()) {  // a legal candidate rule
                    ArrayList<ProjectSequence> newDB = relationToProjectedDB.get(relations);
                    EPosition ePosition = new EPosition(eventPos, projectSequence.getEPositions().getUtility()
                            + event.utility);
                    ePosition.setValid(true);
                    if (newDB == null) {
                        newDB = new ArrayList<>();
                        newDB.add(new ProjectSequence(seq, ePosition));
                        relationToProjectedDB.put(relations, newDB);
                    } else {
                        newDB.add(new ProjectSequence(seq, ePosition));
                        relationToProjectedDB.put(relations, newDB);
                    }
                    if (relationToRSU.get(relations) == null) {
                        int localRSU = ePosition.getUtility() + event.ru;
                        relationToRSU.put(relations, localRSU);
                        int localUtility = ePosition.getUtility();
                        relationToUtility.put(relations, localUtility);
                        relationToSupport.put(relations, 1);
                    } else {
                        int localRSU = ePosition.getUtility() + event.ru;
                        int localUtility = ePosition.getUtility();
                        relationToRSU.put(relations, relationToRSU.get(relations) + localRSU);
                        relationToUtility.put(relations, relationToUtility.get(relations) + localUtility);
                        relationToSupport.put(relations, relationToSupport.get(relations) + 1);
                    }
                }
            }

            consequent.add(evt);
            for (Map.Entry<ArrayList<Integer>, Integer> entry: relationToRSU.entrySet()) {
                ArrayList<Integer> newRelation = entry.getKey();
                if (entry.getValue() >= threshold && (double)relationToSupport.get(newRelation) / anteSup >= confidence) {
                    if (ruleRelation == null) ruleRelation = new ArrayList<>();
                    ruleRelation.add(newRelation);

                    ArrayList<ProjectSequence> newDB = relationToProjectedDB.get(newRelation);
                    if (relationToUtility.get(newRelation) >= threshold) {
                        ruleCount++;
                        save(antecedent, consequent, anteRelation, ruleRelation, relationToUtility.get(newRelation),
                                relationToSupport.get(newRelation), (double)relationToSupport.get(newRelation) / anteSup);
                        if (printOutput) {
                            if (consequent.size() > 1)
                                System.out.println("rule: " + antecedent + "-->" + consequent + " with relation: " + anteRelation + " and "
                                        + ruleRelation + " utility: " + relationToUtility.get(newRelation)+ " confidence: " + (double)relationToSupport.get(newRelation) / anteSup +
                                        " ante sup: " + anteSup + " con sup: " + relationToSupport.get(newRelation));
                        }
                    }

                    // extend rule
                    if (consequent.size() < maxConsequent) {
                        MemoryLogger.getInstance().checkMemory();
                        runExtendConsequent(newDB, antecedent, anteRelation, consequent, ruleRelation, anteSup, maxConsequent);
                    }

                    ruleRelation.remove(ruleRelation.size() - 1);
                }
                MemoryLogger.getInstance().checkMemory();
            }
            consequent.remove(consequent.size() - 1);
        }

        return rsu;
    }

    private void runExtendConsequent(ArrayList<ProjectSequence> projectDB, ArrayList<Integer> antecedent, ArrayList<ArrayList<Integer>> anteRelation
            , ArrayList<Integer> consequent, ArrayList<ArrayList<Integer>> ruleRelation, double anteSup, int maxConsequent) throws IOException {
        HashSet<Integer> setEvtExtendConsequent;
        if (antecedent.size() == 1) setEvtExtendConsequent = getSetEventExtendConsequent(projectDB);
        else setEvtExtendConsequent = getSetEventExtendConsequent(projectDB, antecedent, consequent, ruleRelation.get(ruleRelation.size() - 1));
        for (int evt: setEvtExtendConsequent) {
            LastEvt lastEvt = getEvtProjectDBC(evt, projectDB, antecedent, consequent, ruleRelation.get(ruleRelation.size() - 1));
            int RSU = lastEvt.RSU;
            if (RSU < threshold) continue;
            ArrayList<ProjectSequence> newProjectedDB = lastEvt.projectDB;
            // extend as the order of b, m, o, e, f, c, s
            for (int i = 0; i < 7; i++) {
                MemoryLogger.getInstance().checkMemory();
                RSU -= extendConsequent(newProjectedDB, evt, antecedent, anteRelation, consequent, ruleRelation, i, anteSup, maxConsequent);    // TODO
                if (complementRSU) {
                    if (RSU < threshold) break;
                }
            }
        }
    }

    private LastEvt getEvtProjectDBC(int evt, ArrayList<ProjectSequence> projectedAnteDB, ArrayList<Integer> antecedent, ArrayList<Integer> consequent, ArrayList<Integer> relation) {
        int RSU = 0;
        ArrayList<ProjectSequence> projectSequences = new ArrayList<>();

        for (ProjectSequence projectSequence : projectedAnteDB) {
            VHRepresentation seq = projectSequence.getSequence();
            ArrayList<VHElement> elements = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            if (idxs.get(consequent.get(consequent.size() - 1)) == null)
                continue; // if the last event of consequent does not appear in the seq, continue.
            // (This is only for the first expand)
            if (idxs.get(evt) == null) continue; // if the extending event does not appear in this seq, continue
            ArrayList<Integer> rule = new ArrayList<>(antecedent);
            rule.addAll(consequent);
            rule.remove(rule.size() - 1);
            ArrayList<Integer> r = getAllRelations(elements, idxs, rule, consequent.get(consequent.size() - 1));
            if (!compare2Relation(r, relation)) continue;
            int lastEvtPos = idxs.get(consequent.get(consequent.size() - 1));
            int evtPos = idxs.get(evt);
            if (lastEvtPos > evtPos) continue;  // the extending event should after the last event of consequent.

            // calculate all relations RSU
            int localRSU = 0;
            VHElement event = elements.get(evtPos);
            // consequent event does appear in this sequence
            VHElement conseEvt = elements.get(lastEvtPos);
            // construct and update the projectDB
            if (consequent.size() == 1) {
                EPosition newPosition = new EPosition(evtPos, projectSequence.getEPositions().getUtility() + conseEvt.utility);
                newPosition.setValid(true);
                projectSequences.add(new ProjectSequence(seq, newPosition));
            } else {
                EPosition newPosition = new EPosition(evtPos, projectSequence.getEPositions().getUtility());
                newPosition.setValid(true);
                projectSequences.add(new ProjectSequence(seq, newPosition));
            }
            // compute the consequent RSU
            // event appear in the left
            // first add the utility in antecedent
            localRSU += projectSequence.getEPositions().getUtility();
            // second add the last event remaining utility, note that we should subtract the right utility
            localRSU += event.ru + event.utility;
            // last we add the right remaining utility
            if (consequent.size() == 1) // for first expand we should add the consequent utility.
                localRSU += conseEvt.utility;
            RSU += localRSU;
        }

        return new LastEvt(RSU, projectSequences);
    }

    private HashSet<Integer> getSetEventExtendConsequent(ArrayList<ProjectSequence> projectedDB, ArrayList<Integer> antecedent, ArrayList<Integer> consequent, ArrayList<Integer> relation) {
        HashSet<Integer> setEvtExtendConse = new HashSet<>();
        for (ProjectSequence sequence: projectedDB) {
            ArrayList<VHElement> elements = sequence.getSequence().horizontalList;
            HashMap<Integer, Integer> idxs = sequence.getSequence().verticalMap;
            ArrayList<Integer> rule = new ArrayList<>(antecedent);
            rule.addAll(consequent);
            int event = rule.get(rule.size() - 1);
            rule.remove(rule.size() - 1);
            if (idxs.get(event) == null) continue;
            if (!compare2Relation(relation, getAllRelations(elements, idxs, rule, event))) continue;
            int pos = idxs.get(event);
            for(int i = pos + 1; i < elements.size(); i++) {
                VHElement evt = elements.get(i);
                setEvtExtendConse.add(evt.event);
            }
        }
        // return all events that can extend to the consequent.
        return setEvtExtendConse;
    }
    private HashSet<Integer> getSetEventExtendConsequent(ArrayList<ProjectSequence> projectedAnteDB) {
        HashSet<Integer> setEvtExtendConse = new HashSet<>();
        for (ProjectSequence sequence: projectedAnteDB) {
            ArrayList<VHElement> elements = sequence.getSequence().horizontalList;
            int pos = sequence.getEPositions().getIndex();
            for(int i = pos + 1; i < elements.size(); i++) {
                VHElement evt = elements.get(i);
                setEvtExtendConse.add(evt.event);
            }
        }
        // return all events that can extend to the consequent.
        return setEvtExtendConse;
    }
    private HashSet<Integer> getSetEventExtendConsequent(ArrayList<ProjectSequence> projectedAnteDB, int ante, int conse, int relation) {
        HashSet<Integer> setEvtExtendConse = new HashSet<>();
        for (ProjectSequence sequence: projectedAnteDB) {
            VHRepresentation seq = sequence.getSequence();
            ArrayList<VHElement> elements = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            if (idxs.get(conse) == null) continue;
            if (getRelation(elements.get(idxs.get(conse)), elements.get(idxs.get(ante))) != relation) continue; // for first extend,
            // if the ante and conse can't for the particular relation, then continue
            for(int idx = idxs.get(conse) + 1; idx < elements.size(); idx++) {
                VHElement evt = elements.get(idx);
                setEvtExtendConse.add(evt.event);
            }
        }
        // return all events that can extend to the consequent.
        return setEvtExtendConse;
    }

    private int extendAntecedent(ArrayList<ProjectSequence> projectedDB, int event, ArrayList<Integer> antecedent, ArrayList<ArrayList<Integer>> anteRelation,
                                 int ent, ArrayList<ArrayList<Integer>> ruleRelation, int r, int maxAntecedent, int maxConsequent) throws IOException {
        candidate++;
        int rsu = 0;
        int roughRSU = 0;
        for(ProjectSequence projectSequence: projectedDB) {
            VHRepresentation seq = projectSequence.getSequence();
            ArrayList<VHElement> elements = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            int evtPos = idxs.get(event);
            if (r != getRelation(elements.get(evtPos), elements.get(idxs.get(antecedent.get(antecedent.size() - 1))))) continue;
            if (idxs.get(ent) != null) {
                int leftPos = elements.get(idxs.get(ent)).sameStPos;
                if(evtPos < leftPos) {
                    VHElement samePosEvt = elements.get(leftPos - 1);
                    VHElement leftEvt = elements.get(idxs.get(ent));
                    VHElement extEvt = elements.get(evtPos);
                    roughRSU += projectSequence.getEPositions().getUtility() + extEvt.utility + extEvt.ru - samePosEvt.ru + leftEvt.utility + leftEvt.ru;
                }
            }
        }

        if (roughRSU >= threshold) {
            HashMap<ArrayList<Integer>, HashMap<ArrayList<Integer>, Integer>> relationToRSU = new HashMap<>(); // <anteRel: <conseRel: rsu>>
            HashMap<ArrayList<Integer>, HashMap<ArrayList<Integer>, Integer>> relationToUtility = new HashMap<>();   // <anteRel: <conseRel: utility>>
            HashMap<ArrayList<Integer>, HashMap<ArrayList<Integer>, Integer>> relationToSupport = new HashMap<>();   // <anteRel: <conseRel: support>>
            HashMap<ArrayList<Integer>, ArrayList<ProjectSequence>> relationToProjectedDB = new HashMap<>();   // <anteRel: DB>
            for (ProjectSequence projectSequence : projectedDB) {
                VHRepresentation seq = projectSequence.getSequence();
                ArrayList<VHElement> elements = seq.horizontalList;
                HashMap<Integer, Integer> idxs = seq.verticalMap;
                int eventPos = idxs.get(event);
                if (r != getRelation(elements.get(eventPos), elements.get(idxs.get(antecedent.get(antecedent.size() - 1)))))
                    continue;
                ArrayList<Integer> relations = getAllRelations(elements, idxs, antecedent, event);
                ArrayList<ProjectSequence> newDB = relationToProjectedDB.get(relations);
                EPosition ePosition = new EPosition(eventPos, projectSequence.getEPositions().getUtility()
                        + elements.get(eventPos).utility);
                if (newDB == null) {
                    newDB = new ArrayList<>();
                    newDB.add(new ProjectSequence(seq, ePosition));
                    relationToProjectedDB.put(relations, newDB);
                } else {
                    newDB.add(new ProjectSequence(seq, ePosition));
                    relationToProjectedDB.put(relations, newDB);
                }
                if (idxs.get(ent) != null) { // a legal candidate rule
                    int conPos = idxs.get(ent);
                    int samPos = elements.get(conPos).sameStPos;
                    if (eventPos < samPos) { // for a legal rule, update its utility and ub
                        VHElement samePos = elements.get(elements.get(conPos).sameStPos);
                        if (!compare2Relation(getAllRelations(elements, idxs, antecedent, ent), ruleRelation.get(ruleRelation.size() - 1)))
                            continue;
                        ArrayList<Integer> newAnte = new ArrayList<>(antecedent);
                        newAnte.add(event);
                        ArrayList<Integer> newRuleRelation = getAllRelations(elements, idxs, newAnte, ent);
                        int localRSU, utility;
                        localRSU = relationToProjectedDB.get(relations).get(relationToProjectedDB.get(relations).size() - 1).getEPositions().getUtility() + elements.get(eventPos).ru +
                                elements.get(conPos).utility + elements.get(conPos).ru - samePos.ru - samePos.utility;
                        utility = relationToProjectedDB.get(relations).get(relationToProjectedDB.get(relations).size() - 1).getEPositions().getUtility() + elements.get(conPos).utility;
                        HashMap<ArrayList<Integer>, Integer> rRSU = relationToRSU.get(relations);
                        HashMap<ArrayList<Integer>, Integer> rSupport = relationToSupport.get(relations);
                        HashMap<ArrayList<Integer>, Integer> rUtility = relationToUtility.get(relations);
                        EPosition newEPosition = new EPosition(eventPos, projectSequence.getEPositions().getUtility()
                                + elements.get(eventPos).utility);
                        newEPosition.setValid(true);
                        newDB = relationToProjectedDB.get(relations);
                        newDB.remove(newDB.size() - 1);
                        newDB.add(new ProjectSequence(seq, newEPosition));
                        relationToProjectedDB.put(relations, newDB);
                        if (rRSU == null) {
                            rRSU = new HashMap<>();
                            rRSU.put(newRuleRelation, localRSU);
                            rUtility = new HashMap<>();
                            rUtility.put(newRuleRelation, utility);
                            rSupport = new HashMap<>();
                            rSupport.put(newRuleRelation, 1);
                            relationToSupport.put(relations, rSupport);
                            relationToRSU.put(relations, rRSU);
                            relationToUtility.put(relations, rUtility);
                        } else {
                            if (rRSU.get(newRuleRelation) == null) {
                                rSupport.put(newRuleRelation, 1);
                                rRSU.put(newRuleRelation, localRSU);
                                rUtility.put(newRuleRelation, utility);
                            } else {
                                rRSU.put(newRuleRelation, rRSU.get(newRuleRelation) + localRSU);
                                rUtility.put(newRuleRelation, rUtility.get(newRuleRelation) + utility);
                                rSupport.put(newRuleRelation, rSupport.get(newRuleRelation) + 1);
                            }
                        }
                        rsu += localRSU;
                    }
                }
            }

            antecedent.add(event);
            ArrayList<Integer> g = ruleRelation.get(ruleRelation.size() - 1);
            ArrayList<Integer> conse = new ArrayList<>();
            conse.add(ent);
            for (Map.Entry<ArrayList<Integer>, HashMap<ArrayList<Integer>, Integer>> entry: relationToRSU.entrySet()) {
                ArrayList<Integer> relation = entry.getKey();
                for (Map.Entry<ArrayList<Integer>, Integer> specificEntry: entry.getValue().entrySet()) {
                    ArrayList<Integer> gRelation = specificEntry.getKey();
                    if(specificEntry.getValue() >= threshold) {
                        if (anteRelation == null) anteRelation = new ArrayList<>();
                        anteRelation.add(relation);
                        ruleRelation.set(ruleRelation.size() - 1, gRelation);
                        ArrayList<ProjectSequence> newDB = relationToProjectedDB.get(relation);
                        if (relationToUtility.get(relation).get(gRelation) >= threshold && (double)relationToSupport.get(relation).get(gRelation)
                                / newDB.size() >= confidence) {
                            ruleCount++;
                            save(antecedent, conse, anteRelation, ruleRelation, relationToUtility.get(relation).get(gRelation),
                                    relationToSupport.get(relation).get(gRelation), (double)relationToSupport.get(relation).get(gRelation) / newDB.size());
                            if (printOutput) {
                                System.out.println("rule: " + antecedent + "-->" + ent + " with relation: " + anteRelation
                                        + " and " + ruleRelation + " utility: " + relationToUtility.get(relation).get(gRelation) + " confidence: "
                                        + (double)relationToSupport.get(relation).get(gRelation) / relationToProjectedDB.get(relation).size() + " ante sup: " + relationToSupport.get(relation).get(gRelation) +
                                        " con sup: " + relationToProjectedDB.get(relation).size());
                            }
                        }
                        if (antecedent.size() < maxAntecedent) {
                            MemoryLogger.getInstance().checkMemory();
                            runExtendAntecedent(newDB, antecedent, anteRelation, ent, ruleRelation, maxAntecedent, maxConsequent);
                        }

                        ArrayList<Integer> consequent = new ArrayList<>();
                        consequent.add(ent);
                        if (consequent.size() < maxConsequent) {
                            MemoryLogger.getInstance().checkMemory();
                            runExtendConsequent(newDB, antecedent, anteRelation, consequent, ruleRelation, (double) newDB.size(), maxConsequent);
                        }
                        anteRelation.remove(anteRelation.size() - 1);
                        ruleRelation.set(ruleRelation.size() - 1, g);
                    }
                }
                MemoryLogger.getInstance().checkMemory();
            }
            antecedent.remove(antecedent.size() - 1);
        }

        return rsu;
    }

    private void runExtendAntecedent(ArrayList<ProjectSequence> projectSequencesDB, ArrayList<Integer> antecedent, ArrayList<ArrayList<Integer>> anteRelation,
                                     int consequent, ArrayList<ArrayList<Integer>> ruleRelation, int maxAntecedent, int maxConsequent) throws IOException {
        HashSet<Integer> setEvtExtendAnte =  getSetEvtExtendAnte(projectSequencesDB, antecedent.get(antecedent.size() - 1), consequent);  // get all extendable events about ante
        for (int evt: setEvtExtendAnte) {
            LastEvt lastEvt = getEvtProjectDB(evt, projectSequencesDB, antecedent, consequent);
            int RSU = lastEvt.RSU;
            if (RSU < threshold) continue;
            ArrayList<ProjectSequence> newProjectedDB = lastEvt.projectDB;

//             extend as the order of b, m, o, e, f, c, s
            for (int i = 0; i < 7; i++) {
                MemoryLogger.getInstance().checkMemory();
                RSU -= extendAntecedent(newProjectedDB, evt, antecedent, anteRelation, consequent, ruleRelation, i, maxAntecedent, maxConsequent);
                if (complementRSU) {
                    if (RSU < threshold) break;
                }
            }
        }
    }
    private ArrayList<Integer> getAllRelations(ArrayList<VHElement> elements, HashMap<Integer, Integer> idxs, ArrayList<Integer> antecedent, int evt) {
        ArrayList<Integer> relA = new ArrayList<>();
        VHElement e1 = elements.get(idxs.get(evt));
        for (Integer integer : antecedent) {
            relA.add(getRelation(e1, elements.get(idxs.get(integer))));
        }
        return relA;
    }

    private ArrayList<Integer> getAllRelations(ArrayList<VHElement> elements, HashMap<Integer, Integer> idxs, ArrayList<Integer> antecedent,
                                               ArrayList<Integer> consequent, int evt) {
        ArrayList<Integer> relA = new ArrayList<>();
        ArrayList<Integer> rule = new ArrayList<>(antecedent);
        rule.addAll(consequent);
        VHElement e1 = elements.get(idxs.get(evt));
        for(Integer integer: rule) {
            relA.add(getRelation(e1, elements.get(idxs.get(integer))));
        }
        return relA;
    }

    private int getRelation(VHElement e1, VHElement e2) {
        if (e1.st > e2.ft) return 0; // before
        if (e1.st == e2.ft) return 1;    // meet
        if (e1.st > e2.st && e1.ft > e2.ft) return 2;   // overlap
        if (e1.st == e2.st && e1.ft == e2.ft) return 3; // equal
        if (e1.st > e2.st && e1.ft == e2.ft) return 4; // finish
        if (e1.st > e2.st) return 5;   // contain
        if (e1.st == e2.st && e1.ft < e2.ft) return 6;  // start
        return -1;
    }

    private LastEvt getEvtProjectDB(int evt, ArrayList<ProjectSequence> projectedAnteDB, ArrayList<Integer> antecedent, int ent) {
        int RSU = 0;
        ArrayList<ProjectSequence> projectSequences = new ArrayList<>();

        for (int i = 0; i < projectedAnteDB.size(); i++) {
            VHRepresentation seq = projectedAnteDB.get(i).getSequence();
            ArrayList<VHElement> elements = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            if(idxs.get(evt) == null) continue;
            int lastEvtPos = idxs.get(antecedent.get(antecedent.size() - 1));
            int evtPos = idxs.get(evt);
            if (lastEvtPos > evtPos) continue;
            // construct and update the projectDB
            EPosition newPosition = new EPosition(evtPos, projectedAnteDB.get(i).getEPositions().getUtility());
            projectSequences.add(new ProjectSequence(seq, newPosition));
            // calculate all relations RSU
            int localRSU = 0;
            VHElement event = elements.get(evtPos);
            Integer consePos = idxs.get(ent);
            if(consePos != null) {
                // consequent event does appear in this sequence
                VHElement conseEvt = elements.get(consePos);
                int samePos = conseEvt.sameStPos;
                if(evtPos < samePos) {
                    // event appear in the left
                    // first add the utility in antecedent
                    localRSU += projectedAnteDB.get(i).getEPositions().getUtility();
                    // second add the last event remaining utility, note that we should subtract the right utility
                    localRSU += event.ru - elements.get(samePos - 1).ru + event.utility;
                    // last we add the right remaining utility
                    localRSU += elements.get(idxs.get(ent)).utility + elements.get(idxs.get(ent)).ru;
                    RSU += localRSU;
                }
            }
        }

        return new LastEvt(RSU, projectSequences);
    }

    private LastEvt getEvtProjectDB(int evt, ArrayList<ProjectSequence> projectedAnteDB, ArrayList<Integer> antecedent, int ent, int r) {
        int RSU = 0;
        ArrayList<ProjectSequence> projectSequences = new ArrayList<>();

        for (ProjectSequence projectSequence : projectedAnteDB) {
            VHRepresentation seq = projectSequence.getSequence();
            ArrayList<VHElement> elements = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            if (idxs.get(evt) == null) continue;
            int lastEvtPos = idxs.get(antecedent.get(antecedent.size() - 1));
            int evtPos = idxs.get(evt);
            if (lastEvtPos > evtPos) continue;
            // construct and update the projectDB
            EPosition newPosition = new EPosition(evtPos, projectSequence.getEPositions().getUtility());
            projectSequences.add(new ProjectSequence(seq, newPosition));
            // calculate all relations RSU
            int localRSU = 0;
            VHElement event = elements.get(evtPos);
            Integer consePos = idxs.get(ent);
            if (consePos != null) {
                // consequent event does appear in this sequence
                VHElement conseEvt = elements.get(consePos);
                if (getRelation(conseEvt, elements.get(lastEvtPos)) != r) {
                    continue;
                }
                int samePos = conseEvt.sameStPos;
                if (evtPos < samePos) {
                    projectSequences.get(projectSequences.size() - 1).getEPositions().setValid(true);
                    // event appear in the left
                    // first add the utility in antecedent
                    localRSU += projectSequence.getEPositions().getUtility();
                    // second add the last event remaining utility, note that we should subtract the right utility
//                    System.out.println(idxs);
                    localRSU += event.ru + event.utility - elements.get(samePos - 1).ru;
                    // last we add the right remaining utility
                    localRSU += elements.get(idxs.get(ent)).utility + elements.get(idxs.get(ent)).ru;
                    RSU += localRSU;
                }
            }
        }

        return new LastEvt(RSU, projectSequences);
    }


    private HashSet<Integer> getSetEvtExtendAnte(ArrayList<ProjectSequence> projectedAnteDB, int ante, int conse) {
        HashSet<Integer> setEvtExtendAnte = new HashSet<>();

        for (ProjectSequence sequence: projectedAnteDB) {
            VHRepresentation seq = sequence.getSequence();
            ArrayList<VHElement> elements = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            if (idxs.get(conse) == null) continue;
            int conseSamePos = elements.get(idxs.get(conse)).sameStPos;
            for(int idx = sequence.getEPositions().getIndex() + 1; idx < conseSamePos; idx++) {
                VHElement evt = elements.get(idx);
                setEvtExtendAnte.add(evt.event);
            }
        }

        return setEvtExtendAnte;
    }

    private int[] getMapEvtbRSU(ArrayList<ProjectSequence> projectedAnteDB, int ante, int conse, int r) {
        int[] rsu = new int[4];

        for (ProjectSequence sequence: projectedAnteDB) {
            VHRepresentation seq = sequence.getSequence();
            ArrayList<VHElement> elements = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            int antePos = idxs.get(ante);
            VHElement antecedent = elements.get(antePos);
            if(idxs.get(conse) == null) continue;
            if(idxs.get(conse) <= antePos) continue;    // first different place
            VHElement consequent = elements.get(idxs.get(conse));
            if (consequent.sameStPos <= antePos) continue;
            if(getRelation(consequent, antecedent) != r) {
                continue;
            }
            int localRSUAnte = antecedent.utility + consequent.utility + consequent.ru + antecedent.ru - elements.get(consequent.sameStPos - 1).ru;
            int localRSUConse = antecedent.utility + consequent.utility + consequent.ru;
            int ruleUility = antecedent.utility + consequent.utility;
            rsu[0] += localRSUAnte; // ante RSU
            rsu[1]++;   // rule support
            rsu[2] += ruleUility;   // rule utility
            rsu[3] += localRSUConse;    // consequent RSU
        }

        return rsu;
    }

    private HashSet<Integer> getAllConsequentEvents(ArrayList<ProjectSequence> projectedAnteDB, ArrayList<Integer> antecedent) {
        HashSet<Integer> allConseEntb = new HashSet<>();

        for(ProjectSequence projectSequence: projectedAnteDB) {
            VHRepresentation seq = projectSequence.getSequence();
            ArrayList<VHElement> events = seq.horizontalList;
            HashMap<Integer, Integer> idxs = seq.verticalMap;
            VHElement e = events.get(idxs.get(antecedent.get(0)));
            for (int idx = idxs.get(antecedent.get(0)) + 1; idx < idxs.size(); idx++) {
                VHElement ne = events.get(idx);
                if(ne.st <= e.st) continue;
                allConseEntb.add(ne.event);
            }
        }

        return allConseEntb;
    }

    private boolean compare2Relation(ArrayList<Integer> r1, ArrayList<Integer> r2) {
        if (r1.size() != r2.size()) return false;

        for(int i = 0; i < r1.size(); i++) {
            if(!r1.get(i).equals(r2.get(i))) return false;
        }

        return true;
    }
}
