import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

class Human {
    private static String message;
    private static Place place;

    public static String getMessage() {
        return message;
    }

    public static void setMessage(String message) {
        Human.message = message;
    }

    public static Place getPlace() {
        return place;
    }

    public static void setPlace(Place place) {
        Human.place = place;
    }

    public static void say() {
        System.out.println();
        System.out.println(getMessage() + " , " + getPlace().getName() + "!");
    }
}

class Place {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

public class TestJackson {

    public static void main(String [] args) throws IOException {

        Place place = new Place();
        place.setName("World");

        Human human = new Human();
        human.setMessage("Hi");
        human.setPlace(place);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(human);
        System.out.println("json " + jsonString); //  напечатает  "json {"message":"Hi","place":{"name":"World"}}"

        // convert from json
        Human newHuman = mapper.readValue(jsonString, Human.class);
        newHuman.say(); //  напечатает "Hi , World!"
    }
}
