package assignment2;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;

public class CatBreedsController {

    private static final int port = 8081;
    private static final String CAT_BREEDS_URL = "https://catfact.ninja/breeds";
    private static final String LOG_FILE_PATH = "cat_breeds.log";
    private static final Logger logger = LoggerFactory.getLogger(CatBreedsController.class);

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(port);
        app.get("/cat-breeds", ctx -> handleCatBreedsRequest(ctx));
        System.out.println("Server started on port " + port);
    }


    private static void handleCatBreedsRequest(Context ctx) {
        List<Map<String, Object>> catBreeds = new ArrayList<>();
        

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(CAT_BREEDS_URL);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String responseBody = EntityUtils.toString(entity);

                
                logResponseToFile(responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray breedsArray = jsonResponse.getJSONArray("data");

                
                int numPages = jsonResponse.getJSONObject("pagination").getInt("pages");
                System.out.println("Number of pages: " + numPages);

                
                for (int i = 1; i <= numPages; i++) {
                    String pageUrl = CAT_BREEDS_URL + "?page=" + i;
                    request = new HttpGet(pageUrl);
                    response = httpClient.execute(request);
                    entity = response.getEntity();

                    if (entity != null) {
                        responseBody = EntityUtils.toString(entity);
                        JSONObject pageResponse = new JSONObject(responseBody);
                        JSONArray pageBreeds = pageResponse.getJSONArray("data");

                             for (int j = 0; j < pageBreeds.length(); j++) {
                            JSONObject breed = pageBreeds.getJSONObject(j);
                            Map<String, Object> breedInfo = new HashMap<>();
                            breedInfo.put("breed", breed.getString("breed"));
                            breedInfo.put("origin", breed.getString("origin"));
                            breedInfo.put("coat", breed.getString("coat"));
                            breedInfo.put("pattern", breed.getString("pattern"));
                            catBreeds.add(breedInfo);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("An error occurred while fetching cat breeds data: {}", e.getMessage());
            ctx.status(500).result("Internal Server Error");
            return;
        }

       
        Map<String, List<Map<String, Object>>> breedsByCountry = groupBreedsByCountry(catBreeds);

        
        ctx.json(breedsByCountry);
    }

    private static void logResponseToFile(String response) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE_PATH, true)))) {
            writer.println(response);
            writer.println();
            writer.flush();
        } catch (IOException e) {
            logger.error("An error occurred while writing to log file: {}", e.getMessage());
        }
    }

    private static Map<String, List<Map<String, Object>>> groupBreedsByCountry(List<Map<String, Object>> breeds) {
        Map<String, List<Map<String, Object>>> breedsByCountry = new HashMap<>();

        for (Map<String, Object> breed : breeds) {
            String country = (String) breed.get("origin");
            if (!breedsByCountry.containsKey(country)) {
                breedsByCountry.put(country, new ArrayList<>());
            }
            breedsByCountry.get(country).add(breed);
        }

        return breedsByCountry;
    }
}

