package agh.cs.maps;

import agh.cs.elements.IMapElement;

public class FailedToRemoveElement extends Exception {
    private IMapElement element;

    FailedToRemoveElement(IMapElement element){
        super("Failed to remove element from the map");
        this.element = element;
    }
}
