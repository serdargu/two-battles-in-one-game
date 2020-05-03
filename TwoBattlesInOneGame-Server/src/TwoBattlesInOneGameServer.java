
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class TwoBattlesInOneGameServer {

	public static void main(String[] args) throws Exception {

        ServerSocket listener = new ServerSocket(8903);
        System.out.println("Server is Running on port 8903..");
      
        try {
        	
            while (true) {
            	
            	Game game = new Game();
                Player playerOne = new Player(listener.accept(), 1, "Player 1");
                Player playerTwo = new Player(listener.accept(), 2, "Player 2");        

                playerOne.setOpponent(playerTwo);
                playerTwo.setOpponent(playerOne);
                
                game.setPlayerOne(playerOne);
                game.setPlayerTwo(playerTwo);
                
                
                game.startPlayingGame();
                
            }
            
        } finally {
        	
            listener.close();
            
        }
               
	}

}

class Game {
	
	private Player playerOne;
	private Player playerTwo;
	
	Game() { 
		
		System.out.println("Game created, waiting for players.."); 
	
	}

	public Player getPlayerOne() {
		return playerOne;
	}
	
	public void setPlayerOne(Player playerOne) {
		this.playerOne = playerOne;
	}
	
	public Player getPlayerTwo() {
		return playerTwo;
	}
	
	public void setPlayerTwo(Player playerTwo) {
		this.playerTwo = playerTwo;
	}

	public void startPlayingGame() {

		
		//The flag that checks whether the player passed the starting point or not.
		boolean hasPassed = false;
		
		//This is for only testing.
		//Deploy the boards automatically so that we can test easily.
		//Will be replaced with the method `deployShips`

		this.playerOne.autoDeployShips();
		//We need to send the board to the player one.
	
		this.playerTwo.autoDeployShips();
		//We need to send the board to the player two.
		
		this.playerOne.printPlayerBoard();

		//Until the game finishes.
		while( !(this.playerOne.isGameFinished() || this.playerTwo.isGameFinished()) ) {
			
			playerOne.output.println("MESSAGE.BOARD_CARD_LOCATIONS:" + BoardGame.printCardCoordinates());
			playerTwo.output.println("MESSAGE.BOARD_CARD_LOCATIONS:" + BoardGame.printCardCoordinates());
			
			hasPassed = BoardGame.play(playerOne);
			
			//If the player one passes the starting point of the board, then call the battleship game.
			if(hasPassed) {
				
				this.playerOne.play();
				
			}
			
			hasPassed = false;
			
			//BoardGame.printBoard();
			
			//If the player two passes the starting point of the board, then call the battleship game.
			hasPassed = BoardGame.play(playerTwo);
			if(hasPassed) {
				
				this.playerTwo.play();
				
			}
				
			hasPassed = false;
			
		}

		if( this.playerOne.isGameFinished() ) {

			playerOne.output.println("MESSAGE.GAME_FINISHES:2");
			playerTwo.output.println("MESSAGE.GAME_FINISHES:2");

		}else{

			playerOne.output.println("MESSAGE.GAME_FINISHES:1");
			playerTwo.output.println("MESSAGE.GAME_FINISHES:1");

		}
		
		
	}
	
}

class Player extends Thread {
	
	private int playerId;
	private String playerName;
	private BattleshipBoard board;
	private PlayerCardDeck playerCardDeck;
	private Player opponent;

	Socket socket;
    BufferedReader input;
    PrintWriter output;
    
    public Player(Socket socket, int id, String name) {
    	
    	System.out.println(name + " connected!");
        this.socket = socket;
        this.playerId = id;
        this.playerName = name;
		this.board = new BattleshipBoard();
		this.playerCardDeck = new PlayerCardDeck();
		this.opponent = null;
		
        try {
        	
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("MESSAGE.WELCOME:Welcome " + name);
            
        } catch (IOException e) {
        	
            System.out.println("Player connection lost: " + e);
            
        }
        
    }
    
	public void printCards() {

		//Get the player's card deck.
		//System.out.println("You have the following cards: ");
		//Will be replaced with the GUI
		//System.out.println("0: " + this.playerCardDeck.getNumberOfSinkPieceCards() + "x SinkPiece Card");
		//System.out.println("1: " + this.playerCardDeck.getNumberOfExtraShootCards() + "x ExtraShoot Card");
		//System.out.println("2: " + this.playerCardDeck.getNumberOfAirstrikeCards() + "x Airstrike Card");
		//System.out.println("3: " + this.playerCardDeck.getNumberOfBadLuckCards() + "x BadLuck Card");
		
		//Send player's card to the thread. This line will look like MESSAGE.PLAYER_CARDS:0|0|0|0
		output.println("MESSAGE.PLAYER_CARDS:" + this.playerCardDeck.getNumberOfSinkPieceCards() + "|" + 
		this.playerCardDeck.getNumberOfExtraShootCards() + "|" + this.playerCardDeck.getNumberOfAirstrikeCards() + "|" 
		+ this.playerCardDeck.getNumberOfBadLuckCards());

	}
	
