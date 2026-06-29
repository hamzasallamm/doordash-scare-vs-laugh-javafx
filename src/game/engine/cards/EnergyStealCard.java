package game.engine.cards;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class EnergyStealCard extends Card implements CanisterModifier {
	private int energy;

	public EnergyStealCard(String name, String description, int rarity, int energy) {
		super(name, description, rarity, true);
		this.energy = energy;
	}

	public int getEnergy() {
		return energy;
	}

	public void performAction(Monster player, Monster opponent) {
		int stolenEnergy = 0;

		if (opponent.isShielded()) {
			opponent.setShielded(false);
		}
		else {
			if (opponent.getEnergy() < getEnergy()) {
				stolenEnergy = opponent.getEnergy();
			}
			else {
				stolenEnergy = getEnergy();
			}

			opponent.alterEnergy(-stolenEnergy);
		}

		player.alterEnergy(stolenEnergy);
	}

	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		monster.alterEnergy(canisterValue);
	}
}
