package edu.ufp.inf.sd.rmi.Project.client.units;

public class Infantry extends Base {
	public Infantry(int owner, int xx, int yy, boolean active) {
		super(owner, xx, yy, active);
		name = "Infantry";
		nick = "Inf";
		desc = "Weakest edu.ufp.inf.sd.rmi.Project.client.units here.";
		img = 0;
		speed = 4;
		raider = true;
	}
}