	//The method that prints the player's board.
	public void printPlayerBoard() {
		
		//System.out.println(this.getPlayerName() + "'s Board");
		this.getOpponent().output.println("MESSAGE.PLAYER_BATTLESHIPBOARD:" + getBoard().printBoard());
		
	}	
	
	//Check the game if it is finished or not.
	//It simply checks the boards of two players, if one of them is fully shooted, then return true.
	public boolean isGameFinished() {
		return this.getBoard().checkGameIfFinishes();
	}
	
	
	//This will use only for the auto deployment.
	public char getRandomDirection() {
		//[v]ertical, [h]orizontal, diagonal [u]pward, diagonal [d]ownward
		char directions[] = {'v', 'h', 'u', 'd'};
		Random rand = new Random();
		return directions[rand.nextInt(4)];			
		
	}
	
	//This will use only for the auto deployment.
	//Also used in the SinkPiece card use method since we need a random x, y there.
	public int getRandomCoordinate() {
		Random rand = new Random();
		return rand.nextInt(10);	
	}
	
	//The method that deploys the ships of players' board automatically.
	public void autoDeployShips() {

		int i;
		int row, col;
		char direction;
						
		for(i=0;i<Carrier.totalShipNumber;i++) {

			//Make a new carrier ship to deploy.
			Ship carrier = makeNewShip(1);
			
			do{
				
				row = getRandomCoordinate();
				col = getRandomCoordinate();
				direction = getRandomDirection();

			//If the random coordinates is not deployable, then loop until it finds a suitable place.
			}while( !board.checkPointsIfDeployable(carrier, row, col, direction) );
			
			board.deployShip(carrier, row, col, direction);

		}
		
		for(i=0;i<Battleship.totalShipNumber;i++) {

			//Make a new battleship ship to deploy.
			Ship battleship = makeNewShip(2);
			
			do{
				
				row = getRandomCoordinate();
				col = getRandomCoordinate();
				direction = getRandomDirection();

			//If the random coordinates is not deployable, then loop until it finds a suitable place.
			}while( !board.checkPointsIfDeployable(battleship, row, col, direction) );
			
			board.deployShip(battleship, row, col, direction);

		}

		for(i=0;i<Cruiser.totalShipNumber;i++) {

			//Make a new cruiser ship to deploy.
			Ship cruiser = makeNewShip(3);

			do{
				
				row = getRandomCoordinate();
				col = getRandomCoordinate();
				direction = getRandomDirection();
				
			//If the random coordinates is not deployable, then loop until it finds a suitable place.	
			}while( !board.checkPointsIfDeployable(cruiser, row, col, direction) );
			
			board.deployShip(cruiser, row, col, direction);

		}

		for(i=0;i<Submarine.totalShipNumber;i++) {

			//Make a new submarine ship to deploy.
			Ship submarine = makeNewShip(4);

			do{
				
				row = getRandomCoordinate();
				col = getRandomCoordinate();
				direction = getRandomDirection();

			//If the random coordinates is not deployable, then loop until it finds a suitable place.	
			}while( !board.checkPointsIfDeployable(submarine, row, col, direction) );

			board.deployShip(submarine, row, col, direction);

		}

		for(i=0;i<Patrolboat.totalShipNumber;i++) {

			//Make a new patrolBoat ship to deploy.
			Ship patrolBoat = makeNewShip(5);

			do{
				
				row = getRandomCoordinate();
				col = getRandomCoordinate();
				direction = getRandomDirection();
				
			//If the random coordinates is not deployable, then loop until it finds a suitable place.
			}while( !board.checkPointsIfDeployable(patrolBoat, row, col, direction) );
			
			board.deployShip(patrolBoat, row, col, direction);

		}

		//this.printPlayerBoard();
		
	}
		
