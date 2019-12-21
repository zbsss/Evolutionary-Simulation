package agh.cs.positioning;

import static java.lang.Integer.min;
import static java.lang.StrictMath.max;

public class Vector2d {
    final public int x;
    final public int y;

    public Vector2d(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public String toString() {
        return "(" + x + "," + y + ")";
    }

    //other poprzedza this
    public boolean precedes(Vector2d other) {
        return other.x <= this.x && other.y <= this.y;
    }

    //other nastepuje po this (czytamy od prawej do lewej <---- )
    public boolean follows(Vector2d other) {
        return other.x >= this.x && other.y >= this.y;
    }

    public Vector2d upperRight(Vector2d other) {
        if (follows(other)) {
            return other;
        }
        if (precedes(other)) {
            return this;
        }
        int x = max(this.x, other.x);
        int y = max(this.y, other.y);
        Vector2d n = new Vector2d(x, y);
        return n;
    }

    public Vector2d lowerLeft(Vector2d other){
        if (follows(other)) {
            return this;
        }
        if (precedes(other)) {
            return other;
        }
        int x = min(this.x, other.x);
        int y = min(this.y, other.y);
        Vector2d n = new Vector2d(x, y);
        return n;
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d(this.x + other.x, this.y + other.y);
    }

    public Vector2d add(Integer number){
        return new Vector2d(this.x + number, this.y + number);
    }

    public Vector2d subtract(Vector2d other) {
        return new Vector2d(this.x - other.x, this.y - other.y);
    }

    public Vector2d subtract(Integer number){
        return new Vector2d(this.x - number, this.y - number);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Vector2d))
            return false;
        Vector2d that = (Vector2d) other;
        return this.x == that.x && this.y == that.y;
    }

    public Vector2d opposite() {
        return new Vector2d(-this.x, -this.y);
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash += this.x * 31;
        hash += this.y * 17;
        return hash;
    }

}
