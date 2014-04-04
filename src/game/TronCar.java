package game;

import org.json.JSONException;
import org.json.JSONObject;

import game.Tron.FieldType;

public class TronCar {
	public enum Direction
	{
		up,
		down,
		left,
		right,
		dead,
		freshlyDead
	}
	private FieldType carType;
	private int x;
	private int y;
	private Direction dir;
	public int player;
	
	public TronCar(int x, int y, FieldType number, int player)
	{
		this.x = x;
		this.y = y;
		carType = number;
		dir = Direction.up;
		this.player = player;
	}
	
	public int[] move()
	{
		switch(dir)
		{
		case up:
			y--;
			break;
		case down:
			y++;
			break;
		case left:
			x--;
			break;
		case right:
			x++;
			break;
		case dead:
		case freshlyDead:
			return null;
		}
		int[] result = new int[]{x,y};
		return result;
	}
	
	public void setDirection(Direction newDir)
	{
		dir = newDir;
	}
	
	public Direction getDirection()
	{
		return dir;
	}
	
	public FieldType getType()
	{
		return carType;
	}
	
	public JSONObject toJson() throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("CARTYPE", carType.ordinal());
		obj.put("DIRECTION", dir.ordinal());
		obj.put("X", x);
		obj.put("Y", y);
		obj.put("PLAYERID", player);
		return obj;
	}
	
	public static TronCar fromJson(JSONObject obj) throws JSONException
	{
		TronCar car = new TronCar(
				obj.getInt("X"),
				obj.getInt("Y"),
				FieldType.values()[obj.getInt("CARTYPE")],
				obj.getInt("PLAYERID"));
		car.setDirection(Direction.values()[obj.getInt("DIRECTION")]);
		return car;
	}
	
	public boolean equals(TronCar other)
	{
		return
				this.dir == other.dir &&
				this.carType == other.carType &&
				this.x == other.x &&
				this.y == other.y;
	}
	public int[] getLocation()
	{
		return new int[]{x,y};
	}
}
