package com.example.logintoboggan.statemachine;

public interface GameState<T, R>
{
	public void onEnter(GameStateMachine<T, R> stateMachine);
	public void onExit(GameStateMachine<T, R> stateMachine);
	public void onReturn(GameStateMachine<T, R> stateMachine);
}