	public void play() {
		
		int choice = 0;
		boolean isMissed;
		
		//Force the player to use the BadLuck card
		if(this.playerCardDeck.getNumberOfBadLuckCards() > 0) {
			
			//System.out.println("Unlucky!!! You have BadLuck card so you will skip this round :(");
			output.println("MESSAGE.CARD_BADLUCK");
			this.useCard(3);
			return;
			
		}
		
		printCards();
		//System.out.print("Do you want to use or shoot? 1-USE 2-CONT: ");
		output.println("MESSAGE.USE_OR_SHOOT:" + (this.playerCardDeck.getNumberOfSinkPieceCards() + this.playerCardDeck.getNumberOfExtraShootCards() + this.playerCardDeck.getNumberOfAirstrikeCards() + this.playerCardDeck.getNumberOfBadLuckCards()) );
		
		Scanner sc = new Scanner(System.in);
		
		//choice = sc.nextInt();
		
        try {
            // Repeatedly get commands from the client and process them.
            while (true) {
                String command = input.readLine();
                if (command.startsWith("MESSAGE.CHOOSE_USE_OR_SHOOT:")) {
                	choice = Integer.parseInt(command.substring(28));
                }
                break;
            }
        } catch (IOException e) {
            System.out.println("Player died: " + e);
        }
		
        //System.out.println(choice);
		//Print the enemy board before shooting or using the cards
		this.getOpponent().printPlayerBoard();
		
		if(choice == 1){
			
			//Prompt user to which card will be used.
			System.out.print("Which one do you want to use: ");
			//output.println("MESSAGE.CHOOSE_CARD_TYPE");
			
			//For now, it is not need to check if the user entered proper card number.
			//We can handle it while using GUI.
			//choice = sc.nextInt();
			
			try {
	            // Repeatedly get commands from the client and process them.
	            while (true) {
	                String command = input.readLine();
	                if (command.startsWith("MESSAGE.USE_CARD:")) {
	                	choice = Integer.parseInt(command.substring(17));
	                }
	                break;
	            }
	        } catch (IOException e) {
	            System.out.println("Player died: " + e);
	        }
			
			//Call use card function to use entered card. 
			useCard(choice);
			
		}else{
			
			do{
				
				isMissed = this.shoot();
				//If the player does not shoot a ship.
				if(isMissed) {
					
					System.out.println("Miss!");
					output.println("MESSAGE.MISS:Miss!");
					
				}
				//If the player shoots a ship.
				else {
					
					System.out.println("Hit!");
					output.println("MESSAGE.HIT:Hit!");
					
				}
				
				this.getOpponent().printPlayerBoard();

			//If the user keep shooting a ship, then give the player shoot right until he miss!
			}while(!isMissed);
			
		}
		
		//Print the enemy board after shooting or using the cards
		//this.getOpponent().printPlayerBoard();

		output.println("MESSAGE.O_TURN");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	//The method that makes the player use his cards.
	public void useCard(int cardType) {
		
		//Get the player's card deck.
		PlayerCardDeck playerCardDeck = this.getPlayerCardDeck();

		playerCardDeck.deleteCard(cardType);
		
		//Get the enemy board from the current player object.
		BattleshipBoard enemyBoard = this.getOpponent().getBoard();
		
		switch(cardType) {
		
		//Use SinkPiece Card
		case 0:
						
			//System.out.println(this.getPlayerName() + " used SinkPiece Card");
					
			int randomXCoordinate, randomYCoordinate;
			
			do{
				
				randomXCoordinate = getRandomCoordinate();
				randomYCoordinate = getRandomCoordinate();
						
			//Find a coordinate until it is not shooted and it has a ship.
			}while(enemyBoard.checkPointIfShooted(randomXCoordinate, randomYCoordinate) || !enemyBoard.checkPointIfDeployed(randomXCoordinate, randomYCoordinate));
			
			//Take the id of the ship piece which is shooted.
			int id = enemyBoard.shootPoint(randomXCoordinate, randomYCoordinate).getId();
			
			output.println("MESSAGE.SINK_PIECE_USED");
			
			this.getOpponent().printPlayerBoard();


			//This will use only for the auto deployment.
			//Also used in the SinkPiece card use method since we need a random x, y there.
			
			break;
			
		//Use ExtraShoot Card
		case 1:
			
			boolean isMissed;
			//System.out.println(this.getPlayerName() + " used ExtraShoot Card");
				
			isMissed = this.shoot();
			if(isMissed) {

				//System.out.println("Miss!");
				output.println("MESSAGE.MISS:Miss!");
			}
			else {

				//System.out.println("Hit!");
				output.println("MESSAGE.HIT:Hit!");

			}

			this.getOpponent().printPlayerBoard();
			//Additional right to shoot.
			do{
				
				isMissed = this.shoot();
				if(isMissed) {

					//System.out.println("Miss!");
					output.println("MESSAGE.MISS:Miss!");
				}
				else {

					//System.out.println("Hit!");
					output.println("MESSAGE.HIT:Hit!");

				}

				this.getOpponent().printPlayerBoard();
				
			}while(!isMissed);
			
			break;
			
		//Use Airstrike Card
		case 2:
			
			//System.out.println(this.getPlayerName() + " used Airstrike Card");
			output.println(this.getPlayerName() + " used Airstrike Card");
			
			boolean isShootable;
			int row = 0, col = 0;
			Scanner sc = new Scanner(System.in);
			
			//This may not need to be used in GUI.
			//Since we cannot click the shooted squares or out of the board.
			do{
				
				//This lines will replace with GUI
				//Get the x coordinate from the current player.
				//System.out.println("Please enter x coordinate: ");
				//row = sc.nextInt();
				
				//Get the y coordinate from the current player.
				//System.out.println("Please enter y coordinate: ");
				//col = sc.nextInt();
					
				output.println("MESSAGE.WAIT_X_Y_COORDINATE");
				//row = sc.nextInt();
				try {
		            // Repeatedly get commands from the client and process them.
		            while (true) {
		                String command = input.readLine();
		                if (command.startsWith("MESSAGE.INPUT_X_Y_COORDINATE:")) {
	                		row = Character.getNumericValue(command.charAt(29));
	                		col = Character.getNumericValue(command.charAt(31));
		                }
		                break;
		            }
		        } catch (IOException e) {
		            System.out.println("Player died: " + e);
		        }

				isShootable = true;
				if(!enemyBoard.checkPointIfOutOfRange(row, col))
					if(!enemyBoard.checkPointIfShooted(row, col)) isShootable = false;

			//Basically checks the point if it is already shooted or the point is out of the range.
			}while(isShootable);

			//Check the points whether they are out of range or not since the player can shoot the point which is on the corner or sides.
			//If it is not, then shoot the point.
			//There is no need to check every point if they are already shooted.
			//We need to check only the entered point which is x and y.
			//Also, no need to be use with GUI.
			if(!enemyBoard.checkPointIfOutOfRange(row-1, col-1)) enemyBoard.shootPoint(row-1, col-1);
			if(!enemyBoard.checkPointIfOutOfRange(row-1, col)) enemyBoard.shootPoint(row-1, col);
			if(!enemyBoard.checkPointIfOutOfRange(row-1, col+1)) enemyBoard.shootPoint(row-1, col+1);
			if(!enemyBoard.checkPointIfOutOfRange(row, col-1)) enemyBoard.shootPoint(row, col-1);
			if(!enemyBoard.checkPointIfOutOfRange(row, col)) enemyBoard.shootPoint(row, col);
			if(!enemyBoard.checkPointIfOutOfRange(row, col+1)) enemyBoard.shootPoint(row, col+1);
			if(!enemyBoard.checkPointIfOutOfRange(row+1, col-1)) enemyBoard.shootPoint(row+1, col-1);
			if(!enemyBoard.checkPointIfOutOfRange(row+1, col)) enemyBoard.shootPoint(row+1, col);
			if(!enemyBoard.checkPointIfOutOfRange(row+1, col+1)) enemyBoard.shootPoint(row+1, col+1);
			
			this.getOpponent().printPlayerBoard();

			break;
			
		//Use BadLuck Card
		case 3:
			
			System.out.println(this.getPlayerName() + " forced to use BadLuck Card");
			break;

		}

	}
	 
	//The method that makes the player shoot the enemy's board.
	public boolean shoot() {
		
		//Get the enemy's board.
		BattleshipBoard enemyBoard = this.getOpponent().getBoard();
		int row = 0, col = 0;
		boolean isShootable;
		
		//Scanner sc = new Scanner(System.in);
		
		//This may not need to be used in GUI.
		//Since we cannot click the shooted squares or out of the board.
		do{
			
			//Get the x coordinate from the current player.
			//System.out.println("Please enter x coordinate: ");
			output.println("MESSAGE.WAIT_X_Y_COORDINATE");
			//row = sc.nextInt();
			try {
	            // Repeatedly get commands from the client and process them.
	            while (true) {
	                String command = input.readLine();
	                if (command.startsWith("MESSAGE.INPUT_X_Y_COORDINATE:")) {
	                	row = Character.getNumericValue(command.charAt(29));
	                	col = Character.getNumericValue(command.charAt(31));
	                }
	                break;
	            }
	        } catch (IOException e) {
	            System.out.println("Player died: " + e);
	        }
						
			//Check the point if it is shootable, it may shooted before or the coordinate is out of the board.
			isShootable = false;
			if(!enemyBoard.checkPointIfOutOfRange(row, col))
				if(!enemyBoard.checkPointIfShooted(row, col)) isShootable = true;
								
		//Basically keep asking coordinates if the entered point is already shooted or out of the range.
		}while(!isShootable);
				
		Ship shootedPiece = enemyBoard.shootPoint(row, col);
		
		//If the shooted point does not have a ship piece.
		if(shootedPiece == null) {
			
			return true;
		
		//Else, that means, the point has a ship piece.
		}else{
			
			return false;
			
		}
		
	}

	//The method that make new ship to use in deployment.
	public Ship makeNewShip(int type) {
		
		switch(type) {
		
			case 1:
				return new Carrier();
				
			case 2:
				return new Battleship();
				
			case 3:
				return new Cruiser();
				
			case 4:
				return new Submarine();
				
			case 5:
				return new Patrolboat();
				
		}
		
		return null;
		
	}
	
    public int getPlayerId() {
		return playerId;
	}

	public String getPlayerName() {
		return playerName;
	}
	
	public BattleshipBoard getBoard() {
		return board;
	}

	public PlayerCardDeck getPlayerCardDeck() {
		return playerCardDeck;
	}

	public void addCard(int cardType) {
		this.playerCardDeck.addCard(cardType);
	}
		
	public Player getOpponent() {
		return opponent;
	}

	public void setOpponent(Player opponent) {
		this.opponent = opponent;
	}
	
}

class BoardSquare {

