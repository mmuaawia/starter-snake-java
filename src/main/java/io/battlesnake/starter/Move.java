package io.battlesnake.starter;

public enum Move {
  LEFT  (-1,  0),
  RIGHT ( 1,  0),
  UP    ( 0, -1),
  DOWN  ( 0,  1);

  public final int xChange;

  public final int yChange;

  Move(int xChange, int yChange) {
    this.xChange = xChange;
    this.yChange = yChange;
  }

  public static boolean isPositionOneMoveAway(Position src, Position dest){
    for(Move move: Move.values()){
      if( (src.x + move.xChange) == dest.x
      &&  (src.y + move.yChange) == dest.y ){
         return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
