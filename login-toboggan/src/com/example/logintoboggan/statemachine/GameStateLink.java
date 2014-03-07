package com.example.logintoboggan.statemachine;

public class GameStateLink
{
	public enum LinkDirection
	{
		To, 
		From, 
		BiDirectional
	}

	public LinkDirection LinkDirection;

	public GameStateLink(LinkDirection linkDirection)
	{
		LinkDirection = linkDirection;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof GameStateLink))return false;

		GameStateLink link = (GameStateLink) o;

		if (link.LinkDirection == LinkDirection)return true;

		return false;
	}

	@Override
	public int hashCode()
	{
		return LinkDirection.ordinal() * 32;
	}
}