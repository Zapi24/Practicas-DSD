package servidor;

import interfaces.IDonaciones;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorDonaciones extends UnicastRemoteObject implements IDonaciones {

    private String nombreReplica;
    private List<String> urlsOtrasReplicas;
    
    //El mapa guarda <NombreCliente, TotalDonadoPorÉl>. 
    //Usamos ConcurrentHashMap para asegurar que la concurrencia sea segura
    private Map<String, Double> clientesLocales;
    private double subtotalLocal;

    public ServidorDonaciones(String nombre, List<String> otrasReplicas) throws RemoteException {
        super();
        this.nombreReplica = nombre;
        this.urlsOtrasReplicas = otrasReplicas;
        this.clientesLocales = new ConcurrentHashMap<>();
        this.subtotalLocal = 0.0;
    }

    // Aquí implementamos los métodos del cliente

    @Override
    public String registrarCliente(String idCliente) throws RemoteException {
        System.out.println("Peticion de registro recibida de: " + idCliente);

        // 1. REQUISITO 5: Comprobar que no exista en NINGUNA réplica
        if (tieneAlCliente(idCliente)) {
            return "ERROR: El cliente ya esta registrado en el sistema.";
        }
        for (String url : urlsOtrasReplicas) {
            try {
                IDonaciones replica = (IDonaciones) Naming.lookup(url);
                if (replica.tieneAlCliente(idCliente)) {
                    return "ERROR: El cliente ya esta registrado en el sistema.";
                }
            } catch (Exception e) {
                System.out.println("Aviso: No se pudo contactar con la replica " + url);
            }
        }

        // 2. REQUISITO 3: Buscar la réplica con menos clientes (Balanceo de carga)
        int minClientes = this.clientesLocales.size();
        String mejorReplicaUrl = null;

        for (String url : urlsOtrasReplicas) {
            try {
                IDonaciones replica = (IDonaciones) Naming.lookup(url);
                int clientesReplica = replica.getNumeroClientesLocales();
                if (clientesReplica < minClientes) {
                    minClientes = clientesReplica;
                    mejorReplicaUrl = url;
                }
            } catch (Exception e) {
                // Si una réplica está caída, simplemente la ignoramos (Tolerancia a fallos)
            }
        }

        // 3. Registrar donde corresponda de forma transparente
        if (mejorReplicaUrl == null) {
            // Me lo quedo yo porque soy el que menos tiene (o hay empate)
            registrarClienteLocal(idCliente);
            return this.nombreReplica;
        } else {
            // Se lo paso a la otra réplica de forma transparente
            try {
                IDonaciones replicaDestino = (IDonaciones) Naming.lookup(mejorReplicaUrl);
                replicaDestino.registrarClienteLocal(idCliente);
                System.out.println("Cliente " + idCliente + " redirigido a " + mejorReplicaUrl);
                // Extraemos el nombre de la réplica 
                return mejorReplicaUrl;
            } catch (Exception e) {
                // Si falla en el último momento, me lo quedo yo
                registrarClienteLocal(idCliente);
                return this.nombreReplica;
            }
        }
    }

    @Override
    public boolean donar(String idCliente, double cantidad) throws RemoteException {
        // REQUISITO 2: Comprobar registro local
        if (!clientesLocales.containsKey(idCliente)) {
            return false; 
        }
        
        // Sumamos a su cuenta personal y al subtotal de esta réplica
        double donadoHastaAhora = clientesLocales.get(idCliente);
        clientesLocales.put(idCliente, donadoHastaAhora + cantidad);
        subtotalLocal += cantidad;
        
        System.out.println("Donacion de " + cantidad + " recibida de " + idCliente);
        return true;
    }

    @Override
    public double consultarTotalDonado(String idCliente) throws RemoteException {
        // REQUISITO 6: Solo si está registrado y ha donado al menos una vez
        if (!clientesLocales.containsKey(idCliente) || clientesLocales.get(idCliente) == 0.0) {
            return -1.0; 
        }

        double totalGlobal = this.subtotalLocal;
        
        // Preguntamos a las demás réplicas cuánto tienen
        for (String url : urlsOtrasReplicas) {
            try {
                IDonaciones replica = (IDonaciones) Naming.lookup(url);
                totalGlobal += replica.getSubtotal();
            } catch (Exception e) {
                System.out.println("Aviso: No se pudo obtener subtotal de " + url);
            }
        }
        return totalGlobal;
    }

    @Override
    public List<String> consultarDonantes(String idCliente) throws RemoteException {
        // REQUISITO 6: Solo si está registrado y ha donado al menos una vez
        if (!clientesLocales.containsKey(idCliente) || clientesLocales.get(idCliente) == 0.0) {
            return null;
        }

        List<String> todosLosDonantes = new ArrayList<>(this.clientesLocales.keySet());

        // Pedimos las listas locales a las demás réplicas
        for (String url : urlsOtrasReplicas) {
            try {
                IDonaciones replica = (IDonaciones) Naming.lookup(url);
                todosLosDonantes.addAll(replica.getDonantesLocales());
            } catch (Exception e) {
                System.out.println("Aviso: No se pudo obtener clientes de " + url);
            }
        }
        return todosLosDonantes;
    }

    // Implementación de los métodos de comunicación entre réplicas

    @Override
    public void registrarClienteLocal(String idCliente) throws RemoteException {
        // Se inicializa con 0 donaciones
        clientesLocales.put(idCliente, 0.0);
        System.out.println("Cliente " + idCliente + " registrado internamente.");
    }

    @Override
    public int getNumeroClientesLocales() throws RemoteException {
        return clientesLocales.size();
    }

    @Override
    public boolean tieneAlCliente(String idCliente) throws RemoteException {
        return clientesLocales.containsKey(idCliente);
    }

    @Override
    public double getSubtotal() throws RemoteException {
        return subtotalLocal;
    }

    @Override
    public List<String> getDonantesLocales() throws RemoteException {
        return new ArrayList<>(clientesLocales.keySet());
    }
}