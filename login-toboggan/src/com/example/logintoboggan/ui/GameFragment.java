package com.example.logintoboggan.ui;

import com.example.logintoboggan.R;
import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.helper.MobageHelper.GetUserInfoCallback;
import com.example.logintoboggan.statemachine.GameStateMachine;
import com.mobage.global.android.data.User;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GameFragment extends BaseFragment
{
	private TextView mHeadingText;
	private TextView mMessageText;
	private Button mUpgradeButton;
	private Button mLogoutButton;

	private LoginToboggan theApp;
	private GameStateMachine<AppState, LoginTobogganGameStateResult> mGameStateMachine;

	private View baseView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		baseView = inflater.inflate(R.layout.fragment_game, container, false);

		mHeadingText = (TextView) baseView.findViewById(R.id.gameHeadingText);
		mMessageText = (TextView) baseView.findViewById(R.id.gameMessageText);
		mUpgradeButton = (Button) baseView.findViewById(R.id.fbUpgradeButton);
		mLogoutButton = (Button) baseView.findViewById(R.id.logoutButton);

		theApp = LoginToboggan.getInstance();
		mGameStateMachine = theApp.getGameStateMachine();

		mUpgradeButton.setOnClickListener(facebookUpgradeButtonClickListener);
		mLogoutButton.setOnClickListener(logoutButtonClickListener);

		return baseView;
	}

	public void updateButtons()
	{
		mUpgradeButton.setVisibility(View.GONE);
		
		theApp.getMobageHelper().getUserInfo(new GetUserInfoCallback(){

			@Override
			public void onComplete(User user)
			{
				// TODO: fix magic result code
				// grade == 1 means guest user
				if (user.getGrade() == 1)
				{
					mUpgradeButton.setVisibility(View.VISIBLE);
				}
				else
				{
					mUpgradeButton.setVisibility(View.GONE);
				}				
			}	
		});	
	}

	public void onResume()
	{
		super.onResume();
		updateButtons();
	}

	public OnClickListener facebookUpgradeButtonClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			mGameStateMachine.moveTo(AppState.FacebookUpgrade);
		}
	};

	public OnClickListener logoutButtonClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			mGameStateMachine.moveTo(AppState.Logout);
		}
	};
}
