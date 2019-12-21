package agh.cs.visualization;

import agh.cs.utilities.Settings;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private List<Simulation> simulations = new ArrayList<>(Settings.NUMBER_OF_SIMULATIONS);

    @Override
    public void start(Stage primaryStage) throws Exception {

        for(int i=0; i < Settings.NUMBER_OF_SIMULATIONS; i++)
            simulations.add(new Simulation());

        for(Simulation simulation : simulations)
            simulation.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        for(Simulation simulation : simulations)
            simulation.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
