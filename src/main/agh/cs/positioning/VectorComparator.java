package agh.cs.positioning;

import java.util.Comparator;

public class VectorComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Vector2d v1 = (Vector2d) o1;
        Vector2d v2 = (Vector2d) o2;

        if(v1.equals(v2))
            return 0;
        else if( v1.x == v2.x){
            if(v1.y >= v2.y)
                return 1;
            else
                return -1;
        }
        else if (v1.x > v2.x)
            return 1;
        else
            return -1;
    }
}
