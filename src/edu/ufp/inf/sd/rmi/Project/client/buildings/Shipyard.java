package edu.ufp.inf.sd.rmi.Project.client.buildings;

//import edu.ufp.inf.sd.rmi.Project.client.engine.Game;

public class Shipyard extends Base {

	public Shipyard(int owner, int xx, int yy) {
		super(owner, xx, yy);
		name="Capital";
		desc="Creates water edu.ufp.inf.sd.rmi.Project.client.units.";
		img = 4;
		Menu = "shipyard";
		//Game.map.map[yy][xx].swim = true;
	}
}
