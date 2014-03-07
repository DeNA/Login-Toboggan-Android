package com.example.logintoboggan.ui;

import com.example.logintoboggan.R;
import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.statemachine.GameStateMachine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class LoginFragment extends BaseFragment
{
	private TextView mHeadingText;
	private Button mFacebookLoginButton;
	private Button mGuestLoginButton;
	private Button mMobageLoginButton;
	private LoginToboggan theApp;
	private GameStateMachine<AppState, LoginTobogganGameStateResult> mGameStateMachine;

	private View baseView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		baseView = inflater.inflate(R.layout.fragment_login, container, false);

		setupUI();

		theApp = LoginToboggan.getInstance();
		mGameStateMachine = theApp.getGameStateMachine();

		return baseView;
	}

	private void setupUI()
	{
		mHeadingText = (TextView) baseView.findViewById(R.id.loginHeadingText);
		mFacebookLoginButton = (Button) baseView.findViewById(R.id.facebookLoginButton);
		mGuestLoginButton = (Button) baseView.findViewById(R.id.guestLoginButton);
		mMobageLoginButton = (Button) baseView.findViewById(R.id.mobageLoginButton);

		mFacebookLoginButton.setOnClickListener(facebookLoginButtonClickListener);
		mGuestLoginButton.setOnClickListener(guestLoginButtonClickListener);
		mMobageLoginButton.setOnClickListener(mobageLoginButtonClickListener);
	}

	public OnClickListener facebookLoginButtonClickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			mGameStateMachine.moveTo(AppState.FacebookLogin);
		}
	};

	public OnClickListener guestLoginButtonClickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			mGameStateMachine.moveTo(AppState.MobageGuestLogin);

		}
	};

	public OnClickListener mobageLoginButtonClickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			mGameStateMachine.moveTo(AppState.MobageLogin);

		}

	};

}
