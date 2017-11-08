package org.ctlv.proxmox.manager;

import java.io.IOException;
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

public class Analyzer {
	ProxmoxAPI api;
	Controller controller;

	public Analyzer(ProxmoxAPI api, Controller controller) {
		this.api = api;
		this.controller = controller;
	}

	public void analyze(Map<String, List<LXC>> myCTsPerServer) throws LoginException, JSONException, IOException {
		//Objet de type Map associant chaque serveur (clé) à sa consommation de RAM (valeur) 
		Map<String, Float> lMemOnServers = new HashMap<String, Float>();
		float totalMem = 0.0f;

		// Itérer sur un Objet de type Map
		for (Iterator i = myCTsPerServer.entrySet().iterator(); i.hasNext();) {
			Entry couple = (Entry) i.next();

			// Récupération de la clé : String (nom du Serveur)
			String cle = (String) couple.getKey();
			// Récupération de la valeur : List<LXC> (CTs)
			List<LXC> valeur = (List<LXC>) couple.getValue();

			System.out.println("\n[ANALYSER]///////////////////////////////////////////\n-> Serveur " + cle);

			lMemOnServers.put(cle, (float) 0.0);
			// Calculer la quantit� de RAM utilis�e par mes CTs sur un serveur
			for (LXC lxc : valeur) {
				System.out.println("\t---> CT : " + lxc.getName());

				// Ajout des RAM de chaque CT trouvé
				lMemOnServers.replace(cle, lMemOnServers.get(cle) + (float) lxc.getMaxmem());
			}
			System.out.println("\t***************************\n\tRAM totale utilisée : " + lMemOnServers.get(cle));
			// Calcul du ratio de mémoire RAM occupée par les CTs sur le serveur 'cle'
			lMemOnServers.replace(cle, lMemOnServers.get(cle) / (float) this.api.getNode(cle).getMemory_total());
			
			System.out.println("\tRatio utilisation RAM : " + lMemOnServers.get(cle));
		}

		// M�moire autoris�e sur chaque serveur => 8% MAX
		float memRatio = 0.08f; // Ratio sur un serveur
		float totalMemRatio = 0.12f; // Ratio sur les deux serveurs

		// Analyze et Actions
		for (Iterator i = lMemOnServers.entrySet().iterator(); i.hasNext();) {
			Entry couple = (Entry) i.next();

			// Récupération de la clé : String (nom du Serveur)
			String cle = (String) couple.getKey();
			// Récupération de la valeur : Mémoire utilisée
			Float valeur = (Float) couple.getValue();

			// Est-ce qu'un des serveurs dépasse les 8% de RAM ??
			if (valeur > memRatio) {
				for (Iterator j = lMemOnServers.entrySet().iterator(); j.hasNext();) {
					Entry coupleJ = (Entry) j.next();

					// Récupération de la clé : String (nom du Serveur)
					String cleJ = (String) coupleJ.getKey();

					if (!cle.matches(cleJ)) {
						System.out.println("\n** MIGRATION ** from " + cle + " TO " + cleJ);
						this.controller.migrateFromTo(cle, cleJ);
						break;
					}
				}
			}

			// Addition Val memOnServer
			totalMem += valeur;
		}

		// Est-ce que les ressources des deux serveurs dépassent les 12% de RAM ?
		if (totalMem > totalMemRatio) {
			for (Iterator i = lMemOnServers.entrySet().iterator(); i.hasNext();) {
				Entry couple = (Entry) i.next();

				// Récupération de la clé : String (nom du Serveur)
				String cle = (String) couple.getKey();
				
				System.out.println("\n** OFFLOAD ** from " + cle)	;
				this.controller.offLoad(cle);
				break;
			}
		}
	}

}
