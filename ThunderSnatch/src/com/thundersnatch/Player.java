package com.thundersnatch;

public class Player {
	public int userGameID;
	public float xPosition;
	public float yPosition;
	public boolean hasOwnFlag;
	public boolean hasOpponentFlag;
	public boolean isBase;
	public int stealsThisGame;
	public int capturesThisGame;
	public int teamID;
	public String userName;
	public boolean base;
	public int teamIndex;
	
	public Player(int id, float x, float y, boolean own, boolean opp, int steals, int captures, int team){
		userGameID = id;
		xPosition = x;
		yPosition = y;
		hasOwnFlag = own;
		hasOpponentFlag = opp;
		stealsThisGame = steals;
		capturesThisGame = captures;
		teamID = team;
	}

	public Player(int id, String name, float x, float y, boolean own,
			boolean opp, int team) {
		userGameID = id;
		xPosition = x;
		yPosition = y;
		hasOwnFlag = own;
		hasOpponentFlag = opp;
		base = false;
		teamID = team;
		userName = name;
	}
	
	// Flag constructor.
	public Player(float x, float y, boolean own, boolean isBase, int team, int index) {
		xPosition = x;
		yPosition = y;
		hasOwnFlag = own;
		base = isBase;
		teamID = team;
		teamIndex = index;
		userGameID = -1;
	}

	public Player(String name, int team){
		userName = name;
		teamID = team;
	}
	
	public String toString() {
		String sReturn = "UserGameID: " + userGameID + " UserName: " + userName + " Team: " + " Own:" + hasOwnFlag + " Opp: " + hasOpponentFlag + " x:" + xPosition + " y:" + yPosition;
		return sReturn;
	}
	
}
