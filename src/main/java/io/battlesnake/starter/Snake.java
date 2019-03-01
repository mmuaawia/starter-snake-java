package io.battlesnake.starter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.*;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

/**
 * Snake server that deals with requests from the snake engine.
 * Just boiler plate code.  See the readme to get started.
 * It follows the spec here: https://github.com/battlesnakeio/docs/tree/master/apis/snake
 */
public class Snake {
  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  private static final Handler HANDLER = new Handler();
  private static final Logger LOG = LoggerFactory.getLogger(Snake.class);
  private static int[][] board;
  static int HEIGHT;
  static int WIDTH;
  static int health;

  /**
   * Main entry point.
   *
   * @param args are ignored.
   */
  public static void main(String[] args) {
    String port = System.getProperty("PORT");
    if (port != null) {
      LOG.info("Found system provided port: {}", port);
    } else {
      LOG.info("Using default port: {}", port);
      port = "8080";
    }
    port(Integer.parseInt(port));
    get("/", (req, res) -> "Battlesnake documentation can be found at " +
    "<a href=\"https://docs.battlesnake.io\">https://docs.battlesnake.io</a>.");
    post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
    post("/ping", HANDLER::process, JSON_MAPPER::writeValueAsString);
    post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
    post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
  }

  /**
   * Handler class for dealing with the routes set up in the main method.
   */
  public static class Handler {

    /**
     * For the ping request
     */
    private static final Map<String, String> EMPTY = new HashMap<>();

    /**
     * Generic processor that prints out the request and response from the methods.
     *
     * @param req
     * @param res
     * @return
     */
    public Map<String, String> process(Request req, Response res) {
      try {
        JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
        String uri = req.uri();
        LOG.info("{} called with: {}", uri, req.body());
        Map<String, String> snakeResponse;
        if (uri.equals("/start")) {
          snakeResponse = start(parsedRequest);
        } else if (uri.equals("/ping")) {
          snakeResponse = ping();
        } else if (uri.equals("/move")) {
          snakeResponse = move(parsedRequest);
        } else if (uri.equals("/end")) {
          snakeResponse = end(parsedRequest);
        } else {
          throw new IllegalAccessError("Strange call made to the snake: " + uri);
        }
        LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));
        return snakeResponse;
      } catch (Exception e) {
        LOG.warn("Something went wrong!", e);
        return null;
      }
    }

    /**
     * /ping is called by the play application during the tournament or on play.battlesnake.io to make sure your
     * snake is still alive.
     *
     * @return an empty response.
     */
    public Map<String, String> ping() {
      return EMPTY;
    }

    /**
     * /start is called by the engine when a game is first run.
     *
     * @param startRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
     * @return a response back to the engine containing the snake setup values.
     */
    public Map<String, String> start(JsonNode startRequest) {
      Map<String, String> response = new HashMap<>();
      HEIGHT = startRequest.get("board").get("height").asInt();
      WIDTH = startRequest.get("board").get("width").asInt();
      response.put("color", "#ff00ff");
      return response;
    }

    /**
     * /move is called by the engine for each turn the snake has.
     *
     * @param moveRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
     * @return a response back to the engine containing snake movement values.
     */
    public Map<String, String> move(JsonNode moveRequest) {
      LOG.info(moveRequest.toString());

      board = new int[HEIGHT][WIDTH];

      Map<String, String> response = new HashMap<>();
      JsonNode food = moveRequest.get("board").get("food");
      JsonNode snakes = moveRequest.get("board").get("snakes");
      JsonNode self = moveRequest.get("you").get("body");
      health = moveRequest.get("you").get("health").asInt();

      populateBoard(food, snakes, self);
      logBoard();

      int currX = self.get(0).get("x").asInt();
      int currY = self.get(0).get("y").asInt();

      LOG.info("Current Position: (" + currX + "," + currY + ")");

      response.put("move", move(currX, currY));
      return response;
    }

    /**
     * /end is called by the engine when a game is complete.
     *
     * @param endRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
     * @return responses back to the engine are ignored.
     */
    public Map<String, String> end(JsonNode endRequest) {
      Map<String, String> response = new HashMap<>();
      return response;
    }


    private void populateBoard(JsonNode food, JsonNode snakes, JsonNode self) {
      populateBoard(food, 1);
      // snakes include self, but difference between other snakes and self might be important later on
      //populateBoard(self, 2);
      for (JsonNode snake : snakes) {
        populateBoard(snake.get("body"), 2);
      }

    }

    private void populateBoard(JsonNode arrOfStuff, int val) {
      for (JsonNode coordinate : arrOfStuff) {
        int x = coordinate.get("x").asInt();
        int y = coordinate.get("y").asInt();
        board[y][x] = val;
      }
    }

    private String move(int currX, int currY) {
      List<Moves> validMoves = new ArrayList<>();
      for (Moves move : Moves.values()) {
        int nextX = currX + move.xChange;
        int nextY = currY + move.yChange;
        if (isMoveValid(nextX, nextY)) {
          validMoves.add(move);
        }
      }
      Random random = new Random();
      //will cause exception if no valid moves at the moment
      int index = random.nextInt(validMoves.size());
      return validMoves.get(index).toString();

    }

    private void logBoard() {
      StringBuilder boardString = new StringBuilder();
      boardString.append('\n');
      for (int y = 0; y < HEIGHT; y++) {
        for (int x = 0; x < WIDTH; x++) {
          boardString.append(board[y][x]);
          boardString.append(' ');
        }
        boardString.append('\n');
      }
      LOG.info(boardString.toString());
    }

    private boolean isMoveValid(int x, int y) {
      return (isMoveInBoundary(x, y) && board[y][x] != 2);
    }

    private boolean isMoveInBoundary(int x, int y) {
      return (x > -1 && x < WIDTH && y > -1 && y < HEIGHT);
    }
  }

}
