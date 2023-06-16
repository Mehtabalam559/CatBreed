package assignment2;


import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CatBreedsApp {

    private static final String CAT_BREEDS_URL = "https://catfact.ninja/breeds";
    private static final Logger logger = Logger.getLogger(CatBreedsController.class);
    
    public static void main(String[] args) {
        SpringApplication.run(CatBreedsApp.class, args);
    }

}
