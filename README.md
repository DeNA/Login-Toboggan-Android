Login Toboggan - Android
=================

Includes
--------

- New Mobage 2.5.3 login flow

Dependencies
------------

This repo uses git submodules; please run these commands after cloning the repo:

`$ git submodule init
$ git submodule update`

Architecture Notes
--------------------

This implementation utilizes a state machine to control the flow of the application. While a state machine is certainly not a prerequisite to implementing the login code showcased in this application, it is included in this version to help illustrate each step of the login flow.

Each step is represented by a state class that implements `com.example.logintoboggan.statemachine.GameState`. The three methods in the interface (onEnter, onExit, and onReturn) allow specific actions to occur in response to each event in the 'lifecycle' of a state. The logic that controls the flow of the application through all of these steps is contained in the state classes under `com.example.logintoboggan.state`.

The core of the Facebook login functionality is contained in `com.example.logintoboggan.helper.FacebookHelper`.

The core of the Mobage login functionality is contained in `com.example.logintoboggan.helper.MobageHelper`.
