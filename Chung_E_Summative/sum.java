/*
Michael Wang and Estelle Chung 
Summative 
Plant Game
Due Friday 19th 2018
*/
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.JOptionPane;
/**
 * 
 * @author EstelleChung & Michael Wang
 *
 */
public class sum{ //main where the menu frame is created
	public static void main(String[] args) {
		MenuFrame frame = new MenuFrame();
		frame.setSize(1366, 768);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}
/**
 * 
 * Class which creates the menuframe during game launch
 *
 */
class MenuFrame extends JFrame implements KeyListener {
	//Declaring Variables
	private String[] str = {"Instructions", "Create Map", "Load", "Quit" }; 
	private int currentSel;
	private Container c;
	private JPanel menuItem;
	public static ArrayList<Tile> mapIn; //static variable used for all classes
	private Image bg;
	private ImageIcon ins;
	private JFileChooser fc;
	private MyMap gameFrame;
	//Constructor
	public MenuFrame() {
		addKeyListener(this);
		setFocusable(true);
		bg = new ImageIcon("menubb.jpg").getImage();
		currentSel = 0;
		ins = new ImageIcon("instructions.jpg");
		menuItem = new JPanel();
		c = getContentPane();
		c.setLayout(new BorderLayout(200, 300));
		c.add(menuItem, BorderLayout.CENTER);
		fc = new JFileChooser();
	}
	//Methods
	public static ArrayList<Tile> getMap() {
		return mapIn;
	}
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(bg, 0, 0, 1366, 800, null); //draws background image
		for (int a = 0; a < str.length; a++) { //text selection
			if (a == currentSel) {
				g.setColor(Color.MAGENTA);
			} else {
				g.setColor(Color.BLACK);
			}
			g.setFont(new Font("Arial", Font.PLAIN, 70));
			g.drawString(str[a], 800 / 2 - 150, 100 + a * 70);
		}
	}
	public void keyPressed(KeyEvent e) { 
		int k = e.getKeyCode();
		if (k == 40) { //down arrow key
			currentSel++;
			if (currentSel >= str.length) {
				currentSel = 0;
			}
		} else if (k == 38) { //up arrow key
			currentSel--;
			if (currentSel < 0) {
				currentSel = str.length - 1;
			}
		}
		if (k == 10) { //make selection
			if (currentSel == 0) { //opens instructions
				JFrame in = new JFrame();
				JLabel iL = new JLabel(ins);
				in.setSize(600,600);
				in.setResizable(false);
				in.setVisible(true);
				in.add(iL);
			} else if (currentSel == 1) { //selection to create a map
				CreateFrame frame2 = new CreateFrame();
				frame2.setSize(700, 700);
				frame2.setResizable(false);
				frame2.setVisible(true);
			} else if (currentSel == 2) { //selection to load a game
				try { //try catch block for file loading
					int returnVal = fc.showOpenDialog(this); 								
					if (returnVal == JFileChooser.APPROVE_OPTION) { 
						File file = fc.getSelectedFile(); // picked name
						openMethod(file);
					}
				} catch (Exception ex) {
					System.out.println(ex);
				}
			} else if (currentSel == 3) { //quits the game
				System.exit(0);
			}
		}
		repaint();
	}
	public void keyReleased(KeyEvent e) { }
	public void keyTyped(KeyEvent e) { }
	public void openMethod(File filePath) { //opens a file
		FileInputStream fileIn = null;
		ObjectInputStream objectIn = null;
		try { //try catch block to real file
			fileIn = new FileInputStream(filePath); //reads from input stream
			objectIn = new ObjectInputStream(fileIn);
			mapIn = (ArrayList<Tile>) objectIn.readObject(); // TODO
			objectIn.close();
			gameFrame = new MyMap();
		} catch (Exception ex) {  //catch errors
			System.out.println(ex);
		}
	}
}
/**
 * 
 *Main Jframe where game is played
 *
 */
class MyMap extends JFrame {
	private Container c;
	private MyPanel gp;
	public MyMap() { 
		super("Game");
		c = getContentPane();
		c.setLayout(new BorderLayout(5, 10));
		gp = new MyPanel();
		gp.setPreferredSize(new Dimension(500, 500));
		c.add(gp, BorderLayout.CENTER);
		this.setVisible(true);
		setSize(600, 700);
		setResizable(false);
	}
}
/** 
 * 
 *JPanel where background is drawn
 *
 */
class BgPanel extends JPanel { //background panel to draw image background
	private Image img;
	public BgPanel() {
		img = new ImageIcon("bg.png").getImage();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, 1366, 768, null);
	}
}
/**
 * 
 * Main panel which runs the game 
 *
 */
