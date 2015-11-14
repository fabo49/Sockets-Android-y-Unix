package com.redes.fabian_stefano.sockets;

/**
 * Método que se encarga de controlar los eventos de la interfaz.
 * @author Fabian Rodriguez
 * @author Stefano Del Vecchio
 */
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Principal extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView text_resultados;
    private EditText input_direccion;
    private EditText input_cant_veces;
    private EditText input_puerto;
    private FloatingActionButton fab;

    private Spinner m_opciones_tamano;

    private String m_tamano_seleccionado;
    private String m_respuesta = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        text_resultados = (TextView) findViewById(R.id.text_resultados);
        input_direccion = (EditText) findViewById(R.id.input_direccion);
        input_puerto = (EditText) findViewById(R.id.input_puerto);
        input_cant_veces = (EditText) findViewById(R.id.input_cant_veces);
        m_opciones_tamano = (Spinner) findViewById(R.id.drop_tamanos);
        llena_spiner();

        m_opciones_tamano.setOnItemSelectedListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validaciones(v)){
                    //Obtiene los datos
                    int cant_veces = Integer.parseInt(input_cant_veces.getText().toString());
                    String direccion = input_direccion.getText().toString();
                    int puerto = Integer.parseInt(input_puerto.getText().toString());

                    //Hace el numero de envios con la cantidad que digito el usuario.
                    for(int i=0; i< cant_veces; ++i){
                        MyClientTask myClientTask = new MyClientTask(direccion, puerto);
                        myClientTask.start();
                        Principal.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                text_resultados.setText("*** Se inicia la conexión ***\n");
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            return true;
        }
        if(id==R.id.action_clear){
            vaciar_campos();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Método que llena las opciones disponibles para los tamaños de los archivos a enviar.
     * */
    private void llena_spiner(){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_spiner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        m_opciones_tamano.setAdapter(adapter);
    }

    /**
     * Método que limpia todos los campos de la interfaz y cierra el socket si está abierto.
     * @author Fabián Rodríguez
     * @author Stefano Del Vecchio
     * */
    private void vaciar_campos(){
        input_direccion.setText("");
        text_resultados.setText("");
        input_cant_veces.setText("");
        m_respuesta = "";

        input_puerto.setText("");
    }

    /**
     * Método que realiza las validaciones de los datos en la interfaz.
     * @author  Fabian Rodriguez
     * @author Stefano del Vecchio
     * @param v que es el View actual
     * @return true si son validos los campos, false si no
     */
    private boolean validaciones(View v){
        if(input_direccion.getText().length() != 0) {
            if(input_puerto.getText().length() != 0){
                if(input_cant_veces.getText().length() != 0){
                    return true;
                }else{
                    Snackbar alerta = Snackbar.make(v, "Tiene que ingresar la cantidad de envíos", Snackbar.LENGTH_LONG);
                    alerta.setAction("Revisar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            input_cant_veces.requestFocus();
                        }
                    });
                    alerta.show();
                    return false;
                }
            }else{
                Snackbar alerta = Snackbar.make(v, "Tiene que ingresar un puerto", Snackbar.LENGTH_LONG);
                alerta.setAction("Revisar", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        input_puerto.requestFocus();
                    }
                });
                alerta.show();
                return false;
            }

        }else{
            Snackbar alerta = Snackbar.make(v, "Tiene que ingresar una dirección IP", Snackbar.LENGTH_LONG);
            alerta.setAction("Revisar", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    input_direccion.requestFocus();
                }
            });
            alerta.show();
            return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        m_tamano_seleccionado = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //No hace nada
    }

    /**
     * Clase que se encarga de realizar la conexión por medio de los sockets. Lo realiza en un hilo separado ya que todas las
     * operaciones de red se tienen que realizar en un hilo en el "background".
     * @author Fabián Rodríguez
     * @author Stefano Del Vecchio
     */
    public class MyClientTask extends Thread{

        String dstAddress;
        int dstPort;

        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        public void run(){
            long t_inicial = 0;
            long t_final = 0;
            Socket socket_cliente = null;


            BufferedInputStream buffer_entrada;

            try {
                String ruta = "";
                switch(m_tamano_seleccionado){ //El nombre del archivo.
                    case "128 Kb":
                        ruta = "128k.txt";
                        break;
                    case "256 Kb":
                        ruta = "256k.txt";
                        break;
                    case "512 Kb":
                        ruta = "512k.txt";
                        break;
                }

                //Carga el archivo
                File archivo = new File(Environment.getExternalStorageDirectory(), "/archivos/"+ruta);
                byte[] buffer_lectura = new byte[(int)archivo.length()];

                socket_cliente = new Socket(dstAddress, dstPort);
                t_inicial = System.currentTimeMillis();

                buffer_entrada = new BufferedInputStream(new FileInputStream(archivo));
                buffer_entrada.read(buffer_lectura, 0, buffer_lectura.length);


                OutputStream stream_salida = socket_cliente.getOutputStream();
                stream_salida.write(buffer_lectura, 0, buffer_lectura.length);
                stream_salida.flush();



                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                InputStream inputStream = socket_cliente.getInputStream(); //Donde viene el ACK del servidor.

                int bytes_leidos;
                while ((bytes_leidos = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytes_leidos);
                    m_respuesta += byteArrayOutputStream.toString("UTF-8");
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                m_respuesta = "UnknownHostException: " + e.toString();

            } catch (IOException e) {
                e.printStackTrace();
                m_respuesta = "IOException: " + e.toString();
            }finally{
                if(socket_cliente != null){
                    try {
                        socket_cliente.close();
                        t_final = System.currentTimeMillis();
                        m_respuesta += "\nDuración --> "+(t_final - t_inicial)+" ms\n" +
                                "*** Se cerró la conexión ***\n";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Principal.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    text_resultados.append(m_respuesta + "\n");
                }
            });
        }

    }
}
