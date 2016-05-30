package techafrkix.work.com.spot.techafrkix.work.com.spot.utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import techafrkix.work.com.spot.bd.Commentaire;
import techafrkix.work.com.spot.bd.MaBaseOpenHelper;
import techafrkix.work.com.spot.bd.Notification;
import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.bd.Tag;
import techafrkix.work.com.spot.bd.Utilisateur;

/**
 * Created by techafrkix0 on 23/05/2016.
 */
public class DBServer {

    private static final String BASE_URL = "https://spotitproject.herokuapp.com";
    private static final String API_KEY = "012YEYQS5653278GHQSD234671QSDF26";
    private static final String URL_USER = "/api/users";
    private static final String URL_CHECK = "/api/userwithpseudo";
    private static final String URL_LOGIN = "/api/login";
    private static final String URL_SPOT = "/api/spots";
    private static final String URL_LIST_COMMENTAIRE = "/api/spotscoms";
    private static final String URL_TAG = "/api/spotstags";
    private static final String URL_ADD_TAG = "/api/tags";
    private static final String URL_COMMENTAIRE = "/api/commentaires";
    private static final String URL_FIND_SPOT = "/api/findspots";
    private static final String URL_FIND_SPOT_2 = "/api/findspotswithtagorhash";
    private static final String URL_ADD_FRIEND = "/api/friendships";
    private static final String URL_LIST_FRIEND = "/api/friendships";
    private static final String URL_LIST_NOTIFICATION = "/api/notifications";
    private static final String URL_FIND_USER = "/api/findusers";
    private static final String URL_FIND_SPOT_USER = "/api/allspotslistwithoffset";

    public static final int CONNEXION_FB = 11;
    public static final int CONNEXION_NORMAL = 1;

    public static final int SUCCESS = 0;

    private static final String TAG = "DBServer";

    private URL url;
    private HttpURLConnection client;
    private OutputStream outputPost;
    private Context context;

    public DBServer(Context ctx){
        context = ctx;
    }

