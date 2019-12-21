package agh.cs.elements;

import java.util.Comparator;

public class AnimalComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Animal a1 = (Animal) o1;
        Animal a2 = (Animal) o2;

        if(a1 == a2)
            return 0;

        else if (a1.getEnergy() > a2.getEnergy())
            return 1;

        else if (a1.getEnergy() == a2.getEnergy()){
            return Integer.compare(a1.hashCode(),a2.hashCode());
        }
        else
            return -1;
    }
}