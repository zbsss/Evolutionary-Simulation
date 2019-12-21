package agh.cs.elements;

import agh.cs.positioning.Vector2d;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;


public interface IMapElement {

    //Returns position of element
    Vector2d getPosition();

    Color toColor();

    Shape toShape(int scale);
}
