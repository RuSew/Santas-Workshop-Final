package CO3401;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

/**
 * @author Rusiru Sewwantha
 */
public class Conveyor {
    public HashSet<Integer> beltDestinations = new HashSet();//belts mapped to sacks
    int id;
    private final Present[] presents; // The requirements say this must be a fixed size array
    private int presentsOnBelt = 0;
    private int capacity = 0;
    private int beltPresentCount = 0;
    private int presentOut = 0;
    int hopperPresentCount = 0;
    Semaphore freeSpacesOnBelt;
    Semaphore onBelt = new Semaphore(0);//for presents on the belt
    Semaphore mutex = new Semaphore(1);

    // TODO - add more members?

    public Conveyor(int id, int size) {
        this.id = id;
        this.capacity = size;//capacity of the belt
        presents = new Present[size];//buffer for capacity of the belt
        freeSpacesOnBelt = new Semaphore(size);//to allow max amount of presents on the belt at a time

        //TODO - more construction likely!
    }

    public void addDestination(int sackId) {
        //belt mapped to the sack
        System.out.println("------------- belt id: " + id + " sack id: " + sackId);//remove this later
        beltDestinations.add(sackId);
    }

    // TODO - add more functions
    public boolean hasSpace() {
        //returns true if there is space on the belt
        return presentsOnBelt < capacity;
    }

    public int getPresentsOnBelt() {
        //returns the no of presents on the belt
        //System.out.println("Presents on belt " + id + " : " + presentsOnBelt);
        return presentsOnBelt;
    }

    public Present[] getPresents(){
        return this.presents;
    }

    public boolean isPresentNull(){
        if(this.id==1 || this.id == 2)
            System.out.println("");
        return this.presents[presents.length-1]==null;
    }

    public String getDestination() {
        return presents[presentOut].readDestination();
    }

    public boolean isDestination(int destination) {
        //System.out.println("Belt destinations+++++++++++" + beltDestinations.size());
        for (int d : beltDestinations) {
            if (d == destination) {
                return true;
            }
        }
        return false;
    }

    public void enter(Present present) throws  InterruptedException{
//        freeSpacesOnBelt.acquire();
//        mutex.acquire();
        presents[beltPresentCount] = present;//set the present onto the belt
        presentsOnBelt++;
        beltPresentCount++;
        if(beltPresentCount == presents.length){
            //checks if the no of presents on belt equals to the capacity of the belt
            //resets the present count
            beltPresentCount = 0;
        }
//        onBelt.release();
//        mutex.release();
    }

    public Present exit() throws InterruptedException{
//        onBelt.acquire();
//        mutex.acquire();
        Present present = presents[presentOut];
        presentsOnBelt--;
        presentOut++;
        if(presentOut == presents.length){
            presentOut = 0;
        }
//        mutex.release();
//        freeSpacesOnBelt.release();
        return present;
    }
}
