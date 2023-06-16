package Assignment3;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class WordCountValidationApp {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(8081);

        app.post("/", ctx -> {
            String payload = ctx.body();
            int wordCount = countWords(payload);
            
            if (wordCount >= 8) {
                ctx.status(200).result("ok");
            } else {
                ctx.status(406).result("No Acceptable");
            }
        });
    }

    private static int countWords(String text) {
        
        String[] words = text.split("\\s+");
        int wordCount = 0;
        
        for (String word : words) {
            if (!word.isEmpty()) {
                wordCount++;
            }
        }
        
        return wordCount;
    }
}

