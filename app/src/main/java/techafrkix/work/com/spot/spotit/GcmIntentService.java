package techafrkix.work.com.spot.spotit;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gcm.server.Message;

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
    // Récupération de l'identification du message
    public static final String MESSAGE_ID = "id";
    // Récupération de l'identification du type de message
    public static final String MESSAGE_TYPE = "typenotification_id";
    // Récupération du nom du message
    public static final String MESSAGE_TITRE = "titre";
    // Récupération de la date et heure du message
    public static final String MESSAGE_DATE_CREATION = "created";
    // Récupération du texte du message
    public static final String MESSAGE_TEXTE = "message";

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
        Log.d(TAG, "Notification ID = " + NOTIFICATION_ID);
        Log.d(TAG, "Notification sent successfully.");
    }

    /**
     * Cette méthode permet à partir des informations envoyées par le serveur * de notification de créer le message et la notification à afficher sur
     * * le terminal de l'utilisateur. * * @param extras les extras envoyés par le serveur de notification
     */
    private void sendMessageNotification(Bundle extras) {
        Log.i(TAG, "Preparing to send notification with message...: " + extras.toString());
        // On crée un objet Message à partir des informations récupérées dans
        // le flux JSON du message envoyé par l'application server
        techafrkix.work.com.spot.bd.Notification msg = extractMessageFromExtra(extras);
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
                        .setContentTitle("Notification from Spot It")
                        .setStyle(new NotificationCompat.InboxStyle()
                                .addLine(msg.getDescription())
                                .addLine(msg.getCreated()))
                        .setContentText(extras.getString(MESSAGE_TEXTE))
                        .setAutoCancel(false)
                        .setDefaults(Notification.DEFAULT_SOUND);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(msg.getId(), mBuilder.build());
        Log.i(TAG, "Notification sent successfully.");
    }

    /**
     * Cette méthode permet d'extraire les informations du message de la notification
     * afin de créer un message.
     * @param extras l'objet contenant les informations du message.
     * @return le message
     */
    private techafrkix.work.com.spot.bd.Notification extractMessageFromExtra(Bundle extras) {

        techafrkix.work.com.spot.bd.Notification notification = null;
        if (extras != null) {
            notification = new techafrkix.work.com.spot.bd.Notification();
            final String id = extras.getString(MESSAGE_ID);
            try {
                notification.setId(Integer.parseInt(id));
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : le message n'a pas un identifiant valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }
            final String dateTime = extras.getString(MESSAGE_DATE_CREATION);
            try {
                notification.setCreated(dateTime);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "error : le message n'a pas une date valide. "+ nfe.getMessage());
                nfe.printStackTrace();
            }
            notification.setTypenotification_id(12); //notification.setTypenotification_id(Integer.parseInt(extras.getString(MESSAGE_TYPE)));
            notification.setDescription(extras.getString(MESSAGE_TEXTE));
        }
        Log.d(TAG, "extractMessageFromExtra - fin");

        return notification;
    }
}
