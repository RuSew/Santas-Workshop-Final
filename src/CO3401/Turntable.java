package CO3401;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rusiru Sewwantha
 */
public class Turntable extends Thread {
    //ports
    static int N = 0;
    static int E = 1;
    static int S = 2;
    static int W = 3;
    // global lookup: age-range -> SackID
    static HashMap<String, Integer> destinations = new HashMap<>();//key->age, value->sackId
    private final int inputDirection0 = 0;
    private final int inputDirection3 = 0;
    String id;
    Present present;
    Connection[] connections = new Connection[4];
    long tableRotationTime = 500; //0.5s to rotate 90 degrees
    long presentMoveTime = 750; //0.75s to move a present either on or off a turntable
    boolean isTurntableFree = true;
    //alignments of the turntable
    int[][] alignments = {
            {0, 2},
            {3, 1}
    };
    //set the initial directions
    final int north = alignments[0][0];
    final int south = alignments[0][1];
    final int west = alignments[1][0];
    final int east = alignments[1][1];
    // this individual table's lookup: SackID -> output port
    HashMap<Integer, Integer> outputMap = new HashMap<>();
    private int outputDirection = 0;
    private HashMap<Integer, String> destinationMap;

    public Turntable(String ID) {
        id = ID;
    }

    public void addConnection(int port/*turntable direction*/, Connection conn/*con type, connected belt, connected sack*/) {

        connections[port] = conn;//4 ports of the turntable

        if (conn != null) {
            if (conn.connType == ConnectionType.OutputBelt) {
                Iterator<Integer> it = conn.belt.beltDestinations.iterator();
                while (it.hasNext()) {
                    int id = it.next();
                    outputMap.put(id, port);
                }
            } else if (conn.connType == ConnectionType.OutputSack) {
                outputMap.put(conn.sack.id, port);
            }
        }
    }

