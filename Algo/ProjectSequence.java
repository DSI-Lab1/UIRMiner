package Algo;

/**
 * @Copyright (C), HITSZ
 * @Author Maohua Lyu, Wensheng, gan
 * @since 2022/11/17
 **/
public class ProjectSequence {
    private VHRepresentation sequence;
    private EPosition ePosition;


    public ProjectSequence(VHRepresentation sequence, EPosition ePosition) {
        this.sequence = sequence;
        this.ePosition = ePosition;
    }

    public VHRepresentation getSequence() {
        return this.sequence;
    }

    public EPosition getEPositions() {
        return this.ePosition;
    }

    @Override
    public String toString() {
        String ret = "ProjectSequence: <Position: [";
        ret += ePosition;
        ret += "]";
        ret += ", sid: " + sequence.getSeqID();
        ret += ">";
        return ret;
    }
}
