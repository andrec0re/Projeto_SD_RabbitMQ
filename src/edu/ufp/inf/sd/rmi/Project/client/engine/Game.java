package edu.ufp.inf.sd.rmi.Project.client.engine;

import com.rabbitmq.client.BuiltinExchangeType;
import edu.ufp.inf.sd.rmi.Project.client.ObserverRI;

import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import edu.ufp.inf.sd.rmi.Project.client.menus.MenuHandler;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import edu.ufp.inf.sd.rmi.Project.project_rabbit.Observer;
import edu.ufp.inf.sd.rmi.Project.project_rabbit.ObserverGuiClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class Game extends JFrame {
	private static final long serialVersionUID = 1L;

	//Application Settings
	private static final String build = "0";
	private static final String version = "2";
	public static final String name = "Strategy Game";
	public static int ScreenBase = 32;//Bit size for the screen, 16 / 32 / 64 / 128
	public static boolean dev = true;//Is this a dev copy or not... useless? D:

	public static enum State {STARTUP, MENU, PLAYING, EDITOR, GAMEOVER, INTRUCTIONS};
	public static State GameState = State.STARTUP;

	//Setup the quick access to all of the other class files.
	public static Map map = new Map();
	public static Gui gui;
	public static LoadImages load;
	public static InputHandler input;
	public static Editor edit = new Editor();
	public static Battle btl = new Battle();
	public static ErrorHandler error = new ErrorHandler();
	public static Pathfinding pathing = new Pathfinding();
	public static ListData list;
	public static Save save = new Save();
	public static ComputerBrain brain = new ComputerBrain();
	public static FileFinder finder = new FileFinder();
	public static ViewPoint view = new ViewPoint();

	//Image handling settings are as follows
	public int fps;
	public int fpscount;
	public static Image[] img_menu = new Image[5];
	public static Image img_tile;
	public static Image img_char;
	public static Image img_plys;
	public static Image img_city;
	public static Image img_exts;
	public static Boolean readytopaint;

	//This handles the different edu.ufp.inf.sd.rmi.Project.client.players and also is used to speed logic arrays (contains a list of all characters they own)
	public static List<edu.ufp.inf.sd.rmi.Project.client.players.Base> player = new ArrayList<edu.ufp.inf.sd.rmi.Project.client.players.Base>();
	public static List<edu.ufp.inf.sd.rmi.Project.client.buildings.Base> builds = new ArrayList<edu.ufp.inf.sd.rmi.Project.client.buildings.Base>();
	public static List<edu.ufp.inf.sd.rmi.Project.client.units.Base> units = new ArrayList<edu.ufp.inf.sd.rmi.Project.client.units.Base>();
	//These are the lists that will hold commander, building, and unit data to use in the menu's
	public static List<edu.ufp.inf.sd.rmi.Project.client.players.Base> displayC = new ArrayList<edu.ufp.inf.sd.rmi.Project.client.players.Base>();
	public static List<edu.ufp.inf.sd.rmi.Project.client.buildings.Base> displayB = new ArrayList<edu.ufp.inf.sd.rmi.Project.client.buildings.Base>();
	public static List<edu.ufp.inf.sd.rmi.Project.client.units.Base> displayU = new ArrayList<edu.ufp.inf.sd.rmi.Project.client.units.Base>();

	public static Game g; // self reference
	public static boolean isOnline = false;
	public static int cmd; // commander selected by client

	//public static Channel chan;
	public static String username; // username
	public static Observer observer; // Declare an instance variable for Observer
	public ObserverGuiClient observerGuiClient; // Declare an instance variable for ObserverGuiClient

	public static String workQueueName = UUID.randomUUID().toString();

	/**e definido os estados possiveis para o modo de jogo **/

	private boolean space_has_been_released = false;
	private boolean keyPressed = false;
	private boolean listenInput = true;


	public Game() throws RemoteException{
		super (name);

		//Default Settings of the JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(20*ScreenBase+6,12*ScreenBase+12));
		setBounds(0,0,20*ScreenBase+6,12*ScreenBase+12);
		setUndecorated(false);
		setResizable(false);
		setLocationRelativeTo(null);
		//startObserver(observer);

		//Creates all the edu.ufp.inf.sd.rmi.Project.client.gui elements and sets them up
		gui = new Gui(this);
		add(gui);
		gui.setFocusable(true);
		gui.requestFocusInWindow();

		//load images, initialize the map, and adds the input settings.
		load = new LoadImages();
		map = new Map();
		input = new InputHandler(observer);
		list = new ListData();

		setVisible(true);//This has been moved down here so that when everything is done, it is shown.
		//gui.LoginScreen();
		//save.LoadSettings();
		GameLoop();
	}


	public Game(String map_name, String username, Observer observer) throws RemoteException {
		super (name);

		//Default Settings of the JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(20*ScreenBase+6,12*ScreenBase+12));
		setBounds(0,0,20*ScreenBase+6,12*ScreenBase+12);
		setUndecorated(false);
		setResizable(false);
		setLocationRelativeTo(null);

		//Creates all the gui elements and sets them up
		gui = new Gui(this);
		add(gui);
		gui.setFocusable(true);
		gui.requestFocusInWindow();
		//startObserver(observer);
		this.observer=observer;

		//load images, initialize the map, and adds the input settings.
		load = new LoadImages();
		map = new Map();
		input = new InputHandler(this.observer);
		list = new ListData();

		setVisible(true);//This has been moved down here so that when everything is done, it is shown.
		//gui.LoginScreen();
		//save.LoadSettings();
		this.username = username;
		this.StartGame(map_name);
		GameLoop();
	}


	public void startObserver(Observer observer) {
		try {
			// Provide the necessary parameters for the Observer constructor
			String host = "localhost";
			int port = 5672;
			String user = "guest";
			String pass = "guest";
			String exchangeName = "my_exchange";
			String queueName = "my_queue";
			String queueFrontServer = "front_server_queue";
			BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
			String messageFormat = "UTF-8";

			// Create an instance of the Observer class
			observer = new Observer(observerGuiClient, host, port, user, pass, exchangeName, queueName, queueFrontServer, exchangeType, messageFormat, username);
			System.out.println("Observer criado!");
			this.observer=observer;
		} catch (IOException | TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void GameLoop() {
		boolean loop=true;
		long last = System.nanoTime();
		long lastCPSTime = 0;
		long lastCPSTime2 = 0;
		@SuppressWarnings("unused")
		int logics = 0;
		logics++;
		while (loop) {
			//Used for logic stuff
			@SuppressWarnings("unused")
			long delta = (System.nanoTime() - last) / 1000000;
			delta++;
			last = System.nanoTime();

			//FPS settings
			if (System.currentTimeMillis() - lastCPSTime > 1000) {
				lastCPSTime = System.currentTimeMillis();
				fpscount = fps;
				fps = 0;
				error.ErrorTicker();
				setTitle(name + " v" + build + "." + version + " : FPS " + fpscount);
				if (GameState == State.PLAYING) {
					if (player.get(btl.currentplayer).npc&&!btl.GameOver) {
						brain.ThinkDamnYou(player.get(btl.currentplayer));
					}
				}
			}
			else fps++;
			//Current Logic and frames per second location (capped at 20 I guess?)
			if (System.currentTimeMillis() - lastCPSTime2 > 100) {
				lastCPSTime2 = System.currentTimeMillis();
				logics = 0;
				if (GameState==State.PLAYING || GameState==State.EDITOR) {
					view.MoveView();
				}//This controls the view-point on the map
				if (GameState == State.EDITOR) {
					if (edit.holding && edit.moved) {edit.AssButton();}
				}
				Game.gui.frame++;//This is controlling the current frame of animation.
				if (Game.gui.frame>=12) {Game.gui.frame=0;}
				gui.repaint();
			}
			else logics++;

			//Paints the scene then sleeps for a bit.
			try { Thread.sleep(30);} catch (Exception e) {};
		}
	}


	/*private static void consumeFromServer() {
		try {
			Game.chan.exchangeDeclare(Game.fanoutExchangeName, "fanout");
			String queueName = chan.queueDeclare().getQueue();
			chan.queueBind(queueName, Game.fanoutExchangeName, "");

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String message = new String(delivery.getBody(), "UTF-8");
				System.out.println(" [x] Received '" + message + "'");
				handleState(message);
			};
			chan.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
		} catch (IOException e) {}
	}*/

	public void movePlayers(String state) {
		System.out.println("Entrei Move Players!");
		if (Game.GameState == Game.State.PLAYING) {
			edu.ufp.inf.sd.rmi.Project.client.players.Base ply = Game.player.get(Game.btl.currentplayer);

			JSONObject json = new JSONObject(state);
			String operation = json.getString("lobby");
			//String user = json.getString("user");
			switch (operation) {
				case "up":
					ply.selecty--;
					if (ply.selecty < 0) {
						ply.selecty++;
					}
					break;
				case "down":
					ply.selecty++;
					if (ply.selecty >= Game.map.height) {
						ply.selecty--;
					}
					break;
				case "left":
					ply.selectx--;
					if (ply.selectx < 0) {
						ply.selectx++;
					}
					break;
				case "right":
					ply.selectx++;
					if (ply.selectx >= Game.map.width) {
						ply.selectx--;
					}
					break;
				case "select":
					Game.btl.Action();
					break;
				case "cancel":
					Game.player.get(Game.btl.currentplayer).Cancle();
					break;
				case "start":
					new edu.ufp.inf.sd.rmi.Project.client.menus.Pause(observer);
					break;
				case "endturn":
					MenuHandler.CloseMenu();
					Game.btl.EndTurn();
					break;
				default:
					String[] params = state.split(":");
					Game.btl.Buyunit(Integer.parseInt(params[1]),
							Integer.parseInt(params[2]),
							Integer.parseInt(params[3]));
					MenuHandler.CloseMenu();
			}
		}
	}


	public void StartGame(String map){
		//if(GameState == State.STARTUP){
		boolean[] npc = { false, false, false, false }; //no npcs

		int[] cmds = new int[4];		//4 personagens
		//MenuHandler.CloseMenu();
		Game.btl.NewGame(map);
		Game.btl.AddCommanders(cmds, npc, 100, 50);
		Game.gui.InGameScreen();
		//}
	}

	public void update_players(edu.ufp.inf.sd.rmi.Project.server.State s) {
		if (Game.GameState == Game.State.PLAYING) {
			switch (s.getJogada()) {
				case "DOWN":
					player.get(s.getJogador()).selecty--;
					break;
				case "UP":
					player.get(s.getJogador()).selecty++;
					break;
				case "RIGHT":
					player.get(s.getJogador()).selectx++;
					break;
				case "LEFT":
					player.get(s.getJogador()).selectx--;
					break;
				case "select":
					Game.btl.Action();
					break;
				case "cancel":
					Game.player.get(Game.btl.currentplayer).Cancle();
					break;
				case "start":
					break;
				case "endturn":
					MenuHandler.CloseMenu();
					Game.btl.EndTurn();
					break;
				default:
					MenuHandler.CloseMenu();
			}
		}
	}

	/**Starts a new game when launched.*/
	public static void main(String args[]) throws Exception {
		//new Game();
	}
}
