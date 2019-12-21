package agh.cs.elements;

import agh.cs.positioning.MapDirection;
import agh.cs.positioning.Vector2d;

import java.util.*;

public class Genotype {
    public final  static int GENE_NUMBER = 32;
    public final List<Gene> genotype = new ArrayList<>(GENE_NUMBER);

    private static Random random = new Random();

    public Genotype(){
        //Create a genotype with random genes
        for(int i=0; i<GENE_NUMBER; i++){
            this.genotype.add(Gene.randomGene());
        }
        this.fixGenotype();
    }

    public Genotype(Genotype dominant, Genotype submissive){
        //Create a Genotype of a child

        //Pick random indexes for intersection
        Integer firstCut = new Random().nextInt(GENE_NUMBER - 3);
        Integer secondCut = new Random().nextInt(GENE_NUMBER - (firstCut + 1)) + firstCut + 1;

        //Add two parts of genes from the dominant parent and one part from the submissive
        this.genotype.addAll(dominant.genotype.subList(0,firstCut + 1));
        this.genotype.addAll(submissive.genotype.subList(firstCut + 1, secondCut + 1));
        this.genotype.addAll(dominant.genotype.subList(secondCut + 1, GENE_NUMBER));

        //Make sure that no genes are lacking and that the genotype is sorted
        this.fixGenotype();
    }

    public void fixGenotype(){
        //This function fixes all missing genes in a genom

        //First check is there is no lacking genes
        List<Gene> lackingGenes = this.getLackingGenes();
        while(!lackingGenes.isEmpty()) {
            //If there are lacking genes add them to the genotype
            for (Gene lackingGene : lackingGenes) {
                this.replaceRandomGeneWith(lackingGene, lackingGenes);
            }

            lackingGenes = this.getLackingGenes();
        }
        //If there are no lacking genes check if
        //all the genes are sorted in ascending order by their numVal
        this.genotype.sort(Comparator.comparingInt(Gene::getNumVal));
    }

    public void replaceRandomGeneWith(Gene thisOne, List<Gene> doNotReplace){
        //Replaces a random gene with the gene that we put in as a parameter
        Gene randomGene = this.getRandomGene();
        while(doNotReplace.contains(randomGene))
            randomGene = this.getRandomGene();

        this.genotype.remove(randomGene);
        this.genotype.add(thisOne);
    }

    public List<Gene> getLackingGenes(){
        //returns a list of all the genes that don't have at least one repentant in the animals genotype
        List<Gene> lackingGenes = new LinkedList<>();

        for(Gene gene : Gene.values()){
            if(!this.genotype.contains(gene))
                lackingGenes.add(gene);
        }
        return lackingGenes;
    }

    public MapDirection getRandomMove(MapDirection direction){
        //This function takes the direction of an animal and
        //changes it according to a randomly chosen gene from the animals genotype

        Gene randomGene = this.getRandomGene();
        return randomGene.activateGene(direction);
    }

    public Gene getRandomGene(){
        //Returns a random gene from the genotype
        Integer index = random.nextInt(GENE_NUMBER);
        return genotype.get(index);
    }

    public List<Gene> asList(){
        return this.genotype;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        for(Gene gene : this.genotype){
            builder.append(gene.toString());
        }
        builder.append("]");

        return builder.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Genotype))
            return false;

        Genotype that = (Genotype) other;
        for(int i=0; i<GENE_NUMBER; i++){
            if(this.genotype.get(i) != that.genotype.get(i))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 13;
        for(Gene gene : genotype){
            hash += gene.hashCode();
        }
        return hash;
    }
}
