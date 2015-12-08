Instrucciones de ejecuci�n del programa:

- Ingresar a la aplicaci�n NetBeans.
- Ir al men� "File" y presionar sobre la opci�n "Open Proyect...".
- Dirigirse a la carpeta donde se encuentra el presente laboratorio, seleccionarlo y presionar el bot�n "Open Proyect".
- Realizar "Build" del proyecto, presionando el bot�n F11 o presionando click derecho sobre la raiz de navegaci�n del proyecto y seleccionando la opci�n "Build".
- Ejecutar el proyecto manualmente desplegando en el arbol de navegaci�n del proyecto, los archivos .java. Primero, presionar el click derecho del mouse y seleccionar la opci�n "Run" sobre "IndexService.java", luego repetir la misma operaci�n sobre "CacheService.java", "FrontService.java" y "Cliente.java", en el dicho orden.

* El programa solo funciona ejecutando los archivos en el orden antes estipulado (Index, Cache, Front, Cliente), debido a la dependencia de conexiones de los socket entre los programas.

Instrucciones para la configuraci�n del programa:

- Para cambiar los par�metros del programa se debe editar el archivo "config.ini". La sintaxis correcta es "<tama�o del Cache> <cantidad de respuestas generadas en el index> <cantidad de hebras del programa> <cantidad de particiones del cache>\n". Por ejemplo la configuracion que viene por defecto es "15 50 5 5\n", donde se establecen 15 slots totales en el cache (3 est�ticos (20%) y 12 din�micos (80%); luego se establecen 50 pares (query_n�, answer_n�) desde el numero 1 al 50, donde se debe consultar en el programa por ejemplo por "query_2" y el programa entregar� el resultado de nombre del sitio "Answer_2" y url "Answer_2.com"; luego se establecen 5 hebras para el programa, que se asignan de manera circular a las consultas del usuario, tanto en el frontService como en el cacheService; y finalmente se establecen 5 particiones del cache, en este caso con tama�os de 2, 2, 2, 2 y 4 slots por particion respectivamente.

*Es importante destacar que el archivo de configuracion DEBE terminar con un salto de linea, sino el programa no lo reconocer� y se generar� un error.
