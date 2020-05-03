import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.GridLayout;

public class TwoBattlesInOneGameClient extends JFrame {

    private static int PORT = 8903;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    //Frame
    JFrame frame = new JFrame("Two Battles In One Game");
    
    //Content Panel
	JPanel contentPane;
	
	//Dice images
	ArrayList<ImageIcon> diceImages = new ArrayList<ImageIcon>();
	
	//Board image
    Image boardImage = new ImageIcon(this.getClass().getResource("/board.png")).getImage();
    
    //Card image
    Image cardImage = new ImageIcon(this.getClass().getResource("/card.png")).getImage();
    
    //Player images
    Image playerOneImage = new ImageIcon(this.getClass().getResource("/player_one.png")).getImage();
    Image playerTwoImage = new ImageIcon(this.getClass().getResource("/player_two.png")).getImage();

    //Use card image
    Image useCardImage = new ImageIcon(this.getClass().getResource("/use_card.png")).getImage();
    
    //Shoot image
    Image shootImage = new ImageIcon(this.getClass().getResource("/shoot.png")).getImage();
    
    //Card images
    Image cardSinkpieceImage = new ImageIcon(this.getClass().getResource("/card_sinkpiece.png")).getImage();
    Image cardExtrashootImage = new ImageIcon(this.getClass().getResource("/card_extrashoot.png")).getImage();
    Image cardAirstrikeImage = new ImageIcon(this.getClass().getResource("/card_airstrike.png")).getImage();
    
	//Panels
	JPanel notificationPanel;
	JPanel boardPanel;
	JPanel dicePanel;
	JLabel boardLabel;
	
	//Buttons
	JButton firstDiceButton; //Button for first dice
	JButton secondDiceButton; //Button for the second dice
	
	//Card labels
	JLabel firstCardLabel;
    JLabel secondCardLabel;
    JLabel thirdCardLabel;
    JLabel fourthCardLabel;
    
    //Player labels
    JLabel playerOneLabel;
    JLabel playerTwoLabel;
   
    //Notification label
    JLabel notificationLabel;
    
    //Battleship panel
    JPanel battleshipBoardPanel;
    
    //Battleship choice panel
    JPanel battleshipChoicePanel;
    
    //Choice buttons
    JButton useCardButton;
    JButton shootButton;
    
    //Use card panel
    JPanel useCardPanel;
    
    //Card buttons
    JButton sinkpieceCardButton;
    JButton extrashootCardButton;
    JButton airstrikeCardButton;
    
    //Battleship grid buttons
    JButton[][] grid;
    
    int firstDice;
    int secondDice;
    int playerId;

    public static void main(String[] args) throws Exception {
		
		String serverAddress = "163.172.166.210";
    	//String serverAddress = "localhost";
        TwoBattlesInOneGameClient client = new TwoBattlesInOneGameClient(serverAddress);      
        client.play();

	}