    public void run() {
        System.out.println("turntable " + this.id + " run");
        // TODO
        while (AssignmentPart1.isMachineRunning) {
            //System.out.println(this.id);
            destinationMap = new HashMap<>();
            boolean inputFromBeltOne = this.connections[0] != null && this.connections[0].connType == ConnectionType.InputBelt;
            boolean inputFromBeltThree = this.connections[3] != null && this.connections[3].connType == ConnectionType.InputBelt;

            try {
                while (destinationMap.isEmpty()) {
                    if ((inputFromBeltOne && connections[0].belt.getPresentsOnBelt() != 0) || (inputFromBeltThree && connections[3].belt.getPresentsOnBelt() != 0)) {
                        if(true) {
                            if (inputFromBeltOne && inputFromBeltOne && connections[0].belt.getPresentsOnBelt() != 0) {
                                while (connections[0].belt.getPresentsOnBelt() == 0) {//checks if there are any waiting presents on the belt
                                    if (!AssignmentPart1.isMachineRunning || connections[0].belt.hopperPresentCount == 0) {
                                        break;
                                    }
                                    System.out.println("Presents on belt " + connections[0].belt.id + " are not ready yet!");
                                    Thread.sleep(5000);
                                }
                                if (connections[0].belt.getPresentsOnBelt() != 0) {
                                    destinationMap.put(0, connections[0].belt.getDestination());
                                }
                            }
                            if (inputFromBeltThree && connections[3].belt.getPresentsOnBelt() != 0) {
                                while (connections[3].belt.getPresentsOnBelt() == 0) {//checks if there are any waiting presents on the belt
                                    if (!AssignmentPart1.isMachineRunning || connections[3].belt.hopperPresentCount == 0) {
                                        break;
                                    }
                                    System.out.println("Presents on belt " + connections[3].belt.id + " are not ready yet!");
                                    Thread.sleep(5000);
                                }
                                if (connections[3].belt.getPresentsOnBelt() != 0)
                                    destinationMap.put(3, connections[3].belt.getDestination());
                            }
                        }

                    } else {
                        break;
                    }
                }
                if (!AssignmentPart1.isMachineRunning) {
                    break;
                }

                Iterator hmIterator = destinationMap.entrySet().iterator();
                // iterate through the hashmap
                while (hmIterator.hasNext()) {
                    Map.Entry mapElement = (Map.Entry) hmIterator.next();
                    outputPresents((String) mapElement.getValue(), (int) mapElement.getKey());
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(Turntable.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (arePresentsOnBelt()) {

            }
        }
    }

    public void outputPresents(String destination, int inputDirection) throws InterruptedException {
        int destinationSackId = destinations.get(destination);//get sackId by age
        Conveyor nextOutputBelt = null;
        Sack outputSack = null;
        while (!this.isTurntableFree) {
            //wait until the turntable is free
            System.out.println("Turntable " + this.id + " is not free. Waiting 1s to check again");
            Thread.sleep(1000);
        }

        if ((inputDirection == north && alignments[0][0] != north) || (inputDirection == west && alignments[1][0] != west)) {
            alignments[0][0] = north;
            alignments[0][1] = south;
            alignments[1][0] = west;
            alignments[1][1] = east;
            System.out.println("Table " + this.id + " is rotating to original position to take the present");
            Thread.sleep(tableRotationTime);
        }

        getAvailablePresent(inputDirection);//present enters the turntable
        if (present != null) {

            for (int b = 0; b < connections.length; b++) {
                if (outputMap.containsKey(destinationSackId) && connections[b] != null) {//check if the turntable can access the destination
                    if (connections[b].belt != null && connections[b].connType == ConnectionType.OutputBelt) {//if output belt
                        if (connections[b].belt.isDestination(destinationSackId)) {//destinationSackId equals to the belt destination
                            outputDirection = outputMap.get(destinationSackId);//output direction
                            nextOutputBelt = connections[b].belt;//output belt from turntable
                            System.out.println("Next output belt from turntable " + this.id + " is " + nextOutputBelt.id);
                        }
                    }
                    if (connections[b].sack != null && connections[b].connType == ConnectionType.OutputSack) {//if output sack
                        if (connections[b].sack.id == destinationSackId) {
                            outputDirection = outputMap.get(destinationSackId);//output direction
                            outputSack = connections[b].sack;//output sack from the turntable
                            System.out.println("Output sack from turntable " + this.id + " is " + outputSack.id);
                        }
                    }
                }
            }
            if (isRotateTurntable(inputDirection)) {
                Thread.sleep(tableRotationTime);
            }

            if (nextOutputBelt != null) {
                while (!nextOutputBelt.hasSpace()) {
                    //sleep thread if there's no space on the output belt
                    Thread.sleep(2000);
                    System.out.println("No space on the belt " + nextOutputBelt.id + ". Waiting 2s to add present");
                }
                Thread.sleep(presentMoveTime);
                nextOutputBelt.enter(present);//present added to the output belt
                this.isTurntableFree = true;//turntable is free
                System.out.println("Present " + present.readDestination() + " added to output belt");
            }
            if (outputSack != null) {
                while (outputSack.isSpaceAvailable() && present != null) {
                    //if there's space in the sack
                    /*if (nextOutputBelt != null) {
                        //moved to the next turntable
                        present = nextOutputBelt.exit();
                        nextOutputBelt = null;
                    }*/
                    Thread.sleep(presentMoveTime);
                    outputSack.fillSack(present);//fill sack
                    this.isTurntableFree = true;//turntable is free
                    present = null;
                }
                if (!outputSack.isSpaceAvailable() && present != null) {
                    //if there's no space in the sack
                    System.out.println("No space in the sack");
                    outputSack.emptySack();
                    Thread.sleep(presentMoveTime);
                    outputSack.fillSack(present);//fill new sack
                    System.out.println("New sack filled");
                    //System.out.println("Present " + present.readDestination() + " has left machine");
                    this.isTurntableFree = true;
                    //present = null;
                }
            }
        }
    }

    public boolean arePresentsOnHopper() {
        for (int i = 0; i < connections.length; i++) {
            if (connections[i] != null && connections[i].belt != null) {
                if (connections[i].belt.hopperPresentCount != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean arePresentsOnBelt() {
        for (int i = 0; i < connections.length; i++) {
            if (connections[i] != null && connections[i].belt != null) {
                if (connections[i].belt.getPresentsOnBelt() != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    //simulation of the present moving onto the turntable
    public void getAvailablePresent(int inputDirection) throws InterruptedException {
        //input belts of turntables
        if (inputDirection == N) {
            if (connections[0].belt.getPresentsOnBelt() > 0) {
                moveToTurntable(connections[0].belt);
                System.out.println("Present " + present.ageRange + " is moving onto the turntable from belt " + connections[0].belt.id);
                System.out.println("Present " + present.ageRange + " is on the turntable: " + this.id + "\n");
            }
        } else if (inputDirection == W) {
            if (connections[3].belt.getPresentsOnBelt() > 0) {
                moveToTurntable(connections[3].belt);
                System.out.println("Present " + present.ageRange + " is moving onto the turntable from belt " + connections[3].belt.id);
                System.out.println("Present " + present.ageRange + " is on the turntable: " + this.id + "\n");
            }
        } else {
            System.out.println("input belt doesnt exist");
        }
    }

    public void moveToTurntable(Conveyor belt) throws InterruptedException {
        Thread.sleep(presentMoveTime);
        present = belt.exit(); // Takes the present from the belt
        //inputBelt.release();
        this.isTurntableFree = false;
    }

    public boolean isRotateTurntable(int inputDirection) {
        if ((inputDirection == alignments[0][0] && outputDirection == alignments[0][1])
                || (inputDirection == alignments[1][0] && outputDirection == alignments[1][1])) {//no rotation
            System.out.println("No table rotation");
            return false;
        } else if ((inputDirection == alignments[0][0] && outputDirection == alignments[1][0])
                || (inputDirection == alignments[1][0] && outputDirection == alignments[0][1])) {//clockwise rotation
            alignments[0][0] = 1;
            alignments[0][1] = 3;
            alignments[1][0] = 0;
            alignments[1][1] = 2;
            System.out.println("Table " + this.id + " is rotating clockwise to output the present");
        } else if ((inputDirection == alignments[0][0] && outputDirection == alignments[1][1])
                || (inputDirection == alignments[1][0] && outputDirection == alignments[0][0])) {//anti-clockwise rotation
            alignments[0][0] = 3;
            alignments[0][1] = 1;
            alignments[1][0] = 2;
            alignments[1][1] = 0;
            System.out.println("Table " + this.id + " is rotating anti-clockwise to output the present");
        }
        return true;
    }
}