class MyPanel extends BgPanel implements KeyListener, ActionListener {
	//Declaring variables
	private Player p;
	private boolean win,freezeAttack;
	private ArrayList<Entity> turnips;
	private BigBoss boss;
	private boolean pause;
	private Timer myTimer;
	//Constructor
	public MyPanel() {
		turnips = new ArrayList<Entity>();
		freezeAttack=false;
		win = false;
		setFocusable(true);
		requestFocusInWindow();
		addKeyListener(this);
		myTimer = new Timer(40, this);
		pause = true;
		for (int num = 0; num < MenuFrame.mapIn.size(); num++) { //iterates through tiles and removes the player and the turnips
			if (MenuFrame.mapIn.get(num).getType() == 6) {
				p = (new Player((int) (MenuFrame.mapIn.get(num).getX()), (int) (MenuFrame.mapIn.get(num).getY())));
				MenuFrame.mapIn.remove(num); //removes player and creates new player
				num--;
			} else if (MenuFrame.mapIn.get(num).getType() == 1) { //removes turnips, creates new arraylist
				turnips.add(new Entity((int) (MenuFrame.mapIn.get(num).getX()), (int) (MenuFrame.mapIn.get(num).getY())));
				MenuFrame.mapIn.remove(num);
				num--;
			}
		}
		boss = new BigBoss(400, 0, 100, 100, p.x); //creates big boss
		myTimer.start(); //start timer
	}
	//Methods
	public void checkIntersect() { //checks the intersection of a player bullet and an object
		Iterator<Bullet> it = p.getBul().iterator(); //uses an iterator to iterate through arraylist
		loop: while (it.hasNext()) { 
			Bullet b = it.next();
			for (int k = 0; k < turnips.size(); k++) { //checks the turnips
				if (b.intersects(turnips.get(k))) { //if bullet intersects turnip
					System.out.println("INTERSECTS TURNIP");
					turnips.get(k).setHealth(); //lowers turnip health
					b.setIntersection(true);
					it.remove(); //removes bullet
					continue loop;
				}
			}
			if (boss.intersects(b)){ //checks the boss
				System.out.println("INTERSECTS BOSS");
				b.setIntersection(true); 
				it.remove();
				boss.isShot();
				continue loop;
			}
			for (int j = 0; j < MenuFrame.mapIn.size(); j++) { //checks platform or is exits screen
				if (b.intersects(MenuFrame.mapIn.get(j)) ||  b.getX() > 700 || b.getX() < 0) {
					System.out.println("INTERSECTS WALL/PLATFORM");
					b.setIntersection(true);
					it.remove();
					continue loop;
				}
			}
			b.setIntersection(false);
		} // end of while loop
	}
	public void checkFreeze(){ //checks for the freeze attack
		for (int i=0;i<turnips.size();i++){
			if (turnips.get(i).intersects(p.getFreezeArea())) { //if a turnip enters the freeze area
				turnips.get(i).getFA().setFrozen(true); //freezes turnip
				System.out.println(i+"is frozen");
			}
		}
		if (boss.intersects(p.getFreezeArea())) { //if the boss enters the freeze area
			System.out.println("Boss is frozen");
			boss.getFA().setFrozen(true);
		}
	}
	// Draws the components
	public void paintComponent(Graphics g) {
		super.paintComponent(g); //calls parent pant
		if (MenuFrame.mapIn != null) { //draws tiles
			for (int i = 0; i < MenuFrame.mapIn.size(); i++) {
				MenuFrame.mapIn.get(i).myDraw(g);
			}
		}
		p.myDraw(g); //draws player
		boolean check = true; 
		if (turnips.size() == 0 && check) { // IF ALL TURNIPS DEAD
			boss.myDraw(g);
			boss.getBossTime().start();
		} else if (turnips.size() == 0) { //draws boss
			boss.myDraw(g);
		}
		for (int i = 0; i < turnips.size(); i++) { //draws turnips
			turnips.get(i).myDraw(g);
		}
		for (int i=0;i<p.getBul().size();i++){ //draws bullets
			if (p.getBul().get(i).getIntersection()==false) {
				p.getBul().get(i).drawB(g);
			}
		}
	}
	public void actionPerformed(ActionEvent e) { //timer calls action performed
		if (e.getSource() == myTimer) {
			checkIntersect();
			if (freezeAttack) //checks freeze attack (keyboard f)
				checkFreeze();	
			if (boss.getFA().getFrozen()) { //if the boss is frozen
				boss.getFA().setFreezeTime(); //adds to counter
			}
			for (int i =0;i<turnips.size();i++){  //checks if turnips are frozen
				if (turnips.get(i).getFA().getFrozen()){
					turnips.get(i).getFA().setFreezeTime();
				}
			}
			if (boss.getFA().getFreezeTime()==100){ //if counter =100, unfreezes the boss
				System.out.println("UNfreeze bosss");
				boss.getFA().resetFreezeTime();
				boss.getFA().setFrozen(false);
			}
			for (int i =0;i<turnips.size();i++){ //unfreezes the turnips
				if (turnips.get(i).getFA().getFreezeTime()==100) {
					System.out.println("UNfreeze turnip");
					turnips.get(i).getFA().resetFreezeTime();
					turnips.get(i).getFA().setFrozen(false);
				}
			}
			p.move(); //moves player
			if (turnips.size() == 0) { //moves boss
				boss.moveBoss();
				boss.changepInt(p.x);
			}
			Iterator<Entity> it = turnips.iterator();
			loop: while (it.hasNext()) {  //iterates and removes turnips if they die
				Entity b = it.next();
				b.setPlayer(p.x, p.y);
				b.move();
				if (b.getHealth()<=0){
					it.remove(); //removes turnip
				}
			}
			if(p.getHealth() < 1){ //IF the player dies
				myTimer.stop();
				BigBoss.getBossTime().stop();
				JOptionPane.showMessageDialog(null,"Oh no you lost!");
			}
			if (boss.getBossHealth()==0){ //If the boss dies (player wins)
				myTimer.stop();
				BigBoss.getBossTime().stop();
				JOptionPane.showMessageDialog(null,"YOU WIN!");
			}
			repaint();
		}
	}
	public void keyTyped(KeyEvent e) {  }
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 32) { //shooting
			p.shoot(true);
			System.out.println("SHOOT");
		}
		if (e.getKeyCode() == 70) {//freezing
			System.out.println("FREEZE");
				freezeAttack=true;
		}
		if (e.getKeyCode() == 82) { //reloading
			p.reload();
			System.out.println("RELOAD");
		}
		if (e.getKeyCode() == 38) { //up
			System.out.println("UP");
			p.setU(true);
			p.setFlyUp(true);
		}
		if (e.getKeyCode() == 37) { //left
			System.out.println("LEFT");
			p.setL(true);
		}
		if (e.getKeyCode() == 39) {//right
			System.out.println("RIGHT");
			p.setR(true);
		}
		if (e.getKeyCode() == 40) {//down
			System.out.println("DOWN");
			p.setD(true);
		}
		if (e.getKeyCode() == 67) { //flying mode
			System.out.println("FLY MODE");
			p.setFly();
		}
		if(e.getKeyCode() == 80){  //Pause all
			if(pause){
				myTimer.stop();
				BigBoss.getBossTime().stop();
				pause = false;
			}else{
				myTimer.start();
				BigBoss.getBossTime().start();
				pause = true;
			}
		}
	}
	public void keyReleased(KeyEvent e) { //key releasing
		if (e.getKeyCode() == 70){
			freezeAttack=false;
		}
		if (e.getKeyCode() == 37) { 
			p.setL(false);
		}
		if (e.getKeyCode() == 39) {
			p.setR(false);
		}
		if (e.getKeyCode() == 38) {
			p.setFlyUp(false);
		}
		if (e.getKeyCode() == 32) {
			p.shoot(false);
		}
		if (e.getKeyCode() == 40) {
			p.setD(false);
		}
	}
}
/**
 * 
 * Player class 
 *
 */
