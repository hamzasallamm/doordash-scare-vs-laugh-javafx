package game.engine;


import java.util.ArrayList;
import game.engine.cards.Card;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.Monster;
import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.monsters.Monster;

public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		cards = new ArrayList<Card>();
		setCardsByRarity();
		reloadCards();
	}
	
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	public static ArrayList<Card> getCards() {
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}
	
	private int[] indexToRowCol(int index) {
		int row = index / Constants.BOARD_COLS;
		int col = index % Constants.BOARD_COLS;
		if (row % 2 != 0) {
			col = Constants.BOARD_COLS - 1 - col;
		}
		int[] arr = new int[2];
		arr[0] = row;
		arr[1] = col;
		return arr;

	}
	
	private Cell getCell(int index) {
		int[] arr = indexToRowCol(index);
		return boardCells[arr[0]][arr[1]];
	}

	private void setCell(int index, Cell cell) {
		int[] arr = indexToRowCol(index);
		boardCells[arr[0]][arr[1]] = cell;
	}
	
	public void initializeBoard(ArrayList<Cell> specialCells) {
		ArrayList<DoorCell> doorCells = new ArrayList<DoorCell>();
		ArrayList<ConveyorBelt> conveyorBelts = new ArrayList<ConveyorBelt>();
		ArrayList<ContaminationSock> contaminationSocks = new ArrayList<ContaminationSock>();

		for (int i = 0; i < specialCells.size(); i++) {
			Cell currentCell = specialCells.get(i);

			if (currentCell instanceof DoorCell) {
				doorCells.add((DoorCell) currentCell);
			}
			else if (currentCell instanceof ConveyorBelt) {
				conveyorBelts.add((ConveyorBelt) currentCell);
			}
			else if (currentCell instanceof ContaminationSock) {
				contaminationSocks.add((ContaminationSock) currentCell);
			}
		}

		for (int i = 0; i < Constants.BOARD_SIZE; i++) {
			if (i % 2 == 0) {
				setCell(i, new Cell("Cell"));
			}
			else {
				setCell(i, doorCells.get(i / 2));
			}
		}
		for (int i = 0; i < Constants.CARD_CELL_INDICES.length; i++) {
			setCell(Constants.CARD_CELL_INDICES[i], new CardCell("Card Cell"));
		}

		for (int i = 0; i < Constants.CONVEYOR_CELL_INDICES.length; i++) {
			setCell(Constants.CONVEYOR_CELL_INDICES[i], conveyorBelts.get(i));
		}

		for (int i = 0; i < Constants.SOCK_CELL_INDICES.length; i++) {
			setCell(Constants.SOCK_CELL_INDICES[i], contaminationSocks.get(i));
		}

		for (int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
			if (stationedMonsters != null && i < stationedMonsters.size()) {
				Monster currentMonster = stationedMonsters.get(i);
				currentMonster.setPosition(Constants.MONSTER_CELL_INDICES[i]);
				setCell(Constants.MONSTER_CELL_INDICES[i], new MonsterCell(currentMonster.getName(), currentMonster));
			}
			else {
				setCell(Constants.MONSTER_CELL_INDICES[i], new Cell("Cell"));
			}
		}
		
	}
	
	private void setCardsByRarity() {
		ArrayList<Card> newCards = new ArrayList<Card>();

		for (int i = 0; i < originalCards.size(); i++) {
			Card currentCard = originalCards.get(i);

			for (int j = 0; j < currentCard.getRarity(); j++) {
				newCards.add(currentCard);
			}
		}

		originalCards = newCards;
	}

	public static void reloadCards() {
		cards = new ArrayList<Card>();

		for (int i = 0; i < originalCards.size(); i++) {
			cards.add(originalCards.get(i));
		}

		for (int i = 0; i < cards.size(); i++) {
			int randomIndex = (int) (Math.random() * cards.size());

			Card temp = cards.get(i);
			cards.set(i, cards.get(randomIndex));
			cards.set(randomIndex, temp);
		}
	}

	public static Card drawCard() {
		if (cards.size() == 0) {
			reloadCards();
		}

		Card drawnCard = cards.get(0);
		cards.remove(0);
		return drawnCard;
	}

	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
		int oldPosition = currentMonster.getPosition();

		currentMonster.move(roll);

		Cell landedCell = getCell(currentMonster.getPosition());
		landedCell.onLand(currentMonster, opponentMonster);

		if (currentMonster.getPosition() == opponentMonster.getPosition()) {
			currentMonster.setPosition(oldPosition);
			updateMonsterPositions(currentMonster, opponentMonster);
			throw new InvalidMoveException();
		}

		if (currentMonster.isConfused()) {
			currentMonster.decrementConfusion();
			opponentMonster.decrementConfusion();
		}

		updateMonsterPositions(currentMonster, opponentMonster);
	}

	private void updateMonsterPositions(Monster player, Monster opponent) {
		for (int i = 0; i < Constants.BOARD_SIZE; i++) {
			getCell(i).setMonster(null);
		}

		getCell(player.getPosition()).setMonster(player);
		getCell(opponent.getPosition()).setMonster(opponent);
	}
}
