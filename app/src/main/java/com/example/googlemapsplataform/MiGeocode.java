package com.example.googlemapsplataform;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MiGeocode extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap gMap;
    Marker marker;
    EditText midireccion;
    String nro = "", address = "";
    JsonObjectRequest jsonObjectRequest;
    RequestQueue request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_geocode);
        midireccion = findViewById(R.id.midireccion);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
        request = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        LatLng Tacna = new LatLng(-18.011737, -70.253529);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Tacna, 15));
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (marker != null) marker.remove();
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                marker = gMap.addMarker(markerOptions);
            }
        });
    }

    public void geocode(View view) {
        if (marker != null) {
            Double latitude = marker.getPosition().latitude;
            Double longitude = marker.getPosition().longitude;
            Log.i("ubicacionmarker", "lat:" + latitude + " lon:" + longitude);
            String URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                    + latitude + "," + longitude + "&key=AIzaSyBxdHb0cog-Wt-zkiXepjZBYq-YDFUUpVE";
            jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject result) {
                    try {
                        if (result.has("results")) {
                            JSONArray array = result.getJSONArray("results");
                            if (array.length() > 0) {
                                JSONObject place = array.getJSONObject(0);
                                JSONArray components = place.getJSONArray("address_components");
                                for (int i = 0; i < components.length(); i++) {
                                    JSONObject component = components.getJSONObject(i);
// Se obtiene el campo del JSONArray "types" y se guarda en otro
                                    JSONArray types = component.getJSONArray("types");
                                    Log.i("Estadovalortype", types.toString());
// Se coge solo el primer elemento del JSONArray
                                    String type = types.getString(0);
//Se verifica que el valor del type sea igual a "route"
                                    if (type.equals("route")) {
                                        Log.i("Estadovalor", "ingreso");
                                        try {
// Se formatea el valor del address para UTF-8 para reconocer tildes
                                            address = new String(component.getString("short_name").getBytes(), "UTF-8");
                                            Log.i("Estadovalor", address);
                                        } catch (UnsupportedEncodingException e) {
                                            e.toString();
                                            Log.i("mierror0", e.toString());
                                        }
                                    }

                                    if (type.equals("street_number")) {
                                        try {
                                            nro = new String(component.getString("short_name").getBytes(), "UTF-8");
                                            Log.i("Estadovalor", nro);
                                        } catch (UnsupportedEncodingException e) {
                                            e.toString();
                                            Log.i("mierror0", e.toString());
                                        }
                                    }
                                }
                                // Se verifica que el campo no sea vacio
                                if (!address.isEmpty()) {
                                    Log.i("Estadovalor", address);
// Se crea una tarea y se coloca en el hilo principal
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            midireccion.setText(address + " " + nro);
                                        }
                                    });
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i("mierror", e.toString());
                    } catch (Exception e) {
                        Log.i("mierror2", e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "No se puede conectar " + error.toString(), Toast.LENGTH_LONG).show();
                    System.out.println();
                    Log.d("ERROR: ", error.toString());
                }
            }
            );
            request.add(jsonObjectRequest);
        }
    }
}
