service Calculadora {
   void ping(),
   double suma(1:double n1, 2:double n2),
   double resta(1:double n1, 2:double n2),
   double multiplica(1:double n1, 2:double n2),
   double divide(1:double n1, 2:double n2),
   double areaCuadrado(1:double n),
   double areaTriangulo(1:double n1, 2:double n2),
   double areaCirculo(1:double n),
   string sumaVectores(1:string vector1, 2:string vector2),
   string productoEscalar(1:string vector1, 2:string vector2),
   string productoVectorial(1:string vector1, 2:string vector2),
   string sumaMatrices(1: string matriz1, 2: string matriz2),
   string multiplicacionMatrices(1: string matriz1, 2: string matriz2),
   string determinanteMatriz(1: string matriz)
}


