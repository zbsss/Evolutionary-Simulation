package agh.cs.utilities;

import org.json.simple.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import org.json.simple.parser.JSONParser;


public class Settings {
    //load settings from json file
    //These settings are used by TorusMap, Animal and MapSimulation
    //Todo Not sure if this is the right idea

    public static final int MAP_DIMENSION;
    public static final int DELAY;

    public static final int BREED_ENERGY;
    public static final int MOVE_ENERGY;
    public static final int PLANT_ENERGY;

    public static final int ANIMALS_AT_START;
    public static final int NEW_PLANTS_PER_BIOM;
    public static final int NUMBER_OF_SIMULATIONS;

    public static final double JUNGLE_RATIO;

    private static final String PATH = "src\\main\\agh\\cs\\utilities\\parameters.json";
    private static JSONObject settings;


    static {
        loadSettings();

        //Set all the constants
        MAP_DIMENSION = ((Long)Settings.get("mapDimension")).intValue();
        DELAY = ((Long)Settings.get("delay")).intValue();

        BREED_ENERGY = ((Long)Settings.get("startEnergy")).intValue() /2;
        MOVE_ENERGY = ((Long)Settings.get("moveEnergy")).intValue();
        PLANT_ENERGY = ((Long)Settings.get("plantEnergy")).intValue();

        ANIMALS_AT_START = ((Long)Settings.get("startAnimalNumber")).intValue();
        NEW_PLANTS_PER_BIOM = ((Long)Settings.get("newPlants")).intValue();
        NUMBER_OF_SIMULATIONS = ((Long)Settings.get("numberOfSimulations")).intValue();

        JUNGLE_RATIO = (Double)Settings.get("jungleRatio");
    }

    @SuppressWarnings("unchecked")
    public static void loadSettings(){

        JSONParser parser = new JSONParser();

        try(FileReader reader = new FileReader(PATH)){
            settings = (JSONObject) parser.parse(reader);

        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
        catch (Exception e) {e.printStackTrace();}
    }


    public static Object get(String setting){
        return Settings.settings.get(setting);
    }

}
