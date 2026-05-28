import glob
import sys
import math #Para poder realizar operaciones


from calculadora import Calculadora

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

import logging

logging.basicConfig(level=logging.DEBUG)


class CalculadoraHandler:
    def __init__(self):
        self.log = {}

    def ping(self):
        print("me han hecho ping()")

    def suma(self, n1, n2):
        print("sumando " + str(n1) + " con " + str(n2))
        return n1 + n2

    def resta(self, n1, n2):
        print("restando " + str(n1) + " con " + str(n2))
        return n1 - n2

    def multiplica(self, n1, n2):
        print("multiplicando " + str(n1) + " con " + str(n2))
        return n1 * n2
    
    def divide(self, n1, n2):
        print("dividiendo " + str(n1) + " con " + str(n2))
        return n1 / n2
    
    def areaCuadrado(self, n):

        print("area de cuadrado con lado " + str(n))

        return n * n;
    
    def areaTriangulo(self, n1, n2):    

        print("area de triangulo con base " + str(n1) + " y altura " + str(n2))
        
        return (n1 * n2) / 2

    def areaCirculo(self, n):

        print("area del circulo con radio " + str(n))

        return math.pi * math.pow(n,2)
    
    def sumaVectores(self, vector1, vector2):

        print("Vector 1 recibido: " + vector1)
        print("Vector 2 recibido: " + vector2)

        v1 = list(map(float, vector1.split()))
        v2 = list(map(float, vector2.split()))

        resultado = [v1[i] + v2[i] for i in range(len(v1))]
        return " ".join(map(str, resultado))

    def productoEscalar(self, vector1, vector2):

        print("Vector 1 recibido: " + vector1)
        print("Vector 2 recibido: " + vector2)

        v1 = list(map(float, vector1.split()))
        v2 = list(map(float, vector2.split()))
        
        resultado = sum(v1[i] * v2[i] for i in range(len(v1)))
        return str(resultado)

    def productoVectorial(self, vector1, vector2):

        print("Vector 1 recibido: " + vector1)
        print("Vector 2 recibido: " + vector2)

        v1 = list(map(float, vector1.split()))
        v2 = list(map(float, vector2.split()))

        
        resultado = [
            v1[1] * v2[2] - v1[2] * v2[1],
            v1[2] * v2[0] - v1[0] * v2[2],
            v1[0] * v2[1] - v1[1] * v2[0]
        ]
        return " ".join(map(str, resultado))
    
    def sumaMatrices(self, matriz1, matriz2):
        
        print("Matriz1 recibida: " + matriz1)
        print("Matriz2 recibida: " + matriz2)

        m1 = [list(map(float, fila.split())) for fila in matriz1.split(';')]    #Las filas están separadas por ;
        m2 = [list(map(float, fila.split())) for fila in matriz2.split(';')]    #Las filas están separadas por ;

        resultado = [[m1[i][j] + m2[i][j] for j in range(len(m1[0]))] for i in range(len(m1))]

        return ';'.join(' '.join(map(str, fila)) for fila in resultado)
    
    def multiplicacionMatrices(self, matriz1, matriz2):

        print("Matriz1 recibida: " + matriz1)
        print("Matriz2 recibida: " + matriz2)

        m1 = [list(map(float, fila.split())) for fila in matriz1.split(';')]
        m2 = [list(map(float, fila.split())) for fila in matriz2.split(';')]
       
        resultado = [[sum(m1[i][k] * m2[k][j] for k in range(len(m2))) for j in range(len(m2[0]))] for i in range(len(m1))]

        return ';'.join(' '.join(map(str, fila)) for fila in resultado)
    
    def determinanteMatriz(self, matriz):
        print("Matriz recibida: " + matriz)
        m = [list(map(float, fila.split())) for fila in matriz.split(';')]

        # Para matrices 2x2
        if len(m) == 2 and len(m[0]) == 2:
            det = (m[0][0] * m[1][1]) - (m[0][1] * m[1][0])
        
        # Para matrices 3x3 
        elif len(m) == 3 and len(m[0]) == 3:
            det = (m[0][0] * m[1][1] * m[2][2] +
                m[0][1] * m[1][2] * m[2][0] +
                m[0][2] * m[1][0] * m[2][1] -
                m[2][0] * m[1][1] * m[0][2] -
                m[2][1] * m[1][2] * m[0][0] -
                m[2][2] * m[1][0] * m[0][1])
    
        else:
            return "Error: Solo se soportan matrices 2x2 y 3x3"

        return str(round(det, 2))  # Redondear a 2 decimales para evitar errores de precisión

if __name__ == "__main__":
    handler = CalculadoraHandler()
    processor = Calculadora.Processor(handler)
    transport = TSocket.TServerSocket(host="127.0.0.1", port=9090)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

    print("iniciando servidor...")
    server.serve()
    print("fin")
