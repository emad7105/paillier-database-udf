package be.heydari.mongo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Emad Heydari Beni
 */
public class Results {

    // for pretty printing only
    private static Gson GSON_SERIALIZER = new GsonBuilder().setPrettyPrinting().create();

    private Map<String,Double> results; // name => sum/avg/etc.

    public Results(Map<String, Double> results) {
        this.results = results;
    }

    public Results() {
        this.results = new HashMap<>();
    }

    public Map<String, Double> getResults() {
        return results;
    }

    public void setResults(Map<String, Double> results) {
        this.results = results;
    }

    public void put(String name, Double value) {
        this.results.put(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Results result = (Results) o;
        return Objects.equals(results, result.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), results);
    }

    @Override
    public String toString() {
        return GSON_SERIALIZER.toJson(this);
    }
}
