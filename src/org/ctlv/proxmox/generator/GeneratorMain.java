package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class GeneratorMain {

	static Random rndTime = new Random(new Date().getTime());

	public static int getNextEventPeriodic(int period) {
		return period;
	}

	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}

	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (-Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int) next;
	}

	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {

		long baseID = Constants.CT_BASE_ID;
		int lambda = 30;

		Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();

		ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		Random rndRAM = new Random(new Date().getTime());

		long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
		long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);

		while (true) {

			// 1. Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
			float memOnServer1 = 0;
			float memOnServer2 = 0;

			// M�moire autoris�e sur chaque serveur => 16% MAX
			float memRatioOnServer1 = 0.16f;
			float memRatioOnServer2 = 0.16f;

			// MAJ de la RAM utilisée par nos CT créés sur les 2 serveurs
			List<LXC> cts = api.getCTs(Constants.SERVER1);
			System.out.println("\n[GENERATOR]//////////////////////////////////////////\nMAJ RAM sr-px1");
			for (LXC lxc : cts) {
				// On cherche nos CTs parmi tous ceux sur le serveur 1
				if (lxc.getName().contains(Constants.CT_BASE_NAME)) {

					System.out.println("\t-- " + lxc.getName());
					// Ajout des RAM de chaque CT trouvé
					memOnServer1 += (float) lxc.getMaxmem();
				}
			}
			// Calcul de la mémoire occupée par les CTs sur le serveur 1
			memOnServer1 = memOnServer1 / (float) api.getNode(Constants.SERVER1).getMemory_total();
			System.out.println("\t\t memOnServer1 = " + memOnServer1 * 100.0 + "%");

			cts = api.getCTs(Constants.SERVER2);
			System.out.println("[GENERATOR]MAJ RAM sr-px2");
			for (LXC lxc : cts) {
				// On cherche nos CTs parmi tous ceux sur le serveur 2
				if (lxc.getName().contains(Constants.CT_BASE_NAME)) {

					System.out.println("\t-- " + lxc.getName());
					// Ajout des RAM de chaque CT trouvé
					memOnServer2 += (float) lxc.getMaxmem();
				}
			}
			// Calcul de la mémoire occupée par les CTs sur le serveur 1
			memOnServer2 = memOnServer2 / (float) api.getNode(Constants.SERVER2).getMemory_total();
			System.out.println("\t\t memOnServer2 = " + memOnServer2 * 100.0 + "%");
			//////////////////////////////////////////////////////////////////////

			if (memOnServer1 < memRatioOnServer1 && memOnServer2 < memRatioOnServer2) { // Exemple de condition de
																						// l'arr�t de la g�n�ration de
																						// CTs

				// choisir un serveur al�atoirement avec les ratios sp�cifi�s 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
					serverName = Constants.SERVER1;
				else
					serverName = Constants.SERVER2;

				// cr�er un contenaire sur ce serveur
				// Création d'un CT
				//// -> Nom du serveur
				//// -> ID du CT
				//// -> Nom du CT
				String ctName = Constants.CT_BASE_NAME + baseID;
				//// -> Mémoire à allouer
				api.createCT(serverName, Long.toString(baseID), ctName, Constants.RAM_SIZE[1]);

				// planifier la prochaine cr�ation
				int timeToWait = getNextEventExponential(lambda); // par exemple une loi expo d'une moyenne de 30sec
				// attendre jusqu'au prochain �v�nement
				Thread.sleep(1000 * timeToWait);

				// Démarrage du CT créé
				api.startCT(serverName, Long.toString(baseID));
				baseID++;
			} else {
				System.out.println("Servers are loaded, waiting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME * 1000);
			}
		}

	}
}
