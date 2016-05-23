package techafrkix.work.com.spot.techafrkix.work.com.spot.utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.spec.ECField;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import techafrkix.work.com.spot.bd.Spot;

/**
 * Created by techafrkix0 on 23/05/2016.
 */
public class DBServer {

    private static final String BASE_URL = "https://spotitproject.herokuapp.com";
    private static final String API_KEY = "012YEYQS5653278GHQSD234671QSDF26";
    private static final String URL_USER = "/api/users";
    private static final String URL_SPOT = "/api/spots";
    private static final String URL_LIST_COMMENTAIRE = "/api/spotscoms";
    private static final String URL_TAG = "/api/spotstags";
    private static final String URL_COMMENTAIRE = "/api/commentaires";
    private static final String URL_FIND_SPOT = "/api/findspots";
    private static final String URL_FIND_SPOT_2 = "/api/findspotswithtagorhash";
    private static final String URL_ADD_FRIEND = "/api/friendships";
    private static final String URL_LIST_FRIEND = "/api/friendships";
    private static final String URL_LIST_NOTIFICATION = "/api/notifications";
    private static final String URL_FIND_USER = "/api/findusers";

    private static final int CONNEXION_FB = 11;
    private static final int CONNEXION_NORMAL = 1;

    private static final String TAG = "DBServer";

    private URL url;
    private HttpURLConnection client;
    private OutputStream outputPost;
    private Context context;

    public DBServer(Context ctx){
        context = ctx;
    }

    public int inscription(String email, String pseudo, String password, int typeconnexion_id){

        try {
            url = new URL(BASE_URL+URL_USER);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setDoOutput(true);

            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));

            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("email", email);
            values.put("pseudo", pseudo);
            values.put("password", password);
            values.put("typeconnexion_id", typeconnexion_id);

            writer.write(getQuery(values));
            writer.flush();
            writer.close();
            outputPost.close();
            Log.i(TAG, getQuery(values));

            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line+"\n");
            }
            br.close();

            try {
                JSONObject json = new JSONObject(builder.toString());
                int statut = Integer.valueOf(json.getString("statut"));
                if (statut == 1){
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("insertId = "+json.getString("insertId"));
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "reponse = "+builder.toString());

        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return 1;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return 1;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.toString());
            return 1;
        }
        finally {
            client.disconnect();
        }


        return 0;
    }

    public int getUser_by_pseudo(String pseudo){

        try {
            url = new URL(BASE_URL+URL_FIND_USER);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setDoOutput(true);

            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));

            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("pseudo", pseudo);

            writer.write(getQuery(values));
            writer.flush();
            writer.close();
            outputPost.close();
            Log.i(TAG, getQuery(values));

            StringBuilder builder = new StringBuilder();
            builder.append(client.getResponseCode())
                    .append(" ")
                    .append(client.getResponseMessage())
                    .append("\n");

            Map<String, List<String>> map = client.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet())
            {
                if (entry.getKey() == null)
                    continue;
                builder.append( entry.getKey())
                        .append(": ");

                List<String> headerValues = entry.getValue();
                Iterator<String> it = headerValues.iterator();
                if (it.hasNext()) {
                    builder.append(it.next());

                    while (it.hasNext()) {
                        builder.append(", ")
                                .append(it.next());
                    }
                }

                builder.append("\n");
            }

            Log.i(TAG, "reponse = "+builder.toString());

        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return 1;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return 1;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.toString());
            return 1;
        }
        finally {
            client.disconnect();
        }


        return 0;
    }

    public int enregistre_spot(Spot spot){

        try {
            url = new URL(BASE_URL+URL_SPOT);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setDoOutput(true);

            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));

            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("respot", spot.getRespot());
            values.put("visibilite_id", spot.getvisibiliteId());
            values.put("hash", spot.getGeohash());
            values.put("long", spot.getLongitude());
            values.put("lat", spot.getLatitude());
            values.put("photo", spot.getPhotokey());

            writer.write(getQuery(values));
            writer.flush();
            writer.close();
            outputPost.close();
            Log.i(TAG, getQuery(values));

            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line+"\n");
            }
            br.close();

            try {
                JSONObject json = new JSONObject(builder.toString());
                int statut = Integer.valueOf(json.getString("statut"));
                if (statut == 1){
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("insertId = "+json.getString("insertId"));
                }
                else
                {
                    builder.append("statut = "+json.getString("statut")+"\n");
                    builder.append("errcode = "+json.getString("errcode")+"\n");
                    builder.append("message = "+json.getString("message")+"\n");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "reponse = "+builder.toString());

        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return 1;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return 1;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.toString());
            return 1;
        }
        finally {
            client.disconnect();
        }

        return 0;
    }

    private String getQuery(ContentValues params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : params.valueSet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        }

        return result.toString();
    }
}
