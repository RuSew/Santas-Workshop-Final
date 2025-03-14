package CO3401;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static java.lang.Thread.sleep;

// This code (which will compile) is to help you get started with the assignment.
// The first section reads the config file and creates the objects.
//
// Later there are some strong hints about what needs to be output, in line with
// the requirements given in the assignment brief.
//
// You WILL need to add code of your own to make it work, after about line 350.
// Feel free to generally improve upon what I have written!

/**
 * @author Rusiru Sewwantha
 */
public class AssignmentPart1 {

    /**
     * @param args the command line arguments
     */
    public static boolean isMachineRunning = false;//boolean to check if the machine is running
    public static boolean isHopperRunning = false;//boolean to check if the hopper is running

    public static void main(String[] args) {
        // These variables will store the configuration
        // of the Present sorting machine

        int numBelts;
        Conveyor[] belts;

        int numHoppers;
        Hopper[] hoppers;

        int numSacks;
        Sack[] sacks;

        int numTurntables;
        Turntable[] tables;

        int timerLength;


        ////////////////////////////////////////////////////////////////////////

        // READ FILE
        // =========
        String filename = "src/CO3401/scenarios/scenario2.txt";
        Scanner inputStream = null;
        try {
            inputStream = new Scanner(new File(filename));
        } catch (FileNotFoundException ex) {
            System.out.println("Error opening file");
            System.exit(0);
        }

        String line = "";

        // READ BELTS
        // ----------
        // Skip though any blank lines to start
        while (!line.startsWith("BELTS") && inputStream.hasNextLine()) {
            line = inputStream.nextLine();
        }

        numBelts = inputStream.nextInt();
        inputStream.nextLine();

        belts = new Conveyor[numBelts];

        for (int b = 0; b < numBelts; b++) {
            line = inputStream.nextLine(); // e.g. 1 length 5 destinations 1 2

            Scanner beltStream = new Scanner(line);
            int id = beltStream.nextInt();
            beltStream.next(); // skip "length"

            int length = beltStream.nextInt(); //capacity of the belt
            belts[b] = new Conveyor(id, length);
            beltStream.next(); // skip "destinations"

            while (beltStream.hasNextInt()) {
                int dest = beltStream.nextInt();
                belts[b].addDestination(dest);//map the belt ids with sack ids
            }
            //System.out.println("Set up Belt " + id);
        } // end of reading belt lines

        // READ HOPPERS
        // ------------
        // Skip though any blank lines
        while (!line.startsWith("HOPPERS") && inputStream.hasNextLine()) {
            line = inputStream.nextLine();
        }

        numHoppers = inputStream.nextInt();
        inputStream.nextLine();

        hoppers = new Hopper[numHoppers];

        for (int h = 0; h < numHoppers; h++) {
            // Each hopper line will look like this:
            // e.g. 1 belt 1 capacity 10 speed 1

            int id = inputStream.nextInt();
            inputStream.next(); // skip "belt"

            int belt = inputStream.nextInt();
            inputStream.next(); // skip "capacity"

            int capacity = inputStream.nextInt();
            inputStream.next(); // skip "speed"

            int speed = inputStream.nextInt();
            line = inputStream.nextLine(); // skip rest of line

            hoppers[h] = new Hopper(id, belts[belt - 1], capacity, speed);

            //System.out.println("Set up Hopper " + id);
        } // end of reading hopper lines

        // READ SACKS
        // ------------
        // Skip though any blank lines
        while (!line.startsWith("SACKS") && inputStream.hasNextLine()) {
            line = inputStream.nextLine();
        }

        numSacks = inputStream.nextInt();
        inputStream.nextLine();

        sacks = new Sack[numSacks];

        for (int s = 0; s < numSacks; s++) {
            // Each sack line will look like this:
            // e.g. 1 capacity 20 age 0-3

            int id = inputStream.nextInt();
            inputStream.next(); // skip "capacity"

            int capacity = inputStream.nextInt();
            inputStream.next(); // skip "age"

            String age = inputStream.next();
            line = inputStream.nextLine(); // skip rest of line

            sacks[s] = new Sack(id, capacity);
            Turntable.destinations.put(age, id);
            System.out.println("----sack ID: " + id + " age: " + age);

            //System.out.println("Set up Sack " + id);
        } // end of reading sack lines

        // READ TURNTABLES
        // ---------------
        // Skip though any blank lines
        while (!line.startsWith("TURNTABLES") && inputStream.hasNextLine()) {
            line = inputStream.nextLine();
        }

        numTurntables = inputStream.nextInt();
        inputStream.nextLine();

        tables = new Turntable[numTurntables];

        for (int t = 0; t < numTurntables; t++) {
            // Each turntable line will look like this:
            // A N ib 1 E null S os 1 W null

            String tableId = inputStream.next();
            tables[t] = new Turntable(tableId);

            int connId = 0;

            inputStream.next(); // skip "N"
            Connection north = null;
            String Ntype = inputStream.next();//port type
            if (!"null".equals(Ntype)) {
                connId = inputStream.nextInt();//belt ids connected to turntables
                if (null != Ntype) {
                    switch (Ntype) {
                        case "os":
                            north = new Connection(ConnectionType.OutputSack, null, sacks[connId - 1]);
                            break;
                        case "ib":
                            north = new Connection(ConnectionType.InputBelt, belts[connId - 1], null);
                            break;
                        case "ob":
                            north = new Connection(ConnectionType.OutputBelt, belts[connId - 1], null);
                            break;
                    }
                    tables[t].addConnection(Turntable.N, north);
                }
            }

            inputStream.next(); // skip "E"
            Connection east = null;
            String Etype = inputStream.next();
            if (!"null".equals(Etype)) {
                connId = inputStream.nextInt();
                if (null != Etype) {
                    switch (Etype) {
                        case "os":
                            east = new Connection(ConnectionType.OutputSack, null, sacks[connId - 1]);
                            break;
                        case "ib":
                            east = new Connection(ConnectionType.InputBelt, belts[connId - 1], null);
                            break;
                        default:
                            east = new Connection(ConnectionType.OutputBelt, belts[connId - 1], null);
                            break;
                    }
                    tables[t].addConnection(Turntable.E, east);
                }
            }

            inputStream.next(); // skip "S"
            Connection south = null;
            String Stype = inputStream.next();
            if (!"null".equals(Stype)) {
                connId = inputStream.nextInt();
                if (null != Stype) {
                    switch (Stype) {
                        case "os":
                            south = new Connection(ConnectionType.OutputSack, null, sacks[connId - 1]);
                            break;
                        case "ib":
                            south = new Connection(ConnectionType.InputBelt, belts[connId - 1], null);
                            break;
                        default:
                            south = new Connection(ConnectionType.OutputBelt, belts[connId - 1], null);
                            break;
                    }
                    tables[t].addConnection(Turntable.S, south);
                }
            }

            inputStream.next(); // skip "W"
            Connection west = null;
            String Wtype = inputStream.next();
            if (!"null".equals(Wtype)) {
                connId = inputStream.nextInt();
                if (null != Wtype) {
                    switch (Wtype) {
                        case "os":
                            west = new Connection(ConnectionType.OutputSack, null, sacks[connId - 1]);
                            break;
                        case "ib":
                            west = new Connection(ConnectionType.InputBelt, belts[connId - 1], null);
                            break;
                        default:
                            west = new Connection(ConnectionType.OutputBelt, belts[connId - 1], null);
                            break;
                    }
                    tables[t].addConnection(Turntable.W, west);
                }
            }

            line = inputStream.nextLine(); // skip rest of line
            //System.out.println("Set up turntable " + tableId);
        } // end of reading turntable lines

        // FILL THE HOPPERS
        // ----------------
        for (int i = 0; i < numHoppers; i++) {
            // Skip though any blank lines
            while (!line.startsWith("PRESENTS") && inputStream.hasNextLine()) {
                line = inputStream.nextLine();
            }
            int numPresents = inputStream.nextInt();
            inputStream.nextLine();
            for (int p = 0; p < numPresents; p++) {
                hoppers[i].fill(new Present(inputStream.next()));
                line = inputStream.nextLine();
            }

            System.out.println("Filled Hopper " + hoppers[i].id);
        }

        // READ TIMER LENGTH
        // -----------------
        // Skip though any blank lines
        while (!line.startsWith("TIMER") && inputStream.hasNextLine()) {
            line = inputStream.nextLine();
        }
        Scanner timerStream = new Scanner(line);
        timerStream.next(); // skip "length"
        timerLength = timerStream.nextInt();

        System.out.println("Machine will run for " + timerLength + "s.\n");

        ///////////////////////////////////////////////////////////////////////
        // END OF SETUP ///////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////


        // START the hoppers!
        isHopperRunning = true;
        isMachineRunning = true;
        for (int h = 0; h < numHoppers; h++) {
            hoppers[h].start();
        }
        // START the turntables!
        for (int t = 0; t < numTurntables; t++) {
            tables[t].start();
        }


        long time = 0;
        long currentTime = 0;
        long startTime = System.currentTimeMillis();
        System.out.println("*** Machine Started ***");

        while (time < timerLength) {
            // sleep in 10 second bursts
            try {
                sleep(10000);
            } catch (InterruptedException ex) {
            }
            currentTime = System.currentTimeMillis();
            time = (currentTime - startTime) / 1000;
            System.out.println("\nInterim Report @ " + time + "s:");

            int giftsInSacks = 0;
            // TODO - calculate number
            for (int s = 0; s < numSacks; s++) {
                giftsInSacks += sacks[s].presentsAddedToSack();
            }

            int giftsInHoppers = 0;
            // TODO - calculate number
            for (int i = 0; i < numHoppers; i++) {
                giftsInHoppers += hoppers[i].presentCount;
            }

            System.out.println(giftsInHoppers + " presents remaining in hoppers;\n" + giftsInSacks + " presents sorted into sacks.\n");

        }

        long endTime = System.currentTimeMillis();
        System.out.println("*** Input Stopped after " + (endTime - startTime) / 1000 + "s. ***");

        // TODO
        isHopperRunning = false;
        long waitTime = System.currentTimeMillis();
        for (int h = 0; h < numTurntables; h++) {
            while (tables[h].arePresentsOnBelt()) {
                try {
                    System.out.println("Presents are still on the machine");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Table " + tables[h].id + " stopped");
        }
        isMachineRunning = false;
        System.out.println("All presents on belts are added to sacks");
        //isHopperRunning = false;
        // Stop the hoppers!
        // Stop the tables!
        // HINT - Wait for everything to finish...

        //isRunning = false;

        endTime = System.currentTimeMillis();
        System.out.println("*** Machine completed shutdown after " + (endTime - startTime) / 1000 + "s. ***");


        // FINAL REPORTING
        ////////////////////////////////////////////////////////////////////////

        System.out.println();
        System.out.println("\nFINAL REPORT\n");
        System.out.println("Configuration: " + filename);
        System.out.println("Total Run Time" + (endTime - startTime) / 1000 + "s.");

        int giftsDeposited = 0;
        // TODO - calculate this number!

        for (int h = 0; h < numHoppers; h++) {
            giftsDeposited += hoppers[h].depositedCount;
            System.out.println("Hopper " + hoppers[h].id + " deposited " + hoppers[h].depositedCount + " presents and waited " + (endTime - waitTime) / 1000 + "s.");
        }
        System.out.println();

        int giftsOnMachine = 0;
        int giftsInSacks = 0;
        for (int s = 0; s < numSacks; s++) {
            giftsInSacks += sacks[s].presentsAddedToSack();
        }
        // TODO - calculate these numbers!
        for (int h = 0; h < numTurntables; h++) {
            while (tables[h].arePresentsOnBelt()) {
                giftsOnMachine += hoppers[h].presentCount;
            }
        }

        System.out.print("\nOut of " + giftsDeposited + " gifts deposited, ");
        System.out.print(giftsOnMachine + " are still on the machine, and ");
        System.out.println(giftsInSacks + " made it into the sacks");

        int missing = giftsDeposited - giftsInSacks - giftsOnMachine;
        System.out.println(missing + " gifts went missing.");

    }
}
