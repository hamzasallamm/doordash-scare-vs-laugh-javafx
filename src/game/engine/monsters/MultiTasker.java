package game.engine.monsters;

import game.engine.Role;

public class MultiTasker extends Monster {
	private int normalSpeedTurns;
	
	public MultiTasker(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.normalSpeedTurns = 0;
	}

	public int getNormalSpeedTurns() {
		return normalSpeedTurns;
	}

	public void setNormalSpeedTurns(int normalSpeedTurns) {
		this.normalSpeedTurns = normalSpeedTurns;
	}

	public void executePowerupEffect(Monster opponentMonster) {
		setNormalSpeedTurns(2);
	}

	public void move(int distance) {
		if (getNormalSpeedTurns() > 0) {
			setPosition(getPosition() + distance);
			setNormalSpeedTurns(getNormalSpeedTurns() - 1);
		}
		else {
			setPosition(getPosition() + distance / 2);
		}
	}
}