	//If the player one is on this square.
	private boolean playerOne;
	//If the player two is on this square.
	private boolean playerTwo;
	//If the square has card.
	private boolean hasCard;
	
	//Total number of cards in the board
	private static int numberOfCardInBoard = 4;
	
	//Current square counter.
	private static int counter = 0;
	
	//Total number of square which belongs to board.
	public static int numberOfSquare = 14;
	
	public BoardSquare() {
		
		if(BoardSquare.counter == 0) {
			
			//In the beginning of the game, the players sit on the starting point.
			this.playerOne = true;
			this.playerTwo = true;
			
		}else{
			
			this.playerOne = false;
			this.playerTwo = false;
			
			//This part is in the else since we cannot have a card in the starting point otherwise, it can confuse us!
			
			//If the allocated card number is not sufficient, then allocate cards to rest of the board.			
			if(BoardSquare.numberOfSquare - BoardSquare.counter == BoardSquare.numberOfCardInBoard) {
				
				this.hasCard = true;
				BoardSquare.numberOfCardInBoard--;
				
			}else if(BoardSquare.numberOfCardInBoard > 0){
				
				//Select randomly if the square has card or not.
				Random rand = new Random();
				if(rand.nextBoolean()){
					
					this.hasCard = true;
					BoardSquare.numberOfCardInBoard--;
					
				}else{
					
					this.hasCard = false;
					
				}
				
			}

		}
		
		//Increment the counter so that we can understand how many board square are created so far.
		BoardSquare.counter++;
		
	}	
	
