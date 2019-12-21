package agh.cs.elements;

import agh.cs.positioning.Vector2d;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class Plant implements IMapElement {
    private Vector2d position;

    public Plant(Vector2d position){
        this.position = position;
    }

    public Vector2d getPosition(){
        return new Vector2d(this.position.x, this.position.y);
    }

    @Override
    public Color toColor() {
        return Color.CYAN;
    }

    @Override
    public Rectangle toShape(int scale) {
        Rectangle rectangle = new Rectangle();
        rectangle.setX(this.getPosition().x * scale);
        rectangle.setY(this.getPosition().y * scale);
        rectangle.setWidth(scale);
        rectangle.setHeight(scale);
        rectangle.setFill(Color.rgb(0, 230, 0));
        return rectangle;
    }

    public String toString(){
        return "p";
    }
}
