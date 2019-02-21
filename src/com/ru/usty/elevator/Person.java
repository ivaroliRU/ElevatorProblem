package com.ru.usty.elevator;

public class Person implements Runnable {

	private int source, destination;
	private int stage;
	private ElevatorScene scene;
	
	public Person(int source, int destination, ElevatorScene scene) {
		this.source = source;
		this.destination = destination;
		this.scene = scene;
		this.stage = 0;
	}
	
	@Override
	public void run() {
		while(stage <= 1) {
			try {
				switch(stage) {
				case 0:
					if(scene.getFloor(source, true).availablePermits() > 0) {
						scene.getSpace().acquire();
						stage++;
					}
					break;
				case 1:
					if(scene.getFloor(destination, false).availablePermits() > 0) {
						scene.getSpace().release();
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