class Player extends Rectangle {
	//Declaring variables 
	private boolean right, left, up, fall,isFlying,down,flyUp,isRight;
	public static Image bullI;
	private double jumpSpeed,fallSpeed;
	private Rectangle freezeArea;
	private HP playerHP;
	private ArrayList<Bullet> buls;
	private Image pFrames[] = new Image[10];
	private int curBul, currentS, fShift,cnt2;
	public static int health,health2; //TODO 
	//constructor
	Player(int x, int y) {
		super(x, y, 40, 40);
		freezeArea= new Rectangle(x-30,y-30,100,100);
		isFlying=false;
		bullI = new ImageIcon("bullet.png").getImage();
		playerHP = new HP();
		buls = new <Bullet>ArrayList();
		fShift = 0; // shifts between frames
		curBul = 10;
		currentS = 0;
		jumpSpeed = 8;
		fallSpeed = .1;
		isRight = false;
		cnt2 = 0;
		health = 25;
		health2 = health;
		for (int i = 0; i < 10; i++) { //populates walking animation
			System.out.println(i + " " + (i + 16));
			pFrames[i] = (SpreadSheet.getI(i + 16));
		}
	}
	//methods
	public void setD(boolean b) {
		down=b;
	}
	public void myDraw(Graphics g) { //drawing player
		g.drawImage(pFrames[currentS], x, y, 40, 40, null);
		g.setColor(new Color(178,255,102));
		g.drawRect(30, 20, 150, 15); //draws health bar outline
		if (health >= 0) {  //draws health bar
			g.fillRect(30, 20, (int)(health * 6), 15);
		}
		for (int i = 1; i <= curBul; i++) { // draw bullets
			g.drawImage(bullI, 40*i,40,20,20,null);
		}
		if(health != health2){ 
			g.drawString("-1", 20,20);
			if(cnt2 == 5){
				health2 = health;
				cnt2 = 0;
			}else{
				cnt2++;
			}
		}
		g.setFont(new Font("Arial", Font.PLAIN, 20)); //displays player health at top
		g.setColor(new Color(76,153,0));
		g.drawString( "" + health, 10, 20);
		playerHP.drawHP(health, x+(width/2), y-10, g); //draws floating HP
	}
	public int getHealth() {
		return health;
	}
	public Rectangle getFreezeArea() { //returns freeze area (100 x 100)
		return freezeArea;
	}
	public void setFreezeArea(int r, int k, int w, int h){
		freezeArea= new Rectangle (r,k,w,h);
	}
	public void setFly(){ //sets the flying
		if (isFlying)
			isFlying=false;
		else
			isFlying=true;
	}
	public void setFlyUp(boolean b){
		flyUp=b;
	}
	public void flip() { //flips the images
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-pFrames[currentS].getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		for (int i = 0; i < pFrames.length; i++) {
			pFrames[i] = op.filter((BufferedImage) pFrames[i], null);
		}
	}
	public int getCurBul() {
		return curBul;
	}
	public void move() { //moves the player
		if (!(right == false && left == false && up == false && fall == false && up == false)) {
			if (currentS == 9)
				fShift = -1;
			else if (currentS == 0)
				fShift = 1;
			currentS += fShift;
			
			if (isFlying){ //is the player flying
				if (right){
					if (isRight) {
						flip();
						isRight=false;
					}
					x+=3;
				}
				if (left) {
					if (!isRight) {
						flip();
						isRight=true;
					}
					x-=3;
				}
				if (flyUp){
					y-=3;
				}
				if (down){
					y+=3;
				}
			}
			else {//if the player is not flying
				if (right) {
					if (isRight) {
						flip();
						isRight = false;
					}
					x += 3;
					if (collide(-6)) {
						x -= 3;
					} else if (!(collide(8)) && !up) {
						fall = true;
					}
				}
			
				if (left) {
					if (isRight == false) {
						flip();
						isRight = true;
					}
					x -= 3;
					if (collide(-6)) {
						x += 3;
					} else if (!(collide(8)) && !up) {
						fall = true;
					}
				}
				if (up) {
					currentS = 0;
					y = y - (int) jumpSpeed;
					jumpSpeed = jumpSpeed - .3;
					if (collide(-10)) {
						y = y + (int) jumpSpeed;
						up = false;
						jumpSpeed = 8;
					}
					if (jumpSpeed <= 0) {
						jumpSpeed = 8;
						up = false;
						fall = true;
					}
				}
				if (fall) {
					currentS = 0;
					y = y + (int) fallSpeed;
					fallSpeed = fallSpeed + .1;
					if (fallSpeed >= 7) {
						fallSpeed = 7;
					}
					if (collide(0)) {
						fall = false;
						y = y - (int) fallSpeed;
						while (collide(0))
							y-= 2;
					}
				}
		}
		} else {
			for (int l = 0; l < MenuFrame.mapIn.size(); l++) {
				if (!(MenuFrame.mapIn.get(l).contains(new Point(x + 25, y + height-11))
						&& MenuFrame.mapIn.get(l).contains(new Point(x + 25 + width - 36, y + height)))) {
					fall = true;
				}
			}
		}
		setFreezeArea(x-30,y-30,100,100); //sets the freeze area	
	}
	public Rectangle getSmallRect(int num) { //returns smaller rectangle
		return new Rectangle(x + 15, y+10, width - 35, height + num - 14);
	}
	public void isShot() {
		health--;
	}
	public boolean collide(int num) { //checks collisions
		for (int i = 0; i < MenuFrame.mapIn.size(); i++) {
			if (this.getSmallRect(num).intersects(MenuFrame.mapIn.get(i)) && (MenuFrame.mapIn.get(i).getType() == 4)) // if																									// river
				return true;
			else if (this.getX() < 3 || this.getY() < 3 || this.getX() > 570 || this.getY() > 647) {
				return true;
			}
		}
		return false;
	}
	public ArrayList<Bullet> getBul() {
		return buls;
	}
	public void setU(boolean d) {
		up = d;
	}
	public void setL(boolean d) {
		left = d;
	}
	public void setR(boolean d) {
		right = d;
	}  
	public void shoot(boolean r) { //player shoots bullets
		if (curBul>0) {
			buls.add(new Bullet(x,y,isRight));
			curBul--; 
		}
	}
	public void reload() { //reloads player bullets
		if (curBul == 0)
			curBul = 10;
	}
}
/**
 * 
 * Bullet class
 *
 */
