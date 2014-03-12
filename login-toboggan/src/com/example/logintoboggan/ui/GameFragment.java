package com.example.logintoboggan.ui;

import com.example.logintoboggan.R;
import com.example.logintoboggan.LoginToboggan;
import com.example.logintoboggan.LoginToboggan.AppState;
import com.example.logintoboggan.LoginToboggan.LoginTobogganGameStateResult;
import com.example.logintoboggan.helper.MobageHelper.GetUserInfoCallback;
import com.example.logintoboggan.statemachine.GameStateMachine;
import com.mobage.global.android.data.User;
import com.mobage.global.android.lang.Error;
import com.mobage.global.android.social.common.Service;
import com.mobage.global.android.social.common.Service.IOpenUserProfileCallback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class GameFragment extends BaseFragment
{
	private TextView mHeadingText;
	private TextView mMessageText;
	
	private ImageButton mCommunityButton;
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
		
		mCommunityButton = (ImageButton) baseView.findViewById(R.id.button_mobage_icon);
		mUpgradeButton = (Button) baseView.findViewById(R.id.fbUpgradeButton);
		mLogoutButton = (Button) baseView.findViewById(R.id.logoutButton);

		theApp = LoginToboggan.getInstance();
		mGameStateMachine = theApp.getGameStateMachine();

		mUpgradeButton.setOnClickListener(facebookUpgradeButtonClickListener);
		mLogoutButton.setOnClickListener(logoutButtonClickListener);
		mCommunityButton.setOnClickListener(mobageButtonClickListener);
		
		mUpgradeButton.setVisibility(View.GONE);
		//mCommunityButton.setVisibility(View.GONE);

		return baseView;
	}

	public void updateButtons()
	{
		mUpgradeButton.setVisibility(View.GONE);
		
		theApp.getUser(new GetUserInfoCallback(){
			
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
	
	public OnClickListener mobageButtonClickListener = new OnClickListener()
	{	
		@Override
		public void onClick(View v)
		{
			theApp.showSpinner("Loading user info...");
			
			// get a User object for the current user, as it is required 
			// for the call to Service.openUserProfile()
			theApp.getUser(new GetUserInfoCallback(){
				
				@Override
				public void onComplete(User user)
				{
					theApp.showSpinner("Loading user profile...");

					/*
					 * Right now, as of version 2.5.5, there is not a consistent way to determine
					 * precisely when the Mobage UI appears. The app will get a notification that it 
					 * is visible long before it is actually visible.
					 * 
					 * The work-around is to display a loading spinner and just let the Mobage UI cover 
					 * it up. It this case we want to hide the spinner when Mobage UI disappears, so
					 * we set a callback to happen when that notification is received.
					 */
					theApp.setOnMobageUINotVisibleCallabck(new Runnable()
					{
						@Override
						public void run()
						{
							theApp.hideSpinner();
						}
					});
					
					Service.openUserProfile(theApp.getMainActivity(), user);
				}	
			});
		}
	};


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
