/* Archivo  rprintmsg.c: programa cliente */
#include <stdio.h>
#include "msg.h"

main(argc, argv)
int argc;
char *argv[];
{
    CLIENT *clnt;
    int *result;
    char *server;
    char *message;

    if (argc != 3)
    {
        fprintf(stderr, "uso: %s maquina mensaje\n", argv[0]);
        exit(1);
    }

    server = argv[1];
    message = argv[2];

    /* Crea estructura de datos(handle) del proceso
       cliente para el servidor designado     */
    clnt = clnt_create(server, MESSAGEPROG, PRINTMESSAGEVERS, "visible");
    if (clnt == (CLIENT *)NULL)
    {
        /* No se pudo establecer conexión con el servidor */
        clnt_pcreateerror(server);
        exit(1);
    }

    /* Llamada remota al procedimiento en el servidor */
    result = printmessage_1(&message, clnt);
    if (result == (int *)NULL)
    {
        /* Ocurrió un error durante la llamada al servidor */
        clnt_perror(clnt, server);
        exit(1);
    }  
    
    if (*result == 0)
    {
        /* El servidor fue incapaz de imprimir nuestro mensaje */
        fprintf(stderr, "%s: no pudo imprimir el mensaje\n", argv[0]);
        exit(1);
    }
    
    printf("Mensaje enviado a %s\n", server);
    clnt_destroy(clnt);
    exit(0);
}
