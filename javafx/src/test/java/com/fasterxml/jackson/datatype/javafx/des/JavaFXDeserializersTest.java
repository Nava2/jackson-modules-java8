package com.fasterxml.jackson.datatype.javafx.des;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.javafx.JavaFXModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by kevin on 17/05/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        JavaFXDeserializersTest.ObservableListCheck.class,
        JavaFXDeserializersTest.ObservableSetCheck.class,
})
public class JavaFXDeserializersTest {

    enum TestEnum {
        FOO,
        BAR;
    }

    public static class ObservableListCheck {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        ObservableList<UUID> underTest = FXCollections.observableArrayList(uuid1, uuid2);

        @SuppressWarnings("unchecked")
        ObservableList<ObservableList<UUID>> nestedLists = FXCollections.observableArrayList(underTest, underTest);

        private ObjectMapper mapper;

        @Before
        public void setUp() {

            mapper = new ObjectMapper();
            mapper.registerModule(new JavaFXModule());
        }

        @Test
        public void checkEmptyList() throws Exception {
            String json = mapper.writeValueAsString(FXCollections.observableArrayList());

            String expected = "[]";
            assertEquals(expected, json);

            assertEquals(FXCollections.observableArrayList(), mapper.readValue(json, new TypeReference<ObservableList<UUID>>() {}));
        }

        @Test
        public void checkUUIDList() throws Exception {
            String json = mapper.writeValueAsString(underTest);

            String expected = "[\"" + uuid1 + "\",\"" + uuid2 + "\"]";
            assertEquals(expected, json);

            assertEquals(underTest, mapper.readValue(json, new TypeReference<ObservableList<UUID>>() {}));
        }

        @Test
        public void checkNestedUUIDList() throws Exception {
            String json = mapper.writeValueAsString(nestedLists);

            String expected = "[[\"" + uuid1 + "\",\"" + uuid2 + "\"]," + "[\"" + uuid1 + "\",\"" + uuid2 + "\"]]";
            assertEquals(expected, json);

            assertEquals(nestedLists, mapper.readValue(json, new TypeReference<ObservableList<ObservableList<UUID>>>() {}));
        }
    }

    public static class ObservableSetCheck {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        UUID uuid4 = UUID.randomUUID();

        ObservableSet<UUID> oneDSet;

        ObservableSet<ObservableSet<UUID>> nestedSets;

        private ObjectMapper mapper;

        @Before
        public void setUp() {

            mapper = new ObjectMapper();
            mapper.registerModule(new JavaFXModule());

            LinkedHashSet<UUID> hashSet1 = new LinkedHashSet<>();
            hashSet1.add(uuid1);
            hashSet1.add(uuid2);
            oneDSet = FXCollections.observableSet(hashSet1);

            LinkedHashSet<UUID> hashSet2 = new LinkedHashSet<>();
            hashSet2.add(uuid3);
            hashSet2.add(uuid4);

            LinkedHashSet<ObservableSet<UUID>> twoDSet = new LinkedHashSet<>();
            twoDSet.add(oneDSet);
            twoDSet.add(FXCollections.observableSet(hashSet2));

            nestedSets = FXCollections.observableSet(twoDSet);
        }

        @Test
        public void checkEmptyList() throws Exception {
            String json = mapper.writeValueAsString(FXCollections.observableSet(EnumSet.noneOf(TestEnum.class)));

            String expected = "[]";
            assertEquals(expected, json);

            ObservableSet<TestEnum> fromJson = mapper.readValue(json, new TypeReference<ObservableSet<TestEnum>>() {});
            assertEquals(FXCollections.observableSet(EnumSet.noneOf(TestEnum.class)), fromJson);
        }

        @Test
        public void checkOneDimension() throws Exception {
            String json = mapper.writeValueAsString(oneDSet);

            String expected = "[\"" + uuid1 + "\",\"" + uuid2 + "\"]";
            assertEquals(expected, json);

            assertEquals(oneDSet, mapper.readValue(json, new TypeReference<ObservableSet<UUID>>() {}));
        }

        @Test
        public void checkTwoDimension() throws Exception {
            String json = mapper.writeValueAsString(nestedSets);

            String expected = "[[\"" + uuid1 + "\",\"" + uuid2 + "\"]," + "[\"" + uuid3 + "\",\"" + uuid4 + "\"]]";
            assertEquals(expected, json);

            assertEquals(nestedSets, mapper.readValue(json, new TypeReference<ObservableSet<ObservableSet<UUID>>>() {}));
        }
    }



    public static class ObservableMapCheck {

        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        ObservableMap<String, UUID> oneD;

        ObservableMap<String, ObservableList<UUID>> twoD;

        private ObjectMapper mapper;

        @Before
        public void setUp() {

            mapper = new ObjectMapper();
            mapper.registerModule(new JavaFXModule());

            LinkedHashMap<String, UUID> oneDBacking = new LinkedHashMap<>();
            oneDBacking.put(uuid1.toString(), uuid1);
            oneDBacking.put(uuid2.toString(), uuid2);
            oneD = FXCollections.observableMap(oneDBacking);


            LinkedHashMap<String, ObservableList<UUID>> twoDBacking = new LinkedHashMap<>();
            twoDBacking.put(uuid1.toString(), FXCollections.observableArrayList(uuid1, uuid1));
            twoDBacking.put(uuid2.toString(), FXCollections.observableArrayList(uuid2, uuid2));
            twoD = FXCollections.observableMap(twoDBacking);
        }

        @Test
        public void checkEmpty() throws Exception {
            String json = mapper.writeValueAsString(FXCollections.emptyObservableMap());
            assertEquals("{}", json);

            assertEquals(FXCollections.emptyObservableMap(),
                    mapper.readValue(json, new TypeReference<ObservableMap<String, UUID>>() {}));
        }

        @Test
        public void checkUUIDList() throws Exception {
            String json = mapper.writeValueAsString(oneD);

            String expected = "{\"" + uuid1 + "\":\"" + uuid1 + "\",\"" + uuid2 + "\":\"" + uuid2 + "\"}";
            assertEquals(expected, json);

            assertEquals(oneD, mapper.readValue(json, new TypeReference<ObservableMap<String, UUID>>() {}));
        }

        @Test
        public void checkNestedUUIDList() throws Exception {
            String json = mapper.writeValueAsString(twoD);

            String expected = "{\"" + uuid1 + "\":[\"" + uuid1 + "\",\"" + uuid1 + "\"],\"" + uuid2 + "\":[\"" + uuid2 + "\",\"" + uuid2 + "\"]}";
            assertEquals(expected, json);

            assertEquals(twoD, mapper.readValue(json, new TypeReference<ObservableMap<String, ObservableList<UUID>>>() {}));
        }
    }
}
