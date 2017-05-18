package com.fasterxml.jackson.datatype.javafx.ser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.javafx.JavaFXModule;
import javafx.beans.property.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by kevin on 17/05/2017.
 */
public class JavaFXSerializersTest {

    final static UUID FOO_UUID = UUID.randomUUID();

    static class RawPropertiesExposed {

        @JsonProperty
        private final ReadOnlyProperty<UUID> value = new SimpleObjectProperty<>(this, "foo", FOO_UUID);

        @JsonProperty
        private final ReadOnlyIntegerProperty integer = new SimpleIntegerProperty(this, "bar", 4);

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RawPropertiesExposed rawPropertiesExposed = (RawPropertiesExposed) o;
            return Objects.equals(value.getValue(), rawPropertiesExposed.value.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(value.getValue());
        }
    }

    @JsonIdentityInfo(property = "foo", generator = ObjectIdGenerators.PropertyGenerator.class)
    public static class ReadOnlyPropertiesExposedWithCtor {

        private final ReadOnlyProperty<UUID> foo;

        private final ReadOnlyIntegerProperty bar;

        public ReadOnlyPropertiesExposedWithCtor(@JsonProperty("foo") UUID foo,
                                                 @JsonProperty("bar") int bar) {
            this.foo = new SimpleObjectProperty<>(this, "foo", foo);
            this.bar = new SimpleIntegerProperty(this, "bar", 4);
        }

        public UUID getFoo() {
            return foo.getValue();
        }

        public ReadOnlyProperty<UUID> fooProperty() {
            return foo;
        }

        public int getBar() {
            return bar.get();
        }

        public ReadOnlyIntegerProperty barProperty() {
            return bar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RawPropertiesExposed rawPropertiesExposed = (RawPropertiesExposed) o;
            return Objects.equals(foo.getValue(), rawPropertiesExposed.value.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(foo.getValue());
        }
    }

    private ObjectMapper mapper;

    @Before
    public void setUp() {

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaFXModule());
    }


    @Test
    public void checkReadOnlyProperty() throws Exception {
        RawPropertiesExposed value = new RawPropertiesExposed();
        String json = mapper.writeValueAsString(value);

        String expected = "{" +
                "\"value\":{" +
                    "\"name\":\"foo\",\"value\":\"" + FOO_UUID + "\"" +
                "}," +
                "\"integer\":{\"name\":\"bar\",\"value\":4}" +
            "}";
        assertEquals(expected, json);
    }

    @Test
    public void checkReadOnlyPropertiesExposedWithCtor() throws Exception {
        ReadOnlyPropertiesExposedWithCtor value = new ReadOnlyPropertiesExposedWithCtor(FOO_UUID, 4);
        String json = mapper.writeValueAsString(value);

        String expected = "{" +
                "\"foo\":\"" + FOO_UUID + "\"," +
                "\"bar\":4" +
                "}";
        assertEquals(expected, json);
    }

    public static class Holding {
        private final ObjectProperty<BoundToOther> boundToOther;

        private final ObjectProperty<ReadOnlyPropertiesExposedWithCtor> readOnlyPropertiesExposedWithCtor;

        public Holding(@JsonProperty("boundToOther") BoundToOther boundToOther,
                       @JsonProperty("readOnlyPropertiesExposedWithCtor") ReadOnlyPropertiesExposedWithCtor ctor) {
            this.boundToOther = new SimpleObjectProperty<>(this, "boundToOther", boundToOther);
            readOnlyPropertiesExposedWithCtor = new SimpleObjectProperty<>(this, "readOnlyPropertiesExposedWithCtor", ctor);
        }

        @JsonManagedReference
        public BoundToOther getBoundToOther() {
            return boundToOther.get();
        }

        public ObjectProperty<BoundToOther> boundToOtherProperty() {
            return boundToOther;
        }

        public void setBoundToOther(BoundToOther boundToOther) {
            this.boundToOther.set(boundToOther);
        }

        public ReadOnlyPropertiesExposedWithCtor getReadOnlyPropertiesExposedWithCtor() {
            return readOnlyPropertiesExposedWithCtor.get();
        }

        public ObjectProperty<ReadOnlyPropertiesExposedWithCtor> readOnlyPropertiesExposedWithCtorProperty() {
            return readOnlyPropertiesExposedWithCtor;
        }

        public void setReadOnlyPropertiesExposedWithCtor(ReadOnlyPropertiesExposedWithCtor readOnlyPropertiesExposedWithCtor) {
            this.readOnlyPropertiesExposedWithCtor.set(readOnlyPropertiesExposedWithCtor);
        }
    }

    public static class BoundToOther {

        private final ObjectProperty<ReadOnlyPropertiesExposedWithCtor> toBind;

        public BoundToOther() {
            this.toBind = new SimpleObjectProperty<>(this, "toBind");
        }

        @JsonBackReference
        public ReadOnlyPropertiesExposedWithCtor getToBind() {
            return toBind.get();
        }

        public ObjectProperty<ReadOnlyPropertiesExposedWithCtor> toBindProperty() {
            return toBind;
        }

        public void setToBind(ReadOnlyPropertiesExposedWithCtor toBind) {
            this.toBind.set(toBind);
        }
    }

    @Test
    public void checkBindingWithReference() throws Exception {

        ReadOnlyPropertiesExposedWithCtor value = new ReadOnlyPropertiesExposedWithCtor(FOO_UUID, 4);
        BoundToOther boundToOther = new BoundToOther();

        Holding holding = new Holding(boundToOther, value);

        boundToOther.toBindProperty().bind(holding.readOnlyPropertiesExposedWithCtorProperty());

        String json = mapper.writeValueAsString(holding);

        Holding fromJson = mapper.readValue(json, Holding.class);

        String expected = "{" +
                "\"foo\":\"" + FOO_UUID + "\"," +
                "\"bar\":4" +
                "}";
        assertEquals(expected, json);
    }
}