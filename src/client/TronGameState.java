package client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import game.Tron.FieldType;
import game.TronCar;
import game.TronCar.Direction;

public class TronGameState {
	public int playerNum;
	public boolean running;
	public TronCar[] myCars;
	public TronCar[] theirCars;
	public int winner;
	public int tick;
	public FieldType[][] board;
	
	public TronGameState(JSONObject obj, int playerNum)
	{
		try {
			this.playerNum = playerNum;
			tick = obj.getInt("TICK");
			winner = obj.getInt("WHOWON");
			running = obj.getBoolean("RUNNING");
			int maxx = obj.getInt("MAX_X");
			int maxy = obj.getInt("MAX_Y");
			board = new FieldType[maxx][maxy];
			JSONArray boardjson = obj.getJSONArray("BOARD");
			for(int x = 0; x < maxx; x++)
			{
				JSONArray rowjson = boardjson.getJSONArray(x);
				for(int y = 0; y < maxy; y++)
				{
					board[x][y] = FieldType.values()[rowjson.getInt(y)];
				}
			}
			TronCar[] p1 = new TronCar[3];
			TronCar[] p2 = new TronCar[3];
			JSONArray cars = obj.getJSONArray("CARS");
			p1[0] = TronCar.fromJson(cars.getJSONObject(0));
			p1[1] = TronCar.fromJson(cars.getJSONObject(1));
			p1[2] = TronCar.fromJson(cars.getJSONObject(2));
			p2[0] = TronCar.fromJson(cars.getJSONObject(3));
			p2[1] = TronCar.fromJson(cars.getJSONObject(4));
			p2[2] = TronCar.fromJson(cars.getJSONObject(5));
			if(playerNum == 0)
			{
				myCars = p1;
				theirCars = p2;
			}
			else
			{
				myCars = p2;
				theirCars = p1;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean canMoveUp(TronCar car)
	{
		int[] location = car.getLocation();
		return car.getDirection() != Direction.dead && board[location[0]][location[1]-1] == FieldType.open;
	}
	
	public boolean canMoveDown(TronCar car)
	{
		int[] location = car.getLocation();
		return car.getDirection() != Direction.dead && board[location[0]][location[1]+1] == FieldType.open;
	}
	
	public boolean canMoveLeft(TronCar car)
	{
		int[] location = car.getLocation();
		return car.getDirection() != Direction.dead && board[location[0]-1][location[1]] == FieldType.open;
	}
	
	public boolean canMoveRight(TronCar car)
	{
		int[] location = car.getLocation();
		return car.getDirection() != Direction.dead && board[location[0]+1][location[1]] == FieldType.open;
	}

	public boolean canMoveForward(TronCar car) {
		switch(car.getDirection())
		{
		case up:
			return canMoveUp(car);
		case down:
			return canMoveDown(car);
		case left:
			return canMoveLeft(car);
		case right:
			return canMoveRight(car);
		default:
			return false;
		}
		
	}

}
