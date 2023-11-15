package Algo;

/**
 * @Copyright (C), HITSZ
 * @Author Maohua Lyu, Wensheng, gan
 * @since 2022/11/17
 **/
public class EPosition {
    private int index;
    private int utility;
    private boolean isValid = false;

    public EPosition(int index, int utility) {
        this.index = index;
        this.utility = utility;
    }

    public int getIndex() {return this.index;}

    public int getUtility() {return this.utility;}

    public void setValid(boolean valid) {this.isValid = valid;}

    public boolean getValid() {return this.isValid;}


    @Override
    public String toString() {
        String ret = "";
        ret += "(index: " + index + " utility: " + utility + " isValid: " + isValid;
        return ret;
    }
}
