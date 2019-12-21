package agh.cs.maps;

import agh.cs.elements.*;
import agh.cs.positioning.Rectangle;
import agh.cs.positioning.Vector2d;

import agh.cs.positioning.VectorComparator;
import agh.cs.utilities.DayData;
import agh.cs.utilities.Settings;
import com.google.common.collect.*;

import java.util.*;

public class TorusMap extends Rectangle implements IPositionChangeObserver, IStateChangeObserver {

    private List<IStateChangeObserver> observers;

    //We keep the plants separate from animals, because there can be only one plant on a
    //position and we would have to look every time what is a plant and what is not.
    public Map<Vector2d, Plant> plantMap = new HashMap<>();

    //List is used to keep order of the animals
    //The multimap stores all positions occupied by animals and all animals on those position
    //if there is more than one animal on a single position
    public List<Animal> animals = new LinkedList<>();
    public TreeMultimap<Vector2d, Animal> animalMap = TreeMultimap.create(new VectorComparator(), new AnimalComparator());

    public Rectangle jungle;

    public int highestEnergy = Settings.BREED_ENERGY * 2; //Todo keep the highest energy for the day, can move it to a different class but remember, it's accessed in Animal to get the color
    public int avgEnergy = Settings.BREED_ENERGY * 2 / Settings.ANIMALS_AT_START;
    public int day = 0;

    public List<DayData> days = new LinkedList<>();

    public Animal selected;

    //Most popular genotype of the day
    //+highlight all animals with that genotype
    //Todo add hashCode() to Genotype
    public Multimap<Genotype, Animal> genotypePopularity = HashMultimap.create();

    public TorusMap(){
       //Set the lowerLeft and upperRight
       super(new Vector2d(0,0),new Vector2d(Settings.MAP_DIMENSION,Settings.MAP_DIMENSION));

       this.observers = new LinkedList<>();

        //Create a jungle with center set at the center of the map
        //and with area equal to jungleRatio times the area of the whole map
        this.jungle = new Rectangle(this.center(), (int)(this.getArea() * Settings.JUNGLE_RATIO));

        this.newDay();
    }

    private DayData newDay(){
        DayData newDay = new DayData(this);
        this.days.add(newDay);
        return newDay;
    }

    public void createWorld(){
        //Creates random animals and plants for the first round of simulation
        for(int i=0; i< Settings.ANIMALS_AT_START; i++){
            Animal animal = new Animal(this);
            this.addAnimal(animal);
        }
    }

    private void addAnimal(Animal animal){
        this.animals.add(animal);
        this.animalMap.put(animal.getPosition(),animal);
        animal.addObserver(this);
        this.beingWasBorn(animal);
        this.genotypePopularity.put(animal.getGenotype(), animal);
    }

    public Vector2d getRandomFreeSpace(){
        //This function returns a random not occupied position on the map
        //This function is used inside a constructor of an animal (Adam and Eve)
        Vector2d position = this.randomPointInRectangle();
        while(this.isOccupied(position))
            position = this.randomPointInRectangle();
        return position;
    }

    public void simulate(){

        this.growPlants();

        //Todo hopefully this works
        //this adds a new day that is a observer of the map and is a copy of the previous day
        //then the state of the daydata changes when new beings are born and die
        //then the day is saved, and over again
        this.newDay().calculate();

        try {
            this.removeDead();
            this.run();
            this.eat();
            this.breed();

        } catch (FailedToRemoveElement failedToRemoveElement) {
            failedToRemoveElement.printStackTrace();
        }

        //All animals and the map, get a day older
        this.growADayOlder();
    }

    private void removeDead() throws FailedToRemoveElement {
        //removes dead animals from the map
        //calls function remove()
        List<Animal> deadAnimals = new LinkedList<>();

        for(Animal animal : this.animals){
            if(animal.getEnergy() <= 0){
                //If animal has no energy it's dead and removed from the map
                deadAnimals.add(animal);
            }
        }

        //We have to remove dead animals here because of java.util.ConcurrentModificationException
        //Same goes for Plants
        for(Animal deadAnimal: deadAnimals) {
            this.beingHasDied(deadAnimal);

            if(!(this.animalMap.remove(deadAnimal.getPosition(), deadAnimal) &&
                this.animals.remove(deadAnimal) &&
                this.genotypePopularity.remove(deadAnimal.getGenotype(),deadAnimal)))
            {
                throw new FailedToRemoveElement(deadAnimal);
            }

        }
    }

    private void run() throws FailedToRemoveElement {
        //make all the animals move
        //call animal.move()

        for(Animal animal : this.animals){
            animal.move();
        }
    }

    private void breed() {

        //This line of code is brought to you by java.util.ConcurrentModificationException!
        List<Animal> children = new LinkedList<>();

        for(Vector2d key : this.animalMap.keySet()) {
            //Get a set of all animals at the same position
            //and choose two that have the highest energy
            //If they have enough energy to breed let them do it :)

            NavigableSet<Animal> animalsAtPosition = this.animalMap.get(key);

            if(animalsAtPosition.size() > 1) {

                Iterator<Animal> iterator = animalsAtPosition.descendingIterator();

                Animal veryEnergetic = iterator.next();
                Animal energetic = iterator.next();

                if(energetic.canBreed()){
                    Animal child = energetic.mateWith(veryEnergetic);

                    //This line of code is brought to you by java.util.ConcurrentModificationException!
                    children.add(child);
                }

            }
        }

        //This line of code is brought to you by java.util.ConcurrentModificationException!
        for(Animal child : children){
            this.addAnimal(child);
        }
    }

