package game.engine.monsters;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	
	private int stealEnergyFrom(Monster target) {
		int stolenAmount;

		if (target.getEnergy() < Constants.SCHEMER_STEAL) {
			stolenAmount = target.getEnergy();
		}
		else {
			stolenAmount = Constants.SCHEMER_STEAL;
		}

		target.alterEnergy(-stolenAmount);
		return stolenAmount;
	}
	
	public void executePowerupEffect(Monster opponentMonster) {
		int totalStolen = 0;

		totalStolen = totalStolen + stealEnergyFrom(opponentMonster);

		for (int i = 0; i < Board.getStationedMonsters().size(); i++) {
			totalStolen = totalStolen + stealEnergyFrom(Board.getStationedMonsters().get(i));
		}

		alterEnergy(totalStolen);
	}
	
	
}
