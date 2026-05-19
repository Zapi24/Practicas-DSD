import { webcrypto } from 'node:crypto';
if (!globalThis.crypto) globalThis.crypto = webcrypto;

import http from 'node:http';
import { join } from 'node:path';
import { readFile } from 'node:fs';
import { Server } from 'socket.io';
import { MongoClient } from 'mongodb';
import TelegramBot from 'node-telegram-bot-api';

const httpServer = http.createServer((request, response)=>{
    
    //Extraemos la url
    let {url} = request;

    let pantalla = ''; //Almacenará el archivo html que se meta dentro de la url

    let tipoContenido = 'text/html; charset=utf-8'; // Por defecto es HTML

    //Si el usuario accede a la raiz, le pasamos el panel de control de usuario
    if(url === '/' || url === '/usuario'){ // Le pongo tambien el de usuario para evitar incongruencias

        pantalla = '/public/usuario.html';

    }else if(url === '/sensores'){ //Le enviamos el panel de los sensores

        pantalla = '/public/sensores.html';

    }else if(url === '/estilos.css'){ //Para que cargue el css
        
        pantalla = '/public/estilos.css';
        tipoContenido = 'text/css'; //Cambiamos el tipo para que el navegador lo entienda
    
    }else{ //Para la gestión de errores

        response.writeHead(404, {'Content-Type': 'text/plain; charset=utf-8'});
        response.write('404 - ERROR, especifica una url válida');
        response.end();
        return; //Cerramos al ejecución
    }

    // Creamos la ruta correcta del archivo html que queremos mostrar
    const archivo = join(process.cwd(), pantalla);

    //Leemos el archivo seleccionado
    readFile(archivo, (err, data)=>{

        if(!err){   //Si no hay errores tenemos que devolver el código 200
            response.writeHead(200, {'Content-Type': tipoContenido});
            response.write(data);

        }else{
            //Por si el archivo se encuentra una ruta que no existe o hay algun problema de persmisos (el codigo 500 es por convección)
            response.writeHead(500, {"Content-Type": "text/plain; charset=utf-8"});
            response.write(`ERROR, no se ha podido leer el html del disco ${pantalla}`);
        }
        response.end(); //Cerramos
    });
});


// Definimos el estado inicial de la casa
//ACLARACIÓN: false = cerrado/apagado y true = abierto/encendido
let estadoCasa={
    temp: 23,   //!!Este valor se sobreescribe con la temperatura de granada recogida de la API
    luz: 50,
    persiana: false, 
    ac: false, // el aire acondicionado
    ventana: false,
    alertaEnviada: false // Para que el bot no me spamee por telegram
};

//Integración con Api Telegram
// Configuración directa de Telegram (¡ACUÉRDATE DE BORRAR LOS NÚMEROS AL ENTREGAR!)
const token = 'introduce aqui el token del bot'; 
const chatId = 'introduce aqui el id del caht';
let bot = new TelegramBot(token, {polling: false});
console.log("Bot de Telegram configurado y listo para avisar.");

cl
// Función principal del agente
function evaluarAgente(){
    let alertas = [];

    //La persiana depende de  la luz (valor entre 0 - 100)
    if(estadoCasa.luz > 80 && estadoCasa.persiana === true){ //Cuando la luz sea mayor a 80 cerramos la persiana

        estadoCasa.persiana = false;
        alertas.push("Luminosidad alta: CERRAMOS la persiana automáticamente.");

    }else if(estadoCasa.luz < 20 && estadoCasa.persiana === false){ // Cuando la luz sea menos a 20 abrimos la persiana
        
        estadoCasa.persiana = true;
        alertas.push("Luminosidad baja: ABRIMOS LA PERSIANA automáticamente.");
    }

    // EL aire acondicionado depende de la temperatura (valor entre -5 - 45)
    if(estadoCasa.temp > 26 && estadoCasa.ac === false){

        estadoCasa.ac = true;
        alertas.push("Temperatura alta: ENCENDEMOS el aire acondicionado automáticamente.");
        
    }else if(estadoCasa.temp < 20 && estadoCasa.ac === true){

        estadoCasa.ac = false;
        alertas.push("Temperatura baja: APAGAMOS el aire acondicionado automáticamente.");

    }

    //Aqui vemos si está el aire acondicionado y la ventana abierta al mismo tiempo
    if(estadoCasa.ac === true && estadoCasa.ventana === true){
        alertas.push("CUIDADO: El aire acondicionado está abierto junto a la ventana.");
        
        // Enviamos la alerta por telegram tambien
        if(estadoCasa.alertaEnviada === false){

            bot.sendMessage(chatId, "ALERTA: El aire acondicionado está abierto junto a la ventana");
            estadoCasa.alertaEnviada = true; 
            console.log("Aviso enviado a Telegram.");
        }

    }else{

        // Para que vuelva enviar la alerta
        estadoCasa.alertaEnviada = false;
    }

    //Si no hay alarmas, enviamos un mensaje de normalidad
    if(alertas.length === 0){

        alertas.push("Todo correcto por ahora (xd).");
    }

    return alertas;
}