    /**
     * fonction qui permet d'enregistrer un nouvel utilisateur dans l'application
     * @param email son email
     * @param pseudo son pseudo qui represente son nom dans l'application
     * @param password son mot de passe
     * @param typeconnexion_id ceci renseigne s'il s'est connecté avec facebook ou non
     * @param birth date de naissance au format yyyy-mm-dd
     * @return retourne un entier qui represente le code de retour
     */
    public int register(String email, String pseudo, String password, int typeconnexion_id, String birth){

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
            values.put("birth", birth);

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
                    builder.append("message = " + json.getString("message"));
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

    /**
     * fonction qui retourne la liste des amis d'un utilisateurs
     * @param user_one id du premier utilisateur
     * @param user_two id de l'utilisateur ami
     * @return retourne le code de retour
     */
    public int add_friend(int user_one, int user_two){
        try {
            url = new URL(BASE_URL+URL_ADD_FRIEND);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setDoOutput(true);

            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));

            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("user_one", user_one);
            values.put("user_two", user_two);

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
                    builder.append("message = " + json.getString("message"));
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

    /**
     * fonction qui permet de loguer un utilisateur
     * @param email email de l'utilisateur
     * @param typeconnexion_id variable représentant le type de connexion qu'il a utilisé pour créer son compte
     * @param password son mot de passe qui est aléatoire s'il a utilisé facebook
     * @return retourne un utilisateur si ok ou null dans le cas contraire
     */
    public Utilisateur login(String email, int typeconnexion_id, String password){

        Utilisateur user = new Utilisateur();
        ContentValues values = new ContentValues();
        values.put("apikey", API_KEY);
        values.put("email", email);
        values.put("password", password);
        values.put("typeconnexion_id", typeconnexion_id);
        try {
            url = new URL(BASE_URL+URL_LOGIN+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            //client.setDoOutput(true);

            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line+"\n");
            }
            br.close();

            Log.i(TAG, "reponse = " + builder.toString());
            try {
                JSONObject json = new JSONObject(builder.toString());
                int statut = Integer.valueOf(json.getString("statut"));
                if (statut == 1){
                    JSONObject json2 = json.getJSONObject("data");

                    user.setPassword((String) json2.get(MaBaseOpenHelper.COLONNE_PASSWORD));
                    user.setEmail((String) json2.get(MaBaseOpenHelper.COLONNE_EMAIL));
                    user.setDate_naissance((String) json2.get(MaBaseOpenHelper.COLONNE_DATE_NAISSANCE));
                    user.setId((int) json2.get(MaBaseOpenHelper.COLONNE_USER_ID));
                    user.setPseudo((String) json2.get(MaBaseOpenHelper.COLONNE_PSEUDO));
                    user.setPhoto((String) json2.get(MaBaseOpenHelper.COLONNE_PHOTO_PROFILE));
                    user.setCreated((String) json2.get(MaBaseOpenHelper.COLONNE_CREATED));
                    user.setNbrespot((int) json2.get(MaBaseOpenHelper.COLONNE_NB_RESPOT));
                    user.setNbspot((int) json2.get(MaBaseOpenHelper.COLONNE_NB_SPOT));
                    user.setTypeconnexion_id((int) json2.get(MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID));

                    return user;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.toString());
            return null;
        }
        finally {
            client.disconnect();
        }
    }

    /**
     * fonction qui retourne la liste des utilisateurs qui ont pour pseudo qui contient une chaine
     * @param pseudo variable représentant le pseudo donné
     * @return retourne une liste d'utilisateurs ou null dans le cas contraire
     */
    public ArrayList<Utilisateur> getUsers_by_pseudo(String pseudo){

        ArrayList<Utilisateur> users = new ArrayList<Utilisateur>();
        Utilisateur user = new Utilisateur();
        try {
            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put(MaBaseOpenHelper.COLONNE_PSEUDO, pseudo);

            url = new URL(BASE_URL+URL_FIND_USER+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            //client.setDoOutput(true); this method put automatically the method to POST
            client.setRequestMethod("GET");

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
                    JSONArray jArr = json.getJSONArray("data");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject json2 = jArr.getJSONObject(i);

                        user.setPassword((String) json2.get(MaBaseOpenHelper.COLONNE_PASSWORD));
                        user.setEmail((String) json2.get(MaBaseOpenHelper.COLONNE_EMAIL));
                        user.setDate_naissance((String) json2.get(MaBaseOpenHelper.COLONNE_DATE_NAISSANCE));
                        user.setId((int) json2.get(MaBaseOpenHelper.COLONNE_USER_ID));
                        user.setPseudo((String) json2.get(MaBaseOpenHelper.COLONNE_PSEUDO));
                        user.setPhoto((String) json2.get(MaBaseOpenHelper.COLONNE_PHOTO_PROFILE));
                        user.setCreated((String) json2.get(MaBaseOpenHelper.COLONNE_CREATED));
                        user.setNbrespot((int) json2.get(MaBaseOpenHelper.COLONNE_NB_RESPOT));
                        user.setNbspot((int) json2.get(MaBaseOpenHelper.COLONNE_NB_SPOT));
                        user.setTypeconnexion_id((int) json2.get(MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID));

                        users.add(user);
                    }
                    Log.i(TAG, "reponse = " + builder.toString());
                    return users;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.getMessage());
            return null;
        }
        finally {
            client.disconnect();
        }
    }


