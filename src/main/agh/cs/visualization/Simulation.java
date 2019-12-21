package agh.cs.visualization;

import agh.cs.elements.Animal;
import agh.cs.elements.Gene;
import agh.cs.maps.IPositionChangeObserver;
import agh.cs.maps.IStateChangeObserver;
import agh.cs.elements.IMapElement;
import agh.cs.elements.Plant;
import agh.cs.maps.TorusMap;
import agh.cs.positioning.Vector2d;
import agh.cs.utilities.DayData;
import agh.cs.utilities.Settings;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.*;

public class Simulation implements IStateChangeObserver, IPositionChangeObserver {

    private HashMap<Animal, Circle> animalCircleHashMap = new HashMap<>();
    private HashMap<Plant, Rectangle> plantRectangleHashMap = new HashMap<>();
    private int scale = 1000 / Settings.MAP_DIMENSION;

    private Group root = new Group();
    private ScheduledExecutorService scheduledExecutorService;
    private AnimationTimer gameLoop;
    private TorusMap map;
    private Stage theStage = new Stage();

    private LineChart<Number, Number> populationGraph;
    private XYChart.Series<Number, Number> populationDataSeries = new XYChart.Series<>();

    private final PieChart pieChart = new PieChart();
    private ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

    private Text selectedAnimalInformation = new Text();
    private Text allStats = new Text();

    private Button pause = new Button();
    private Button save = new Button();