//Iniciamos la conexión a mongodb
MongoClient.connect("mongodb://localhost:27017/").then(async (db) => {
    console.log("Conexion exitosa a MongoDB");
    const dbo = db.db("sistemaDomotico");
    const historial = dbo.collection("eventos");

    //Definimos el WebSocket
    const io = new Server(httpServer);

    //Funcion para ir almacenando los eventos en la base de datos con la respectiva fecha
    function registrarEvento(mensaje) {
        const registro = { fecha: new Date(), accion: mensaje };
        historial.insertOne(registro).then(() => {
            //Cuando se realice la operación, hay que avisar a todos los clientes para que actualicen sus listas
            io.emit('nuevo-evento', registro);
        });
    }

    //API para coger la temperatura de Gradana
    try{

        console.log("Consultando la temperatura de Granada...");
        const respuesta = await fetch("https://api.open-meteo.com/v1/forecast?latitude=37.1882&longitude=-3.6067&current_weather=true");
        const datosClima = await respuesta.json();
        estadoCasa.temp = Math.round(datosClima.current_weather.temperature);
        registrarEvento(`Temperatura de Granada obtenida de Internet: ${estadoCasa.temp} C`);

    }catch(error){
        console.log("ERROR: no se ha obtenido la temperatura (se recoje el valor de 22 grados por defecto).");
        registrarEvento("No se ha recogido correctamente la temperatura (Temperatura por defecto).");
    }
    //Websocket:
    io.on('connection', (socket) => {
        
        //Estado inicial, hay que mostrar el estado actual (inicial) cuando se conecte el cliente
        socket.emit('actualizar-estado', estadoCasa);
        socket.emit('actualizar-alertas', evaluarAgente());

        //Al conectarse un usuario, le enviamos toooodo el historial de la BD
        historial.find().sort({fecha: -1}).toArray().then((resultados) => { 
            socket.emit('historial-completo', resultados);
        });

        // Escuhchamos cuando cambiar los valores de luz y temperatura
        socket.on('cambio-sensor', (datos) => {
            if (datos.tipo === 'temp') {
                estadoCasa.temp = datos.valor;
                registrarEvento(`El sensor de TEMPERATURA cambio a ${datos.valor} C`);
            }
            if (datos.tipo === 'luz') {
                estadoCasa.luz = datos.valor;
                registrarEvento(`El sensor de LUZ cambio a ${datos.valor} %`);
            }

            const alertasActuales = evaluarAgente();
            io.emit('actualizar-estado', estadoCasa);
            io.emit('actualizar-alertas', alertasActuales);
        });

        //Escuchamos cuando cambian los valores manuales (boolenaos)
        socket.on('comando-manual', (comando) => {
            if (comando === 'persiana') {
                estadoCasa.persiana = !estadoCasa.persiana;
                registrarEvento(`Usuario manual: Persiana ${estadoCasa.persiana ? 'ABIERTA' : 'CERRADA'}`);
            }
            if (comando === 'ac') {
                estadoCasa.ac = !estadoCasa.ac;
                registrarEvento(`Usuario manual: Aire Acondicionado ${estadoCasa.ac ? 'ENCENDIDO' : 'APAGADO'}`);
            }
            if (comando === 'ventana') {
                estadoCasa.ventana = !estadoCasa.ventana;
                registrarEvento(`Usuario manual: Ventana ${estadoCasa.ventana ? 'ABIERTA' : 'CERRADA'}`);
            }

            const alertasActuales = evaluarAgente();
            io.emit('actualizar-estado', estadoCasa);
            io.emit('actualizar-alertas', alertasActuales);
        });
    });


    //Así es mas comodo acceder a los distintos paneles
    httpServer.listen(8080, () => {
        console.log('==================================================');
        console.log('SISTEMA DOMÓTICO INICIADO');
        console.log('Panel de usuario: http://localhost:8080/');
        console.log('Panel de sensores: http://localhost:8080/sensores');
        console.log('==================================================');
    });

}).catch((err) => {
    console.error("FALLO: Es posible que Mongo DB no esé encendido.", err);
});