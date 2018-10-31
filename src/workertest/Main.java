package workertest;

import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        HttpClient http = new HttpClient();
        http.celsiusToFahrenheit(1, (Optional<Double> t) -> {
            System.out.println(
                t.isPresent() ?
                "1 Celsius = " + t.get().toString() + " Fahrenheit" :
                "No conversion Possible");
        });

        http.fahrenheitToCelsius(1, (Optional<Double> t) -> {
            System.out.println(
                t.isPresent() ?
                "1 Fahrenheit = " + t.get().toString() + " Celsius" :
                "No conversion Possible");
        });
    }
}
