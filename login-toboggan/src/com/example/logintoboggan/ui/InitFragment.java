package com.example.logintoboggan.ui;

import com.example.logintoboggan.R;
import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.statemachine.GameStateMachine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class InitFragment extends BaseFragment
{
	private TextView mHeadingText;
	private TextView mMessageText;
	private Button mRetryButton;
	private LoginToboggan theApp;
	private GameStateMachine<AppState, LoginTobogganGameStateResult> mGameStateMachine;

	private View baseView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		baseView = inflater.inflate(R.layout.fragment_init, container, false);

		mHeadingText = (TextView) baseView.findViewById(R.id.initHeadingText);
		mMessageText = (TextView) baseView.findViewById(R.id.initMessageText);
		mRetryButton = (Button) baseView.findViewById(R.id.retryInitButton);

		mRetryButton.setOnClickListener(retryInitButtonClickListener);

		theApp = LoginToboggan.getInstance();
		mGameStateMachine = theApp.getGameStateMachine();

		return baseView;
	}

	public void enableRetryInit(String errorMsg)
	{
		mHeadingText.setText("Error occurred during initializtion.");
		mMessageText.setText(errorMsg);
		mRetryButton.setVisibility(View.VISIBLE);

	}

	public OnClickListener retryInitButtonClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			mGameStateMachine.moveTo(AppState.Init);
		}

	};
}