    public TwoBattlesInOneGameClient(String serverAddress) throws Exception {
	
		// Setup networking
		socket = new Socket(serverAddress, PORT);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
        
		diceImages.add(new ImageIcon(this.getClass().getResource("/dice_one.png")));
		diceImages.add(new ImageIcon(this.getClass().getResource("/dice_two.png")));
		diceImages.add(new ImageIcon(this.getClass().getResource("/dice_three.png")));
		diceImages.add(new ImageIcon(this.getClass().getResource("/dice_four.png")));
		diceImages.add(new ImageIcon(this.getClass().getResource("/dice_five.png")));
		diceImages.add(new ImageIcon(this.getClass().getResource("/dice_six.png")));

        //Frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(606, 729);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.getContentPane().setLayout(null);
        
        //Battleship Panel
        battleshipBoardPanel = new JPanel();
        battleshipBoardPanel.setBounds(0, 30, 600, 600);
        frame.getContentPane().add(battleshipBoardPanel);
        battleshipBoardPanel.setLayout(new GridLayout(10,10,5,5));
        battleshipBoardPanel.setVisible(false);
        
        grid = new JButton[10][10];
        for(int j = 0; j < 10; j++) {
        	
        	for(int k = 0; k < 10; k++) {
            	
            	grid[j][k] = new JButton();
            	battleshipBoardPanel.add(grid[j][k]);
            	grid[j][k].setBackground(Color.GREEN);
            	grid[j][k].setEnabled(true);
            	grid[j][k].addMouseListener(new MouseListener());
            	
            }	
        	
        }
        
        //Battleship choice panel
        battleshipChoicePanel = new JPanel();
        battleshipChoicePanel.setBounds(0, 630, 600, 70);
        frame.getContentPane().add(battleshipChoicePanel);
        battleshipChoicePanel.setVisible(false);
        
        useCardButton = new JButton();
        useCardButton.setBackground(Color.WHITE);
        useCardButton.addActionListener(new ActionListener() {
        	
        	public void actionPerformed(ActionEvent e) {
        		
        		battleshipChoicePanel.setVisible(false);
        		useCardPanel.setVisible(true);
        		out.println("MESSAGE.CHOOSE_USE_OR_SHOOT:" + 1);
        		
        	}
        	
        });
        battleshipChoicePanel.setLayout(null);
        useCardButton.setBounds(222, 1, 70, 70);
        useCardButton.setIcon(new ImageIcon(useCardImage));
        battleshipChoicePanel.add(useCardButton);
        
        shootButton = new JButton();
        shootButton.setBackground(Color.WHITE);
        shootButton.addActionListener(new ActionListener() {
        	
        	public void actionPerformed(ActionEvent e) {
        		
        		battleshipChoicePanel.setVisible(false);
        		out.println("MESSAGE.CHOOSE_USE_OR_SHOOT:" + 2);
        		
                for(int j = 0; j < 10; j++) {
                	
                	for(int k = 0; k < 10; k++) {
                    	
                    	grid[j][k].setEnabled(true);
                    	
                    }	
                	
                }

        	}
        	
        });
        shootButton.setBounds(302, 1, 70, 70);
        shootButton.setIcon(new ImageIcon(shootImage));
        battleshipChoicePanel.add(shootButton);
        
        //Use Card panel
        useCardPanel = new JPanel();
        useCardPanel.setBounds(0, 630, 600, 70);
        frame.getContentPane().add(useCardPanel);
        useCardPanel.setLayout(null);
        useCardPanel.setVisible(false);
        
        //Use card buttons
        sinkpieceCardButton = new JButton();
        sinkpieceCardButton.setBackground(Color.WHITE);
        sinkpieceCardButton.setBounds(161, 1, 70, 70);
        sinkpieceCardButton.setIcon(new ImageIcon(cardSinkpieceImage));
        sinkpieceCardButton.addActionListener(new ActionListener() {
        	
        	public void actionPerformed(ActionEvent e) {
        		
        		useCardPanel.setVisible(false);
        		out.println("MESSAGE.USE_CARD:" + 0);
        		
        	}
        	
        });
        useCardPanel.add(sinkpieceCardButton);
        
        extrashootCardButton = new JButton();
        extrashootCardButton.setBackground(Color.WHITE);
        extrashootCardButton.setBounds(255, 1, 70, 70);
        extrashootCardButton.setIcon(new ImageIcon(cardExtrashootImage));
        extrashootCardButton.addActionListener(new ActionListener() {
        	
        	public void actionPerformed(ActionEvent e) {
        		
        		useCardPanel.setVisible(false);
        		out.println("MESSAGE.USE_CARD:" + 1);
        		
                for(int j = 0; j < 10; j++) {
                	
                	for(int k = 0; k < 10; k++) {
                    	
                    	grid[j][k].setEnabled(true);
                    	
                    }	
                	
                }
        		
        	}
        	
        });
        useCardPanel.add(extrashootCardButton);
        
        airstrikeCardButton = new JButton();
        airstrikeCardButton.setBackground(Color.WHITE);
        airstrikeCardButton.setBounds(349, 1, 70, 70);
        airstrikeCardButton.setIcon(new ImageIcon(cardAirstrikeImage));
        airstrikeCardButton.addActionListener(new ActionListener() {
        	
        	public void actionPerformed(ActionEvent e) {
        		
        		useCardPanel.setVisible(false);
        		out.println("MESSAGE.USE_CARD:" + 2);
        		
                for(int j = 0; j < 10; j++) {
                	
                	for(int k = 0; k < 10; k++) {
                    	
                    	grid[j][k].setEnabled(true);
                    	
                    }	
                	
                }
                        		
        	}
        	
        });
        useCardPanel.add(airstrikeCardButton);
        
        //Dice panel
        dicePanel = new JPanel();
        dicePanel.setBounds(0, 630, 600, 70);
        frame.getContentPane().add(dicePanel);
        dicePanel.setLayout(null);
        dicePanel.setVisible(false);
        
        //First dice
        firstDiceButton = new JButton();
        firstDiceButton.addActionListener(new ActionListener() {
        	
        	public void actionPerformed(ActionEvent e) {
        		
        		dicePanel.setVisible(false);
        		out.println("MESSAGE.CHOOSE_BOARD_COORDINATE:" + 1);
            	
        	}
        	
        });
        firstDiceButton.setBounds(205, 4, 64, 64);
        dicePanel.add(firstDiceButton);
        
        //Second dice
        secondDiceButton = new JButton();
		secondDiceButton.addActionListener(new ActionListener() {
		 	
			public void actionPerformed(ActionEvent e) {
		 		
				dicePanel.setVisible(false);
				out.println("MESSAGE.CHOOSE_BOARD_COORDINATE:" + 2);
				
		 	}

		 	
		});
		secondDiceButton.setBounds(311, 4, 64, 64);
		dicePanel.add(secondDiceButton);

        //Notification panel
        notificationPanel = new JPanel();
        notificationPanel.setBounds(0, 0, 600, 30);
        frame.getContentPane().add(notificationPanel);
        
        notificationLabel = new JLabel("New label");
        notificationLabel.setFont(new Font("Yu Gothic UI", Font.PLAIN, 14));
        notificationPanel.add(notificationLabel);
                
        //Board panel
        boardPanel = new JPanel();
        boardPanel.setBounds(0, 30, 600, 603);
        frame.getContentPane().add(boardPanel);
        boardPanel.setLayout(null);
        
        playerTwoLabel = new JLabel();
        playerTwoLabel.setBounds(54, 115, 64, 64);
        boardPanel.add(playerTwoLabel);
        playerTwoLabel.setIcon(new ImageIcon(playerTwoImage));
        
        playerOneLabel = new JLabel();
        playerOneLabel.setBounds(10, 115, 64, 64);
        boardPanel.add(playerOneLabel);
        playerOneLabel.setIcon(new ImageIcon(playerOneImage));
        
        //Card labels
        firstCardLabel = new JLabel();
        firstCardLabel.setBounds(242, 64, 115, 115);
        boardPanel.add(firstCardLabel);
        firstCardLabel.setIcon(new ImageIcon(cardImage));
        firstCardLabel.setVisible(false);
        
        secondCardLabel = new JLabel();
        secondCardLabel.setBounds(363, 64, 115, 115);
        boardPanel.add(secondCardLabel);
        secondCardLabel.setIcon(new ImageIcon(cardImage));
        secondCardLabel.setVisible(false);
        
        thirdCardLabel = new JLabel();
        thirdCardLabel.setBounds(123, 64, 115, 115);
        boardPanel.add(thirdCardLabel);
        thirdCardLabel.setIcon(new ImageIcon(cardImage));
        thirdCardLabel.setVisible(false);
        
        fourthCardLabel = new JLabel();
        fourthCardLabel.setBounds(482, 64, 115, 115);
        boardPanel.add(fourthCardLabel);
        fourthCardLabel.setIcon(new ImageIcon(cardImage));
        fourthCardLabel.setVisible(false);
        
        //Board
        boardLabel = new JLabel();
        boardLabel.setBounds(0, 0, 600, 600);
        boardPanel.add(boardLabel);
        boardLabel.setIcon(new ImageIcon(boardImage));

    }