	public boolean isPlayerOne() {
		return playerOne;
	}

	public boolean setPlayerOne(boolean playerOne) {
		this.playerOne = playerOne;
		return this.hasCard;
	}

	public boolean isPlayerTwo() {
		return playerTwo;
	}

	public boolean setPlayerTwo(boolean playerTwo) {
		this.playerTwo = playerTwo;
		return this.hasCard;
	}

	public boolean isHasCard() {
		return hasCard;
	}

}

class BoardGame {
	
	//Store the player's current locations.
	public static int playerOneCurrentLocation = 0;
	public static int playerTwoCurrentLocation = 0;
	
	//The board holds the squares which hold players or cards.
	public static List<BoardSquare> board = new ArrayList<BoardSquare>() {
	    {
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	        add(new BoardSquare());
	    }
	};
	
	public static boolean play(Player player) {
		
		int[] dices = new int[2];
		boolean hasCard = false;

		int newLocation = 0;
		
		dices[0] = rollDice();
		dices[1] = rollDice();
		
		int choice = 0;
		
		Scanner sc = new Scanner(System.in);
		
		//System.out.print(player.getPlayerName() + ", please select one of them: 1: " + dices[0] + " 2: " + dices[1] + ": " );
		player.output.println("MESSAGE.SELECT_DICE:" + dices[0] + "|" + dices[1]);
		//choice = sc.nextInt();
        try {
            // Repeatedly get commands from the client and process them.
            while (true) {
                String command = player.input.readLine();
                if (command.startsWith("MESSAGE.CHOOSE_BOARD_COORDINATE:")) {
                	choice = Integer.parseInt(command.substring(32));
                }
                break;
            }
            
        } catch (IOException e) {
            System.out.println("Player died: " + e);
        }
		

		//No need to check if the user enters 1 or 2 for now! since GUI will not allow the player to enter different number.
		//Since first index of arrays starts with 0.
		choice -= 1;
			
		//If it's player 1's turn.
		if(player.getPlayerId() == 1) {

			board.get(playerOneCurrentLocation).setPlayerOne(false);
			newLocation = playerOneCurrentLocation + dices[choice];
			hasCard = board.get(playerOneCurrentLocation = newLocation % 14).setPlayerOne(true);

		//If it's player 2's turn.
		}else if(player.getPlayerId() == 2) {
			
			board.get(playerTwoCurrentLocation).setPlayerTwo(false);
			newLocation = playerTwoCurrentLocation + dices[choice];
			hasCard = board.get(playerTwoCurrentLocation = newLocation % 14).setPlayerTwo(true);
					
		}
		
        player.output.println("MESSAGE.BOARD_PLAYER_LOCATIONS:" + printPlayerCoordinates());
    	player.getOpponent().output.println("MESSAGE.BOARD_PLAYER_LOCATIONS:" + printPlayerCoordinates());

		//If the position of the board which player sits has card, then get a random card.
		if(hasCard) {
			
			int cardType = getCard(player);
			System.out.println("You got " + cardType + " card!");
			player.output.println("MESSAGE.GOT_CARD:" + cardType);
			
		}else{
			
			player.output.println("MESSAGE.WAIT_OPPONENT");
			
		}
		
		//If the current player sits on the beginning square or passes.
		if(newLocation >= BoardSquare.numberOfSquare) return true;
		else return false;
			
	}

