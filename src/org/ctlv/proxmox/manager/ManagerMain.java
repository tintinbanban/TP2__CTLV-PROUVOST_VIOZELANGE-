package org.ctlv.proxmox.manager;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.generator.GeneratorMain;

public class ManagerMain {

	public static void main(String[] args) throws Exception {
		//Thread Manager
		ProxmoxAPI api = new ProxmoxAPI();
		Thread th = new Thread(new Monitor(api, new Analyzer(api, new Controller(api))));
		th.start();		
		
		//Generator
		GeneratorMain.main(null);
	}

}