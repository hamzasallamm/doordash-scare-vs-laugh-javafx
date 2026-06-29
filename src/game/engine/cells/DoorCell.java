package game.engine.cells;

import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.Board;
import game.engine.monsters.Monster;
import java.util.ArrayList;

public class DoorCell extends Cell implements CanisterModifier {
	private Role role;
	private int energy;
	private boolean activated;
	
	public DoorCell(String name, Role role, int energy) {
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}
	
	public Role getRole() {
		return role;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean isActivated) {
		this.activated = isActivated;
	}

	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		if (monster.getRole() == getRole()) {
			monster.alterEnergy(canisterValue);
		}
		else {
			monster.alterEnergy(-canisterValue);
		}
	}

	
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);

		if (isActivated()) {
			return;
		}

		int canisterValue = getEnergy();

		int oldEnergy = landingMonster.getEnergy();
		boolean oldShield = landingMonster.isShielded();

		modifyCanisterEnergy(landingMonster, canisterValue);

		if (oldShield && landingMonster.getEnergy() == oldEnergy) {
			return;
		}

		ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();

		for (int i = 0; i < stationedMonsters.size(); i++) {
			Monster currentMonster = stationedMonsters.get(i);

			if (currentMonster.getRole() == landingMonster.getRole()) {
				modifyCanisterEnergy(currentMonster, canisterValue);
			}
		}

		setActivated(true);
	}
}
