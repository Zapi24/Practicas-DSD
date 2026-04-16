from calculadora import Calculadora

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

transport = TSocket.TSocket("localhost", 9090)
transport = TTransport.TBufferedTransport(transport)
protocol = TBinaryProtocol.TBinaryProtocol(transport)

client = Calculadora.Client(protocol)

transport.open()

def solicitar_matriz(nombre):

    filas = 0
    columnas = 0
    matriz = []

    try:
        filas = int(input(f"Inserta el número de filas de la matriz: {nombre}: "))
        columnas = int(input(f"Inserta el número de columnas de la matriz: {nombre}: "))
    except ValueError:
        print("Error:   No has insertado un formato válido.")
        return "", 0, 0 #No se ha ingresao un formato válido

    print(f"Ingrese los valores de la matriz {nombre} fila por fila:")
    for i in range(filas):
        fila_valida = False
        while not fila_valida:
            try:
                fila = input(f"Fila {i}: ").strip()
                elementos = list(map(float, fila.split()))
                if len(elementos) == columnas:
                    matriz.append(elementos) #Agregamos un elemento al final
                    fila_valida = True
                else:
                    print(f"Error: La fila debe tener {columnas} valores.")
            except ValueError:
                print("Error: Ingrese solo números enteros separados por espacios.")

    matriz_str = ";".join(" ".join(map(str, fila)) for fila in matriz)
    return matriz_str, filas, columnas

print("hacemos ping al server")
client.ping()


