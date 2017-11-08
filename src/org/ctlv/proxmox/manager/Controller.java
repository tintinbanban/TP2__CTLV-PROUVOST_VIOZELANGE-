package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Controller {

	ProxmoxAPI api;

	public Controller(ProxmoxAPI api) {
		this.api = api;
	}

	// migrer un conteneur du serveur "srcServer" vers le serveur "dstServer"
	public void migrateFromTo(String srcServer, String dstServer) throws LoginException, JSONException, IOException {
		LXC mLXC = null;

		// Parcours de tous les CTs du serveur 'srcServer'
		List<LXC> cts = this.api.getCTs(srcServer);
		for (LXC lxc : cts) {
			// On choisit le premier de nos CTs trouvé
			if (lxc.getName().contains(Constants.CT_BASE_NAME)) {
				System.out.println("[MIGRATION] of CT : " + lxc.getName());

				mLXC = lxc;
				break;
			}
		}

		this.api.migrateCT(srcServer, mLXC.getVmid(), dstServer);
	}

	// arr�ter le plus vieux conteneur sur le serveur "server"
	public void offLoad(String server) throws LoginException, JSONException, IOException {
		int min = 11199;
		LXC oldLxc = null;

		// Parcours de tous les CTs du serveur passé en paramètre
		List<LXC> cts = this.api.getCTs(server);
		for (LXC lxc : cts) {
			if (lxc.getName().contains(Constants.CT_BASE_NAME)) {
				int numCT = Integer.parseInt(lxc.getName().substring(20));
				// Si on a une valeur 'CT_BASE_ID' + NUM < à la variable 'min'...
				if (min >= numCT) {
					// On met à jour la variable 'max' : CT plus vieux trouvé
					min = numCT;
					oldLxc = lxc;
				}
			}
		}

		// Arrêt du CT le plus vieux
		System.out.println("[OFFLOAD] of CT : " + oldLxc.getName());
		api.stopCT(server, oldLxc.getVmid());
	}

}
