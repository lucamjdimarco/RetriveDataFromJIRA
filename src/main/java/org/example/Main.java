package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.*;


public class Main {

    public static void getIssues(int number) throws IOException {

        int index = 1;
        int startAt=0;

        try {
            for(int i=0; i<number; i++){
                String urlSet = "https://issues.apache.org/jira/rest/api/2/search?jql=project=SYNCOPE%20AND%20issuetype=Bug%20AND%20status%20in%20(Resolved,%20Closed)%20AND%20resolution=Fixed%20ORDER%20BY%20priority%20DESC,%20updated%20DESC&maxResults=1&startAt="+ startAt;;
                ProcessBuilder processBuilder = new ProcessBuilder("curl", "-X", "GET", urlSet);
                Process process = processBuilder.start();

                // Leggi la risposta della richiesta GET
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String response = stringBuilder.toString();

                // Analizza la risposta JSON
                Gson gson1 = new Gson();
                JsonObject jsonObject = gson1.fromJson(response, JsonObject.class);


                JsonElement issues = jsonObject.get("issues");
                String key = null;
                if (issues != null && issues.isJsonArray()) {
                    for (JsonElement issue : issues.getAsJsonArray()) {
                        JsonObject issueObj = issue.getAsJsonObject();
                        JsonElement fixVersions = issueObj.get("fields").getAsJsonObject().get("fixVersions");
                        key = issueObj.get("key").getAsString();



                        if (fixVersions != null && fixVersions.isJsonArray()) {
                            index=1;
                            for (JsonElement version : fixVersions.getAsJsonArray()) {
                                //System.out.println(fixVersions.getAsJsonArray());
                                //System.out.println(index);
                                switch(index){
                                    case 1:
                                        String name = version.getAsJsonObject().get("name").getAsString();
                                        String bug = version.getAsJsonObject().get("name").getAsString();
                                        System.out.println("Key: " + key + ", Bug Version Name: " + name);
                                        break;
                                    case 2:
                                        String name2 = version.getAsJsonObject().get("name").getAsString();
                                        String fix = version.getAsJsonObject().get("name").getAsString();
                                        System.out.println("Key: " + key + ", Fix Version Name: " + name2);
                                        break;
                                    default:
                                        //System.out.println(index);
                                        //System.out.println("Error");
                                        break;
                                }
                                index++;
                            }

                        }
                    }
                }

                getCommand(key);

                startAt++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public static void getCommand(String key) throws IOException {

        try {
            if(key != null){

                String directoryPath = "/home/ubuntu/Scrivania/syncope/syncope";
                ProcessBuilder pb = new ProcessBuilder("git", "log", "--grep=" + key);
                pb.directory(new File(directoryPath));
                Process p = pb.start();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line2;
                while ((line2 = reader2.readLine()) != null) {
                    System.out.println(line2);
                }
                if(line2 == null){
                    System.out.println("No commit found");
                }
                p.waitFor();

                System.out.println("----------------------------------------------------");

            } else {
                System.out.println("No key found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        //usare git show per vedere le modifiche all'interno del commit e l'autore che ci ha lavorato
        try {
            getIssues(10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}