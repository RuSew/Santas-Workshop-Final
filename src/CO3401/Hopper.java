package CO3401;

/**
 * @author Rusiru Sewwantha
 */
public class Hopper extends Thread {
    public int presentCount = 0;
    int id;
    Conveyor belt;
    int speed;
    int depositedCount;

    Present[] collection;

    public Hopper(int id, Conveyor con, int capacity, int speed) {
        collection = new Present[capacity];
        this.id = id;
        belt = con;
        this.speed = speed;
    }

    public void fill(Present present) {
        // TODO
        //add presents to the collection array
        if (present != null) {
            collection[presentCount] = present;
            presentCount++;
            System.out.println("Present count : " + presentCount);
        }

    }

    @Override
    public void run() {
        depositedCount = 0;
        System.out.println("Hopper running");
        int hopperId = this.id;
        while (collection.length > 0 && AssignmentPart1.isHopperRunning && presentCount != 0) {
            belt.hopperPresentCount = presentCount;
            addPresentsToBelt(hopperId);
        }
        belt.hopperPresentCount = presentCount;
        System.out.println("Hopper " + id + " stopped");
    }

    //add presents to the belt
    public void addPresentsToBelt(int hopperId) {
        for (Present present : collection) {
            if (!AssignmentPart1.isHopperRunning) {
                break;
            }
            if (present != null) {
                try {
                    while (!belt.hasSpace()) {
                        Thread.sleep(speed);//No space on the belt, thread sleeps until there's space
                    }

                    System.out.println("Present " + present.ageRange + " added to " + belt.id + " from hopper " + hopperId);
                    belt.enter(present);
                    depositedCount++;
                    collection[presentCount - 1] = null;//present left the hopper
                    int count = collection.length;
                    for (Present p : collection) {
                        if (p == null) {
                            count--;
                        }
                    }
                    System.out.println("Presents on hopper " + hopperId + " : " + count);
                    presentCount--;
                    sleep(speed);
                } catch (InterruptedException ex) {
                    System.out.println("Error adding present to belt");
                }
            }

        }
    }
}
