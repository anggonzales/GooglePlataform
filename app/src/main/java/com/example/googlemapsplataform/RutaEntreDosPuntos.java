package com.example.googlemapsplataform;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RutaEntreDosPuntos extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap gMap;
    Polyline polyline = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    JsonObjectRequest jsonObjectRequest;
    RequestQueue request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruta_entre_dos_puntos);
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
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                Marker marker = gMap.addMarker(markerOptions);
                latLngList.add(latLng);
                markerList.add(marker);
            }
        });
    }

    public void dibujar(View view) {
        if (polyline != null) polyline.remove();
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(latLngList).clickable(true);
        polyline = gMap.addPolyline(polylineOptions);
    }

    public void limpiar(View view) {
        if (polyline != null) polyline.remove();
        for (Marker marker : markerList) marker.remove();
        latLngList.clear();
        markerList.clear();
    }

    public void dibujaRuta(View view) {

        Log.i("miposicionsize", "size:" + latLngList.size());
        if (latLngList.size() > 1) {
            Log.i("miposicion1", "lat1:" + latLngList.get(0).latitude + " lon1:" + latLngList.get(0).longitude);
            Log.i("miposicion2", "lat2:" + latLngList.get(1).latitude + " lon2:" + latLngList.get(1).longitude);
            String latinicial = String.valueOf(latLngList.get(0).latitude);
            String loninicial = String.valueOf(latLngList.get(0).longitude);
            String latfinal = String.valueOf(latLngList.get(1).latitude);
            String lonfinal = String.valueOf(latLngList.get(1).longitude);
            webServiceObtenerRuta(latinicial, loninicial, latfinal, lonfinal);
        }

    }

    public void Decode(View view) {
        DecodePolyline("bj~lBh}xkLYBWNKX?Ro@e@u@w@yBsC}CaEuAyBwA{Be@@aBF_@Dq@Ju@Ps@TiEiIgDcGwC|By@h@");
    }

    Polyline polyline2 = null;

    public void DecodePolyline(String encodepolyline) {
        List<LatLng> list = PolyUtil.decode(encodepolyline);
        if (polyline2 != null) polyline2.remove();
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(list).clickable(true);
        polyline2 = gMap.addPolyline(polylineOptions);
        polyline2.setColor(Color.BLUE);
    }

    private void webServiceObtenerRuta(String latitudInicial, String longitudInicial, String latitudFinal, String longitudFinal) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + latitudInicial + "," + longitudInicial
                + "&destination=" + latitudFinal + "," + longitudFinal + "&key=";
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray jRoutes = null;
                try {
                    jRoutes = response.getJSONArray("routes");
                    String polyline = "";
                    polyline = (String) ((JSONObject) ((JSONObject) jRoutes.get(0)).get("overview_polyline")).get("points");
                    DecodePolyline(polyline);
                    JSONArray jLegs = null;
                    jLegs = ( (JSONObject)jRoutes.get(0)).getJSONArray("legs");
                    String distancia=(String) ((JSONObject) ((JSONObject) jLegs.get(0)).get("distance")).get("text");
                    String tiempo=(String) ((JSONObject) ((JSONObject) jLegs.get(0)).get("duration")).get("text");
                    Log.i("distanciatiempo","distancia:"+distancia+" tiempo:"+tiempo);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No se puede conectar " + error.toString(), Toast.LENGTH_LONG).show();
                System.out.println();
                Log.d("ERROR: ", error.toString());
            }
        });
        request.add(jsonObjectRequest);
    }
}
