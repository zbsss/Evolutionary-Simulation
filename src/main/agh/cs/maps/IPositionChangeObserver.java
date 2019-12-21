package agh.cs.maps;

import agh.cs.elements.Animal;
import agh.cs.positioning.Vector2d;

public interface IPositionChangeObserver {
   public void positionChanged(Animal animal, Vector2d oldPosition, Vector2d newPosition) throws FailedToRemoveElement;
}
