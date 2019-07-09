package DataRecording;

/**
 * Created by gcdc on 6/7/17.
 * Simple class to store a subindex reading and its origin
 */
public class SubIndexValue {
    private int index, subindex, val;
    private long elapsedTime;
    public SubIndexValue(int index, int subindex, int val, long elapsedTime){
        this.index = index;
        this.subindex = subindex;
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public int getSubindex() {
        return subindex;
    }

    public int getIndex() {
        return index;
    }

    public String toString(){
        int fullAddress = index<<8|subindex;
        return String.format("0x%06x: %d",fullAddress,val);
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
}
