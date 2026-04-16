# RPCgen: Generación de código RPC

`rpcgen` es una herramienta que facilita la creación de programas distribuidos utilizando RPC (Remote Procedure Call). Automáticamente genera código en C a partir de una especificación escrita en el **lenguaje RPC**. Esto incluye:

1. **Un archivo de cabecera** (`*.h`) con las definiciones comunes para el servidor y el cliente.
2. **Rutinas XDR** para traducir los tipos de datos definidos en el archivo de especificación.
3. **Stubs** (esqueletos) para el servidor y el cliente, que manejan la comunicación entre ambos.

Además, `rpcgen` puede generar una **tabla de informe de RPC** para verificar autorizaciones y gestionar rutinas de servicio. También permite configurar opciones avanzadas, como plazos de tiempo para servidores, selección de transportes o generación de código compatible con ANSI-C.

---

## Pasos para desarrollar un programa distribuido con `rpcgen`

### 1. Definir la especificación del protocolo

El primer paso es definir los tipos de datos de los argumentos y el procedimiento remoto en un archivo de especificación (con extensión `.x`). Este archivo describe el protocolo RPC.

#### Ejemplo: Protocolo para imprimir un mensaje remoto

Supongamos que queremos crear un procedimiento remoto llamado `printmessage()` que recibe una cadena de caracteres (el mensaje) y devuelve un entero (por ejemplo, un código de estado). El archivo de especificación (`msg.x`) sería:

```c
/* Archivo msg.x: Protocolo de impresión de un mensaje remoto */
program MESSAGEPROG {
    version PRINTMESSAGEVERS {
        int PRINTMESSAGE(string) = 1;  // Procedimiento 1
    } = 1;  // Versión 1
} = 0x20000001;  // Número de programa
```

#### Explicación:
- **`program MESSAGEPROG`**: Define un programa RPC con el nombre `MESSAGEPROG`.
- **`version PRINTMESSAGEVERS`**: Define una versión del programa.
- **`int PRINTMESSAGE(string)`**: Declara un procedimiento remoto llamado `PRINTMESSAGE` que toma una cadena (`string`) como argumento y devuelve un entero (`int`).
- **`= 1`**: Asigna el número 1 al procedimiento `PRINTMESSAGE`.
- **`= 0x20000001`**: Asigna un número de programa único. Este número debe estar en el rango `0x20000000 - 0x3FFFFFFF`, reservado para programas definidos por usuarios.

---

### 2. Generar el código con `rpcgen` (lo hace solo, no es muy necesaraio)

Una vez definido el archivo `.x`, usamos `rpcgen` para generar el código necesario:

```bash
rpcgen msg.x
```

Esto generará los siguientes archivos:
1. **`msg.h`**: Archivo de cabecera con las definiciones comunes.
2. **`msg_clnt.c`**: Stub del cliente.
3. **`msg_svc.c`**: Stub del servidor.
4. **`msg_xdr.c`**: Rutinas XDR para la traducción de datos.

---

### 3. Implementar el servidor y el cliente

#### Archivo de cabecera (`msg.h`)

Este archivo contiene las definiciones generadas por `rpcgen`. Por ejemplo:

```c
#ifndef _MSG_H_RPCGEN
#define _MSG_H_RPCGEN

#include <rpc/rpc.h>

#define MESSAGEPROG 0x20000001
#define PRINTMESSAGEVERS 1

#define PRINTMESSAGE 1

extern  int * printmessage_1(char **, CLIENT *);
extern  int * printmessage_1_svc(char **, struct svc_req *);

#endif /* !_MSG_H_RPCGEN */
```

#### Implementación del servidor (`msg_server.c`)

Aquí implementamos la lógica del procedimiento remoto `PRINTMESSAGE`:

```c
#include "msg.h"

int * printmessage_1_svc(char **msg, struct svc_req *req) {
    static int result;  // Variable estática para almacenar el resultado

    printf("Mensaje recibido: %s\n", *msg);  // Imprimir el mensaje en el servidor
    result = 1;  // Código de estado (por ejemplo, 1 = éxito)
    return &result;
}
```

#### Implementación del cliente (`msg_client.c`)

El cliente llama al procedimiento remoto:

```c
#include "msg.h"

int main(int argc, char *argv[]) {
    CLIENT *clnt;
    char *server = "localhost";  // Dirección del servidor
    char *message = "Hola, mundo!";  // Mensaje a enviar
    int *result;

    // Crear un cliente RPC
    clnt = clnt_create(server, MESSAGEPROG, PRINTMESSAGEVERS, "udp");
    if (clnt == NULL) {
        clnt_pcreateerror(server);
        exit(1);
    }

    // Llamar al procedimiento remoto
    result = printmessage_1(&message, clnt);
    if (result == NULL) {
        clnt_perror(clnt, server);
        exit(1);
    }

    // Mostrar el resultado
    printf("Código de estado: %d\n", *result);

    // Liberar recursos
    clnt_destroy(clnt);
    return 0;
}
```

---

### 4. Compilar y ejecutar

Compilamos el servidor y el cliente:

```bash
cc -o server msg_server.c msg_svc.c msg_xdr.c -lnsl
cc -o client msg_client.c msg_clnt.c msg_xdr.c -lnsl
```

Ejecutamos el servidor:
```bash
./server
```

Ejecutamos el cliente:
```bash
./client
```

---

