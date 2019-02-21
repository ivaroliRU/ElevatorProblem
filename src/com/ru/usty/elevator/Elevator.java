package com.ru.usty.elevator;

import java.util.concurrent.Semaphore;

public class Elevator implements Runnable {

	private ElevatorScene scene;
	private int currentFloor;
	private boolean letPeopleIn;
	
	public Elevator(ElevatorScene scene) {
		this.scene = scene;
		this.currentFloor = 0;
		this.letPeopleIn = true;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				//if we are letting people in the elevator
				if(letPeopleIn) {
					//open the door
					Semaphore door = scene.getFloor(currentFloor, true);
					if(door.availablePermits() <= 0)
						door.release();
					//wait for people to gtfo
					Thread.sleep(500);
					//close the door
					letPeopleIn = false;
					door.acquire();
					//go to next floor
					currentFloor = getNextFloor();
					scene.setFloorForElevator(currentFloor);
				}
				//if we are letting people out
				else {
					Semaphore door = scene.getFloor(currentFloor, false);
					if(door.availablePermits() <= 0)
						door.release();
					//wait for people to gtfo
					Thread.sleep(500);
					//close the door
					//and let more people in
					letPeopleIn = true;
					door.acquire();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int getNextFloor() {
		//replace this
		if(currentFloor == 0) {
			return 1;
		}
		else {
			return 0;
		}
	}

}