class Bullet extends Rectangle { 
	//Declare variables
	private boolean wasRight,intersection;
	//Constructor
	public Bullet(int x, int y, boolean wasRight) {
		intersection=false;
		width=5;
		height=5;
		this.x = x + 3;
		this.y = y + 10;
		this.wasRight = wasRight; //was the player facing left or right when bullet was shot?
	}
	//Methods
	public boolean getIntersection() {
		return intersection;
	}
	public void setIntersection(boolean intersection) {
		this.intersection = intersection;
	}
	public void drawB(Graphics g) { //draws the bullet
			g.setColor(Color.blue);
			g.fillOval(x, y, width, height);
			move();
	}
	public void move() { //moves the bullet
		if (wasRight == false)
			x += 10;
		else if (wasRight)
			x -= 10;
	}
}
/**
 * 
 * Class that helps display floating hp
 *
 */
class HP extends Rectangle{
	public void drawHP(int health,int x, int y,Graphics g){ //one method to draw the hp
		g.setFont(new Font("Arial", Font.PLAIN, 15));
		g.setColor(new Color(255,0,127));
		g.drawString( "" + health, x, y);
	}
}
class FreezeAttack{
	private boolean isFrozen;
	private int freezeTime;
	public void resetFreezeTime() {
		freezeTime=0;
	}
	public int getFreezeTime() {
		return freezeTime;
	}
	public void setFreezeTime() {
		freezeTime++;
	}
	public void setFrozen(boolean isFrozen) {
		this.isFrozen = isFrozen;
	}
	public boolean getFrozen(){
		return isFrozen;
	}
	public void myDraw(int x, int y,int w,int h,Graphics g){
		if (isFrozen){
			g.setColor(new Color(51,51,225));
			sn(x+(w/2),y+(h/2),10,6,g,20,1);
		}
	}
	  public void sn(int x,double y,int size,int num,Graphics g,int angle,double height){
			int endx=0,endy=0;
			if (size<2)
				return;
			for (int i=0;i<num;i++){
				endx=(int)(x+size*Math.cos((Math.toRadians((360*i/num)+angle))));
				endy=(int)(y-height*size*Math.sin((Math.toRadians((360*i/num)+angle))));
				g.drawLine(x,(int)y,endx,endy);
				sn(endx,endy,size/3,num,g,angle,height);
			}
		}
}
/**
 * 
 * class of entities for the turnips
 *
 */
