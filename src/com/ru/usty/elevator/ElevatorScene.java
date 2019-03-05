package com.ru.usty.elevator;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * The base function definitions of this class must stay the same
 * for the test suite and graphics to use.
 * You can add functions and/or change the functionality
 * of the operations at will.
 *
 */

public class ElevatorScene {

	//TO SPEED THINGS UP WHEN TESTING,
	//feel free to change this.  It will be changed during grading
	public static final int VISUALIZATION_WAIT_TIME = 1000;  //milliseconds
	public static final int MAX_PEOPLE_IN_ELEVATOR = 6;

	private int numberOfFloors;
	private int numberOfElevators;
	private int currentFloor[];
	private boolean elevatorFloorButtons[];
	private Semaphore floors[][][];
	private Semaphore availableSpaceInElevator[];
	private Thread elevatorThreads[];
	private ArrayList<Thread> runningThreads = new ArrayList<Thread>();
	
	int personCount[]; //use if you want but
									//throw away and
									//implement differently
									//if it suits you
	ArrayList<Integer> exitedCount = null;
	public static Semaphore exitedCountMutex;

	//Base function: definition must not change
	//Necessary to add your code in this one
	public void restartScene(int numberOfFloors, int numberOfElevators) {
		this.numberOfFloors = numberOfFloors;
		this.numberOfElevators = numberOfElevators;
		//set up floor sephamore
		//floors[destination][out/in]
		this.floors = new Semaphore[numberOfElevators][numberOfFloors][2];
		
		this.elevatorFloorButtons = new boolean[numberOfFloors];
		//threads for each elevator
		this.elevatorThreads = new Thread[numberOfElevators];
		//set the counter of how many people are in the elevator
		availableSpaceInElevator = new Semaphore[numberOfElevators];
		//set the elevator starting floor
		this.currentFloor = new int[numberOfFloors];

		personCount = new int[numberOfFloors];

		if(exitedCount == null) {
			exitedCount = new ArrayList<Integer>();
		}
		else {
			exitedCount.clear();
		}
		for(int i = 0; i < getNumberOfFloors(); i++) {
			this.exitedCount.add(0);
		}
		exitedCountMutex = new Semaphore(1);
		
		//setup for all elevators
		for(int i = 0; i < numberOfElevators;i++) {
			
			//door setup
			for(int j = 0; j < numberOfFloors;j++) {
				this.floors[i][j][0] = new Semaphore(0);
				this.floors[i][j][1] = new Semaphore(0);
			}
			
			//set space in elevators
			availableSpaceInElevator[i] = new Semaphore(MAX_PEOPLE_IN_ELEVATOR);
			//set the starting floor of an elevator to random
			currentFloor[i] = (int)(Math.random() * numberOfFloors - 0);
			//start elevator threads
			this.elevatorThreads[i] = new Thread(new Elevator(this, numberOfFloors, VISUALIZATION_WAIT_TIME, i, currentFloor[i]));
			this.runningThreads.add(this.elevatorThreads[i]);
			this.elevatorThreads[i].start();
		}
	}

	//This returns the door semaphore, either let in door or let out door
	public Semaphore getFloor(int floor, boolean in, int elevator) {
		if(in) {
			return floors[elevator][floor][0];
		}
		else {
			return floors[elevator][floor][1];
		}
	}
	
	public Semaphore getSpace(int elevator) {
		return availableSpaceInElevator[elevator];
	}
	
	//function for a person to push a button inside of the elevator to the destination floor
	//or for a elevator to reset the button
	public void pushElevatorButton(int destination, boolean value) {
		elevatorFloorButtons[destination] = value;
	}
	
	//function for a elevator to check if the buttons inside the elevator have been pushed
	public boolean isElevatorButtonPushed(int destination) {
		return elevatorFloorButtons[destination];
	}
	
	//Base function: definition must not change
	//Necessary to add your code in this one
	public Thread addPerson(int sourceFloor, int destinationFloor) {

		/**
		 * Important to add code here to make a
		 * new thread that runs your person-runnable
		 * 
		 * Also return the Thread object for your person
		 * so that it can be reaped in the testSuite
		 * (you don't have to join() yourself)
		 */
		
		Thread thread = new Thread(new Person(sourceFloor, destinationFloor, numberOfElevators, this));
		thread.start();
		
		//personCount.set(sourceFloor, personCount[sourceFloor] + 1);
		personCount[sourceFloor]++;
		this.runningThreads.add(thread);
		return thread;  //this means that the testSuite will not wait for the threads to finish
	}

	public void setFloorForElevator(int floor, int elevator) {
		System.out.println("Elevator: " + elevator + " is at floor " + floor);
		currentFloor[elevator] = floor;
	}
	
	//call this when person enters elevator
	public void personEnteringElevatorAtFloor(int floor) {
		personCount[floor]--;
	}
	
	//Base function: definition must not change, but add your code
	public int getCurrentFloorForElevator(int elevator) {
		return currentFloor[elevator];
	}

	//Base function: definition must not change, but add your code
	public int getNumberOfPeopleInElevator(int elevator) {
		return MAX_PEOPLE_IN_ELEVATOR - availableSpaceInElevator[elevator].availablePermits();
	}

	//Base function: definition must not change, but add your code
	public int getNumberOfPeopleWaitingAtFloor(int floor) {
		return personCount[floor];
	}

	//Base function: definition must not change, but add your code if needed
	public int getNumberOfFloors() {
		return numberOfFloors;
	}

	//Base function: definition must not change, but add your code if needed
	public void setNumberOfFloors(int numberOfFloors) {
		this.numberOfFloors = numberOfFloors;
	}

	//Base function: definition must not change, but add your code if needed
	public int getNumberOfElevators() {
		return numberOfElevators;
	}

	//Base function: definition must not change, but add your code if needed
	public void setNumberOfElevators(int numberOfElevators) {
		this.numberOfElevators = numberOfElevators;
	}

	//Base function: no need to change unless you choose
	//				 not to "open the doors" sometimes
	//				 even though there are people there
	public boolean isElevatorOpen(int elevator) {

		return isButtonPushedAtFloor(getCurrentFloorForElevator(elevator));
	}
	//Base function: no need to change, just for visualization
	//Feel free to use it though, if it helps
	public boolean isButtonPushedAtFloor(int floor) {

		return (getNumberOfPeopleWaitingAtFloor(floor) > 0);
	}

	//Person threads must call this function to
	//let the system know that they have exited.
	//Person calls it after being let off elevator
	//but before it finishes its run.
	public void personExitsAtFloor(int floor) {
		try {
			
			exitedCountMutex.acquire();
			exitedCount.set(floor, (exitedCount.get(floor) + 1));
			exitedCountMutex.release();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Base function: no need to change, just for visualization
	//Feel free to use it though, if it helps
	public int getExitedCountAtFloor(int floor) {
		if(floor < getNumberOfFloors()) {
			return exitedCount.get(floor);
		}
		else {
			return 0;
		}
	}


}
