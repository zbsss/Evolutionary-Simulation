package agh.cs.elements;

import agh.cs.positioning.MapDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public enum Gene  {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7);

    private int numVal;
    private static Map map = new HashMap<>();

    Gene(int numVal) {
        this.numVal = numVal;
    }

    //The map is used to easily convert Integers to Genes
    //for example valueOf() method
    static {
        for(Gene gene : Gene.values()){
            map.put(gene.numVal, gene);
        }
    }

    public int getNumVal(){
        return this.numVal;
    }


    public static Gene valueOf(int geneNumVal){
        return (Gene) map.get(geneNumVal);
    }

    public MapDirection activateGene(MapDirection direction){
        return direction.turn45DegRight(this.getNumVal());
    }

    public static Gene randomGene(){
        return Gene.valueOf(new Random().nextInt(8));
    }

    public String toString(){
        return  String.valueOf(this.numVal);
    }

}