	public void play() throws Exception {
    	
        String response;
        int choice;
        Scanner sc = new Scanner(System.in);
        
        try {
        	while (true) {
        		
        		response = in.readLine();
        		
        		if(response != null) {

        			if(response.startsWith("MESSAGE.O_TURN")) {

                        for(int j = 0; j < 10; j++) {
                        	
                        	for(int k = 0; k < 10; k++) {
                            	
                            	grid[j][k].setEnabled(false);
                            	
                            }	
                        	
                        }
        				
        				try {
        					Thread.sleep(2000);
        				} catch (InterruptedException e1) {
        					// TODO Auto-generated catch block
        					e1.printStackTrace();
        				}
        				
        				notificationLabel.setText("Please wait your opponent!");
                    	battleshipBoardPanel.setVisible(false);
                    	boardPanel.setVisible(true);
                    
                    	
                    	
                    }else if(response.startsWith("MESSAGE.WELCOME:")) {
            			
            			playerId = Integer.parseInt(response.substring(response.length() - 1));

                        if(playerId == 1) {
                        	
                        	notificationLabel.setText("Welcome Player " + playerId + ", you are green!, please wait your opponent!");
                        	
                        }else{
                        	
                        	notificationLabel.setText("Welcome Player " + playerId + ", you are red!");
                        	
                        }

                    }else if(response.startsWith("MESSAGE.SINK_PIECE_USED")) {
            			
            			notificationLabel.setText("Enemy ship's piece was sunk!");

                    }else if(response.startsWith("MESSAGE.BOARD_CARD_LOCATIONS:")) {
                    	                 	
                    	String[] parts = response.split(":");
                    	                    	
                    	String[] cardLocations = parts[1].split("-");
                    	
        				int firstCardCoordinate = Integer.parseInt(cardLocations[0]);
        				int secondCardCoordinate = Integer.parseInt(cardLocations[1]);
        				int thirdCardCoordinate = Integer.parseInt(cardLocations[2]);
        				int fourthCardCoordinate = Integer.parseInt(cardLocations[3]);

        		        setCardCoordinates(firstCardLabel, firstCardCoordinate);
        		        setCardCoordinates(secondCardLabel, secondCardCoordinate);
        		        setCardCoordinates(thirdCardLabel, thirdCardCoordinate);
        		        setCardCoordinates(fourthCardLabel, fourthCardCoordinate);
        				
                    }else if(response.startsWith("MESSAGE.BOARD_PLAYER_LOCATIONS:")) {
                    	
                    	String[] parts = response.split(":");
                    	                    	
                    	String[] playerCoordinates = parts[1].split("-");
                    	
        				int playerOneCoordinate = Integer.parseInt(playerCoordinates[0]);
        				int playerTwoCoordinate = Integer.parseInt(playerCoordinates[1]);

        				setPlayerPosition(playerOneCoordinate, playerTwoCoordinate);
        				
                    }else if (response.startsWith("MESSAGE.PLAYER_CARDS:")) {

                    	int numberOfSinkpieceCards = Character.getNumericValue(response.charAt(21));
                    	int numberOfExtrashootCards = Character.getNumericValue(response.charAt(23));
                    	int numberOfSAirstrikeCards = Character.getNumericValue(response.charAt(25));
                    	
                    	System.out.println(response);
                    	
                    	if(numberOfSinkpieceCards < 1) {
                    		
                    		sinkpieceCardButton.setEnabled(false);
                    		
                    	}else{
                    		
                    		sinkpieceCardButton.setEnabled(true);
                    		
                    	}
                    	
                    	if(numberOfExtrashootCards < 1) {
                    		
                    		extrashootCardButton.setEnabled(false);
                    		
                    	}else{
                    		
                    		extrashootCardButton.setEnabled(true);
                    		
                    	}
						                    	
						if(numberOfSAirstrikeCards < 1) {
							
							airstrikeCardButton.setEnabled(false);
							
						}else{
							
							airstrikeCardButton.setEnabled(true);
							
						}
                    	
                    	
                    	//System.out.println(response);
                    	
                    }else if (response.startsWith("MESSAGE.PLAYER_BATTLESHIPBOARD:")) {
                    	
                    	String[] parts = response.split(":");
                    	
                    	String[] rows = parts[1].split("O");
                    	
                    	System.out.println(response);
                    	
                    	for(int j=0; j < 10; j++) {
                    		
                    		String[] cols = rows[j].split("R");
                    		
                    		for(int k=0; k < 10; k++) {
                    			
                    			int hasShip = Character.getNumericValue(cols[k].charAt(0));
                    			int isShoot = Character.getNumericValue(cols[k].charAt(2));
                    			
                    			if(hasShip == 1 && isShoot == 1) {
                    				
                    				grid[j][k].setBackground(Color.RED);
                    				
                    			}else if(isShoot == 1){
                    				
                    				grid[j][k].setBackground(Color.BLUE);
                    				
                    			}     			
                    			
                    		}                  		
                    		                    		
                    	}
                    	                    	
                    	//System.out.println(response);
                    	
                    	
                    }else if(response.startsWith("MESSAGE.SELECT_DICE:")) {
                    	
                        for(int j = 0; j < 10; j++) {
                        	
                        	for(int k = 0; k < 10; k++) {
                            	
                            	grid[j][k].setEnabled(false);
                            	
                            }	
                        	
                        }
                                            	
                    	dicePanel.setVisible(true);
                    	
                    	firstDice = Character.getNumericValue(response.charAt(20));
                    	secondDice = Character.getNumericValue(response.charAt(22));
                    	
                    	firstDiceButton.setIcon(diceImages.get(firstDice-1));
                    	secondDiceButton.setIcon(diceImages.get(secondDice-1));

                    	notificationLabel.setText("Please select one dice");
                                  	
                    }else if(response.startsWith("MESSAGE.USE_OR_SHOOT:")) {
                    	
                    	battleshipBoardPanel.setVisible(true);
                    	battleshipChoicePanel.setVisible(true);
                    	useCardButton.setEnabled(true);
                    	if( Character.getNumericValue(response.charAt(21)) < 1) {
                    		
                    		System.out.println(Character.getNumericValue(response.charAt(21)));
                    		useCardButton.setEnabled(false);
                    		
                    	}

                    }else if(response.startsWith("MESSAGE.GOT_CARD:")) {
                    	
                    	switch(Integer.parseInt(response.substring(17))) {
                    		
                    		case 0:
                    			
                    			notificationLabel.setText("You got SinkPiece card");
                    			break;
                    			
                    		case 1:
                    			
                    			notificationLabel.setText("You got ExtraShoot card");
                    			break;
                    			
                    		case 2:
                    			
                    			notificationLabel.setText("You got Airstrike card");
                    			break;
                    			
                    		case 3:
                    			
                    			notificationLabel.setText("You got BadLuck card");
                    			break;
                    		
                    	}

                    }else if(response.startsWith("MESSAGE.CHOOSE_CARD_TYPE")) {
                    	
                    	//System.out.println(response);
                    	
                    }else if(response.startsWith("MESSAGE.WAIT_X_Y_COORDINATE")) {
                    	
                    	//System.out.println(response);
                    	                    	
                    }else if(response.startsWith("MESSAGE.CARD_BADLUCK")) {
                    	
                    	notificationLabel.setText("Unlucky!!! You have BadLuck card so you will skip this round :(");
                    	
                    }else if(response.startsWith("MESSAGE.WAIT_OPPONENT")) {
                    	
                    	notificationLabel.setText("Please wait your opponent!");
                    	
                    }else if(response.startsWith("MESSAGE.MISS")){
                    	
                    	notificationLabel.setText("MISS!");
                    	
                    }else if(response.startsWith("MESSAGE.HIT")){
                    	
                    	notificationLabel.setText("HIT!");
                    	
                    }else if(response.startsWith("MESSAGE.GAME_FINISHES:")){
                    	
                    	int playerWonGame = Character.getNumericValue(response.charAt(22));
                    	notificationLabel.setText("Player " + playerWonGame + ", has won the game! GG");
        				try {
        					Thread.sleep(2000);
        				} catch (InterruptedException e1) {
        					// TODO Auto-generated catch block
        					e1.printStackTrace();
        				}
        				frame.dispose();
                    	
                    }else {
                    	
                    	System.out.println(response);
                    	
                    }
        			
        		}
        		
        	}
        }
        finally {
            socket.close();
        }

        
    }
    