class Entity extends Rectangle {
	//Declare variables
	private int currentS, fShift, rShift, health,hitP,count; 
	private boolean walkL, walkR, onP;
	private HP entHP;
	private FreezeAttack fA;
	private SpreadSheet ss;
	private Image[] mySprites;
	private int playerX,playerY; //TODO
	private boolean hit2;
	private boolean aggro;
	//Constructor
	public Entity(int x, int y) {
		this.x=x;
		this.y=y;
		fA= new FreezeAttack();
		hit2=true;
		aggro = false;
		health = 5;
		entHP= new HP();
		setLocation(x, y);
		ss = new SpreadSheet();
		walkL = true;
		walkR = false;
		rShift = 10;
		fShift = 0; // shifts between frames
		width = 30;
		height = 30;
		currentS = 0;
		mySprites = new Image[7];
		count = 0;
		hitP = 1;
		for (int i = 0; i < 7; i++) {
			mySprites[i] = ss.getI(i); //loads images
		}
		flip();
	}

	//methods
	public void setHealth() {
		health--;
	}
	public int getHealth(){
		return health;
	}
	public void myDraw(Graphics g) { //draws the turnips
		g.drawImage(mySprites[currentS], x, y, width, height, null);
		fA.myDraw(x, y,width,height, g);
		entHP.drawHP(health, x+(width/2), y-10, g);
	}
	// Get player x and y coordinates
	public void setPlayer(int x, int y) {
		playerX = x;
		playerY = y;
	}
	public Rectangle getAggro(){
		return new Rectangle(x - 80,y - 10,160,30);
	}
	public FreezeAttack getFA(){
		return fA;
	}
	// Check if on the platform
	public void checkP() {
		onP = false;
		for (int i = 0; i < MenuFrame.mapIn.size(); i++) {
			if (MenuFrame.mapIn.get(i).getType() == 4 && (MenuFrame.mapIn.get(i).contains(new Point(x + 15, y + 33)))
					&& (x < 700 && x > 0)) { // if on platform
				onP = true;
				break;
			}
		}
	}
	public void move() {
		if (!fA.getFrozen()) {
			// Makes sure it is walking on the platform 
			checkP();
			// Checks if the player is nearby 
			if(getAggro().contains(new Point(playerX, playerY))){
				aggro = true;
			}else{
				aggro = false;
			}
			
			// Moves the entity if it is on the platform or if it is chasing the player
			if (onP && !aggro) {
				if (walkL) {
					rShift = -3;
				}
				if (walkR) {
					rShift = 3;
				}
			} else if(onP && aggro){
				if(playerX < x){
					walkR = false;
					walkL = true;
					rShift = -4;
				}else if(playerX > x){
					walkL = false;
					walkR = true;
					rShift = 4;
				}
			} else if(!onP && aggro){
				rShift = 0;
			}else {
				change: {
					if (walkL) {
						walkL = false;
						x += 8;
						walkR = true;
						flip();
						break change;
					}
					if (walkR) {
						walkR = false;
						x -= 8;
						walkL = true;
						flip();
						break change;
					}
				}
			}
			
			// Checks for side collision
			for(int g = 0; g < MenuFrame.mapIn.size(); g++){
				if(MenuFrame.mapIn.get(g).getType() == 4 ){
					if(MenuFrame.mapIn.get(g).intersects(this) ){
						if(walkR && !aggro){
							rShift = -8;
							walkR = false;
							walkL = true;
							flip();
						}else if(walkL && !aggro){
							rShift = 8;
							walkL = false;
							walkR = true;
							flip();
						}else if(walkR && aggro){
							rShift = 0;
						}else if(walkL && aggro){
							rShift = 0;
						}
						
						
					}
				}
			}
			
			x += rShift;
			// Checks collision with the player
			if (this.getRect().contains(new Point(playerX + 20, playerY + 15)) && hit2) {
				System.out.println(playerX + " " + playerY);
				if(hitP == 1){
					Player.health--;
					hitP = 0;
				}else{
					hitP++;
				}
				hit2 = false;
			}
			if (count == 10) {
				hit2 = true;
				count = 0;
			} else {
				count++;
			}
			if (currentS == 6)
				fShift = -1;
			else if (currentS == 0)
				fShift = 1;
			currentS += fShift;
		}
	}

	// Gets the rectangle of the entity 
	public Rectangle getRect() {
		return new Rectangle(x, y, 30, 30);
	}
	public void flip() { //flips the image
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-mySprites[currentS].getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		for (int i = 0; i < mySprites.length; i++) {
			mySprites[i] = op.filter((BufferedImage) mySprites[i], null);
		}
	}
}// end of entity class
/**
 * 
 * Map Creator class
 *
 */
