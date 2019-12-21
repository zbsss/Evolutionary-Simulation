package agh.cs.utilities;

import agh.cs.elements.*;
import agh.cs.maps.TorusMap;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DayData {
    private final static String PATH = "SimulationReports\\";

    private TorusMap map;
    public final int day;
    private boolean isCalculated;

    public Genotype mostPopular;
    private Collection<Animal> members;
    private int numberOfMembers;

    //Popularity of every gene
    //This can be used for painting the pie chart
    public Map<Gene, Integer> genePopularity;

    //Avg energy, age
    private double avgEnergy;
    private double avgAge;
    private double avgNumberOfChildren;

    //Number of animals, plants
    public int numberOfAnimals;
    private int numberOfPlants;

    //Max
    private int highestEnergy;
    private int oldestAge;
    private int highestNumberOfChildren;

    public DayData(TorusMap map) {
        this.day = map.day;
        this.map = map;
        this.setUpGenePopularity();
        this.isCalculated = false;
    }

    private void setUpGenePopularity(){
        this.genePopularity = new HashMap<>();
        for (Gene gene : Gene.values())
            this.genePopularity.put(gene, 0);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Day: " + this.day + "\n");
        builder.append("Number of animals: " + this.numberOfAnimals + "\n");
        builder.append("Number of plants: " + this.numberOfPlants + "\n");
        builder.append("Average energy: " + String.format("%.2f", this.avgEnergy) + "\n");
        builder.append("Most popular Genotype: " + this.mostPopular + ", ");
        builder.append("is " + this.numberOfMembers + "/" + this.numberOfAnimals + "\n");
        builder.append("Average age: " + String.format("%.2f", this.avgAge) + "\n");
        builder.append("Average number of children: " + String.format("%.2f", this.avgNumberOfChildren) + "\n");
        builder.append("Highest energy: " + this.highestEnergy + "\n");
        builder.append("Oldest animal's age: " + this.oldestAge + "\n");
        builder.append("Highest number of children: " + this.highestNumberOfChildren + "\n");

        return builder.toString();
    }

    public static void saveSimulationReport(List<DayData> allDays) throws IOException {
        //Counts avg of all values from all days and saves it to a .txt file

        //Create a txt file titled "simulation report dd-mm hh--mm--ss
        //At the beginning of the file give the average stats from all the days of simulation
        //Then append data from all the days

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");

        String reportName = "Simulation Report " + dateTime.format(dateTimeFormatter) +".txt";

        FileWriter writer = new FileWriter(PATH + reportName);

        writer.write(prepareAverageReport(allDays));

        for(DayData day : allDays){
            if(day.isCalculated)
                writer.write(day.toString() + "\n\n");
        }

    }

    private static String prepareAverageReport(List<DayData> allDays){
        StringBuilder builder = new StringBuilder();
        builder.append("AVERAGE DATA FROM THE WHOLE SIMULATION: \n");
        int numberOfDays = allDays.size();

        double avgAnimalNum = 0;
        double avgPlantNum = 0;
        double avgEnergy = 0;
        double avgLife = 0;
        double avgChildren = 0;

        int allTimeHighestEnergy = 0;
        int allTimeHighestNumberOfChildren = 0;
        int allTimeHighestAge = 0;

        for (DayData day : allDays){
            if(day.isCalculated) {
                avgAnimalNum += day.numberOfAnimals;
                avgPlantNum += day.numberOfPlants;
                avgEnergy += day.avgEnergy;
                avgLife += day.avgAge;
                avgChildren += day.avgNumberOfChildren;

                allTimeHighestEnergy = Math.max(day.highestEnergy,allTimeHighestEnergy);
                allTimeHighestNumberOfChildren = Math.max(day.highestNumberOfChildren,allTimeHighestNumberOfChildren);
                allTimeHighestAge = Math.max(day.oldestAge,allTimeHighestAge);
            }
        }

        avgAnimalNum /= numberOfDays;
        avgPlantNum /= numberOfDays;
        avgEnergy /= numberOfDays;
        avgLife /= numberOfDays;
        avgChildren /= numberOfDays;

        builder.append("Total number of days: " + numberOfDays + "\n");
        builder.append("Average number of animals: " + avgAnimalNum + "\n");
        builder.append("Average number of plants: " + avgPlantNum + "\n");
        builder.append("Average energy: " + avgEnergy + "\n");
        builder.append("Average lifespan: " + avgLife + "\n");
        builder.append("Average number of children: " + avgChildren + "\n");
        builder.append("All time highest energy: " + allTimeHighestEnergy + "\n");
        builder.append("All time highest number of children: " + allTimeHighestNumberOfChildren + "\n");
        builder.append("All time oldest age: " + allTimeHighestAge + "\n");
        builder.append("\n\n");
        return builder.toString();
    }

    public void calculate() {
        //After all changes were applied to the map, we calculate the summary of that day
        this.numberOfAnimals = this.map.animals.size();
        this.numberOfPlants = this.map.plantMap.size();

        //avg and highest energy
        this.avgEnergy = 0;
        this.avgAge = 0;
        this.avgNumberOfChildren = 0;
        this.highestEnergy = 0;
        this.oldestAge = 0;
        this.highestNumberOfChildren = 0;

        for (Animal animal : this.map.animals) {
            this.avgEnergy += animal.getEnergy();
            this.avgAge += animal.getAge();
            this.avgNumberOfChildren += animal.getNumberOfChildren();
            this.highestEnergy = Math.max(animal.getEnergy(),this.highestEnergy);
            this.oldestAge = Math.max(animal.getAge(),this.oldestAge);
            this.highestNumberOfChildren = Math.max(animal.getNumberOfChildren(), this.highestNumberOfChildren);

            for(Gene gene : animal.getGenotype().asList()){
                this.genePopularity.replace(gene,this.genePopularity.get(gene) + 1 );
            }

        }
        this.avgEnergy = this.avgEnergy / this.numberOfAnimals;
        this.avgAge = this.avgAge / this.numberOfAnimals;
        this.avgNumberOfChildren = this.avgNumberOfChildren / this.numberOfAnimals;

        //Most popular Genotype
        int max = 0;
        for (Map.Entry<Genotype, Collection<Animal>> pair : this.map.genotypePopularity.asMap().entrySet()) {
            if (((Set) pair.getValue()).size() > max) {
                this.mostPopular = (Genotype) pair.getKey();
                this.members = (Set) pair.getValue();
                max = ((Set) pair.getValue()).size();
            }
        }
        this.numberOfMembers = max;

        this.isCalculated = true;
    }

    public int getHighestEnergy() {
        return highestEnergy;
    }

    public double getAvgEnergy() {
        return avgEnergy;
    }
}


