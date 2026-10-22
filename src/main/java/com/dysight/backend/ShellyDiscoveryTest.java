package com.dysight.backend;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import java.net.InetAddress;

public class ShellyDiscoveryTest {

    public static void main(String[] args) throws Exception {

        // Usamos específicamente la interfaz de red real de tu PC
        InetAddress direccionLocal = InetAddress.getByName("192.168.100.16");

        System.out.println("==========================================");
        System.out.println("     PRUEBA DE DESCUBRIMIENTO SHELLY");
        System.out.println("==========================================");
        System.out.println("Interfaz utilizada: " + direccionLocal.getHostAddress());
        System.out.println();
        System.out.println("Buscando dispositivos en la red...");
        System.out.println();

        JmDNS jmdns = JmDNS.create(direccionLocal);

        jmdns.addServiceListener("_http._tcp.local.", new javax.jmdns.ServiceListener() {

            @Override
            public void serviceAdded(javax.jmdns.ServiceEvent event) {
                System.out.println("Servicio encontrado: " + event.getName());
            }

            @Override
            public void serviceRemoved(javax.jmdns.ServiceEvent event) {
                // No necesitamos hacer nada aquí.
            }

            @Override
            public void serviceResolved(javax.jmdns.ServiceEvent event) {

                ServiceInfo info = event.getInfo();

                System.out.println("------------------------------------------");
                System.out.println("DISPOSITIVO ENCONTRADO");
                System.out.println("Nombre : " + info.getName());
                System.out.println("Tipo   : " + info.getType());
                System.out.println("IP     : " + java.util.Arrays.toString(info.getInet4Addresses()));
                System.out.println("Puerto : " + info.getPort());
                System.out.println("URL    : " + java.util.Arrays.toString(info.getURLs()));
                System.out.println("------------------------------------------");
            }
        });

        // Esperamos 10 segundos para recibir respuestas.
        Thread.sleep(10000);

        jmdns.close();

        System.out.println();
        System.out.println("==========================================");
        System.out.println("Fin de la búsqueda.");
        System.out.println("==========================================");
    }
}