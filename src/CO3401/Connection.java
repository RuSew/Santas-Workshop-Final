package CO3401;

/**
 *
 * @author Rusiru Sewwantha
 */
public class Connection {
    ConnectionType connType;
    Conveyor belt;
    Sack sack;

    //con type, connected belt, connected sack
    public Connection(ConnectionType ct, Conveyor c, Sack s)
    {
        connType = ct;
        belt = c;
        sack = s;
    }
}
