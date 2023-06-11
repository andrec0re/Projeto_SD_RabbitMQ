package edu.ufp.inf.sd.rmi.Project.client.menus;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import edu.ufp.inf.sd.rmi.Project.client.engine.Game;

/**
 * This menu is for loading already existing maps into the editor.
 * @author SergeDavid
 * @version 0.2
 */
public class LoadMap implements ActionListener {//TODO: implement this
	JList maps_list = new JList();
	DefaultListModel maps_model = new DefaultListModel();
	JButton Return = new JButton("Return");
	JButton Load = new JButton("Load");
	
	public LoadMap() {
		Point size = MenuHandler.PrepMenu(300,320);
		SetBounds(size);
		AddGui();
		AddListeners();
		MapListStuff(size);
	}
	private void SetBounds(Point size) {
		Load.setBounds(size.x+15, size.y+20, 100, 32);
		Return.setBounds(size.x+15, size.y+60, 100, 32);
	}
	private void AddGui() {
		Game.gui.add(Load);
		Game.gui.add(Return);
	}
	private void AddListeners() {
		Return.addActionListener(this);
		Load.addActionListener(this);
	}
	
	private void MapListStuff(Point me) {
		maps_model = Game.finder.GrabMaps();
		JScrollPane maps_pane = new JScrollPane(maps_list = new JList(maps_model));
		maps_pane.setBounds(me.x+140, me.y+10, 140, 300);
		Game.gui.add(maps_pane);
		Dimension size = maps_list.getPreferredSize();
		maps_list.setBounds(4, 2, size.width, size.height);
		maps_list.setSelectedIndex(0);
	}

	@Override public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s==Load) {
			// Get the selected map name from the list
			String selectedMap = (String) maps_list.getSelectedValue();

			if (selectedMap == null) {
				Game.error.ShowError("No map selected");
				return;
			}

			// Load the map - here you might need to add the method that actually loads the map based on its name
			//Game.map = Game.finder.GrabMaps();
			if (Game.map == null) {
				Game.error.ShowError("Failed to load map: " + selectedMap);
				return;
			}

			// If loading successful, close menu
			MenuHandler.CloseMenu();
		}
		else if (s==Return) {
			MenuHandler.CloseMenu();
		}
	}
}
