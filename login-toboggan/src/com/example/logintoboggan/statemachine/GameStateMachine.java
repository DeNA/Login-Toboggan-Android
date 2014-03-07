package com.example.logintoboggan.statemachine;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Stack;

import com.example.logintoboggan.statemachine.GameStateLink.LinkDirection;

/**
 * T should be an enum by which GameState instance can be referred to
 * 
 * R is a customized GameStateResult class. It will be passed from state
 * to state and the state machine makes its transitions so that states
 * can communicate with each other.
 * 
 */
public class GameStateMachine<T, R>
{
	public enum WaitState
	{
		None, Waiting
	}

	// Link states to the other states to which they can transition
	// For each state S there is a HashMap that maps other states to
	// the type of link they have with S.
	private HashMap<T, HashMap<T, GameStateLink>> stateLinks;

	// links enum value T to its GameState instance
	private HashMap<T, GameState<T, R>> gameStates;

	// link GameState instance to its enum value T
	private HashMap<GameState<T, R>, T> gameStatesReverseMap;

	private T startStateID;
	private T currentStateID;

	private Stack<T> progress;
	private ArrayDeque<T> queuedStates;

	private R gameStateResult;
	private T returnFromState;

	private boolean inMove;
	private WaitState currentWaitState;

	private final Object waitLock = new String("waitLock");

	public GameStateMachine()
	{

		stateLinks = new HashMap<T, HashMap<T, GameStateLink>>();
		gameStates = new HashMap<T, GameState<T, R>>();
		gameStatesReverseMap = new HashMap<GameState<T, R>, T>();

		progress = new Stack<T>();
		queuedStates = new ArrayDeque<T>();

		gameStateResult = null;
		currentStateID = null;
		startStateID = null;

		inMove = false;
		currentWaitState = WaitState.None;
	}

	// set the GasmeStateResult instance that will be passed from state to state
	public void setGameStateResultObject(R gameStateResult)
	{
		this.gameStateResult = gameStateResult;
	}

	// add a GameState instance to the machine
	public void addState(T uniqueID, GameState<T, R> gameState)
	{
		gameStates.put(uniqueID, gameState);
		gameStatesReverseMap.put(gameState, uniqueID);
	}

	/** 
	 * Set start state
	 * 
	 * @param stateID
	 * 
	 */
	public void setStartState(T stateID)
	{
		this.startStateID = stateID;
	}

	public void initialize()
	{
		reset();
	}

	/**
	 * Restart state machine.
	 * 
	 */
	public void reset()
	{
		currentStateID = startStateID;
		progress.clear();
	}

	public R GetGameStateResult()
	{
		return gameStateResult;
	}

	/**
	 * Set a link between two states.
	 * 
	 * @param stateAID From state.
	 * @param stateBID To State.
	 * @param linkDirection Link direction.
	 */
	public void linkStates(T stateAID, T stateBID, LinkDirection linkDirection)
	{
		if (stateAID == stateBID)return;

		HashMap<T, GameStateLink> existingLinksA = stateLinks.get(stateAID);
		if (existingLinksA == null)
		{
			existingLinksA = new HashMap<T, GameStateLink>();
			stateLinks.put(stateAID, existingLinksA);
		}

		HashMap<T, GameStateLink> existingLinksB = stateLinks.get(stateBID);
		if (existingLinksB == null)
		{
			existingLinksB = new HashMap<T, GameStateLink>();
			stateLinks.put(stateBID, existingLinksB);
		}

		GameStateLink aToB = null;
		GameStateLink bToA = null;

		switch (linkDirection)
		{
		case BiDirectional:
			aToB = new GameStateLink(LinkDirection.BiDirectional);
			bToA = new GameStateLink(LinkDirection.BiDirectional);
			break;
		case To:
			aToB = new GameStateLink(LinkDirection.To);
			bToA = new GameStateLink(LinkDirection.From);
			break;
		case From:
			aToB = new GameStateLink(LinkDirection.From);
			bToA = new GameStateLink(LinkDirection.To);
			break;
		}

		if (existingLinksA.get(stateBID) == null)
			existingLinksA.put(stateBID, aToB);
		if (existingLinksB.get(stateAID) == null)
			existingLinksB.put(stateAID, bToA);
	}