class CreateFrame extends JFrame implements ActionListener, MouseMotionListener { // Create	
	//Delare variables
	private JFileChooser fc;
	private MyButton plant,plat,erase,player;
	private MyButton[] buttons;
	private Container c;
	private JPanel p,p2,p3;
	private JButton save;
	public int picture = 0;
	public boolean hasPlayer = false;
	//Constructor
	public CreateFrame() {
		super("Game");
		c = getContentPane();
		c.setLayout(new BorderLayout(5, 10));
		p = new JPanel();
		p2 = new JPanel();
		p3 = new JPanel();
		p2.setPreferredSize(new Dimension(100, 30));
		c.add(p, BorderLayout.CENTER);
		c.add(p2, BorderLayout.EAST);
		p2.add(p3, BorderLayout.CENTER);
		p.setLayout(new GridLayout(20, 20, 0, 0));
		p.setOpaque(false);
		p3.setLayout(new GridLayout(9, 1, 5, 5));
		erase = new MyButton("Erase");
		erase.addActionListener(this);
		plant = new MyButton("plant");
		plant.setIcon(new ImageIcon("plant.png"));
		plant.addActionListener(this);
		plat = new MyButton("plat");
		plat.setIcon(new ImageIcon("plat.png"));
		plat.addActionListener(this);
		player = new MyButton("player");
		player.setIcon(new ImageIcon("player.png"));
		player.addActionListener(this);
		save = new JButton("Save");
		save.addActionListener(this);
		fc = new JFileChooser();
		buttons = new MyButton[400];
		for (int i = 0; i < 400; i++) {
			buttons[i] = new MyButton();
			buttons[i].addActionListener(this);
			buttons[i].addMouseMotionListener(this);
			p.add(buttons[i]);
			if (i >= 380) {
				buttons[i].notFloor = false;
				buttons[i].setIcon(MyButton.platI);
				buttons[i].iconType = 4;
			}
		}
		p3.add(plant);
		p3.add(plat);
		p3.add(player);
		p3.add(erase);
		p3.add(save);
	}
	//Methods
	public void actionPerformed(ActionEvent evt) {
		// Save method and pathfinder 
		if ((evt.getActionCommand()).equals("Save")) {
			int[][] arr = new int[20][20];
			int cnt = 0;
			int pX = 0;
			int pY = 0;
			boolean hasP = false;
			boolean path = true;
			boolean onPlat = true;
			boolean pPlat = true;
			ArrayList<Integer> arr2 = new ArrayList<Integer>();
			
			// Checks if the monsters are on the platform and stores them into an array
			for(int r = 0; r < 20; r++){
				for(int y = 0; y < 20; y++){
					if(buttons[cnt].iconType == 4){
						arr[r][y] = 1;
					}else if(buttons[cnt].iconType == 6){
						if(buttons[cnt + 20].iconType != 4){
							pPlat = false;
							break;
						}
						arr[r][y] = 6;
						pX = r;
						pY = y;
						hasP = true;
					}else if(buttons[cnt].iconType == 1){
						if(buttons[cnt+20].iconType != 4){
							onPlat = false;
							break;
						}
						arr2.add(r);
						arr2.add(y);
						arr[r][y] = -1;
					}else{
						arr[r][y] = -1;
					}
					cnt++;
				}
			}
			
			
			
			// Checks if monsters are on the platform and if the player is on the screen
			if(onPlat){
				if(pPlat) {
				if(hasP){
					
					// Checks if there is a possible path for the entity 
					int zz = arr2.size()/2;
					for(int e = 0; e < zz; e++){
						cnt = 0;
						int[][] arr3 = arr;
						if(pathFind(arr3, pX, pY, arr2.get(0), arr2.get(1), 0) == 0){
							arr2.remove(0);
							arr2.remove(0);
							path = false;
							break;
						}else{
							arr2.remove(0);
							arr2.remove(0);
							for(int r = 0; r < 20; r++){
								for(int y = 0; y < 20; y++){
									if(buttons[cnt].iconType == 4){
										arr[r][y] = 1;
									}else if(buttons[cnt].iconType == 6){
										arr[r][y] = 6;
										pX = r;
										pY = y;
										hasP = true;
									}else{
										arr[r][y] = -1;
									}
									cnt++;
								}
							}
						}
					}
					// Saves if all the requirements are met 
					if(path){
						try {
							int returnVal = fc.showSaveDialog(this);
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								File file = fc.getSelectedFile();
								saveMethod(file);
							}
						} catch (Exception ex) {
							System.out.print("Error" + ex);
						}
					}else{
						JOptionPane.showMessageDialog(null,"No path to monster.");
					}
				}else{
					JOptionPane.showMessageDialog(null,"Please put a player on the screen.");
				}
			}
				else{
					JOptionPane.showMessageDialog(null,"Please put a player on the a platform.");
				}
					
			}else{
				JOptionPane.showMessageDialog(null,"Make sure the plants are on top of a platform.");
			}
		} else {
			// Buttons needed to create the game
			MyButton b = (MyButton) evt.getSource();
			if ((b.getActionCommand()).equals("plant")) {
				picture = 1;
			} else if ((b.getActionCommand()).equals("plat")) {
				picture = 4;
			} else if ((b.getActionCommand()).equals("Erase")) {
				picture = 5;
			} else if ((b.getActionCommand()).equals("player")) {
				picture = 6;
			} else if (b.iconType != -1 && picture == 5 && b.notFloor) {
				if (b.iconType == 6) {
					hasPlayer = false;
				}
				b.iconType = -1;
				b.setIcon(null);
				b.setBorderPainted(true);
			} else if (b.iconType == -1 && picture != 5) {
				if (picture == 1) {
					b.setIcon(MyButton.plantI);
					b.setBorderPainted(false);
				} else if (picture == 4) {
					b.setIcon(MyButton.platI);
					b.setBorderPainted(false);
				} else if (picture == 6 && !(hasPlayer)) {
					b.setIcon(MyButton.playerI);
					b.setBorderPainted(false);
					hasPlayer = true;
				}
				b.iconType = picture;
			}
		}
	}
	public void mouseMoved(MouseEvent e) {
	}
	public void mouseDragged(MouseEvent e) {
		int mAtX = e.getXOnScreen(); // mouse X and Y relative to screen
		int mAtY = e.getYOnScreen();
		int w = buttons[0].getWidth();
		// width and height of first button which is the same for every button
		int h = buttons[0].getHeight();
		for (int i = 0; i < buttons.length; i++) {
			int btnX = buttons[i].getLocationOnScreen().x; // x and y of the
															// button, relative
															// to Screen
			int btnY = buttons[i].getLocationOnScreen().y;
			if (mAtX >= btnX && mAtX <= btnX + w && mAtY >= btnY && mAtY <= btnY + h) { // Set the button icon
				buttons[i].setContentAreaFilled(false);
				if (buttons[i].iconType == -1 && picture != 5) {
					if (picture == 1) {
						buttons[i].setIcon(MyButton.plantI);
						buttons[i].setBorderPainted(false);
					} else if (picture == 4) {
						buttons[i].setIcon(MyButton.platI);
						buttons[i].setBorderPainted(false);
					} else if (picture == 6 && !(hasPlayer)) {
						buttons[i].setIcon(MyButton.playerI);
						buttons[i].setBorderPainted(false);
						hasPlayer = true;
					}
					buttons[i].iconType = picture;
				} else if (buttons[i].iconType != -1 && picture == 5 && buttons[i].notFloor) { // Erases buttons
					if (buttons[i].iconType == 6) {
						hasPlayer = false;
					}
					buttons[i].iconType = -1;
					buttons[i].setIcon(null);
					buttons[i].setBorderPainted(true);
				}
			}
		}
	}
	// Writes the array list to the file
	public void saveMethod(File filePath) {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		try {
			fout = new FileOutputStream(filePath); // file name from file
													// chooser
			oos = new ObjectOutputStream(fout);
			oos.writeObject(saveButs());
			System.out.println("SAVED");
			fout.close();
			oos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	
	// Saves an array list of tiles for the buttons 
	public ArrayList<Tile> saveButs() {
		ArrayList<Tile> tileArr = new ArrayList<Tile>();
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].getIconType() != -1) {
				tileArr.add(new Tile(buttons[i].getLocation().x, buttons[i].getLocation().y, buttons[i].getIconType()));
				System.out.println(
						buttons[i].getLocation().x + " " + buttons[i].getLocation().y + " " + buttons[i].getIconType());
			}
		}
		return tileArr;
	}
	
	// Path finder method
	public int pathFind(int[][] arr3, int pX, int pY, int eX, int eY, int numT){
		if(pX == eX && pY == eY){
			return 1;
		}else{
			
			// Checks if player can go left, right, up, or down
			if(pX + 1 < 20){
				if(arr3[pX + 1][pY] == -1){
					arr3[pX + 1][pY] = 6;
					numT = numT + pathFind(arr3, pX + 1, pY, eX, eY, numT);
				}
			}
			if(pX - 1 >= 0){
				if(arr3[pX - 1][pY] == -1){
					arr3[pX - 1][pY] = 6;
					numT = numT + pathFind(arr3, pX - 1, pY, eX, eY, numT);
				}
			}
			if(pY + 1 < 20){
				if(arr3[pX][pY + 1] == -1){
					arr3[pX][pY + 1] = 6;
					numT = numT + pathFind(arr3, pX, pY + 1, eX, eY, numT);
				}
			}
			if(pY - 1 >= 0){
				if(arr3[pX][pY - 1] == -1){
					arr3[pX][pY - 1] = 6;
					numT = numT + pathFind(arr3, pX, pY - 1, eX, eY, numT);
				}
			}
			
			// Returns the number of different ways to get to the entity from the player 
			// If the value of numT is 0 that means there is no way to get to the entity 
			return numT;
		}
	}
}
/**
 * 
 * custom button class
 *
 */
