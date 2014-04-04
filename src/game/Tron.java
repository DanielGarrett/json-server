package game;

import game.TronCar.Direction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Tron implements GenGame {
	public enum FieldType {
		open,
		p0car1,
		p0car2,
		p0car3,
		p1car1,
		p1car2,
		p1car3,
		wall
	}
	public enum CommandStatus
	{
		executedCorrectly,
		gameNotRunning,
		someFailed,
		badJSON
	}
	
	private static final int MAX_PLAYERS = 2;
	private static final int MIN_BOARD_SIZE = 35;
	private static final int MAX_BOARD_SIZE = 45;
	private static final int TICK_TIME = 250;
	private int playerCount;
	private boolean isRunning;
	private long startTime;
	private int tick;
	public FieldType[][] board;
	public TronCar[] cars;
	private Random rand;
	private int winner;
	private FileWriter log;
	
	public Tron()
	{
		rand = new Random();
		playerCount = 0;
		isRunning = false;
		initializeBoard();
		winner = -1;
		try {
			log = new FileWriter(new File("logs/TronLog" + System.currentTimeMillis()/1000 + ".log"));
			log.write(board.length + " " + board[0].length + "\n");
		} catch (IOException e) {
			//oops
		}
		initializeCars();
		
	}
	
	public void startup() {mainLoop();}
	
	private void mainLoop()
	{
		while(!isRunning)
		{
			try {
				Thread.sleep(450);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		while(isRunning)
		{
			try {
				Thread.sleep(timeToNextTick());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tick++;
			doTick();
		}
	}
	
	private int timeToNextTick()
	{
		int time = (int)(((tick+1)*TICK_TIME)+startTime-System.currentTimeMillis());
		System.out.println(time);
		return time > 0 ? time : 0;
	}
	
	private void initializeCars() {
		cars = new TronCar[6];
		cars[0] = new TronCar(3, board[0].length-1, FieldType.p0car1, 0);
		cars[1] = new TronCar(8, board[0].length-1, FieldType.p0car2, 0);
		cars[2] = new TronCar(13, board[0].length-1, FieldType.p0car3, 0);
		cars[3] = new TronCar(board.length-4, board[0].length-1, FieldType.p1car1, 1);
		cars[4] = new TronCar(board.length-9, board[0].length-1, FieldType.p1car2, 1);
		cars[5] = new TronCar(board.length-14, board[0].length-1, FieldType.p1car3, 1);
		doTick();
		
	}
	private void initializeBoard() 
	{
		board = new FieldType[getDimension()][getDimension()];
		for(int i = 0; i < board.length; i++)
		{
			for(int j = 0; j < board[0].length; j++)
			{
				board[i][j] = FieldType.open;
			}
		}
		for(int i = 0; i < board.length; i++)
		{
			board[i][0] = FieldType.wall;
			board[i][board[0].length-1] = FieldType.wall;
		}
		for(int i = 0; i < board[0].length; i++)
		{
			board[0][i] = FieldType.wall;
			board[board.length-1][i] = FieldType.wall;
		}
	}
	@Override
	public JSONObject getStatus() 
	{
		JSONObject obj = new JSONObject();
		try {
			obj.put("TICK", tick);
			obj.put("MAX_X", board.length);
			obj.put("MAX_Y", board[0].length);
			JSONArray boardArr = new JSONArray();
			for(int x = 0; x < board.length; x++)
			{
				JSONArray subArr = new JSONArray();
				for(int y = 0; y < board[x].length; y++)
				{
					subArr.put(board[x][y].ordinal());
				}
				boardArr.put(subArr);
			}
			obj.put("BOARD", boardArr);
			JSONArray carArr = new JSONArray();
			for(int i = 0; i < cars.length; i++)
			{
				carArr.put(cars[i].toJson());
			}
			obj.put("CARS", carArr);
			obj.put("RUNNING", isRunning);
			obj.put("WHOWON", winner);
		} catch (JSONException e) {
			// oops
		}
		return obj;
	}

	@Override
	public JSONObject doCommand(JSONObject input) throws JSONException 
	{
		JSONObject result = new JSONObject();
		CommandStatus error = CommandStatus.executedCorrectly;
		if(!isRunning)
		{
			error = CommandStatus.gameNotRunning;
		}
		JSONArray moves = input.getJSONArray("MOVES");
		int player = input.getInt("ID");
		for(int i = 0; i < moves.length(); i++)
		{
			JSONObject move = moves.getJSONObject(i);
			TronCar car = getCarByFieldType(FieldType.values()[move.getInt("CAR")]);
			if(car == null || car.player != player || car.getDirection() == Direction.dead)
			{
				error = CommandStatus.someFailed;
				continue;
			}
			Direction dir = Direction.values()[move.getInt("DIR")];
			car.setDirection(dir);
			
		}
		result.put("COMMAND", input);
		result.put("STATUS", getStatus());
		result.put("ERROR", error.ordinal());
		return result;
	}
	
	public TronCar getCarByFieldType(FieldType type)
	{
		//The poor hack was going to be somewhere
		//why not only one somewhere
		switch(type)
		{
		case p0car1:
			return cars[0];
		case p0car2:
			return cars[1];
		case p0car3:
			return cars[2];
		case p1car1:
			return cars[3];
		case p1car2:
			return cars[4];
		case p1car3:
			return cars[5];
		default:
				return null;
		}
	}

	@Override
	public boolean doInit(int gId) 
	{
		if (playerCount >= MAX_PLAYERS)
			return false;
		else {
			playerCount++;
			if(playerCount == MAX_PLAYERS)
			{
				isRunning = true;
				startTime = System.currentTimeMillis();
				tick = 0;
				MainLoop loop = new MainLoop();
				Thread loopThread = new Thread(loop);
				loopThread.start();
			}
			return true;
		}
	}
	
	public int getDimension()
	{
		return rand.nextInt(MAX_BOARD_SIZE-MIN_BOARD_SIZE)+MIN_BOARD_SIZE;
	}
	
	public int getCurrentTick()
	{
		if(!isRunning)
			return 0;
		//if this cast is ever an issue, we have bigger problems
		return (int)((System.currentTimeMillis()-startTime) / TICK_TIME);
	}
	
	public void doTick()
	{
		ArrayList<int[]> deadThisRound = new ArrayList<int[]>();
		for(TronCar car : cars)
		{
			int[] result = car.move();
			if(result == null)
				continue;
			if(board[result[0]][result[1]] != FieldType.open)
			{
				car.setDirection(Direction.freshlyDead);
				deadThisRound.add(result);
				continue;
			}
			board[result[0]][result[1]] = car.getType();
		}
		for(TronCar car : cars)
		{
			for(int [] dead : deadThisRound)
				if(car.getLocation()[0] == dead[0] && car.getLocation()[1] == dead[1])
					car.setDirection(Direction.freshlyDead);
		}
		for(TronCar car : cars)
		{
			if(car.getDirection() == Direction.freshlyDead)
			{
				clearFromBoard(car.getType());
				car.setDirection(Direction.dead);
			}
		}
		checkWin();
		writeState();
	}
	
	private void checkWin() {
		boolean p1 = false, p2 = false;
		if(cars[0].getDirection() == Direction.dead &&
		   cars[1].getDirection() == Direction.dead &&
		   cars[2].getDirection() == Direction.dead)
		{
			p2 = true;
		}
		if(cars[3].getDirection() == Direction.dead &&
		   cars[4].getDirection() == Direction.dead &&
		   cars[5].getDirection() == Direction.dead)
		{
			p1 = true;
		}
		if(p1 && p2)
		{
			winner = 2;
			isRunning = false;
			return;
		}
		if(p1)
		{
			winner = 0;
			isRunning = false;
			return;
		}
		if(p2)
		{
			winner = 1;
			isRunning = false;
			return;
		}
		
	}
	private void clearFromBoard(FieldType type) {
		for(int x = 0; x < board.length; x++)
		{
			for(int y = 0; y < board[x].length; y++)
			{
				if(board[x][y] == type)
				{
					board[x][y] = FieldType.open;
				}
			}
		}
		
	}
	
	private void writeState()
	{
		try {
			for(int i = 0; i < board[0].length; i ++)
			{
				for(int j = 0; j < board.length; j++)
				{
					log.write(board[j][i].ordinal() + " ");
				}
				log.write("\n");
			}
			log.flush();
		} catch (IOException e) {
			// oops
		}
	}
	
	private class MainLoop implements Runnable
	{
		public void run() 
		{
			mainLoop();
		}
	}

}
