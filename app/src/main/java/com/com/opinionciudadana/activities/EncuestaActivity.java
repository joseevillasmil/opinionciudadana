package com.com.opinionciudadana.activities;

import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EncuestaActivity extends DefaultActivity {

    private AnyChartView chart;
    private Button yes;
    private Button no;
    private String key;

    private Pie pie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        key = getIntent().getStringExtra("key");

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
            } else {
            }
        });
    }

    public void sendYes(View view) {
        firestoreManager.getDocument("encuestas", key, queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isSuccessful()) {
                DocumentSnapshot encuesta = queryDocumentSnapshots.getResult();
                Encuesta miEncuesta = encuesta.toObject(Encuesta.class);
                int respuestasActuales = miEncuesta.getResultados().get(0);
                respuestasActuales++;
                miEncuesta.getResultados().set(0, respuestasActuales);
                firestoreManager.setField("encuestas", key, "respuestas", miEncuesta.getResultados(), task -> {
                    addData(miEncuesta);
                    chart.refreshDrawableState();
                });
            }
        });
    }

    public void sendNo(View view) {
        firestoreManager.getDocument("encuestas", key, queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isSuccessful()) {
                DocumentSnapshot encuesta = queryDocumentSnapshots.getResult();
                Encuesta miEncuesta = encuesta.toObject(Encuesta.class);
                int respuestasActuales = miEncuesta.getResultados().get(1);
                respuestasActuales++;
                miEncuesta.getResultados().set(1, respuestasActuales);
                firestoreManager.setField("encuestas", key, "respuestas", miEncuesta.getResultados(), task -> {
                    addData(miEncuesta);
                    chart.refreshDrawableState();
                });
            }
        });
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
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry(encuesta.getPreguntas().get(0), encuesta.getResultados().get(0)));
        data.add(new ValueDataEntry(encuesta.getPreguntas().get(1), encuesta.getResultados().get(1)));
        pie.data(data);
    }
}
