/* msg_proc.c: implementación del procedimiento remoto */

#include <stdio.h>
#include "msg.h" /* el archivo lo genera rpcgen */

int *printmessage_1_svc(msg, req)
char **msg;
struct svc_req *req; /* detalles de la llamada */
{
    static int result; /* es obligatorio que sea estática */
    FILE *f;

    f = fopen("/dev/tty", "w");
    if (f == (FILE *)NULL)
    {
      result = 0;
      return (&result);
    }

    fprintf(f, "%s\n", *msg);
    fclose(f);

    result = 1;
    return (&result);
}
