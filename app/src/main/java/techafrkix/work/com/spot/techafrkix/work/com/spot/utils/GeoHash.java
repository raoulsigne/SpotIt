package techafrkix.work.com.spot.techafrkix.work.com.spot.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by techafrkix0 on 12/04/2016.
 */
public class GeoHash {

    private double longitude;
    private double latitude;
    private String hash;

    private final int LONGUEUR_BITS = 60;
    private final int LONGUEUR_HASH = 12;
    private final int POIDS_LOURD = 16;
    private final int LONG_DIGIT = 5;
    private final double MIN_LATITUDE = -90;
    private final double MIN_LONGITUDE = -180;
    private final double MAX_LATITUDE = 90;
    private final double MAX_LONGITUDE = 180;

    private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private final static Map<Character, Integer> decodeMap = new HashMap<>();

    static {
        int sz = base32.length;
        for (int i = 0; i < sz; i++) {
            decodeMap.put(base32[i], i);
        }
    }

    public GeoHash(double lat, double lng){
        this.latitude = lat;
        this.longitude = lng;
    }

    public GeoHash(String hash){
        this.hash = hash;
    }

    public GeoHash(){
        super();
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String encoder(){
        long result = 0;
        double minLat = MIN_LATITUDE,  maxLat = MAX_LATITUDE;
        double minLng = MIN_LONGITUDE, maxLng = MAX_LONGITUDE;

        for (int i = 0; i < LONGUEUR_BITS; i++) {
            if (i % 2 == 0){ // even bit: bisect longitude

                double midpoint = (minLng + maxLng) / 2;

                if (longitude < midpoint){
                    result <<= 1;                   // push a zero bit
                    maxLng = midpoint;              // shrink range downwards
                } else{
                    result = result << 1 | 1;       // push a one bit
                    minLng = midpoint;              // shrink range upwards
                }
            }
            else {// odd bit: bisect latitude

                double midpoint = (minLat + maxLat) / 2;
                if (latitude < midpoint) {
                    result <<= 1;                   // push a zero bit
                    maxLat = midpoint;              // shrink range downwards
                } else {
                    result = result << 1 | 1;       // push a one bit
                    minLat = midpoint;              // shrink range upwards
                }
            }
        }
        this.hash = convertToHexa(result);
        return convertToHexa(result);
    }

    public String convertToHexa (long nombre){
        String chaine = Long.toBinaryString(nombre);
        while (chaine.length() < LONGUEUR_BITS) // ensure that length of word is LONGUEUR_BITS
            chaine = "0" + chaine;

        StringBuilder bui = new StringBuilder();
        for (int i = 0; i < LONGUEUR_BITS; i = i+LONG_DIGIT) {
            int val = 0;
            int base = POIDS_LOURD;
            for (int j = 0; j < LONG_DIGIT; j++) {
                if(chaine.charAt(i + j) == '1')
                    val += base;
                base /= 2;
            }
            bui.append(base32[val]);
        }

        return bui.toString();
    }

    /**
     * convert an integer to a binary string of length 5
     * @param val value to convert
     * @return string
     */
    public String toBinaryString(int val){
        StringBuilder bui = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if(val % 2 == 0)
                bui.append('0');
            else
                bui.append('1');
            val /= 2;
        }
        return bui.reverse().toString();
    }

    public String convertToBits(String chaine){
        StringBuilder bui = new StringBuilder();

        for (int i = 0; i < LONGUEUR_HASH; i++) {
            int val = decodeMap.get(chaine.charAt(i));
            String s = toBinaryString(val);
            for (int j = 0; j < 5; j++) {
                bui.append(s.charAt(j));
            }
        }

        return bui.toString();
    }

    public void decode (){
        String bits = convertToBits(hash);
        StringBuilder bitsLatitude = new StringBuilder(), bitsLongitude = new StringBuilder();
        double minLat = MIN_LATITUDE,  maxLat = MAX_LATITUDE;
        double minLng = MIN_LONGITUDE, maxLng = MAX_LONGITUDE;

        for (int i = 0; i < LONGUEUR_BITS; i++) {
            if (i % 2 == 0)
                bitsLongitude.append(bits.charAt(i));
            else
                bitsLatitude.append(bits.charAt(i));
        }

        String ch = bitsLongitude.toString();
        for (int i = 0; i < ch.length(); i++) {
            double midpoint = (minLng + maxLng) / 2;
            if (ch.charAt(i) == '0') // even bit: bisect longitude
                maxLng = midpoint;
            else // odd bit: bisect latitude
                minLng = midpoint;

        }
        this.longitude = (minLng + maxLng) /2;

        ch = bitsLatitude.toString();
        for (int i = 0; i < ch.length(); i++) {
            double midpoint = (minLat + maxLat) / 2;
            if (ch.charAt(i) == '0') // even bit: bisect longitude
                maxLat = midpoint;
            else // odd bit: bisect latitude
                minLat = midpoint;
        }
        this.latitude = (minLat + maxLat) /2;
    }

    public String adjacent (String geohash, char direction){
        String result = "";

        if (geohash.length() == 0)
            return " ";
        if ("nsew".indexOf(direction) == -1)
            return " ";

        Map<Character, String[]> neighbour = new HashMap<>();
        String[] nord = {"p0r21436x8zb9dcf5h7kjnmqesgutwvy", "bc01fg45238967deuvhjyznpkmstqrwx"},
                 sud  = {"14365h7k9dcfesgujnmqp0r2twvyx8zb", "238967debc01fg45kmstqrwxuvhjyznp"},
                 est  = {"bc01fg45238967deuvhjyznpkmstqrwx", "p0r21436x8zb9dcf5h7kjnmqesgutwvy"},
                 west = {"238967debc01fg45kmstqrwxuvhjyznp", "14365h7k9dcfesgujnmqp0r2twvyx8zb"};
        neighbour.put('n', nord);
        neighbour.put('s', sud);
        neighbour.put('e', est);
        neighbour.put('w', west);

        Map<Character, String[]> border = new HashMap<>();
        String[] nord1 = {"prxz", "bcfguvyz" },
                 sud1 = { "028b", "0145hjnp" },
                 est1 = { "bcfguvyz", "prxz" },
                 west1 = { "0145hjnp", "028b" };
        border.put('n',nord1);
        border.put('s', sud1);
        border.put('e', est1);
        border.put('w', west1);

        char lastCh = geohash.charAt(geohash.length() - 1);
        String parent = "";
        try {
             parent = geohash.substring(0, geohash.length() - 1);
        }catch (Exception e)
        {
            parent = " ";
        }

        int type = geohash.length() % 2;

        if (border.get(direction)[type].indexOf(lastCh) != -1 && parent != " ")
            parent = adjacent(parent, direction);

        try {
            result = parent + base32[neighbour.get(direction)[type].indexOf(lastCh)];
        }catch (Exception e)
        {
            result = parent;
        }

        return  result.toString();
    }

    Map<String, String> neighbours(String geohash){
        Map<String, String> result = new HashMap<>();

        result.put("n",adjacent(geohash, 'n'));
        result.put("ne",adjacent(adjacent(geohash, 'n'),'e'));
        result.put("e",adjacent(geohash, 'e'));
        result.put("se",adjacent(adjacent(geohash,'s'), 'e'));
        result.put("s",adjacent(geohash, 's'));
        result.put("sw",adjacent(adjacent(geohash,'s'), 'w'));
        result.put("w",adjacent(geohash, 'w'));
        result.put("nw",adjacent(adjacent(geohash,'n'), 'w'));

        return result;
    }
}
