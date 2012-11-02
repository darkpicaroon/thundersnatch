package com.thundersnatch;

public class Player {
	public int userID;
	public float xPosition;
	public float yPosition;
	public boolean hasOwnFlag;
	public boolean hasOpponentFlag;
	public int stealsThisGame;
	public int capturesThisGame;
	public int teamID;
	
	public Player(int id, float x, float y, boolean own, boolean opp, int steals, int captures, int team){
		userID = id;
		xPosition = x;
		yPosition = y;
		hasOwnFlag = own;
		hasOpponentFlag = opp;
		stealsThisGame = steals;
		capturesThisGame = captures;
		teamID = team;
	}
	
	public Player(int id, float x, float y, boolean own, boolean opp, int team){
		userID = id;
		xPosition = x;
		yPosition = y;
		hasOwnFlag = own;
		hasOpponentFlag = opp;
		teamID = team;
	}
	
	
}
