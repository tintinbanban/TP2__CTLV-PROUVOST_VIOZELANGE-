package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Monitor implements Runnable {

	Analyzer analyzer;
	ProxmoxAPI api;
	//Objet de type Map stockant les serveurs et les CTs associés : pour chaque serveur (clé), on renvoie une liste (valeur) de CTs  
	Map<String, List<LXC>> myCTsPerServer;

	public Monitor(ProxmoxAPI api, Analyzer analyzer) {
		this.api = api;
		this.analyzer = analyzer;
		myCTsPerServer = new HashMap<String, List<LXC>>();
	}

	@Override
	public void run() {
		//Liste de CTs
		List<LXC> myList = new ArrayList<>();

		while (true) {
			////////////////*************************************************************
			// R�cup�rer les donn�es sur les serveurs
			try {
				myList.clear();
				
//				System.out.println("[MONITOR]-> Serveur " + Constants.SERVER1);
				
				List<LXC> cts = this.api.getCTs(Constants.SERVER1);
				for (LXC lxc : cts) {
					// On cherche nos CTs parmi tous ceux sur le serveur 1
					if (lxc.getName().contains(Constants.CT_BASE_NAME)) {
						
//						System.out.println("\t---> CT : " + lxc.getName());
						
						myList.add(lxc);
					}
				}
				//Ajout des informations trouvées sur le serveur 1
				this.myCTsPerServer.put(Constants.SERVER1, myList);
				
				
				//////////////////////////////////////////////////
				//Réinitialisation de la liste des CTs
				myList = new ArrayList<>();
				
//				System.out.println("[MONITOR]-> Serveur " + Constants.SERVER2);
				
				cts = this.api.getCTs(Constants.SERVER2);
				for (LXC lxc : cts) {
					// On cherche nos CTs parmi tous ceux sur le serveur 1
					if (lxc.getName().contains(Constants.CT_BASE_NAME)) {
						
//						System.out.println("\t---> CT : " + lxc.getName());
						
						myList.add(lxc);
					}
				}
				//Ajout des informations trouvées sur le serveur 2
				this.myCTsPerServer.put(Constants.SERVER2, myList);

				
				////////////////*************************************************************
				// Lancer l'anlyse
				this.analyzer.analyze(myCTsPerServer);
				
				this.myCTsPerServer.clear();
				
				// attendre une certaine p�riode
				try {
					Thread.sleep(Constants.MONITOR_PERIOD * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (LoginException | JSONException | IOException e1) {
				e1.printStackTrace();
			}

		}
	}
}
