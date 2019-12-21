package agh.cs.elements;

import agh.cs.maps.FailedToRemoveElement;
import agh.cs.maps.TorusMap;
import agh.cs.positioning.MapDirection;
import agh.cs.positioning.Vector2d;
import agh.cs.maps.IPositionChangeObserver;
import agh.cs.utilities.Settings;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Animal implements IMapElement{
    private MapDirection orientation;
    private Genotype genotype;

    public int energy;
    private Vector2d position;

    private int age;
    private int numberOfChildren;

    private TorusMap map;
    private List<IPositionChangeObserver> observers = new ArrayList<>();

    private static List<Color> colors;

    static {
        colors = new ArrayList<>(5);
        colors.add(Color.rgb(255, 236, 25));
        colors.add(Color.rgb(255, 193, 0));
        colors.add(Color.rgb(255, 152, 0));
        colors.add(Color.rgb(255, 86, 7));
        colors.add(Color.rgb(246, 65, 45));
    }

    public Animal(TorusMap map){
        this.map = map;
        this.genotype = new Genotype();
        this.position = this.map.getRandomFreeSpace();
        this.orientation = this.genotype.getRandomMove(MapDirection.N);
        this.energy = Settings.BREED_ENERGY * 2;

        this.age = 0;
        this.numberOfChildren = 0;
    }

    private Animal(Animal dominant, Animal submissive){
        this.genotype = new Genotype(dominant.genotype, submissive.genotype);
        this.map = dominant.map;

        //Get energy from parents
        this.energy = 0;
        try {
            this.suckEnergyFrom(dominant);
            this.suckEnergyFrom(submissive);
        } catch (FailedToRemoveElement failedToRemoveElement) {
            failedToRemoveElement.printStackTrace();
        }

        this.numberOfChildren = 0;
        dominant.numberOfChildren++;
        submissive.numberOfChildren++;

        //Pick random direction for start
        this.orientation= MapDirection.getRandomDirection();

        //Get a free position for the child
        this.position = this.getNewbornPosition(dominant.getPosition());

        this.age = 0;
    }

    private Vector2d getNewbornPosition(Vector2d position) {
        //This function returns a random free position for the newborn child

        //The list stores all free positions around the parents
        //and then one position is chosen randomly
        List<Vector2d> freePositions = new  LinkedList<>();
        List<Vector2d> allPositions = new  LinkedList<>();

        for(MapDirection dir : MapDirection.values()){
            allPositions.add(position.add(dir.toUnitVector()));
            if(this.map.isOccupied(position.add(dir.toUnitVector()))){
                freePositions.add(position.add(dir.toUnitVector()));
            }
        }

        //Make sure that the new position is not outside the map
        for(Vector2d freePosition : freePositions)
            freePositions.set(freePositions.indexOf(freePosition),this.map.wrapMap(freePosition));

        for(Vector2d somePosition : allPositions)
            allPositions.set(allPositions.indexOf(somePosition),this.map.wrapMap(somePosition));

        //If there is no free positions we have to pick one that is taken, but do so randomly
        if(freePositions.isEmpty())
            return allPositions.get(new Random().nextInt(allPositions.size()));
        return freePositions.get(new Random().nextInt(freePositions.size()));
    }

    private void suckEnergyFrom(Animal parent) throws FailedToRemoveElement {
        int parentEnergy = parent.getEnergy();
        this.map.changeEnergy(parent, -parent.getEnergy()/4);
        this.energy += parentEnergy/4;
    }

    public void move() throws FailedToRemoveElement {
        //To make a move we peak a random gene and according to the gene
        //we change direction of the animal
        this.orientation = this.genotype.getRandomMove(this.orientation);

        Vector2d newPosition = this.position.add(this.orientation.toUnitVector());
        newPosition = this.map.wrapMap(newPosition);
        this.positionChanged(this.position, newPosition);
        this.position = newPosition;

        this.map.changeEnergy(this, -Settings.MOVE_ENERGY);
    }

    public Animal mateWith(Animal other){
        //Returns a child animal

        //Pick which animal is dominant (which one gives 2 parts of it's genes)
        Animal dominant;
        Animal submissive;
        if(new Random().nextBoolean()){
            dominant = this;
            submissive = other;
        }
        else {
            dominant = other;
            submissive = this;
        }

        return new Animal(dominant,submissive);
    }

    public MapDirection getOrientation(){
        return this.orientation;
    }

    public Vector2d getPosition(){
        return new Vector2d(this.position.x, this.position.y);
    }

    @Override
    public Color toColor() {

        if(map.day == 0)
            return Animal.colors.get(0);
        int interval = (int) this.map.days.get(this.map.days.size() -2).getAvgEnergy()  *3 / 5;

        if(map.selected != null && map.selected.equals(this))
            return Color.PURPLE;

        if(this.genotype.equals(this.map.days.get(this.map.days.size() -2).mostPopular))
            return Color.BLUE;

        if(interval == 0 || this.getEnergy()/interval < 0)
            return Animal.colors.get(0);

        if(this.getEnergy() / interval >= 5)
            return Animal.colors.get(4);

        return Animal.colors.get(this.getEnergy()/interval);
    }

    public String toString(){
        return this.orientation.toString();
    }

    public void addObserver(IPositionChangeObserver observer){
        this.observers.add(observer);
    }

    public void removeObserver(IPositionChangeObserver observer){
        this.observers.remove(observer);
    }

    public void positionChanged(Vector2d oldPosition, Vector2d newPosition) throws FailedToRemoveElement {
        for(IPositionChangeObserver observer : observers){
            observer.positionChanged(this, oldPosition, newPosition);
        }
    }

    public int getEnergy(){
        return this.energy;
    }

    public boolean canBreed(){
        return this.getEnergy() >= Settings.BREED_ENERGY;
    }

    @Override
    public Circle toShape(int scale){
        Circle circle = new Circle();
        circle.setCenterX(this.getPosition().x * scale);
        circle.setCenterY(this.getPosition().y * scale);
        circle.setRadius(scale / 2);
        circle.setFill(this.toColor());
        return circle;
    }

    public void growADayOlder(){
        this.age++;
    }

    public Genotype getGenotype(){
        return this.genotype;
    }

    public int getAge(){
        return  this.age;
    }

    public int getNumberOfChildren(){
        return this.numberOfChildren;
    }

    public String getInformation() {
        StringBuilder builder = new StringBuilder();
        builder.append("Age: " + this.age +"\n");
        builder.append("Energy: " + this.energy + "\n");
        builder.append("Number of children: " +this.numberOfChildren + "\n");
        builder.append("Genotype: " + this.genotype + "\n");
        return builder.toString();
    }

}
