package agh.cs.positioning;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public enum MapDirection {
    N(0),
    NE(1),
    E(2),
    SE(3),
    S(4),
    SW(5),
    W(6),
    NW(7);

    private int numVal;
    private static Map map = new HashMap<>();
    private static final int CARDINALITY = MapDirection.values().length;

    MapDirection(int numVal){this.numVal = numVal;}

    static {
        for(MapDirection direction : MapDirection.values()){
            map.put(direction.numVal,direction);
        }
    }

    public int getNumVal(){
        return this.numVal;
    }

    public static MapDirection valueOf(int numVal){
        if(numVal >= CARDINALITY)
            numVal = numVal % CARDINALITY;

        else if(numVal < 0)
            numVal = Math.floorMod(numVal,CARDINALITY);

        return (MapDirection) map.get(numVal);
    }

    public String toString() {
        switch(this){
            case N:
                return "N";
            case NE:
                return "NE";
            case NW:
                return "NW";
            case E:
                return "E";
            case W:
                return "W";
            case S:
                return "S";
            case SE:
                return "SE";
            case SW:
                return "SW";
        }
        return null;
    }

    public MapDirection next() {
        //Turn clockwise by 45 degrees
        return MapDirection.valueOf(this.numVal + 1);
    }

    public MapDirection previous() {
        return MapDirection.valueOf(this.numVal -1 );
    }

    public MapDirection opposite(){
        switch (this) {
            case N:
                return MapDirection.S;
            case NE:
                return MapDirection.SW;
            case NW:
                return MapDirection.SE;
            case S:
                return MapDirection.N;
            case SE:
                return MapDirection.NW;
            case SW:
                return MapDirection.NE;
            case E:
                return MapDirection.W;
            case W:
                return MapDirection.E;
        }
        return null;
    }

    public Vector2d toUnitVector() {
        int x = 0, y = 0;
        switch (this) {
            case N:
                y++;
                break;
            case NE:
                x++;
                y++;
                break;
            case NW:
                x--;
                y++;
                break;
            case S:
                y--;
                break;
            case SE:
                x++;
                y--;
                break;
            case SW:
                x--;
                y--;
                break;
            case E:
                x++;
                break;
            case W:
                x--;
                break;
        }
        Vector2d v = new Vector2d(x, y);
        return v;
    }

    public MapDirection turn45DegRight(int nTimes){
        //Turns 45 degrees right n times
        return MapDirection.valueOf(this.numVal + nTimes);
    }

    public MapDirection turn45DegLeft(int nTimes){
        //Turns 45 degrees left n times
        return MapDirection.valueOf(this.numVal - nTimes);
    }

    public static MapDirection getRandomDirection(){
        return MapDirection.valueOf(new Random().nextInt(MapDirection.values().length));
    }

}
