package CO3401;

/**
 * @author Rusiru Sewwantha
 */
public class Sack {
    int id;
    Present[] accumulation;
    int sackPresentCount = 0;
    int totalPresents = 0;
    int replaced = 0;

    public Sack(int id, int capacity) {
        accumulation = new Present[capacity];
        this.id = id;
    }

    //TODO - Add more methods
    //fill sack
    public void fillSack(Present present) {
        if (present != null) {
            accumulation[sackPresentCount] = present;
            System.out.println("Present " + present.readDestination() + " added to sack " + this.id);
            System.out.println("No of Presents in sack " + id + " is " + (sackPresentCount + 1));
            sackPresentCount++;
        }
    }

    public int presentsAddedToSack() {
        totalPresents = replaced + sackPresentCount;
        return totalPresents;
    }

    public void emptySack() {
        replaced = sackPresentCount;
        sackPresentCount = 0;
        for (int p = 0; p < accumulation.length; p++) {
            accumulation[p] = null;
        }
        System.out.println("Sack " + id + " replaced by elf");
    }

    //returns true if the sack has more space for presents
    boolean isSpaceAvailable() {
        return accumulation.length > sackPresentCount;
    }
}
