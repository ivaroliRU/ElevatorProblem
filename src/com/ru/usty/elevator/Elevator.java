package com.ru.usty.elevator;

import java.util.concurrent.Semaphore;

public class Elevator implements Runnable {

	private ElevatorScene scene;
	private int currentFloor, numberOfFloors, time, elevatorNumber;
	private boolean letPeopleIn, up;
	
	public Elevator(ElevatorScene scene, int numberOfFloors, int time, int elevatorNumber, int floor) {
		this.scene = scene;
		//always start on the first floor - **change this**
		this.currentFloor = floor;
		//begin by letting people in the elevator
		this.letPeopleIn = true;
		this.numberOfFloors = numberOfFloors;
		this.time = time;
		//randomly set the direction of the elevator
		this.up = (Math.random() > 0.5)?true:false;
		this.elevatorNumber = elevatorNumber;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				//if we are letting people in the elevator
				if(letPeopleIn) {
					//open the door
					Semaphore door = scene.getFloor(currentFloor, true, elevatorNumber);
					if(door.availablePermits() <= 0)
						door.release();
					//wait for people to gtfo
					Thread.sleep(time/2 - 1);
					//close the door
					letPeopleIn = false;
					door.acquire();
					//go to next floor 
					currentFloor = getNextFloor();
					scene.setFloorForElevator(currentFloor,elevatorNumber);
				}
				//if we are letting people out
				else {
					Semaphore door = scene.getFloor(currentFloor, false, elevatorNumber);
					scene.pushElevatorButton(currentFloor, false);
					if(door.availablePermits() <= 0)
						door.release();
					//wait for people to gtfo
					Thread.sleep(time/2 - 1);
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
	
	/**
	 * Below is the main logic for the elevator.
	 * If we are going in a direction, continue in that direction until we reach a end of the building or
	 * no one is going in that direction or if there aren't any people waiting in that direction
	*/
	private int getNextFloor() {
		int nextFloor = currentFloor;
		
		if(currentFloor == numberOfFloors-1 || currentFloor == 0) {
			up = !up;
		}
		
		if(peopleWaiting()) {
			//if we are going up then increment floor else decrement
			nextFloor = (up)?currentFloor + 1:currentFloor - 1;
		}
		
		return nextFloor;
	}
	
	//are people waiting for an elevator in the direction we are going in??
	private boolean peopleWaiting() {
		if(up) {
			for(int i = currentFloor+1; i < numberOfFloors; i++) {
				if(scene.getNumberOfPeopleWaitingAtFloor(i) > 0 || scene.isElevatorButtonPushed(i)) {
					return true;
				}
			}
		}
		else {
			for(int i = currentFloor-1; i >= 0; i--) {
				if(scene.getNumberOfPeopleWaitingAtFloor(i) > 0 || scene.isElevatorButtonPushed(i)) {
					return true;
				}
			}
		}
		return false;
	}

}