	//The method that prints the board.
	public static String printCardCoordinates() {
		
		String output = "";
		
		for(int i=0; i<BoardSquare.numberOfSquare; i++) {

			if(BoardGame.board.get(i).isHasCard()) output += (i + 1) + "-";			
			
		}
		
		return output;

	}
	
	//The method that prints the board.
	public static String printPlayerCoordinates() {
		
		String output = "";
		
		for(int i=0; i<BoardSquare.numberOfSquare; i++) {

			if(BoardGame.board.get(i).isPlayerOne()) output += (i + 1) + "-";			
			
		}
		
		for(int i=0; i<BoardSquare.numberOfSquare; i++) {

			if(BoardGame.board.get(i).isPlayerTwo()) output += (i + 1) + "-";			
			
		}
		
		return output;
		

	}
	
	//The method that rolls one dice.
	public static int rollDice() {
		
		Random rand = new Random();
		return rand.nextInt(6) + 1;
		
	}	

	//The method that adds the card to the player card deck.
	public static int getCard(Player player) {
		
		int cardType = makeNewCard();
		player.addCard(cardType);
		return cardType;
		
	}
	
	//Method that makes a new card randomly for the player if he needs to take a card
	public static int makeNewCard() {
		
		Random rand = new Random();
		int randomCard = rand.nextInt(10) + 1;
		
		//The chance for getting Bad Luck card is 1/10 according to game rules.
		if(randomCard == 1){
			
			return 3;
		
		//The chance for getting Airstrike card is 2/10 according to game rules.
		}else if(randomCard == 2 || randomCard == 3) {
			
			return 2;
		
		//The chance for getting Extra Shoot card is 3/10 according to game rules.
		}else if(randomCard == 4 || randomCard == 5 || randomCard == 6) {
			
			return 1;
			
			//The chance for getting Sink Piece card is 4/10 according to game rules.
		}else if(randomCard == 7 || randomCard == 8 || randomCard == 9 || randomCard == 10) {
			
			return 0;
			
		}
		
		return 0;
		
	}
	
	
}

class PlayerCardDeck {

	private int numberOfCards[];
	
	public PlayerCardDeck() {
		
		this.numberOfCards = new int[4];
		//Number of Sink Piece cards
		this.numberOfCards[0] = 0;
		
		//Number of Extra Shoot cards.
		this.numberOfCards[1] = 0;
		
		//Number of Airstrike cards.
		this.numberOfCards[2] = 0;
		
		//Number of Bad Luck cards.
		this.numberOfCards[3] = 0;
		
	}

	public int getNumberOfSinkPieceCards() {
		return this.numberOfCards[0] > 0 ? 1 : 0;
	}
	
	public int getNumberOfExtraShootCards() {
		return this.numberOfCards[1] > 0 ? 1 : 0;
	}
	
	public int getNumberOfAirstrikeCards() {
		return this.numberOfCards[2] > 0 ? 1 : 0;
	}
	
	public int getNumberOfBadLuckCards() {
		return this.numberOfCards[3] > 0 ? 1 : 0;
	}
	
	public int addCard(int cardType) {
		return ++this.numberOfCards[cardType];		
	}
	
	public int deleteCard(int cardType) {
		return --this.numberOfCards[cardType];		
	}

}

class BattleshipBoardSquare {

	//It holds the object that square has. If the square does not have a ship, then it will be null pointer.
	private Ship shipPointer;
	
	//The flag that holds the square is shooted or not.
	private boolean isShooted;

	//Override the default constructor.
	public BattleshipBoardSquare() {
		
		//In the beginning, the square has no ship to hold.
		this.shipPointer = null;
		//In the beginning, the square is not shooted yet.		
		this.isShooted = false;
		
	}
	
