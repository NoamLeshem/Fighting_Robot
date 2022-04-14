package com.talandnoam.fightingrobot.classes;

import androidx.annotation.NonNull;

public class Match
{
	private String id, winner, date, time, type, format, result, roundsCap;

	public Match()
	{
		// TODO Auto-generated constructor stub
	}

	public Match(String id, String winner, String date, String time, String type, String format, String result, String roundsCap)
	{
		this.setRoundsCap(roundsCap);
		this.setWinner(winner);
		this.setFormat(format);
		this.setResult(result);
		this.setDate(date);
		this.setTime(time);
		this.setType(type);
		this.setId(id);
	}

	public String getRoundsCap ()
	{
		return roundsCap;
	}

	private void setRoundsCap (String roundsCap)
	{
		this.roundsCap = roundsCap;
	}

	public String getId ()
	{
		return this.id;
	}

	public void setId (String id)
	{
		this.id = id;
	}

	public String getWinner ()
	{
		return this.winner;
	}

	public void setWinner (String winner)
	{
		this.winner = winner;
	}

	public String getDate ()
	{
		return this.date;
	}

	public void setDate (String date)
	{
		this.date = date;
	}

	public String getTime ()
	{
		return this.time;
	}

	public void setTime (String time)
	{
		this.time = time;
	}

	public String getType ()
	{
		return this.type;
	}

	public void setType (String type)
	{
		this.type = type;
	}

	public String getFormat ()
	{
		return this.format;
	}

	public void setFormat (String format)
	{
		this.format = format;
	}

	public String getResult ()
	{
		return this.result;
	}

	public void setResult (String result)
	{
		this.result = result;
	}

	@NonNull
	public String toString()
	{
		return "Match ID: " + id + "\n" +
				"Winner: " + winner + "\n" +
				"Date: " + date + "\n" +
				"Time: " + time + "\n" +
				"Type: " + type + "\n" +
				"Format: " + format + "\n" +
				"Result: " + result + "\n" +
				"Rounds Cap: " + roundsCap;
	}
}
