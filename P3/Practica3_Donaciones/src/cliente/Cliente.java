package cliente;

import interfaces.IDonaciones;
import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) {
        // Activamos el gestor de seguridad como en los ejemplos anteriores
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("   BIENVENIDO AL SISTEMA DE DONACIONES   ");
        System.out.println("=========================================");
        
        System.out.print("Introduce la IP del servidor (ej. localhost): ");
        String host = scanner.nextLine();
        
        System.out.print("Introduce el puerto del registro RMI (ej. 1099): ");
        String puerto = scanner.nextLine();
        
        System.out.print("Introduce el nombre de la replica a la que conectar (ej. Replica1): ");
        String replicaActual = scanner.nextLine();

        try {
            // Buscamos el objeto remoto en las "Páginas Amarillas"
            String url = "rmi://" + host + ":" + puerto + "/" + replicaActual;
            IDonaciones servidor = (IDonaciones) Naming.lookup(url);
            System.out.println("\n[OK] Conectado exitosamente a " + replicaActual);

            boolean salir = false;
            
            // BUCLE DEL MENÚ INTERACTIVO
            while (!salir) {
                System.out.println("\n--- MENU DE OPCIONES ---");
                System.out.println("Conectado actualmente a: " + replicaActual);
                System.out.println("1. Registrarse como nuevo donante");
                System.out.println("2. Realizar una donacion");
                System.out.println("3. Consultar total recaudado (Global)");
                System.out.println("4. Ver lista de donantes (Global)");
                System.out.println("5. Salir");
                System.out.print("Elige una opcion: ");
                
                int opcion = scanner.nextInt();
                scanner.nextLine(); // Limpiar el buffer del scanner

                String idCliente;

                switch (opcion) {
                    case 1:
                        System.out.print("Introduce tu nombre o ID para registrarte: ");
                        idCliente = scanner.nextLine();
                        
                        // Llamamos al método remoto
                        String respuestaRegistro = servidor.registrarCliente(idCliente);
                        
                        if (respuestaRegistro.startsWith("ERROR")) {
                            System.out.println(">> " + respuestaRegistro);
                        } else {
                            System.out.println(">> Registro completado.");
                            // Si el servidor nos devuelve una URL, nos redirigimos
                            if (respuestaRegistro.startsWith("rmi://")) {
                                System.out.println(">> INFO: Por balanceo de carga, el servidor te ha redirigido.");
                                System.out.println(">> Redirigiendo conexion de forma transparente...");
                                
                                // Cambiamos nuestro 'stub' usando la URL exacta que nos dio el servidor
                                url = respuestaRegistro;
                                servidor = (IDonaciones) Naming.lookup(url);
                                
                                // Actualizamos el nombre visual para que el menú quede bonito
                                replicaActual = url.substring(url.lastIndexOf("/") + 1);
                            }
                        }
                        break;

                    case 2:
                        System.out.print("Introduce tu ID de cliente: ");
                        idCliente = scanner.nextLine();
                        System.out.print("Introduce la cantidad a donar: ");
                        double cantidad = scanner.nextDouble();
                        
                        boolean exito = servidor.donar(idCliente, cantidad);
                        if (exito) {
                            System.out.println(">> ¡Gracias por tu donacion de " + cantidad + " euros!");
                        } else {
                            System.out.println(">> ERROR: No estas registrado. Registrate primero (Opcion 1).");
                        }
                        break;

                    case 3:
                        System.out.print("Introduce tu ID de cliente para verificar permisos: ");
                        idCliente = scanner.nextLine();
                        
                        double total = servidor.consultarTotalDonado(idCliente);
                        if (total == -1.0) {
                            System.out.println(">> ERROR: Permiso denegado. Debes estar registrado y haber donado.");
                        } else {
                            System.out.println(">> TOTAL RECAUDADO GLOBALMENTE: " + total + " euros.");
                        }
                        break;

                    case 4:
                        System.out.print("Introduce tu ID de cliente para verificar permisos: ");
                        idCliente = scanner.nextLine();
                        
                        List<String> donantes = servidor.consultarDonantes(idCliente);
                        if (donantes == null) {
                            System.out.println(">> ERROR: Permiso denegado. Debes estar registrado y haber donado.");
                        } else {
                            System.out.println(">> LISTA DE DONANTES A NIVEL GLOBAL:");
                            for (String donante : donantes) {
                                System.out.println("   - " + donante);
                            }
                        }
                        break;

                    case 5:
                        salir = true;
                        System.out.println(">> Desconectando. ¡Hasta pronto!");
                        break;

                    default:
                        System.out.println(">> Opcion no valida.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}