package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;

public abstract class Monster implements Comparable<Monster> {
	private String name;
	private String description;
	private Role role;
	private Role originalRole; // For confusion card
	private int energy;
	private int position;
	private boolean frozen;
	private boolean shielded;
	private int confusionTurns;
	
	public Monster(String name, String description, Role originalRole, int energy) {
		super();
		this.name = name;
		this.description = description;
		this.role = originalRole;
		this.originalRole = originalRole; 
		this.energy = energy;
		this.position = 0;
		this.frozen = false;
		this.shielded = false;
		this.confusionTurns = 0;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}

	public Role getOriginalRole() {
		return originalRole;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = Math.max(Constants.MIN_ENERGY, energy);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position % Constants.BOARD_SIZE;
	}
	
	public boolean isFrozen() {
		return frozen;
	}
	
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}
	
	public boolean isShielded() {
		return shielded;
	}
	
	public void setShielded(boolean shielded) {
		this.shielded = shielded;
	}
	
	public int getConfusionTurns() {
		return confusionTurns;
	}
	
	public void setConfusionTurns(int confusionTurns) {
		this.confusionTurns = confusionTurns;
	}

	public int compareTo(Monster other) {
		return this.position - other.position;
	}
	
	public abstract void executePowerupEffect(Monster opponentMonster);
	
	public boolean isConfused() {
		return getConfusionTurns()>0;
	}
	
	public void move(int distance) {
		setPosition(getPosition() + distance);
	}
	
	public final void alterEnergy(int energy) {
		if (isShielded() && energy < 0) {
			setShielded(false);
			return;
		}

		int changedEnergy = energy;

		if (this instanceof Dynamo) {
			changedEnergy = changedEnergy * 2;
		}
		else if (this instanceof MultiTasker) {
			changedEnergy = changedEnergy + Constants.MULTITASKER_BONUS;
		}
		else if (this instanceof Schemer) {
			changedEnergy = changedEnergy + Constants.SCHEMER_STEAL;
		}

		setEnergy(getEnergy() + changedEnergy);
	}
	
	public void decrementConfusion() {
		if (getConfusionTurns() > 0) {
            setConfusionTurns(getConfusionTurns()-1);

            if (getConfusionTurns() == 0) {
                setRole(getOriginalRole());
            }
        }
	}

}