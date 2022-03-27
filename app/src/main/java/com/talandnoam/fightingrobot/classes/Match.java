package com.talandnoam.fightingrobot.classes;

import androidx.annotation.NonNull;

public class Match
{
	private String matchId, matchWinner, matchDate, matchTime, matchType, matchFormat, matchResult;

	public Match()
	{
		// TODO Auto-generated constructor stub
	}

	public Match(String matchId, String matchWinner, String matchDate, String matchTime, String matchType, String matchFormat, String matchResult)
	{
		this.setMatchWinner(matchWinner);
		this.setMatchFormat(matchFormat);
		this.setMatchResult(matchResult);
		this.setMatchDate(matchDate);
		this.setMatchTime(matchTime);
		this.setMatchType(matchType);
		this.setMatchId(matchId);
	}

	public String getMatchId()
	{
		return this.matchId;
	}

	public void setMatchId(String matchId)
	{
		this.matchId = matchId;
	}

	public String getMatchWinner()
	{
		return this.matchWinner;
	}

	public void setMatchWinner(String matchWinner)
	{
		this.matchWinner = matchWinner;
	}

	public String getMatchDate()
	{
		return this.matchDate;
	}

	public void setMatchDate(String matchDate)
	{
		this.matchDate = matchDate;
	}

	public String getMatchTime()
	{
		return this.matchTime;
	}

	public void setMatchTime(String matchTime)
	{
		this.matchTime = matchTime;
	}

	public String getMatchType()
	{
		return this.matchType;
	}

	public void setMatchType(String matchType)
	{
		this.matchType = matchType;
	}

	public String getMatchFormat()
	{
		return this.matchFormat;
	}

	public void setMatchFormat(String matchFormat)
	{
		this.matchFormat = matchFormat;
	}

	public String getMatchResult()
	{
		return this.matchResult;
	}

	public void setMatchResult(String matchResult)
	{
		this.matchResult = matchResult;
	}

	@NonNull
	public String toString()
	{
		return "Match ID: " + matchId + "\n" +
				"Winner: " + matchWinner + "\n" +
				"Date: " + matchDate + "\n" +
				"Time: " + matchTime + "\n" +
				"Type: " + matchType + "\n" +
				"Format: " + matchFormat + "\n" +
				"Result: " + matchResult;
	}
}
