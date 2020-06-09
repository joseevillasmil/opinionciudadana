package com.com.opinionciudadana.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.com.opinionciudadana.R;
import com.com.opinionciudadana.activities.EncuestaActivity;
import com.com.opinionciudadana.adapters.EncuestasAdapter;
import com.com.opinionciudadana.model.Encuesta;
import com.google.firebase.firestore.DocumentSnapshot;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class ListFragment extends DefaultFragment {

    private List<String> keys;
    private List<String> titulos;
    private List<String> filtrados;
    private RecyclerView lista;
    private String userId;
    private EditText searchText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        getEncuestas();
        this.searchText = root.findViewById(R.id.editText);
        this.searchText.setText("");
        this.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                filter(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return root;
    }

    @Override
    public View setFragmentLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frament_list, container, false);
    }

    @Override
    public void createViewItems(View root) {
        lista = root.findViewById(R.id.encuestas_list);

    }


    public void getEncuestas() {
        firestoreManager.getCollection("encuestas", queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isSuccessful()) {
                List<DocumentSnapshot> encuestas = queryDocumentSnapshots.getResult().getDocuments();
                keys = new ArrayList<>();
                titulos = new ArrayList();
                for(int i = 0; i < encuestas.size() ; i++) {
                    DocumentSnapshot encuestaSnap = encuestas.get(i);
                    keys.add(i, encuestaSnap.getId());
                    Encuesta encuesta = encuestaSnap.toObject(Encuesta.class);
                    titulos.add(i, encuesta.getPregunta());
                    Gson gson = new Gson();
                    String string = gson.toJson(encuesta);
                }
                this.filter("");
            } else {
            }
        });
    }

    /* Filtramos la encuesta */
    public void filter(String texto) {
        filtrados = new ArrayList<>();
        for(int i = 0; i < titulos.size() ; i++) {
            if(titulos.get(i).contains(texto) || texto.equals("") || texto.isEmpty()) {
                filtrados.add(titulos.get(i));
            }
        }

        final EncuestasAdapter encuestasAdapter = new EncuestasAdapter(filtrados);
        encuestasAdapter.setOnClickListener(v -> goToEncuestaPage(keys.get(lista.getChildAdapterPosition(v))));

        lista.setAdapter(encuestasAdapter);
        lista.setLayoutManager(new LinearLayoutManager(thisFragment.getActivity(), LinearLayoutManager.VERTICAL, false));
        lista.invalidate();

    }

    public void goToEncuestaPage(String key){
        Intent intent = new Intent(thisFragment.getActivity(), EncuestaActivity.class);
        intent.putExtra("key", key);
        Log.i("debug", "Enviando la encuesta" + key);
        startActivity(intent);
    }
}