	public boolean shootShipPiece() {
		
		//The flag holds the
		boolean isSunk = false;
		
		//If there is a ship and it is not shooted already, then make it shooted.
		if(this.shipPointer != null && this.isShooted == false) {
			isSunk = this.shipPointer.shoot();
		}
		
		//Return if the ship is sunk true so that we can know and increment the total sunk ship in the board.
		return isSunk;
		
	}
	
	//Getter method for the ship pointer.
	public Ship getShipPointer() {
		return shipPointer;
	}
	
	//Setter method for the ship pointer.
	public void setShipPointer(Ship shipPointer) {
		this.shipPointer = shipPointer;
	}
	
	//Getter method for the isShooted flag.
	public boolean isShooted() {
		return isShooted;
	}
	
	//Setter method for the isShooted flag.
	public void setShooted(boolean isShooted) {
		this.isShooted = isShooted;
	}
	
}


class BattleshipBoard {

	//This list stores the 10*10 board square classes which hold ship pointers and flag shooted or not.
	private List<List<BattleshipBoardSquare>> board;
	
	//We need to know how many ships in one board so that we can check if all ships are sunk or not.
	private int totalShips;
	
	//We need to store number of sunk ships so that we can check if the game finishes or not.
	private int totalSunkShips;
	
	//Initialize the board with null so that we can distinguish if the square has ship or not.
	//At the beginning, the whole board is 
	private void initializeBoard() {		
		
		this.board = new ArrayList<List<BattleshipBoardSquare>>();
		for(int i=0; i<10; i++){

			this.board.add(new ArrayList<BattleshipBoardSquare>());
			List<BattleshipBoardSquare> boardRow = this.board.get(i);
		
			for(int c=0; c<10; c++) {
				boardRow.add(new BattleshipBoardSquare());
			}
		}				
	}
		
	//Override the default constructor.
	public BattleshipBoard() {
		
		//It is the number that one board has totalShips amount ships.
		this.totalShips = Cruiser.totalShipNumber + Carrier.totalShipNumber + Battleship.totalShipNumber + Submarine.totalShipNumber + Patrolboat.totalShipNumber;
		this.totalSunkShips = 0;
		this.initializeBoard();
		
	}
	
	//The getter method for the board.
	public List<List<BattleshipBoardSquare>> getBoard() {
		return board;
	}

	//The setter method for the board.
	public void setBoard(List<List<BattleshipBoardSquare>> board) {
		this.board = board;
	}
	
	//Basically check the total ship and sunk ship number, if they are equal that means the game finishes.
	public boolean checkGameIfFinishes() {
		return this.totalShips == this.totalSunkShips;
	}
	
	//Deploy ship function
	//For now, we can deploy ships in diagonal upward and downward. However, we can remove them when we use GUI since we may not manage the put ships in diagonal way.
	public void deployShip(Ship ship, int row, int col, char direction) {
		
		switch(direction) {

			//Case that the player deploys in vertical direction.
			case 'v':

				for(int i = 0; i < ship.getSize(); i++) this.board.get(row + i).get(col).setShipPointer(ship);
				break;

			//Case that the player deploys in horizontal direction.
			case 'h':

				for(int j = 0; j < ship.getSize(); j++) this.board.get(row).get(col + j).setShipPointer(ship);
				break;
			
			//Case that the player deploys in diagonal upward direction.
			case 'u':
				
				for(int j = 0; j < ship.getSize(); j++)this.board.get(row + j).get(col + j).setShipPointer(ship);
				break;
			
			//Case that the player deploys in diagonal downward direction.
			case 'd':
				
				for(int j = 0; j < ship.getSize(); j++) this.board.get(row - j).get(col - j).setShipPointer(ship);
				break;

		}
		
	}
	
	
	public Ship shootPoint(int row, int col) {
		
		//Check if the player sunk the ship, then increment the sunk ship total
		if(this.board.get(row).get(col).shootShipPiece()) {
			this.totalSunkShips++;
		}
		this.board.get(row).get(col).setShooted(true);
		return this.board.get(row).get(col).getShipPointer();
	
	}
	
	//Check the given point, if it is out of the board or not.
	//This is not need when we use GUI since we do not show the points which are out of the board.
	public boolean checkPointIfOutOfRange(int row, int col) 
	{
		if(row >= 10 || row < 0 || col >= 10 || col < 0) return true;			
		return false;
	}
	
	//Check the point if it is already shooted. This will be usually used while shooting the point.
	//This is also not need when we use GUI since we can disable click if the point is already shooted.
	//However, we can still use while displaying the board in the GUI...
	public boolean checkPointIfShooted(int row, int col) {	
		
		return this.board.get(row).get(col).isShooted();
		
	}
	
	//This is the method that we check the point if it is already deployed with a ship or not.
	//If so, return true.
	public boolean checkPointIfDeployed(int row, int col) {	
		
		if(this.board.get(row).get(col).getShipPointer() != null) return true;
		return false;
		
	}
	
