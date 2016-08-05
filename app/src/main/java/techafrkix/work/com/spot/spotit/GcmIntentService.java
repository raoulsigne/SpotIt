package techafrkix.work.com.spot.spotit;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import techafrkix.work.com.spot.bd.NotificationEntity;

/**
 * Created by techafrkix0 on 29/06/2016.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    /* * Constantes permettant la récupération des informations
    * * du message dans les extras de la notification envoyée
    * * par le serveur de notification. */

    // Récupération de l'identification de la notification
    public static final String _ID = "id";

    // Récupération de l'identification de l'utilisateur
    public static final String _USER_ID = "id";

    // Récupération de l'identification du type de message
    public static final String _TYPE_ID = "typenotification_id";

    // Récupération de la date et heure du message
    public static final String _DATE_CREATION = "created";

    // Récupération du texte du message
    public static final String _DATA = "data";

    // Récupération de l'identification de la personne qui envoie
    public static final String _SENDER_ID = "sender_id";

    // Récupération du texte du message
    public static final String _DESCRIPTION = "description";

    // Récupération du texte du message
    public static final String _PHOTOSENDER = "photosender";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCM";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // Le paramètre intent de la méthode getMessageType() est la notification push
        // reçue par le BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            /* * On filtre le message (ou notification) sur son type.
            * * On met de côté les messages d'erreur pour nous concentrer sur
            * * le message de notre notification. */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
                // Si c'est un message "classique".
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Cette boucle représente le travail qui devra être effectué.

                // Traite les informations se trouvant dans l'extra de l'intent
                // pour générer une notifiation android que l'on enverra.
                sendMessageNotification(extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }

        sendInsideNotification();

        // On indique à notre broadcastReceiver que nous avons fini le traitement.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Cette méthode permet de créer une notification à partir * d'un message passé en paramètre.
     */
    private void sendNotification(String msg) {
        Log.d(TAG, "Preparing to send notification...: " + msg);
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Connexion.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logospotitblue)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg)
                        .setDefaults(Notification.DEFAULT_ALL);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        Log.d(TAG, "NotificationActivity ID = " + NOTIFICATION_ID);
        Log.d(TAG, "NotificationActivity sent successfully.");
    }

    /**
     * Cette méthode permet à partir des informations envoyées par le serveur * de notification de créer le message et la notification à afficher sur
     * * le terminal de l'utilisateur. * * @param extras les extras envoyés par le serveur de notification
     */
    private void sendMessageNotification(Bundle extras) {
        Log.i(TAG, "Preparing to send notification with message...: " + extras.toString());
        // On crée un objet Message à partir des informations récupérées dans
        // le flux JSON du message envoyé par l'application server
        NotificationEntity msg = extractMessageFromExtra(extras);
        // On associe notre notification à une Activity. Ici c'est l'activity
        // qui affiche le message à l'utilisateur
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        new Intent(this, MainActivity.class).putExtra("notification", msg), 0);

        // On récupère le gestionnaire de notification android
        mNotificationManager = (NotificationManager)
        this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logospotitblue)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.logospotitblue))
                        .setContentTitle("NotificationActivity from Spot It")
                        .setStyle(new NotificationCompat.InboxStyle()
                                .addLine(msg.getDescription())
                                .addLine(msg.getCreated()))
                        .setContentText(extras.getString(_DATA))
                        .setAutoCancel(false)
                        .setDefaults(Notification.DEFAULT_SOUND);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(msg.getId(), mBuilder.build());
        Log.i(TAG, "NotificationActivity sent successfully.");
    }

    /**
     * Cette méthode permet d'extraire les informations du message de la notification
     * afin de créer un message.
     * @param extras l'objet contenant les informations du message.
     * @return le message
     */
    private NotificationEntity extractMessageFromExtra(Bundle extras) {

        NotificationEntity notificationEntity = null;
        if (extras != null) {
            notificationEntity = new NotificationEntity();
            final String id = extras.getString(_ID);
            try {
                notificationEntity.setId(Integer.parseInt(id));
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : le message n'a pas un identifiant valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }

            final String userid = extras.getString(_USER_ID);
            try {
                notificationEntity.setUser_id(Integer.parseInt(userid));
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : l'id de l'utilisateur concerné est non valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }

            final String typeid = extras.getString(_TYPE_ID);
            try {
                notificationEntity.setTypenotification_id(Integer.parseInt(typeid));
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : l'id du type de la notificationEntity n'est valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }

            final String dateTime = extras.getString(_DATE_CREATION);
            try {
                notificationEntity.setCreated(dateTime);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : le message n'a pas une date valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }

            final String data = extras.getString(_DATA);
            try {
                notificationEntity.setData(data);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : le champ data n'est pas valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }

            final String senderid = extras.getString(_SENDER_ID);
            try {
                notificationEntity.setSender_id(Integer.parseInt(senderid));
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : l'id de l'utilisateur ayant envoyé la notificationEntity n'est valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }

            final String description = extras.getString(_DESCRIPTION);
            try {
                notificationEntity.setDescription(description);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : le champ description n'est pas valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }

            final String photosender = extras.getString(_PHOTOSENDER);
            try {
                notificationEntity.setPhotosender(photosender);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : le champ photosender n'est pas valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }
        }
        Log.d(TAG, "extractMessageFromExtra - fin");

        return notificationEntity;
    }

    // Send an Intent with an action named "custom-event-name". The Intent sent should
    // be received by the ReceiverActivity.
    private void sendInsideNotification() {
        Log.i("localbroadcast", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "there is a new notification!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