    /**
     * fonction qui teste si un utilisateur avec un pseudo donné existe
     * @param pseudo pseudo à tester
     * @return retourne un utilisateur s'il existe ou null dans le cas contraire
     */
    public Utilisateur getUser_by_pseudo(String pseudo){

        Utilisateur user = new Utilisateur();
        try {
            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put(MaBaseOpenHelper.COLONNE_PSEUDO, pseudo);

            url = new URL(BASE_URL+URL_CHECK+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            //client.setDoOutput(true); this method put automatically the method to POST
            client.setRequestMethod("GET");

            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line+"\n");
            }
            br.close();
            Log.i(TAG, "reponse = " + builder.toString());

            try {
                JSONObject json = new JSONObject(builder.toString());
                int statut = Integer.valueOf(json.getString("statut"));
                if (statut == 1){
                    JSONObject json2 = json.getJSONObject("data");

                    user.setPassword((String) json2.get(MaBaseOpenHelper.COLONNE_PASSWORD));
                    user.setEmail((String) json2.get(MaBaseOpenHelper.COLONNE_EMAIL));
                    user.setDate_naissance((String) json2.get(MaBaseOpenHelper.COLONNE_DATE_NAISSANCE));
                    user.setId((int) json2.get(MaBaseOpenHelper.COLONNE_USER_ID));
                    user.setPseudo((String) json2.get(MaBaseOpenHelper.COLONNE_PSEUDO));
                    user.setPhoto((String) json2.get(MaBaseOpenHelper.COLONNE_PHOTO_PROFILE));
                    user.setCreated((String) json2.get(MaBaseOpenHelper.COLONNE_CREATED));
                    user.setNbrespot((int) json2.get(MaBaseOpenHelper.COLONNE_NB_RESPOT));
                    user.setNbspot((int) json2.get(MaBaseOpenHelper.COLONNE_NB_SPOT));
                    user.setTypeconnexion_id((int) json2.get(MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID));

                    return user;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.getMessage());
            return null;
        }
        finally {
            client.disconnect();
        }
    }

