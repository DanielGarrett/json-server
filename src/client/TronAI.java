package client;

import game.TronCar;
import game.TronCar.Direction;

import java.util.Random;

public class TronAI {
	
	public static void main(String[] args) throws InterruptedException
	{
		Random rand = new Random();
		long start = System.currentTimeMillis();
		TronClient client = TronClient.connect("localhost", 8000);
		start = System.currentTimeMillis();
		TronGameState state = client.getStatus();
		while(state.running)
		{
			Thread.sleep(50);
			for(TronCar car : state.myCars)
			{
				if(state.canMoveForward(car))
				{
					if(state.canMoveDown(car) && rand.nextInt(50) == 0) car.setDirection(Direction.down);
					else if(state.canMoveLeft(car) && rand.nextInt(50) == 0) car.setDirection(Direction.left);
					else if(state.canMoveRight(car) && rand.nextInt(50) == 0) car.setDirection(Direction.right);
					else if(state.canMoveUp(car) && rand.nextInt(50) == 0) car.setDirection(Direction.up);
				}
				else
				{
					if(state.canMoveDown(car)) car.setDirection(Direction.down);
					if(state.canMoveLeft(car)) car.setDirection(Direction.left);
					if(state.canMoveRight(car)) car.setDirection(Direction.right);
					if(state.canMoveUp(car)) car.setDirection(Direction.up);
				}
			}
			start = System.currentTimeMillis();
			state = client.move(state.myCars);
			System.out.println(System.currentTimeMillis() - start);
		}
	}

}