while True:
    print("\n--- Calculadora Avanzada ---")
    print("1. Suma")
    print("2. Resta")
    print("3. Multiplicación")
    print("4. División")
    print("5. Area de un cuadrado")
    print("6. Area de un triangulo")
    print("7. Area de un circulo")
    print("8. Suma de dos vectores")
    print("9. Producto escalar de dos vectores")
    print("10. Producto vectorial de dos vectores (solo en 3D)")
    print("11. Suma de dos matrices")
    print("12. Multiplicación de dos matrices")
    print("13. Determinante de dos matrices")
    print("14. Salir")
    
    opcion = input("Seleccione una opción: ")

    # PARA PODER LEER LOS NÚMEROS
    #imput(Ingrese..) Muestra por pantalla el mensaje, y nos da un string con el resultado
    #split transforma el string del tipo "7.2 6" a "[7.2 , 6]"
    #map(float, [vector strings] )Devuelve los numeros pero ya no como dos strings, si no como dos flotantes
    
    if opcion == "1":
        try:
            a, b = map(float, input("Ingrese dos números separados por espacio: ").split())

            resultado = client.suma(a, b)
            print("Resultado de sumar: " + str(resultado))
        except ValueError:
            print("Error: Ingrese dos números válidos separados por espacio.")

    elif opcion == "2":
        try:
            a, b = map(float, input("Ingrese dos números separados por espacio: ").split())

            resultado = client.resta(a,b)
            print("Resultado de restar: "+ str(resultado))
        except ValueError:
            print("Error: Ingrese dos números válidos separados por espacio.")

    elif opcion == "3":
        try:
            a, b = map(float, input("Ingrese dos números separados por espacio: ").split())

            resultado = client.multiplica(a, b)
            print("Resultado de multiplicar: " + str(resultado))
        except ValueError:
            print("Error: Ingrese dos números válidos separados por espacio.")

    elif opcion == "4":
        try:
            a, b = map(float, input("Ingrese dos números separados por espacio: ").split())

            if b == 0:
                print("Error: has intentado dividir por 0, no se puede dividir por 0")
            else:
                resultado = client.divide(a, b)
                print("Resultado de dividir: " + str(resultado))
        except ValueError:
            print("Error: Ingrese dos números válidos separados por espacio.")

    elif opcion == "5":
        try:
            n = float(input("Ingrese el lado del cuadrado para calcular su área: "))

            if n < 0:
                print("Error: no es lógico calcular el área de un cuadrado con lado negativo.")
            else:
                resultado = client.areaCuadrado(n)
                print("Resultado del area del cuadrado: " + str(resultado))  

        except ValueError:
            print("Error: Ingrese un número entero válido.")

    elif opcion == "6":
        try:
            a,b = map(float, input("Ingrese dos números separados por espacio (primero base y despues altura): ").split())  #Recibo el string

            if a < 0 or b < 0:

                print("Error: no es lógico calcular el area de un triangulo con su base o altura negativos")
            else:

                resultado = client.areaTriangulo(a,b)
                print("Resultado del area del triangulo "+ str(resultado))

        except ValueError:
            print("Error: Ingrese dos números válidos separados por espacio.")

    elif opcion == "7":
        try:
            n = float(input("Ingrese el radio del circulo para calcular su área: "))

            if n < 0:
                print("Error: no es lógico calcular el área de un circulo con radio negativo.")
            else:
                resultado = client.areaCirculo(n)
                print("Resultado del area del circulo: " + str(resultado))  

        except ValueError:
            print("Error: Ingrese un número entero válido.")

    elif opcion == "8":
        try:
            print("Ingrese los valores del primer vector separados por espacio:")
            v1 = list(map(float, input("Vector 1: ").split())) #Lo transformamos en list para poder usar funciones como len(v1) Ej: v1 = [3.5, 4.2, 1.8]

            print("Ingrese los valores del segundo vector separados por espacio:")
            v2 = list(map(float, input("Vector 2: ").split()))

            if len(v1) != len(v2):
                print("Error: Los vectores deben tener la misma longitud")
            else:
                resultado = client.sumaVectores(" ".join(map(str, v1)), " ".join(map(str, v2))) #Para enviar vectores como: client.sumaVectores("3.5 4.2 1.8", "1.2 2.3 0.5")
                                                    #Map nos convierte cada elemento de la lista en string, y join nos lo transforma en uno solo
                print("Resultado de la suma de los vectores:  " + resultado)
                
        except ValueError:
            print("Error: Ingrese valores numéricos válidos para los vectores.")

    elif opcion == "9":
        try:
            print("Ingrese los valores del primer vector separados por espacio:")
            v1 = list(map(float, input("Vector 1: ").split()))

            print("Ingrese los valores del segundo vector separados por espacio:")
            v2 = list(map(float, input("Vector 2: ").split()))

            if len(v1) != len(v2):
                print("Error: Los vectores deben tener la misma longitud")
            else:
                resultado = client.productoEscalar(" ".join(map(str, v1)), " ".join(map(str, v2)))
                print("Resultado del producto escalar de los vectores:  " + resultado)

        except ValueError:
            print("Error: Ingrese valores numéricos válidos para los vectores.")

    elif opcion == "10":
        try:
            print("Ingrese los valores del primer vector separados por espacio:")
            v1 = list(map(float, input("Vector 1: ").split()))

            print("Ingrese los valores del segundo vector separados por espacio:")
            v2 = list(map(float, input("Vector 2: ").split()))

            if len(v1) != 3 or len(v2) != 3:
                print("Error: Solo se pueden multiplicar vectorialmente vectores de tamaño 3")
            else:
                resultado = client.productoVectorial(" ".join(map(str, v1)), " ".join(map(str, v2)))
                print("Resultado del producto vectorial de los vectores:  " + resultado)

        except ValueError:
            print("Error: Ingrese valores numéricos válidos para los vectores.")
    elif opcion == "11":
        matriz1_str, filas1, columnas1 = solicitar_matriz("A")
        matriz2_str, filas2, columnas2 = solicitar_matriz("B")

        if filas1 != filas2 or columnas1 != columnas2:
            print("Error: Para la suma, las matrices deben tener el mismo tamaño.")
        else:

            resultado = client.sumaMatrices(matriz1_str, matriz2_str)
            newResultado = resultado.replace(";", "\n") #Para que se muestre la matriz de forma mas intuitiva, cambiamos los ; por saltos de linea
            print("Resultado de la suma de las dos matrices:\n" + newResultado)

    elif opcion == "12":

        matriz1_str, filas1, columnas1 = solicitar_matriz("A")
        matriz2_str, filas2, columnas2 = solicitar_matriz("B")

        if columnas1 != filas2:

            print("Error: Para la multiplicación, el número de columnas de la primera matriz debe ser igual al número de filas de la segunda matriz.")
        else:

            resultado = client.multiplicacionMatrices(matriz1_str, matriz2_str)
            newResultado = resultado.replace(";", "\n")
            print("Resultado de la multiplicación de las dos matrices:\n" + newResultado)

    elif opcion == "13":

        matriz_str, filas, columnas = solicitar_matriz("A")

        if filas != columnas or (filas not in [2, 3] or columnas not in [2, 3]):

            print("Error: Solo se puede calcular el determinante de matrices cuadradas 2x2 o 3x3.")
        else:

            resultado = client.determinanteMatriz(matriz_str)
            print("Determinante de la matriz:  " + resultado)

    elif opcion == "14":
        print("Saliendo... ¡Hasta luego!")
        break
    else:
        print("Opción no válida, intente de nuevo.")

transport.close()









