package edu.ufp.inf.sd.rmi.Project.client.engine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import edu.ufp.inf.sd.rmi.Project.project_rabbit.Observer;
import org.json.*;

/**
 * Keyboard handling for the game along with the mouse setup for game handling.
 * Menus are being moved to edu.ufp.inf.sd.rmi.Project.client.gui.gms
 * @author SergeDavid
 * @version 0.1
 */
@SuppressWarnings("unused")
public class InputHandler implements KeyListener,MouseListener,ActionListener {
	
	//Development buttons and the exit game button (escape key)
	private final int dev1 = KeyEvent.VK_NUMPAD1;
	private final int dev2 = KeyEvent.VK_NUMPAD2;
	private final int dev3 = KeyEvent.VK_NUMPAD3;
	private final int dev4 = KeyEvent.VK_NUMPAD4;
	private final int dev5 = KeyEvent.VK_NUMPAD5;
	private final int dev6 = KeyEvent.VK_NUMPAD6;
	private final int dev7 = KeyEvent.VK_NUMPAD7;
	private final int dev8 = KeyEvent.VK_NUMPAD8;
	private final int dev9 = KeyEvent.VK_NUMPAD9;
	private final int exit = KeyEvent.VK_ESCAPE;
	
	//Movement buttons
	private final int up = KeyEvent.VK_UP;
	private final int down = KeyEvent.VK_DOWN;
	private final int left = KeyEvent.VK_LEFT;
	private final int right = KeyEvent.VK_RIGHT;

	//Command buttons
	private final int select = KeyEvent.VK_Z;
	private final int cancel = KeyEvent.VK_X;
	private final int start = KeyEvent.VK_ENTER;
	
	//Mouse (right/left clicks)
	private final int main = MouseEvent.BUTTON1;
	private final int alt = MouseEvent.BUTTON1;
	public Observer observer; // Add a member variable to store the observer instance

	public InputHandler(Observer observer) {
		Game.gui.addKeyListener(this);
		Game.gui.addMouseListener(this);
		this.observer = observer; // Store the observer instance
	}

	int DevPathing = 1;
	public void keyPressed(KeyEvent e) {
		int i=e.getKeyCode();
		if (i==exit) {System.exit(0);}
		try {
			if (Game.GameState==Game.State.PLAYING) {
				//edu.ufp.inf.sd.rmi.Project.client.players.Base ply = Game.player.get(Game.btl.currentplayer);

				JSONObject json = new JSONObject();
				json.put("operation","MovePlayer");
				System.out.println("MovePlayer sent!");
				json.put("lobby",Game.observer.donoLobby);
				//System.out.println("Dono lobby " + Game.observer.donoLobby);
				json.put("user", Game.username);

				if (i == up) {
					String message = Game.username + ";" + "up";
					System.out.println(" Sent '" + message + "'");
					json.put("move","up");		// "up" received in Game.movePlayers() and moves player
					Game.observer.sendMessage(json.toString());
					//Game.observer.sendMessage(message);
					//observer.sendMessage(message);
					//observer.channelToRabbitMq.basicPublish("",observer.exchangeName, null, message.getBytes("UTF-8"));
				} else if (i == down) {
					String message = Game.username + ";" + "down";
					System.out.println(" Sent '" + message + "'");
					json.put("move","down");
					Game.observer.sendMessage(json.toString());
					//Game.observer.sendMessage(message);
					//observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
				} else if (i == left) {
					String message = Game.username + ";" + "left";
					System.out.println(" [x] Sent '" + message + "'");
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
				} else if (i == right) {
					String message = Game.username + ";" + "right";
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
					System.out.println(" [x] Sent '" + message + "'");
				} else if (i == select) {
					String message = Game.username + ";" + "select";
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
					System.out.println(" [x] Sent '" + message + "'");
				} else if (i == cancel) {
					String message = Game.username + ";" + "cancel";
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
					System.out.println(" [x] Sent '" + message + "'");
				} else if (i == start) {
					new edu.ufp.inf.sd.rmi.Project.client.menus.Pause(observer);
				}
			}
		} catch (IOException remoteException) {
			remoteException.printStackTrace();
		}

		try {
			if (Game.GameState==Game.State.EDITOR) {
				if (i == up) {
					String message = Game.username + ";" + "up";
					System.out.println(" [x] Sent '" + message + "'");
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
				} else if (i == down) {
					String message = Game.username + ";" + "down";
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
					System.out.println(" [x] Sent '" + message + "'");
				} else if (i == left) {
					String message = Game.username + ";" + "left";
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
					System.out.println(" [x] Sent '" + message + "'");
				} else if (i == right) {
					String message = Game.username + ";" + "right";
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
					System.out.println(" [x] Sent '" + message + "'");
				} else if (i == select) {
					String message = Game.username + ";" + "select";
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
					System.out.println(" [x] Sent '" + message + "'");
				} else if (i == cancel) {
					String message = Game.username + ";" + "cancel";
					observer.channelToRabbitMq.basicPublish("", observer.exchangeName, null, message.getBytes("UTF-8"));
					System.out.println(" [x] Sent '" + message + "'");
				} else if (i == start) {
					new edu.ufp.inf.sd.rmi.Project.client.menus.Pause(observer);
				}
			}
		} catch (IOException remoteException) {
			remoteException.printStackTrace();
		}

		if (i==dev1) {Game.gui.LoginScreen();}
		else if (i==dev2) {Game.load.LoadTexturePack("Test");}
		else if (i==dev3) {
			DevPathing++;
			switch (DevPathing) {
				case 1:Game.pathing.ShowCost=false;break;
				case 2:Game.pathing.ShowHits=true;break;
				case 3:Game.pathing.ShowHits=false;Game.pathing.ShowCost=true;DevPathing=0;break;
			}
		}
		else if (i==dev4) {Game.btl.EndTurn();}
		else if (i==dev5) {Game.player.get(Game.btl.currentplayer).npc = !Game.player.get(Game.btl.currentplayer).npc; Game.btl.EndTurn();}
		else if (i==dev6) {new edu.ufp.inf.sd.rmi.Project.client.menus.StartMenu();}
	}

	public void keyReleased(KeyEvent e) {
		int i=e.getKeyCode();
		if (Game.GameState==Game.State.EDITOR) {
			if (i==select) {Game.edit.holding = false;}
		}
	}
	public void keyTyped(KeyEvent arg0) {}
	public void mousePressed() {}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		Game.gui.requestFocusInWindow();
		Object s = e.getSource();
	}
}