	/**
	 * Move from the current state to the state specified.
	 * 
	 * @param stateID To destination state.
	 * @return Boolean that indicates the transition was successfully processed.
	 */
	public boolean moveTo(T stateID)
	{
		// if waiting on an asynchronous even, queue the state
		// and return
		synchronized (waitLock)
		{
			if (currentWaitState == WaitState.Waiting)
			{
				queuedStates.addLast(stateID);
				return false;
			}
		}

		// queue the state to be processed
		queuedStates.addLast(stateID);

		// process queued states, which now include the new
		// state to be transitioned to
		processQueuedStates();

		return true;
	}

	/**
	 * Get the state visited before the current state.
	 * 
	 * @return The previous state.
	 */
	public T getPreviousState()
	{
		if (progress.size() > 0)
			return progress.peek();
		else
			return null;
	}

	public R getGameStateResult()
	{
		return gameStateResult;
	}
	
	/**
	 * Perform work to move from current state to state specified by 'to'.
	 * 
	 * @param to Destination state.
	 * @param push Indicates whether or not to push the current state onto the history stack.
	 * @param isReturn Indicates whether this transition is moving to a new state or returning to an old state.
	 */
	private void processStateTransition(T to, boolean push, boolean isReturn)
	{		
		inMove = true;
		try
		{
			HashMap<T, GameStateLink> links = stateLinks.get(currentStateID);
			GameStateLink link = links.get(to);
			if (link != null)
			{
				if (link.LinkDirection == LinkDirection.To || link.LinkDirection == LinkDirection.BiDirectional)
				{
					GameState<T, R> currentState = gameStates.get(currentStateID);
					currentState.onExit(this);
	
					if(push)progress.push(currentStateID);
					if(isReturn)returnFromState = currentStateID;
	
					currentStateID = to;
					currentState = gameStates.get(currentStateID);
					if(isReturn)currentState.onReturn(this);
					else currentState.onEnter(this);
				}
			}
		}
		finally
		{
			inMove = false;
		}
	}	
			
	/**
	 * Return to the state the machine was at before the current state.
 	 * Pops the current state of the stack and executes the onReturn()
	 * method of the state that is now at the top of the stack.
	 * A call to returnPrevious() includes the assumption that we are not
	 * nested within a state processing call higher up the call stack.
	 * 
	 * @return Boolean that indicates the transition was successfully processed.
	 */
	public boolean returnPrevious()
	{
		if (progress.size() <= 0)
			return false;

		T returnToStateID = progress.pop();
		processStateTransition(returnToStateID,false,true);		

		// after returning to the previous state, continue processing
		// queued state changes
		processQueuedStates();

		return true;
	}

	/**
	 * Get the state that we are returning from. This should be used
	 * in implementations of onReturn() in GameStates.
	 * 
	 * @return The state from which the state machine is returning.
	 */
	public T getReturnFromState()
	{
		return returnFromState;
	}

	/**
	 *  Perform any queued state transitions.
	 */
	private void processQueuedStates()
	{
		// process queued states if not nested within a call to
		// processQueuedStates higher up the call stack
		if (inMove)return;

		synchronized (waitLock)
		{
			if (currentWaitState == WaitState.Waiting)return;
		}

		while (queuedStates.size() > 0)
		{
			T newStateID = queuedStates.removeFirst();
			
			processStateTransition(newStateID,true,false);

			// if last state transition caused state machine to
			// wait, stop processing states
			synchronized (waitLock)
			{
				if (currentWaitState == WaitState.Waiting)return;
			}
		}
	}

	/**
	 * Tells the state machine to stop any further processing of state transitions.
	 */
	public void setWaitStateWaiting()
	{
		synchronized (waitLock)
		{
			currentWaitState = WaitState.Waiting;
		}
	}

	/**
	 * Tells the state machine to resume processing of state transitions.
	 */
	public void setWaitStateRunning()
	{
		synchronized (waitLock)
		{
			currentWaitState = WaitState.None;
		}
	}
}
