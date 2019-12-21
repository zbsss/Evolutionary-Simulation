package agh.cs.maps;

import agh.cs.elements.IMapElement;

public interface IStateChangeObserver {

   //Status meaning: dead or alive

   public void beingWasBorn(IMapElement being);

   public void beingHasDied(IMapElement being);
}
