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
  static Map<Integer, String> directionMap = new HashMap<>();

  /**
   * Main entry point.
   *
   * @param args are ignored.
   */
  public static void main(String[] args) {
    directionMap.put(0, "left");
    directionMap.put(1, "down");
    directionMap.put(2, "right");
    directionMap.put(3, "up");
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
     * @param pingRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
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
      board = new int[HEIGHT][WIDTH];
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

      Map<String, String> response = new HashMap<>();
      //moveRequest.
      System.out.println(moveRequest.toString());
//      System.out.println(moveRequest.);
      JsonNode food = moveRequest.get("board").get("food");
      JsonNode snakes = moveRequest.get("board").get("snakes");
      health = moveRequest.get("you").get("health").asInt();

      JsonNode self = moveRequest.get("you").get("body");
      populateGrid(food, snakes, self);
      int currX = self.get(0).get("x").asInt();
      int currY = self.get(0).get("y").asInt();




      response.put("move", move(currX, currY));
      return response;
    }


    private void populateGrid(JsonNode food, JsonNode snakes, JsonNode self) {
      populateGrid(food, 1);
      populateGrid(self, 2);
      for (JsonNode snake : snakes) {
        populateGrid(snake.get("body"), 2);
      }

    }

    private void populateGrid(JsonNode arrOfStuff, int val){
      for (JsonNode coordinates : arrOfStuff) {
        int x = coordinates.get("x").asInt();
        int y = coordinates.get("y").asInt();
        board[y][x] = val;
      }
    }

    private String move(int currX, int currY) {
      int[] dx = {-1, 0, 1, 0};
      int[] dy = {0, 1, 0, -1};
      List<Integer> moves = new ArrayList<>();
      for (int i = 0 ; i < 4; i++){
        int nextX = currX + dx[i];
        int nextY = currY + dy[i];
        if (nextX > -1 && nextX < HEIGHT && nextY > -1 && nextY < WIDTH){
          if (board[nextY][nextX] != 2) {
            moves.add(i);
          }
        }
      }
      Random random = new Random();
      int index = random.nextInt(moves.size());
      return directionMap.get(moves.get(index));

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
  }

}
