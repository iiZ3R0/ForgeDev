package ForgeBeta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DestinyUser {
    private String BungieID;


    private String memberID;
    private String memberType;
    private int NRaidCount;
    private int PRaidCount;
    private int TRaidCount;
    private String[] CharacterList = new String[2];
    static int TotalRaidCount;

    static String BungieApiKey = "X-API-KEY";
    static String domainBase = "https://www.bungie.net";

    //method used when creating user
    public DestinyUser(String BungieID) {
        this.BungieID = BungieID;
    }

    //method to validate bungieid
    public boolean ValidDestinyUser() throws IOException {
        boolean validUser = false;
        BungieID = BungieID.replaceAll("#", "%23");
        String url = domainBase + "/Platform/Destiny2/SearchDestinyPlayer/-1/" + BungieID + "/";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set Request
        con.setRequestMethod("GET");

        // Set header
        con.setRequestProperty("X-API-KEY", BungieApiKey);

        // Log Response
        System.out.println("Sending 'GET' request to Bungie.Net : " + url);
        System.out.println("Response Code : " + con.getResponseCode() + " [" + con.getResponseMessage() + "]");

        // Parse Response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        String response = "";
        while ((inputLine = in.readLine()) != null) {
            response += inputLine;
        }
        in.close();
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(response);

        // Print and set fields
        if (!json.toString().contains("[]")) {
            setMemberID(json.getAsJsonArray("Response").get(0).getAsJsonObject().get("membershipId").getAsString());
            System.out.println("\nmembershipID: " + getMemberID());
            setMemberType(json.getAsJsonArray("Response").get(0).getAsJsonObject().get("membershipType").getAsString());
            System.out.println("\nmembershipType: " + getMemberType());
            validUser = true;

        } else {
            System.out.println("No Stats for BNID found. Returning False Boolean");
            validUser = false;
        }
        return validUser;
    }

    public void GetCharacterIDs() throws IOException {
        String url = domainBase + "/platform/Destiny2/" + getMemberType() + "/Account/" + getMemberID() + "/Stats/";
        System.out.println(url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set Request
        con.setRequestMethod("GET");

        // Set header
        con.setRequestProperty("X-API-KEY", BungieApiKey);

        // Log Response
        System.out.println("Sending 'GET' request to Bungie.Net : " + url);
        System.out.println("Response Code : " + con.getResponseCode() + " [" + con.getResponseMessage() + "]");

        // Parse Response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        String response = "";
        while ((inputLine = in.readLine()) != null) {
            response += inputLine;
        }

        in.close();
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(response);
        //  printResponse(json);

        String[] CharacterIDs = new String[3];
        JsonObject j1 = json.getAsJsonObject("Response");
        // printResponse(j1);
        JsonObject j2 = j1.getAsJsonObject("profile");
        // printResponse(j2);
        JsonObject j3 = j2.getAsJsonObject("data");
        // printResponse(j3);

        String CharacterIDString = j3.getAsJsonArray("characterIds").toString();
        //if only one character on account
        if (!CharacterIDString.contains(",")) {
            CharacterIDString = CharacterIDString.replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "");
            CharacterIDs[0] = CharacterIDString;
            System.out.println(CharacterIDString);
        } else {
            //if >1 character on account
            String[] CIDSSplit = CharacterIDString.split(",");
            for (int i = 0; i < CIDSSplit.length; i++) {
                CIDSSplit[i] = CIDSSplit[i].replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "");
                CharacterIDs[i] = CIDSSplit[i];
                System.out.println(CharacterIDs[i]);
            }
        }
        //set field
        setCharacterList(CharacterIDs);
    }

    public void CountRaids() throws IOException {
        //reset counters
        setTRaidCount(0);
        TotalRaidCount = 0;
        //read in character list for Duser Object
        String[] CharacterIDs = getCharacterList();
        //put into list
        List<String> CIDs = new ArrayList<String>(Arrays.asList(CharacterIDs));
        //remove any empty elements in list
        CIDs.removeAll(Arrays.asList("", null));
        for (int i = 0; i < CIDs.size(); i++) {
            String url = domainBase + "/Platform/Destiny2/" + getMemberType() + "/Account/" + getMemberID() + "/Character/" + CIDs.get(i) + "/Stats/Activities/?page=0&mode=raid&count=250";
            System.out.println(url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Set Request
            con.setRequestMethod("GET");

            // Set header
            con.setRequestProperty("X-API-KEY", BungieApiKey);

            // Log Response
            System.out.println("Sending 'GET' request to Bungie.Net : " + url);
            System.out.println("Response Code : " + con.getResponseCode() + " [" + con.getResponseMessage() + "]");

            // Parse Response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            String response = "";
            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }

            in.close();
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(response);
            // printResponse(json);

            JsonObject j1 = json.getAsJsonObject("Response");
            // printResponse(j1);
            String ActivityListString = j1.toString();
            //split string by activity occurrence
            String[] ActivityArray = ActivityListString.split("activityDetails");
            List<String> ActivityList = new ArrayList<String>(Arrays.asList(ActivityArray));

            //loop through activity occurance
            for (String aActivityList : ActivityList) {
                //if activity was completed - removes odd splits
                if (aActivityList.contains("completed")) {
                    //split to find if completed
                    String[] HashSplit = aActivityList.split("\"statId\":\"completed\",\"basic\":\\{\"value\":");
                    //split on value & cast into int
                    String[] ValueSplit = HashSplit[1].split(",");
                    String StringValue = ValueSplit[0];
                    double d = Double.parseDouble(StringValue);
                    int Value = (int) d;
                    //increment total raid count
                    TotalRaidCount = TotalRaidCount + Value;
                    //if completed = 1 / true
                    if (Value == 1) {
                        //if activity hash is a 'normal raid' ADD NEW NORMAL RAIDS HERE
                        if (aActivityList.contains("26931") || aActivityList.contains("3089205") || aActivityList.contains("119944") || aActivityList.contains("119944")) {
                            //+1 to normal raid count
                            NRaidCount++;
                        } else {
                            //if activity has is not a 'normal raid', it is a 'prestige raid' I THINK
                            //+1 to prestige raid count
                            PRaidCount++;
                        }

                    }

                }

            }

        }
        //print for visibility
        System.out.println("Total Raid Count: " + TotalRaidCount);
        System.out.println("NRaid Count: " + NRaidCount);
        System.out.println("PRaid Count: " + PRaidCount);
        //set DUser fields
        setNRaidCount(NRaidCount);
        setPRaidCount(PRaidCount);
        setTRaidCount(TotalRaidCount);
    }


    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public int getNRaidCount() {
        return NRaidCount;
    }

    public void setNRaidCount(int NRaidCount) {
        this.NRaidCount = NRaidCount;
    }

    public int getPRaidCount() {
        return PRaidCount;
    }

    public void setPRaidCount(int PRaidCount) {
        this.PRaidCount = PRaidCount;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String[] getCharacterList() {
        return CharacterList;
    }

    public void setCharacterList(String[] characterList) {
        CharacterList = characterList;
    }

    public int getTRaidCount() {
        return TRaidCount;
    }

    public void setTRaidCount(int TRaidCount) {
        this.TRaidCount = TRaidCount;
    }

    public String getBungieID() {
        return BungieID;
    }

    public void setBungieID(String bungieID) {
        BungieID = bungieID;
    }

    //method for printing JSON response in a nice format. Thanks Matt.
    public static void printResponse(JsonObject json) {
        Gson gson = new Gson();
        gson = new GsonBuilder().setPrettyPrinting().create();
        String str = gson.toJson(json);
        System.out.println("\n" + str);
    }

}