package com.ru.usty.elevator;

public class Person implements Runnable {

	private int source, destination, stage, currentElevator, numberOfElevators;
	private ElevatorScene scene;
	
	public Person(int source, int destination, int numberOfElevators, ElevatorScene scene) {
		this.source = source;
		this.destination = destination;
		this.scene = scene;
		this.stage = 0;
		this.numberOfElevators = numberOfElevators;
	}
	
	@Override
	public void run() {
		while(stage <= 1) {
			try {
				switch(stage) {
				case 0:
					for(int i = 0; i < numberOfElevators; i++) {
						//remember to add the distinction for which elevator we are waiting for
						if(scene.getFloor(source, true,i).availablePermits() > 0) {
							//acquire space in the elevator
							scene.getSpace(i).acquire();
							//exit the floor and enter the elevator
							scene.personEnteringElevatorAtFloor(source);
							//push the elevator button to the floor
							scene.pushElevatorButton(destination, true);
							stage++;
							//I am in this elevator
							currentElevator = i;
							break;
						}
					}
					break;
				case 1:
					if(scene.getFloor(destination, false,currentElevator).availablePermits() > 0) {
						scene.getSpace(currentElevator).release();
						scene.personExitsAtFloor(destination);
						stage++;
					}
					break;
				}
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
