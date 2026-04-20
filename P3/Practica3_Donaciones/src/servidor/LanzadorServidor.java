package servidor;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

public class LanzadorServidor {

    public static void main(String[] args) {
        // Necesitamos al menos el puerto y el nombre de la réplica
        if (args.length < 2) {
            System.out.println("Uso: java servidor.LanzadorServidor <puerto> <nombreReplica> [urlOtraReplica1 urlOtraReplica2 ...]");
            System.exit(1);
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            int puerto = Integer.parseInt(args[0]);
            String nombreReplica = args[1];

            // Recogemos las URLs de las otras réplicas que pasemos por consola
            List<String> otrasReplicas = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                otrasReplicas.add(args[i]);
            }

            // Levantamos el registro RMI en el puerto indicado (¡Magia del Ejemplo 3!)
            LocateRegistry.createRegistry(puerto);

            // Creamos nuestro objeto servidor pasándole su nombre y sus compañeras
            ServidorDonaciones servidor = new ServidorDonaciones(nombreReplica, otrasReplicas);

            // Lo publicamos para que los clientes (y otras réplicas) puedan encontrarlo
            String urlLocal = "rmi://localhost:" + puerto + "/" + nombreReplica;
            Naming.rebind(urlLocal, servidor);

            System.out.println("==================================================");
            System.out.println("  " + nombreReplica + " ACTIVA y escuchando en puerto " + puerto);
            System.out.println("  Conectada a " + otrasReplicas.size() + " replicas compañeras.");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("Error arrancando el servidor:");
            e.printStackTrace();
        }
    }
}