package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IDonaciones extends Remote {

    //Aquí definimos los métodos para el cliente, que son las funciones que llamará el usuario desde su consola

    /**
     * Registra a un cliente en el sistema.
     * Requisito 3: Balanceo de carga. Si el cliente contacta con la Réplica 1, 
     * pero la Réplica 2 tiene menos clientes, lo registraremos en la 2.
     * @return El nombre de la réplica donde REALMENTE ha sido registrado.
     */
    String registrarCliente(String idCliente) throws RemoteException;

    /**
     * Realiza una donación. 
     * Requisito 2: Solo se puede donar si estás registrado.
     * @return true si la donación se hizo correctamente, false si no estaba registrado.
     */
    boolean donar(String idCliente, double cantidad) throws RemoteException;

    /**
     * Consulta el total recaudado a nivel global (sumando todas las réplicas).
     * Requisito 6: Solo puede consultar si está registrado y ha donado alguna vez.
     * @return El total donado, o -1 si el usuario no tiene permisos para ver esto.
     */
    double consultarTotalDonado(String idCliente) throws RemoteException;

    /**
     * Consulta la lista global de todos los donantes de todas las réplicas.
     * Requisito 6: Igual que el anterior, necesita permisos.
     * @return Lista de IDs de los clientes, o null si no tiene permisos.
     */
    List<String> consultarDonantes(String idCliente) throws RemoteException;

    //Aqui definimos los métodos iternos entre réplicas, los clientes no deberían usar esto, es para que los servidores hablen

    /**
     * Requisito 3 (Balanceo): Una réplica le pregunta a otra cuántos clientes tiene.
     */
    int getNumeroClientesLocales() throws RemoteException;

    /**
     * Requisito 5 (Unicidad): Antes de registrar a alguien, una réplica le pregunta
     * a las demás si ese cliente ya existe en sus bases de datos.
     */
    boolean tieneAlCliente(String idCliente) throws RemoteException;

    /**
     * Requisito 4 y 6: Para calcular el gran total, una réplica le pide a las 
     * demás que le envíen el dinero que tienen acumulado en su hucha particular.
     */
    double getSubtotal() throws RemoteException;

    /**
     * Requisito 6: Para montar la lista global de donantes, una réplica le pide
     * a las demás que le envíen su lista local de clientes.
     */
    List<String> getDonantesLocales() throws RemoteException;

    /**
     * Requisito 3: Método interno para que una réplica registre a un cliente 
     * directamente por orden de otra réplica (sin volver a balancear).
     */
    void registrarClienteLocal(String idCliente) throws RemoteException;
}