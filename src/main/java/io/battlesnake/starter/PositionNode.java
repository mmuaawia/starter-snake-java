package io.battlesnake.starter;

public class PositionNode extends Position {
  public int distance;
  public Move initMove = Move.LEFT;

  PositionNode(Position position, int distance) {
    super(position.x, position.y);
    this.distance = distance;

  }
}
