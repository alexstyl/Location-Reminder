package com.alexstyl.locationreminder.util;

import android.content.Context;
import android.location.Location;

import com.alexstyl.locationreminder.DeLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p>Created by alexstyl on 28/02/15.</p>
 */
final public class AddressHelper {


    private static final String TAG = "AddressHelper";
    private static final String PROVIDER = "map-api";

    private AddressHelper() {
    }

    /**
     * https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA
     */
    private static final String URL_MAPS_UPI =
            "https://maps.googleapis.com/maps/api/geocode/json?address=";

    /**
     * @param address
     */
    public static Location convertToLocation(Context context, String address) {

        if (!Utils.isOnline(context)) {
            DeLog.v(TAG, "Can't use the JSON API while offline!");
            return null;
        }
        Location location = null;
        address = URL_MAPS_UPI + address.replace(" ", "+");
        String response = connect(address);
        if (response != null) {
            try {
                JSONObject respObject = new JSONObject(address);
                location = extractLocation(respObject);
            } catch (JSONException e) {
                DeLog.log(e);
            }
        }

        return location;
    }

    private static Location extractLocation(JSONObject object) throws JSONException {
        if (object == null) {
            return null;
        }

        JSONObject geometry = (JSONObject) ((JSONObject) object.get("results")).get("geometry");
        JSONObject location = (JSONObject) geometry.get("location");
        Location loc = new Location(PROVIDER);
        double lat = location.getDouble("lat");
        double lng = location.getDouble("lng");
        loc.setLongitude(lng);
        loc.setLatitude(lat);
        return loc;
    }

    private static String connect(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                String result = convertStreamToString(instream);
                instream.close();
                return result;
            }
        } catch (ClientProtocolException e) {
            DeLog.log(e);
        } catch (IOException e) {
            DeLog.log(e);
        }
        return null;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
