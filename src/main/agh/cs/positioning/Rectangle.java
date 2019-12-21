package agh.cs.positioning;

import java.util.Random;

public class Rectangle {
    private Vector2d lowerLeft;
    private Vector2d upperRight;

    public Rectangle(Vector2d lowerLeft, Vector2d upperRight){
        if(lowerLeft.follows(upperRight)) {
            this.lowerLeft = lowerLeft;
            this.upperRight = upperRight;
        }
        else{
            this.lowerLeft = lowerLeft.lowerLeft(upperRight);
            this.upperRight = lowerLeft.upperRight(upperRight);
        }
    }

    public Rectangle(Vector2d center, Integer area){
        //This constructor returns a rectangle that is a square
        //with a center set at the center parameter
        Integer side = (int) Math.sqrt(area);
        this.lowerLeft = center.subtract(side/2);
        this.upperRight = center.add(side/2);
    }

    public boolean inRectangle(Vector2d point){
        return point.follows(this.upperRight) && point.precedes(this.lowerLeft);
    }

    public Vector2d randomPointInRectangle(){
        Random random = new Random();

        int x = random.nextInt((this.getWidth()) + 1) + this.lowerLeft.x;
        int y = random.nextInt((this.getHeight()) + 1) + this.lowerLeft.y;

        return new Vector2d(x,y);
    }

    public Vector2d center(){
        return new Vector2d(this.lowerLeft.x + this.getWidth()/2, this.lowerLeft.y + this.getHeight()/2);
    }

    public Integer getArea(){
        return this.getHeight() * this.getWidth();
    }

    public Integer getWidth(){
        return this.upperRight.x - this.lowerLeft.x;
    }

    public Integer getHeight(){
        return this.upperRight.y - this.lowerLeft.y;
    }

    public Vector2d getLowerLeft() {
        return this.lowerLeft;
    }

    public Vector2d getUpperRight() {
        return this.upperRight;
    }
}