	//Check if the points is suitable to deploy, if not out of range and not deployed before.
	public boolean checkPointsIfDeployable(Ship ship, int row, int col, char direction) {
		
		switch(direction) {

			case 'v':
	
				for(int i = 0; i < ship.getSize(); i++) {
					
					if(!checkPointIfOutOfRange(row + i, col)) {
						if( checkPointIfDeployed(row + i, col) )
							return false;
					}else return false;				   		
			        	
				}	
				
			break;
	
			case 'h':
	
				for(int j = 0; j < ship.getSize(); j++) {
					
					if(!checkPointIfOutOfRange(row, col + j)) {
						if(checkPointIfDeployed(row, col + j))
							return false;							
					}else return false;
				}
	
			break;
			
			case 'u':
				
				for(int j = 0; j < ship.getSize(); j++) {
					
					if(!checkPointIfOutOfRange(row + j, col + j)) {
						if(checkPointIfDeployed(row + j, col + j))
							return false;
					}else return false;
					
				}
				
			break;
			
			case 'd':
				
				for(int j = 0; j < ship.getSize(); j++) {
					
					if(!checkPointIfOutOfRange(row - j, col - j)) {
						if(checkPointIfDeployed(row - j, col - j))
							return false;
					}else return false;
					
				}
				
			break;
	
		}
		
		return true;
		
	}
	
	//This is the method that we print out the board to the console.
	//This method will be changed when we use GUI.
	public String printBoard() {
		
		String output = "";
		
		for(int i=0; i<10; i++){

			for(int c=0; c<10; c++) {
				
				if(!this.board.get(i).get(c).isShooted()) {
					
					//If the entered point to shoot of the enemy board is shooted and it has a ship, then show the id of the ship. 
					if(this.board.get(i).get(c).getShipPointer() != null) {
						
						output += "1T0";
					
					//Else, there is no ship in the entered point, show 0 for now.
					}else {
						
						output += "0T0";						
						
					}
					
				//If the point is not shooted yet, show X.
				}else {
					
					if(this.board.get(i).get(c).getShipPointer() != null) {
						
						output += "1T1";
					
					//Else, there is no ship in the entered point, show 0 for now.
					}else {
						
						output += "0T1";						
						
					}
					
				}

				if(c != 9) output += "R";
					
			}
			
			output += "O";

		}
		
		return output;
						
	}
		
}

abstract class Ship {

	private static int numberOfShips = 0;
	private int id;
	
	//The size that the ship occupies in the board.
	private int size;
	
	//How many of ship's piece is shooted.
	private int shootedPiece;
	
	//Is sunk or not yet?
	private boolean sunk;

	public Ship() {

		//This assign id to the ships.
		id = ++numberOfShips;
		sunk = false;
		shootedPiece = 0;

	}

	public int getId() {

		return id;

	}

	public int getSize() {

		return size;

	}

	public void setSize(int newSize) {

		size = newSize;

	}

	public boolean isSunk() {

		return sunk;

	}

	//If the size and shooted pieces are equal, that means the ship is sunk.
	private boolean checkSunk() {

		if(size == shootedPiece) sunk = true;
		return sunk;

	}

	public boolean shoot() {

		shootedPiece++;
		return checkSunk();

	}
	
}

class Carrier extends Ship {

	//This is the number that for each player's board there are 1 "Carrier". 
	public static int totalShipNumber = 1;

	//Override the default constructor.
	public Carrier() {
		
		super();
		//The size that the ship occupies in the board.
		setSize(5);

	}
	
}

class Battleship extends Ship {

	//This is the number that for each player's board there are 2 "Battleship". 
	public static int totalShipNumber = 2;

	//Override the default constructor.
	public Battleship() {
		
		super();
		//The size that the ship occupies in the board.
		setSize(4);

	}
	
}

class Cruiser extends Ship {

	//This is the number that for each player's board there are 3 "Cruiser". 
	public static int totalShipNumber = 3;

	//Override the default constructor.
	public Cruiser() {
		
		super();
		//The size that the ship occupies in the board.
		setSize(3);

	}
	
}

class Submarine extends Ship {

	//This is the number that for each player's board there are 4 "Submarine". 
	public static int totalShipNumber = 4;

	//Override the default constructor.
	public Submarine() {
		
		super();
		//The size that the ship occupies in the board.
		setSize(2);

	}

}

class Patrolboat extends Ship {

	//This is the number that for each player's board there are 5 "Patrolboat". 
	public static int totalShipNumber = 5;

	//Override the default constructor.
	public Patrolboat() {

		super();
		//The size that the ship occupies in the board.
		setSize(1);

	}
	
}