class MyButton extends JButton { // Buttons class
	//Declare variables
	public static ImageIcon plantI = new ImageIcon("plant.png");
	public static ImageIcon platI = new ImageIcon("plat.png");
	public static ImageIcon playerI = new ImageIcon("player.png");
	public boolean notFloor = true;
	public int iconType = -1;
	//Constructors
	public MyButton(String s, ImageIcon i) {
		super(s, i);
		this.setContentAreaFilled(false);
	}
	public MyButton() {
		this(null, null);
	}
	public MyButton(String s) {
		this(s, null);
	}
	//Method
	public int getIconType() {
		return iconType;
	}
}
/**
 * 
 * tile class used for saving loading
 *
 */
class Tile extends Rectangle implements Serializable {
	//Delare variables
	private int imgI;
	public static Image platI = new ImageIcon("plat.png").getImage();
	public Tile(int x, int y, int imgI) { //Contructor
		super(x, y, 30, 30);
		this.imgI = imgI;
	}
	//Methods
	public void myDraw(Graphics g) { //draws the tiles
		if (imgI == 4) {
			g.drawImage(platI, x, y, null);
		}
	}
	public int getType() {
		return imgI;
	}
}
/**
 * 
 * Spreadsheet where the animations are loaded from
 *
 */
class SpreadSheet {
	//Delclaring variables
	private BufferedImage sheet;
	private static Image[] images;
	private int index;
	//Constructor
	SpreadSheet() {
		images = new Image[30];
		try {
			sheet = ImageIO.read(new File("spritesheet.png"));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		populate: { //populates the array of images
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if (index == 30)
						break populate;
					images[index] = crop(j, i, 512, 512);
					index++;
				}
			}
		}
	}
	public static Image getI(int n) { //returns images
		System.out.println("add image" + n);
		return images[n];
	}
	public BufferedImage crop(int col, int row, int w, int h) { //crops the images
		return sheet.getSubimage(col * 512, row * 512, w, h);
	}
}
/**
 * 
 * Final boss class
 *
 */
