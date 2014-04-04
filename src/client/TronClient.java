package client;

import game.TronCar;
import game.TronCar.Direction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;


public class TronClient {
	
	private String hostname;
	private int port;
	private static final String connect = "/connect";
	private static final String move = "/game/move";
	private static final String status = "/game/status";
	private int playerId, playerAuth, gameId;
	
	public static TronClient connect(String host, int portId)
	{
		TronClient tc = new TronClient();
		tc.hostname = host;
		tc.port = portId;
		JSONObject req = new JSONObject();
		JSONObject resp = tc.sendRequest("http://"+tc.hostname+":"+tc.port+connect, req);
		try {
			tc.gameId = resp.getInt("GAMEID");
			tc.playerAuth = resp.getInt("AUTH");
			tc.playerId = resp.getInt("ID");
		} catch (Exception e) {
			//oops
		} 
		
		return tc;
	}
	
	public TronGameState move(TronCar... cars)
	{
		JSONObject request = new JSONObject();
		try 
		{
			request.put("GAMEID", gameId);
			request.put("AUTH", playerAuth);
			request.put("ID", playerId);
			JSONArray moves = new JSONArray();
			for(TronCar car : cars)
			{
				JSONObject carobj = new JSONObject();
				carobj.put("CAR", car.getType().ordinal());
				carobj.put("DIR", car.getDirection().ordinal());
				moves.put(carobj);
			}
			request.put("MOVES", moves);
			JSONObject response = sendRequest("http://"+hostname+":"+port+move, request);
			return new TronGameState(response.getJSONObject("STATUS"), playerId);
		} 
		catch (Exception e)
		{
			return null; //oops
		}
	}
	
	public TronGameState getStatus()
	{
		JSONObject request = new JSONObject();
		try 
		{
			request.put("GAMEID", gameId);
			request.put("AUTH", playerAuth);
			request.put("ID", playerId);
			JSONObject response = sendRequest("http://"+hostname+":"+port+status, request);
			return new TronGameState(response, playerId);
		} 
		catch (Exception e)
		{
			return null; //oops
		}
	}
	
//	public void startMove()
//	{
//		
//	}
	
	private JSONObject sendRequest(String address, JSONObject request)
	{
		JSONObject resp = null;
		String response = "";
		try {
			String data;
			data = URLEncoder.encode(request.toString(), "UTF-8");
			// Send data
			URL url = new URL(address);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				response += line;
				// System.out.println(line);
			}
			wr.close();
			rd.close();
			resp = new JSONObject(response);
			

		} catch (Exception e) { // PRO error handling!
			//oops
		} 
		return resp;
	}
}