    public void setCardCoordinates(JLabel cardLabel, int coordinate) {
    	
    	if(coordinate < 6) {
    		
    		cardLabel.setBounds(123 + (119) * (coordinate - 2), 64, 115, 115);

    	}else if(coordinate < 9) {
    		
    		cardLabel.setBounds(482, 64 + (119) * (coordinate - 5), 115, 115);
    		
    	}else if(coordinate < 13) {
    		
    		cardLabel.setBounds(482 - (119) * (coordinate - 8), 421, 115, 115);
    		
    	}else {
    		
    		cardLabel.setBounds(4, 421 - 119 * (coordinate - 12), 115, 115);
    		
    	}

    	cardLabel.setVisible(true);
        
    }
    

    public void setPlayerPosition(int firstPlayerPosition, int secondPlayerPosition) {

    	System.out.println(firstPlayerPosition + " " + secondPlayerPosition);
    	
		if(firstPlayerPosition < 6) {
    		
			playerOneLabel.setBounds(10 + 119 * (firstPlayerPosition - 1), 115, 64, 64);

    	}else if(firstPlayerPosition < 8) {
    		
    		playerOneLabel.setBounds(480, 115 + 119 * (firstPlayerPosition - 5), 64, 64);
    		
    	}else if(firstPlayerPosition < 13) {
    		
    		playerOneLabel.setBounds(480 - 119 * (firstPlayerPosition - 8), 472, 64, 64);
    		
    	}else {
    		
    		playerOneLabel.setBounds(10, 352 - 119 * (firstPlayerPosition - 13), 64, 64);
    		
    	}

		if(secondPlayerPosition < 6) {
    		
			playerTwoLabel.setBounds(54 + 119 * (secondPlayerPosition - 1), 115, 64, 64);

    	}else if(secondPlayerPosition < 8) {
    		
    		playerTwoLabel.setBounds(533, 115 + 119 * (secondPlayerPosition - 5), 64, 64);
    		
    	}else if(secondPlayerPosition < 13) {
    		
    		playerTwoLabel.setBounds(533 - 119 * (secondPlayerPosition - 8), 472, 64, 64);
    		
    	}else {
    		
    		playerTwoLabel.setBounds(54, 352 - 119 * (secondPlayerPosition - 13), 64, 64);
    		
    	}
    		
	}
    
    public class MouseListener extends MouseAdapter {

    	public void mouseClicked(MouseEvent e) {
    		
    		if(e.getComponent().isEnabled()) {
    			
	    		int coordinateX = (e.getComponent().getX() - 2) / 60;
	    		int coordinateY = (e.getComponent().getY() - 2) / 60;
	
	    		out.println("MESSAGE.INPUT_X_Y_COORDINATE:" + coordinateY + "-" + coordinateX);
	    			
    		}

    	}
    		
    }
 
}