class BigBoss extends Rectangle implements ActionListener {
	//Declaring Variables
	private Image bossIL;
	private Image bossIR;
	private FreezeAttack fA;
	private double bossFallSpeed;
	private static Timer bossTime;
	private boolean ready,go,hit,isRight;
	private int erupt,cycle,pInt,bossHealth;
	private HP bbHP;
	//Constructor
	public BigBoss(int x, int y, int width, int height, int pInt) {
		super(x, y, width, height);
		isRight=true;
		this.x = x;
		bossIL = new ImageIcon("bbl.png").getImage();
		bossIR= new ImageIcon("bbr.png").getImage();
		fA = new FreezeAttack();
		bbHP= new HP();
		this.y = y;
		this.width = width;
		this.height = height;
		this.pInt = pInt;
		bossFallSpeed = 0.1;
		bossHealth = 50;
		setBossTime(new Timer(1000, this));
		// bossTime.start();
		ready = true;
		go = false;
		cycle = 0;
		hit = true;
	}
	//Methods
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == getBossTime() && cycle == 0) {
			System.out.println("1");
			erupt = pInt;
			ready = false;
			cycle++;
			hit = true;
		} else if (e.getSource() == getBossTime() && cycle == 1) {
			System.out.println("2");
			go = true;
			cycle++;
		} else if (cycle == 2) {
			System.out.println("3");
			go = false;
			ready = true;
			cycle = 0;
		}
		
	}
	public void myDraw(Graphics g) {
	
		if (isRight)
			g.drawImage(bossIR, x, y, width, height, null);
		else
			g.drawImage(bossIL, x, y, width, height, null);
		if (ready) {
			g.setColor(new Color(255,255,153,255));
			g.fillRect(pInt, 635, 30, 30);
		} else if (go) {
			g.setColor(new Color(255,255,153,170));
			g.fillRect(erupt, 0, 30, 665);
			if (pInt + 10 < erupt + 30 && pInt + 10 > erupt && hit) {
				Player.health--;
				hit = false;
			}
			
			Rectangle eruption = new Rectangle(erupt,0, 30,665);
			
			for(int d = 0; d < MenuFrame.mapIn.size()-20; d++){
				if(MenuFrame.mapIn.get(d).getType() == 4){
					if(eruption.contains(new Point(MenuFrame.mapIn.get(d).x,y)) || eruption.contains(new Point(MenuFrame.mapIn.get(d).x + 30,y))){
						MenuFrame.mapIn.remove(d);
					}
				}
			}
			
		} else {
			g.setColor(new Color(255,255,153,100));
			g.fillRect(erupt, 635, 30, 30);
		}
		g.setColor(new Color(255,102,102)); 
		g.drawRect(500,20,150,15);
		g.fillRect(500, 20,(int)(bossHealth * 3),15);
		bbHP.drawHP(bossHealth, x+(width/2), y-10, g); 
		fA.myDraw(x, y,width,height, g); //draws snowflake
	}
	public void changepInt(int pInt) {
		this.pInt = pInt;
	}
	public void isShot(){
		bossHealth--;
	}
	public void moveBoss() {
		if (!fA.getFrozen()) {
			y = y + (int) bossFallSpeed;
			bossFallSpeed = bossFallSpeed + .3;
			collide();
			if (y > 527) {
				bossFallSpeed = 0;
				y = 531;
			}
			if (y == 531) {
				if (pInt > x) {
					isRight=true;
					x += 1;
				} else if (pInt + 3 < x) {
					isRight=false;
					x -= 1;
				}
			}
			if (bossFallSpeed > 8) {
				bossFallSpeed = bossFallSpeed - .3;
			}
		}
	}
	public int getBossHealth(){
		return bossHealth;
	}
	public FreezeAttack getFA(){
		return fA;
	}
	public Rectangle getSmallRect() {
		return new Rectangle(x, y, width, height);
	}
	public void collide() {
		for (int i = 0; i < MenuFrame.mapIn.size(); i++) {
			if (this.getSmallRect().intersects(MenuFrame.mapIn.get(i)) && (MenuFrame.mapIn.get(i).getType() == 4)) {
				MenuFrame.mapIn.remove(i);
			}
		}
	}
	public static Timer getBossTime() {
		return bossTime;
	}
	public static void setBossTime(Timer bossTime) {
		BigBoss.bossTime = bossTime;
	}
}