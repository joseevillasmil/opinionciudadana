package com.com.opinionciudadana.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.com.opinionciudadana.R;
import com.com.opinionciudadana.model.Encuesta;
import com.com.opinionciudadana.model.Respuesta;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class EncuestaActivity extends DefaultActivity {

    private AnyChartView chart;
    private Button yes;
    private Button no;
    private String key;
    private Pie pie;
    private FloatingActionButton compartir;
    private boolean usuarioHabilitado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        key = getIntent().getStringExtra("key");
        Log.i("debug", "Recibiendo la encuesta" + key);
        this.usuarioHabilitado = false;
        getEncuesta();
    }

    @Override
    void setLayout() {
        setContentView(R.layout.activity_encuesta);
    }

    @Override
    public void createViews() {
        super.createViews();
        chart = findViewById(R.id.any_chart_view);
        yes = findViewById(R.id.si);
        yes.setOnClickListener(view -> sendYes(view));
        no = findViewById(R.id.no);
        no.setOnClickListener(view -> sendNo(view));

    }


    public void getEncuesta() {
        firestoreManager.getDocument("encuestas", key, queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isSuccessful()) {
                DocumentSnapshot encuesta = queryDocumentSnapshots.getResult();
                Encuesta miEncuesta = encuesta.toObject(Encuesta.class);
                createChart(miEncuesta);


                Button si = findViewById(R.id.si);
                si.setText(miEncuesta.getPreguntas().get(0).toString());
                //si.setEnabled(false);
                si.setVisibility(View.INVISIBLE);

                Button no = findViewById(R.id.no);
                no.setText(miEncuesta.getPreguntas().get(1).toString());
                no.setVisibility(View.INVISIBLE);
                //no.setEnabled(false);
                Log.i("debug", "id de encuest: " + key);
                validarUsuario(key);

                this.compartir = findViewById(R.id.floatingActionButton);
                this.compartir.setOnClickListener(v -> {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, miEncuesta.getPregunta());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                });
            } else {
            }
        });
    }

    public boolean verificar() {
        if(this.usuarioHabilitado) {
            return true;
        }

        new AlertDialog.Builder(EncuestaActivity.this)
                .setTitle("Ups")
                .setMessage("Ya usted contestó esta encuesta")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        finish();
                    }
                }).show();

        return false;
    }

    public void sendYes(View view) {
        if(!this.verificar()) {
            return;
        }
        firestoreManager.getDocument("encuestas", key, queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isSuccessful()) {
                DocumentSnapshot encuesta = queryDocumentSnapshots.getResult();
                Encuesta miEncuesta = encuesta.toObject(Encuesta.class);
                int respuestasActuales = miEncuesta.getResultados().get(0);
                respuestasActuales++;
                miEncuesta.getResultados().set(0, respuestasActuales);
                firestoreManager.setField("encuestas", key, "resultados", miEncuesta.getResultados(), task -> {
                    addData(miEncuesta);
                    chart.refreshDrawableState();
                    this.setRespuesta(key);
                });
            }
        });
    }

    public void sendNo(View view) {
        if(!this.verificar()) {
            return;
        }
        firestoreManager.getDocument("encuestas", key, queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isSuccessful()) {
                DocumentSnapshot encuesta = queryDocumentSnapshots.getResult();
                Encuesta miEncuesta = encuesta.toObject(Encuesta.class);
                int respuestasActuales = miEncuesta.getResultados().get(1);
                respuestasActuales++;
                miEncuesta.getResultados().set(1, respuestasActuales);
                firestoreManager.setField("encuestas", key, "resultados", miEncuesta.getResultados(), task -> {
                    addData(miEncuesta);
                    chart.refreshDrawableState();
                    this.setRespuesta(key);
                });
            }
        });
    }

    public void validarUsuario(String encuestaId) {
        this.usuarioHabilitado = false;
        firestoreManager.getDocument("users", authManager.getUser().getId(), task -> {
            if(!task.isSuccessful()) {
                Log.i("debug", "la validación fallo");
                return;
            }
            DocumentSnapshot _respuesta = task.getResult();
            Respuesta respuesta = _respuesta.toObject(Respuesta.class);

            if(respuesta == null || respuesta.getEncuestas() == null) {
                Log.i("debug", "la validación llega nula x2");
                this.usuarioHabilitado = true;
                findViewById(R.id.si).setEnabled(true);
                findViewById(R.id.no).setEnabled(true);
                return;
            }

            boolean activar = true;
            for (String res : respuesta.getEncuestas()) {
                Log.i("debug", res);
                Log.i("debug", encuestaId);
                if(res.equals(encuestaId)) {
                    Log.i("debug", "lo anulé");
                    activar = false;
                }
            }

            this.usuarioHabilitado = activar;
            findViewById(R.id.si).setEnabled(activar);
            findViewById(R.id.no).setEnabled(activar);

        });
    }

    public void setRespuesta(String key) {
        firestoreManager.getDocument("users", authManager.getUser().getId(), task1 -> {
            if(!task1.isSuccessful()) {
                Log.i("debug", "esta nula");
                return;
            }

            DocumentSnapshot document = task1.getResult();
            Respuesta respuesta = document.toObject(Respuesta.class);
            if(respuesta == null) {
                Log.i("debug", "respuesta nula");
                respuesta = new Respuesta();
            }

            if(respuesta.getEncuestas() == null) {
                Log.i("debug", "nulo nulo");
                respuesta.setEncuestas(new ArrayList<>());
            }

            respuesta.getEncuestas().add(key);
            Log.i("debug", "agregamos el key " + key);
            firestoreManager.saveObject("users", authManager.getUser().getId(), respuesta, task2 -> {
                Log.i("debug", "llegue");
            });
        });
        validarUsuario(key);

        new AlertDialog.Builder(EncuestaActivity.this)
                .setTitle("Muchas gracias!!")
                .setMessage("Su respuesta fue guardada satisfactoriamente")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        finish();
                    }
                }).show();
    }


    public void createChart(Encuesta encuesta) {
        pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(thisActivity, event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        Log.i("debug", "Resultados"  + encuesta.getResultados().get(0).toString());
        data.add(new ValueDataEntry(encuesta.getPreguntas().get(0), encuesta.getResultados().get(0)));
        data.add(new ValueDataEntry(encuesta.getPreguntas().get(1), encuesta.getResultados().get(1)));

        pie.data(data);

        pie.title(encuesta.getPregunta());

        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("Opciones")
                .padding(0d, 0d, 10d, 0d);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);
        chart.setChart(pie);


    }


    public void addData(Encuesta encuesta) {
        /* List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry(encuesta.getEncuestas().get(0), encuesta.getResultados().get(0)));
        data.add(new ValueDataEntry(encuesta.getEncuestas().get(1), encuesta.getResultados().get(1)));
        pie.data(data);
         */
    }

}
