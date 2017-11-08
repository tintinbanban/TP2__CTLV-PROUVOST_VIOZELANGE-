package org.ctlv.proxmox.tester;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;

public class Main {

	public static void main(String[] args) throws LoginException, JSONException, IOException {

		ProxmoxAPI api = new ProxmoxAPI();		
		
//		List<LXC> cts = api.getCTs("srv-px1");
		//Affichage de tous les CT créés sur le serveur 'srv-px1'
//		for (LXC lxc : cts) {
//			System.out.println(lxc.getName());
//		}
		
		//Récupération du noeud du serveur 'srv-px1'
		Node srv = api.getNode(Constants.SERVER1);
		System.out.println("--Serveur " + Constants.SERVER1);
		//Affichage de l'utilisation CPU
		System.out.println("\tCPU conso : " + 100.0 * srv.getCpu() + "%" );
		//Affichage de l'utilisation du disque dur
		System.out.println("\tDisk used : " + 100.0 * srv.getRootfs_used() / srv.getRootfs_total() + "%");
		//Affichage de la mémoire utilisée
		System.out.println("\tMemory used : " + 100.0 * srv.getMemory_used() / srv.getMemory_total() + "%");
		System.out.println("\n");
		
		
		//Création d'un CT
		//// -> Nom du serveur
		//// -> ID du CT
		//// -> Nom du CT
		String ctName = Constants.CT_BASE_NAME + Constants.CT_BASE_ID;
		//// -> Mémoire à allouer
		api.createCT(Constants.SERVER1, Long.toString(Constants.CT_BASE_ID), ctName, Constants.RAM_SIZE[1]);
		
		try {
			Thread.sleep(Constants.GENERATION_WAIT_TIME*30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Démarrage du CT créé 
		api.startCT(Constants.SERVER1, Long.toString(Constants.CT_BASE_ID));
		
		List<LXC> cts = api.getCTs("srv-px1");
		for (LXC lxc : cts) {
			//On cherche notre CT parmi tous ceux sur le serveur
			if (lxc.getName().equals(ctName)) {
				
				System.out.println("--CT " + lxc.getName());
				//Affichage de l'état du CT
				System.out.println("\tStatus : " + lxc.getStatus());
				//Affichage de l'utilisation CPU
				System.out.println("\tCPU conso : " + (double) (lxc.getCpu()) + "%" );
				//Affichage de l'utilisation du disque dur
				System.out.println("\tDisk used : " + 100.0 * lxc.getDisk() / lxc.getMaxdisk() + "%");
				//Affichage de la mémoire utilisée
				System.out.println("\tMemory used : " + 100.0 * lxc.getMem() / lxc.getMaxmem() + "%");
				System.out.println("\n");
			}
		}
		
		//Arrêt d'un CT qui est démarré
		//System.out.println("/!\ Arrêt du CT...");
		//api.stopCT(Constants.SERVER1, Long.toString(Constants.CT_BASE_ID));
				
		

	}

}
