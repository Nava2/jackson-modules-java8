package com.fasterxml.jackson.datatype.javafx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kevin on 18/05/2017.
 */
public class Scratch {

    public static class Foo {

        private final ObjectProperty<Bar> bar;

        public Foo(@JsonProperty("bar") Bar bar) {
            this.bar = new SimpleObjectProperty<>(this, "bar", bar);
        }

        public Bar getBar() {
            return bar.get();
        }

        public ObjectProperty<Bar> barProperty() {
            return bar;
        }

        public void setBar(Bar bar) {
            this.bar.set(bar);
        }
    }

    public static class Bar {
        public String name;

        public Bar(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    @Test
    public void test() throws Exception {
        Foo f1 = new Foo(null);
        Foo f2 = new Foo(null);

        Bar bar = new Bar("bar");
        f1.barProperty().setValue(bar);

        f2.barProperty().bind(f1.barProperty());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaFXModule());

        String json = mapper.writeValueAsString(Arrays.asList(f1, f2));
        /*
        [
            {
                "bar": {
                    "value": {
                        "@id": 1,
                        "name": "bar"
                    }
            },
            {
                "bar": {
                    "bound": true,  // states it was bound, not actually holding a value
                    "value": 1      // bound to the @id tag
                }
            }
        ]
        */
        List<Foo> fromJson = mapper.readValue(json, new TypeReference<List<Foo>>() {});
    }
}