    private void eat() throws FailedToRemoveElement {
        //Go through all plants and see if there is an animal
        //on the same position as the plant
        //if so, the most energetic animal eats the plant and gains
        //energy, and the plant is removed from the map
        List<Vector2d> plantsToRemove = new LinkedList<>();
        for(Vector2d plantPosition : this.plantMap.keySet()) {
            if (this.animalMap.containsKey(plantPosition)) {
                NavigableSet<Animal> animalsAtPosition = this.animalMap.get(plantPosition);
                Iterator<Animal> iterator = animalsAtPosition.descendingIterator();
                Animal mostEnergetic = iterator.next();

                this.changeEnergy(mostEnergetic,Settings.PLANT_ENERGY);
                plantsToRemove.add(plantPosition);
            }
        }

        //We have to remove plants here because of java.util.ConcurrentModificationException
        for(Vector2d plant : plantsToRemove) {
            this.beingHasDied(this.plantMap.get(plant));
            this.plantMap.remove(plant);
        }
    }

    private void growPlants() {
        //There is a slight problem because when one biom is full the loop will go on forever
        //we can prevent this by counting how many times the loop has chosen a random element
        //and allow it to do it only as many times as the area of the biom,
        //but then we can't be sure that we actually visited every spot on the map


        for(int i = 0; i < Settings.NEW_PLANTS_PER_BIOM; i++){
            int mapFullCounter = 0;

            Plant plant = new Plant(this.randomPointInRectangle());

            while(this.isOccupied(plant.getPosition()) || this.jungle.inRectangle(plant.getPosition())) {
                //is there is already a plant at that position or the new position is in jungle find new position
                plant = new Plant(this.randomPointInRectangle());
                if(mapFullCounter >= this.getArea())
                    break;
                mapFullCounter++;
            }
            if(mapFullCounter < this.getArea()) {
                this.plantMap.put(plant.getPosition(), plant);
                this.beingWasBorn(plant);
            }


            //grow new plants in the jungle
            mapFullCounter = 0;
            plant = new Plant(this.jungle.randomPointInRectangle());

            while(this.isOccupied(plant.getPosition())) {
                //is there is already a plant at that position or the new position is in jungle find new position
                plant = new Plant(this.jungle.randomPointInRectangle());
                if(mapFullCounter >= this.getArea())
                    break;
                mapFullCounter++;
            }
            if(mapFullCounter < this.getArea()) {
                this.plantMap.put(plant.getPosition(), plant);
                this.beingWasBorn(plant);
            }

        }
    }

    private void growADayOlder(){
        this.day++;
        for(Animal animal : this.animals){
            animal.growADayOlder();
        }
    }

    public boolean isOccupied(Vector2d position){
        return this.plantMap.containsKey(position) || this.animalMap.containsKey(position);
    }

    public Vector2d wrapMap(Vector2d newPosition){
        int x = newPosition.x; int y = newPosition.y;
        if(!this.inRectangle(newPosition)){
            if(x > this.getUpperRight().x || x < this.getLowerLeft().x)
                x = x > this.getUpperRight().x ? this.getLowerLeft().x : this.getUpperRight().x;
            if(y > this.getUpperRight().y || y < this.getLowerLeft().y)
                y = y > this.getUpperRight().y ? this.getLowerLeft().y : this.getUpperRight().y;
        }
        return new Vector2d(x,y);
    }

    @Override
    public void positionChanged(Animal animal, Vector2d oldPosition, Vector2d newPosition) throws FailedToRemoveElement {
        if(!oldPosition.equals(newPosition)){
            boolean flag = this.animalMap.remove(oldPosition, animal);
            if(!flag)
                throw new FailedToRemoveElement(animal);

            this.animalMap.put(newPosition, animal);
        }
    }

    public void addObserver(IStateChangeObserver observer){
        this.observers.add(observer);
    }

    private void removeObserver(IStateChangeObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public void beingWasBorn(IMapElement being) {
        for(IStateChangeObserver observer : this.observers){
            observer.beingWasBorn(being);
        }
    }

    @Override
    public void beingHasDied(IMapElement being) {
        //Every time an being dies we notify the simulation to remove it from the Scene
        //This is used for both Animals and Plants
        for(IStateChangeObserver observer : this.observers){
            observer.beingHasDied(being);
        }
    }


    public void changeEnergy(Animal animal, int delta) throws FailedToRemoveElement {
        boolean flag = this.animalMap.remove(animal.getPosition(),animal);
        if(!flag)
            throw new FailedToRemoveElement(animal);

        animal.energy += delta;
        this.animalMap.put(animal.getPosition(),animal);
    }
}