    /**
     * fonction qui retourne la liste des amis d'un utilisateur
     * @param user_id represente l'id de l'utilisateur concerné
     * @return retourne une liste d'utilisateur
     */
    public ArrayList<Utilisateur> getAllFriends(int user_id){
        ArrayList<Utilisateur> users = new ArrayList<Utilisateur>();
        Utilisateur user = new Utilisateur();
        try {
            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("user_id", user_id);

            url = new URL(BASE_URL+URL_LIST_FRIEND+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");

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
                    JSONArray jArr = json.getJSONArray("data");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject json2 = jArr.getJSONObject(i);

                        //user.setPassword(""); //user.setPassword((String) json2.get(MaBaseOpenHelper.COLONNE_PASSWORD));
                        user.setEmail((String) json2.get(MaBaseOpenHelper.COLONNE_EMAIL));
                        //user.setDate_naissance((String) json2.get(MaBaseOpenHelper.COLONNE_DATE_NAISSANCE));
                        user.setId((int) json2.get(MaBaseOpenHelper.COLONNE_USER_ID));
                        user.setPseudo((String) json2.get(MaBaseOpenHelper.COLONNE_PSEUDO));
                        user.setPhoto((String) json2.get(MaBaseOpenHelper.COLONNE_PHOTO_PROFILE));
                        //user.setCreated((String) json2.get(MaBaseOpenHelper.COLONNE_CREATED));
                        //user.setNbrespot((int) json2.get(MaBaseOpenHelper.COLONNE_NB_RESPOT));
                        //user.setNbspot((int) json2.get(MaBaseOpenHelper.COLONNE_NB_SPOT));
                        //user.setTypeconnexion_id((int) json2.get(MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID));

                        users.add(user);
                    }
                    Log.i(TAG, "reponse = " + builder.toString());
                    return users;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.getMessage());
            return null;
        }
        finally {
            client.disconnect();
        }
    }

    /**
     * fnoction qui permet d'enregistrer un spot
     * @param spot spot à enregistrer
     * @return retourne un code qui représente la réponse du serveur
     */
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
            values.put(SpotsDBAdapteur.COLONNE_RESPOT, spot.getRespot());
            values.put(SpotsDBAdapteur.COLONNE_VISIBILITE, spot.getvisibiliteId());
            values.put(SpotsDBAdapteur.COLONNE_GEOHASH, spot.getGeohash());
            values.put(SpotsDBAdapteur.COLONNE_LONGITUDE, spot.getLongitude());
            values.put(SpotsDBAdapteur.COLONNE_LATITUDE, spot.getLatitude());
            values.put(SpotsDBAdapteur.COLONNE_PHOTO, spot.getPhotokey());
            values.put(SpotsDBAdapteur.COLONNE_USER_ID, spot.getUser_id());

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

    /**
     * fonction qui recherche un spot en se basant sur son hash
     * @param hash geohash du spot à rechercher
     * @return retourne une liste de spot qui ont comme geohash celui passé en paramètre
     */
    public ArrayList<Spot> find_spot(String hash){
        ArrayList<Spot> spots = new ArrayList<>();
        Spot spot = new Spot();
        ContentValues values = new ContentValues();
        values.put("apikey", API_KEY);
        values.put("hash", hash);
        try {
            url = new URL(BASE_URL+URL_FIND_SPOT+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");

            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line+"\n");
            }
            br.close();

            Log.i(TAG, "reponse = " + builder.toString());
            try {
                JSONObject json = new JSONObject(builder.toString());
                int statut = Integer.valueOf(json.getString("statut"));
                if (statut == 1){
                    JSONArray jArr = json.getJSONArray("data");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject json2 = jArr.getJSONObject(i);
                        spot.setRespot((int) json2.get(SpotsDBAdapteur.COLONNE_RESPOT));
                        spot.setVisibilite_id((int) json2.get("visibilite_id"));
                        spot.setGeohash((String) json2.get(SpotsDBAdapteur.COLONNE_GEOHASH));
                        spot.setId((int) json2.get(MaBaseOpenHelper.COLONNE_USER_ID));
                        spot.setLongitude((String) json2.get(SpotsDBAdapteur.COLONNE_LONGITUDE));
                        spot.setLatitude((String) json2.get(SpotsDBAdapteur.COLONNE_LATITUDE));
                        spot.setPhotokey((String) json2.get(SpotsDBAdapteur.COLONNE_PHOTO));
                        spot.setUser_id((int) json2.get(SpotsDBAdapteur.COLONNE_USER_ID));
                        spots.add(spot);
                    }
                    return spots;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.toString());
            return null;
        }
        finally {
            client.disconnect();
        }
    }

    /**
     * fonction qui retourne les spots d'un utilisateur
     * @param user_id id dudit utilisateur
     * @param offset reprsente l'indice du premier élément
     * @param interval représente le nombre d'éléments à recuperer
     * @return retourne une liste de spots
     */
    public ArrayList<Spot> find_spot_user(int user_id, int offset, int interval){
        ArrayList<Spot> spots = new ArrayList<>();
        Spot spot = new Spot();
        ContentValues values = new ContentValues();
        values.put("apikey", API_KEY);
        values.put("user_id", user_id);
        values.put("offset", offset);
        values.put("interval", interval);
        try {
            url = new URL(BASE_URL+URL_FIND_SPOT_USER+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");

            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line+"\n");
            }
            br.close();
            Log.i(TAG, "reponse = " + builder.toString());
            try {
                JSONObject json = new JSONObject(builder.toString());
                int statut = Integer.valueOf(json.getString("statut"));
                if (statut == 1){
                    JSONArray jArr = json.getJSONArray("data");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject json2 = jArr.getJSONObject(i);
                        spot = new Spot();
                        spot.setRespot((int) json2.get("respot"));
                        spot.setVisibilite_id((int) json2.get("visibilite_id"));
                        spot.setGeohash((String) json2.get("hash"));
                        spot.setId((int) json2.get("id"));
                        spot.setLongitude(json2.getDouble("gpslong") + "");
                        spot.setLatitude(json2.getDouble("gpslat") + "");
                        spot.setPhotokey((String) json2.get("photo"));
                        //spot.setUser_id((int) json2.get(SpotsDBAdapteur.COLONNE_USER_ID));
                        spots.add(spot);
                    }
                    return spots;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.toString());
            return null;
        }
        finally {
            client.disconnect();
        }
    }


    /**
     * fonction qui ajoute un commentaire à un spot
     * @param spot_id id dudit spot
     * @param user_id id de l'user qui fait le commentaire
     * @param commentaire chaine de caractère correspondant au commentaire
     * @return retourne un entier représentant la valeur de retour du serveur
     */
    public int add_comment(int spot_id, int user_id, String commentaire){
        try {
            url = new URL(BASE_URL+URL_COMMENTAIRE);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setDoOutput(true);

            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));

            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("spot_id", spot_id);
            values.put("user_id", user_id);
            values.put("commentaire", commentaire);

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

    /**
     * fonction qui retourne la liste des commentaires d'un spot
     * @param spot_id représente l'id du spot concerné
     * @return
     */
    public ArrayList<Commentaire> commentaires_spot(int spot_id){
        ArrayList<Commentaire> comments = new ArrayList<Commentaire>();
        Commentaire comment = new Commentaire();
        try {
            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("spot_id", spot_id);

            url = new URL(BASE_URL+URL_LIST_COMMENTAIRE+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");

            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line+"\n");
            }
            br.close();
            Log.i(TAG, "reponse = " + builder.toString() + "  " + client.getResponseCode());

            try {
                JSONObject json = new JSONObject(builder.toString());
                int statut = Integer.valueOf(json.getString("statut"));
                if (statut == 1){
                    JSONArray jArr = json.getJSONArray("data");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject json2 = jArr.getJSONObject(i);

                        comment.setSpot_id((int) json2.get("spot_id"));
                        comment.setUser_id((int) json2.get("user_id"));
                        comment.setCommentaire((String) json2.get("commentaire"));
                        comment.setCreated((String) json2.get("created"));

                        comments.add(comment);
                    }
                    return comments;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.getMessage());
            return null;
        }
        finally {
            client.disconnect();
        }
    }

    /**
     * fonction qui ajoute des tags à un spot
     * @param tags contient la liste des chaines représentant les tag
     * @return retourne un entier correspondant au code de retour venant du serveur
     */
    public int add_tag(String[] tags, int spot_id){
        try {
            url = new URL(BASE_URL+URL_ADD_TAG);
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            client.setDoOutput(true);

            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));
            StringBuilder liste_tag = new StringBuilder();
            liste_tag.append("[");
            for (int i=0; i<tags.length; i++){
                if (i<tags.length - 1)
                    liste_tag.append("\"" + tags[i] + "\",");
                else
                    liste_tag.append("\"" + tags[i] + "\"");
            }
            liste_tag.append("]");
            Log.i(TAG, liste_tag.toString());
            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("spot_id", spot_id);
            values.put("tags", liste_tag.toString());

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
                    builder.append("spot_id = "+json.getString("spot_id"));
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


    /**
     * fonction qui retourne les tags d'un spot
     * @param spot_id id du dit spot
     * @return retourne une liste d'objets Tag
     */
    public ArrayList<Tag> tags_spot(int spot_id){
        ArrayList<Tag> tags = new ArrayList<Tag>();
        Tag tag = new Tag();
        try {
            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("spot_id", spot_id);

            url = new URL(BASE_URL+URL_TAG+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");

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
                    JSONArray jArr = json.getJSONArray("data");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject json2 = jArr.getJSONObject(i);

                        tag.setSpot_id((int) json2.get("spot_id"));
                        tag.setId((int) json2.get("id"));
                        tag.setTag((String) json2.get("tag"));
                        tag.setCreated((String) json2.get("created"));

                        tags.add(tag);
                    }
                    return tags;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.getMessage());
            return null;
        }
        finally {
            client.disconnect();
        }
    }

    /**
     * fonction qui liste les notifications d'un utilisateur
     * @param user_id id dudit utilisateur
     * @return retoune une liste d'objet de type notification ou null en cas d'echec
     */
    public ArrayList<Notification> notifications_user(int user_id){
        ArrayList<Notification> notifications = new ArrayList<>();
        Notification notification = new Notification();

        try {
            ContentValues values = new ContentValues();
            values.put("apikey", API_KEY);
            values.put("user_id", user_id);

            url = new URL(BASE_URL+URL_LIST_NOTIFICATION+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");

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
                    JSONArray jArr = json.getJSONArray("data");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject json2 = jArr.getJSONObject(i);

                        notification.setId((int) json2.get("id"));
                        notification.setUser_id((int) json2.get("user_id"));
                        notification.setMessage((String) json2.get("message"));
                        notification.setCreated((String) json2.get("created"));
                        notification.setTypenotification_id((int) json2.get("typenotification_id"));

                        notifications.add(notification);
                    }
                    Log.i(TAG, "reponse = " + builder.toString());
                    return notifications;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.getMessage());
            return null;
        }
        finally {
            client.disconnect();
        }
    }


    public ArrayList<Spot> find_spot_tag(String tag, String hash){
        ArrayList<Spot> spots = new ArrayList<>();
        Spot spot = new Spot();
        ContentValues values = new ContentValues();
        values.put("apikey", API_KEY);
        values.put("hash", hash);
        values.put("tags", tag);
        try {
            url = new URL(BASE_URL+URL_FIND_SPOT_2+"?"+getQuery(values));
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");

            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line+"\n");
            }
            br.close();
            Log.i(TAG, url.toString());
            Log.i(TAG, "reponse = " + builder.toString());
            try {
                JSONObject json = new JSONObject(builder.toString());
                int statut = Integer.valueOf(json.getString("statut"));
                if (statut == 1){
                    JSONArray jArr = json.getJSONArray("data");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject json2 = jArr.getJSONObject(i);
                        spot.setRespot((int) json2.get("respot"));
                        spot.setVisibilite_id((int) json2.get("visibilite_id"));
                        spot.setGeohash((String) json2.get("hash"));
                        spot.setId((int) json2.get("idspot"));
                        spot.setLongitude(json2.getDouble("gpslong") + "");
                        spot.setLatitude(json2.getDouble("gpslat") + "");
                        spot.setPhotokey((String) json2.get("photo"));
                        //spot.setUser_id((int) json2.get(SpotsDBAdapteur.COLONNE_USER_ID));
                        spots.add(spot);
                    }
                    return spots;
                }
                else
                {
                    builder.append("statut = "+json.getString("statut"));
                    builder.append("errcode = "+json.getString("errcode"));
                    builder.append("message = "+json.getString("message"));

                    Log.i(TAG, "reponse = " + builder.toString());
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e.getMessage());
                return null;
            }
        }
        catch(MalformedURLException error) {
            //Handles an incorrectly entered URL
            Log.e(TAG,"MalformedURLException "+error.getMessage());
            return null;
        }
        catch(SocketTimeoutException error) {
            //Handles URL access timeout.
            Log.e(TAG,"SocketTimeoutException "+error.getMessage());
            return null;
        }
        catch (IOException error) {
            //Handles input and output errors
            Log.e(TAG,"IOException "+error.toString());
            return null;
        }
        finally {
            client.disconnect();
        }
    }

    /**
     * fonction qui construit la chaine devant être passée à une requête de type GET
     * @param params un ensemble de couple clé-valeur de string
     * @return retourne une chaine de caractère pour être passé à la requète
     * @throws UnsupportedEncodingException
     */
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

    /**
     * fonction qui en fonction du code d'erreur retourne un string
     * @param errorcode code d'erreur
     * @return retourne une chaine correspondant à la chaine d'erreur
     */
    private String obtenirErreur(int errorcode){
        switch (errorcode){
            case -1:
                return "Requête!non!autorisée!";
            case -2:
                return "Paramètres!manquants!";
            case -3:
                return "Erreur!lors!de!la!connexion!à!la!base!de!données!";
            case -4:
                return "Erreur!lors!de!l’exécution!d’une!requête!";
            case -5:
                return "Données!non!trouvées!dans!la!base!de!données!";
            case -6:
                return "Mauvaise!méthode!d’authentification!!";
            case -7:
                return "Mauvais!mot!de!passe!";
            case -8:
                return "Evènement!inhabituel!–!erreur!ponctuelle!";
        }
        return " ";
    }
}