    public void start() {
        //INITIALIZE SIMULATION
        initSimulation();

        //POPULATION GRAPH
        initGraph();

        //PIE CHART
        initPieChart();

        //REFRESH DATA IN GRAPHS EVERY SECOND
        // setup a scheduled executor to periodically put data into the chart
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Integer random = map.days.get(map.days.size() -1).numberOfAnimals;

            Platform.runLater(() -> {
                populationDataSeries.getData().add(new XYChart.Data<>(map.days.get(map.days.size() -1).day, random));


                pieChartData.clear();
                for(Gene gene : map.days.get(map.days.size()-1).genePopularity.keySet()){
                    pieChartData.add(new PieChart.Data(gene.toString(),map.days.get(map.days.size()-1).genePopularity.get(gene)));
                }
            });
        }, 0, 1, TimeUnit.SECONDS);

        //SELECTED ANIMAL'S DATA:
        showSelectedAnimalData();

        //ALL INFORMATION FROM A DAY
        showDayData();

        //GAME LOOP
        final long startNanoTime = System.nanoTime();
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            public void handle(long currentNanoTime) {
                double t = (currentNanoTime - startNanoTime) / 1000000000.0;

                if (map.animals.size() <= 1)
                    stop();

                //Delay
                if (currentNanoTime - lastUpdate >= Settings.DELAY * 1000000) {

                    if(map.selected != null)
                        selectedAnimalInformation.setText("SELECTED ANIMAL: \n" + map.selected.getInformation());
                    allStats.setText("DATA:\n" + map.days.get(map.days.size()-1).toString());

                    map.simulate();
                    lastUpdate = currentNanoTime;
                }
            }
        };
        gameLoop.start();


        //PAUSE BUTTON
        initPauseButton();

        //SAVE REPORT BUTTON
        initSaveButton();

        //SHOW THE WHOLE SIMULATION
        theStage.show();
    }

    private void initSimulation(){
        theStage.setTitle( "Generator Ewolucyjny" );

        Settings.loadSettings();
        map = new TorusMap();
        map.addObserver(this);

        Scene theScene = new Scene( root,1800, 1000);

        Rectangle grassLand = new Rectangle(0,0,1010,1000);
        grassLand.setFill(Color.rgb(159, 255, 128));
        root.getChildren().add(grassLand);

        Rectangle jungle = new Rectangle(map.jungle.getLowerLeft().x * scale, map.jungle.getLowerLeft().y * scale,
                map.jungle.getWidth() * scale, map.jungle.getHeight()* scale);
        jungle.setFill(Color.rgb(0, 77, 0));
        root.getChildren().add(jungle);

        theStage.setScene( theScene );

        map.createWorld();
    }

    private void initGraph(){
        final NumberAxis xAxis = new NumberAxis(); // we are gonna plot against time
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        xAxis.setAnimated(false); // axis animations are removed
        yAxis.setLabel("Number of animals");
        yAxis.setAnimated(false); // axis animations are removed

        populationGraph = new LineChart<>(xAxis,yAxis);

        //creating the line chart with two axis created above
        populationGraph.setAnimated(false); // disable animations
        populationGraph.relocate(1000,0);
        populationGraph.setTitle("Animal population");
        populationGraph.setLegendVisible(false);


        // add series to chart
        populationGraph.getData().add(populationDataSeries);

        root.getChildren().add(populationGraph);
    }

    private void initPieChart(){
        pieChart.setTitle("Gene popularity");
        pieChart.setAnimated(false);
        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 24 arial;");
        pieChart.setLegendVisible(false);
        pieChart.setMaxWidth(330);

        pieChart.setData(pieChartData);

        root.getChildren().add(pieChart);
        pieChart.relocate(1480,0);
    }

    private void showSelectedAnimalData(){
        selectedAnimalInformation.relocate(1050,400);
        StringBuilder builder = new StringBuilder();
        builder.append("Age: " + 0 +"\n");
        builder.append("Energy: " + 0 + "\n");
        builder.append("Number of children: " +0 + "\n");
        builder.append("Genotype: " + 0 + "\n");
        selectedAnimalInformation.setText("SELECTED ANIMAL: \n" + builder.toString());
        root.getChildren().add(selectedAnimalInformation);
    }

    private void showDayData(){
        allStats.relocate(1050,600);

        allStats.setText("DATA:\n" + map.days.get(map.days.size()-1).toString());
        root.getChildren().add(allStats);
    }

    private void initPauseButton(){
        pause.setText("Pause");
        pause.relocate(1400,950);

        pause.setOnAction(new EventHandler<ActionEvent>() {
            boolean paused = false;
            @Override
            public void handle(ActionEvent event) {
                if(!paused) {
                    gameLoop.stop();
                    paused = true;
                    pause.setText("Resume");
                }
                else {
                    gameLoop.start();
                    paused = false;
                    pause.setText("Pause");
                }
            }
        });

        root.getChildren().add(pause);
    }

    private void initSaveButton(){
        save.setText("Save Report");
        save.relocate(1460,950);

        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                try {
                    DayData.saveSimulationReport(map.days);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"Saved successfully!");
                    alert.show();
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR,"Failed to save report!");
                    alert.show();
                    e.printStackTrace();
                }
            }
        });

        root.getChildren().add(save);
    }

    @Override
    public void beingWasBorn(IMapElement being) {
        if(being instanceof Animal) {
            Circle circle =((Animal) being).toShape(scale);
            animalCircleHashMap.put((Animal) being, circle);
            root.getChildren().add(circle);
            ((Animal) being).addObserver(this);

            circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Animal oldSelected = map.selected;

                    map.selected = (Animal) being;

                    if(oldSelected != null)
                        animalCircleHashMap.get(oldSelected).setFill(oldSelected.toColor());
                    animalCircleHashMap.get(map.selected).setFill(map.selected.toColor());
                }
            });

        }
        else if(being instanceof Plant) {
            Rectangle rectangle = ((Plant) being).toShape(scale);
            plantRectangleHashMap.put((Plant)being, rectangle);
            root.getChildren().add(rectangle);
        }
    }

    @Override
    public void beingHasDied(IMapElement being) {
        //Remove the dead being from the animation
        if(being instanceof Animal) {
            root.getChildren().remove(animalCircleHashMap.get((Animal) being));
            this.animalCircleHashMap.remove((Animal) being);
        }
        else if(being instanceof Plant) {
            root.getChildren().remove(plantRectangleHashMap.get((Plant) being));
            this.plantRectangleHashMap.remove((Plant) being);
        }
    }

    @Override
    public void positionChanged(Animal animal, Vector2d oldPosition, Vector2d newPosition) {
        animalCircleHashMap.get(animal).relocate(animal.getPosition().x * scale, animal.getPosition().y * scale);
        animalCircleHashMap.get(animal).setFill(animal.toColor());
    }

    public void stop() throws Exception{
        gameLoop.stop();
        scheduledExecutorService.shutdownNow();
    }
}
