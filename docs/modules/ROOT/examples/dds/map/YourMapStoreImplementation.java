import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapLoaderLifecycleSupport;
import com.hazelcast.map.MapStore;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

//tag::ymsi[]
public class YourMapStoreImplementation implements MapStore<String, Supplement>, MapLoaderLifecycleSupport {

    private MongoClient mongoClient;
    private MongoCollection collection;

    public YourMapStoreImplementation() {
    }

    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        String mongoUrl = (String) properties.get("mongo.url");
        String dbName = (String) properties.get("mongo.db");
        String collectionName = (String) properties.get("mongo.collection");
        this.mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
        this.collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
    }
//end::ymsi[]
    @Override
    public void destroy() {
        mongoClient.close();
    }

    @Override
    public Supplement load(String key) {
        System.out.println("Load " + key);
        Document document = (Document) collection.find(eq("_id", key)).first();
        String name = (String) document.get("name");
        Integer price = document.getInteger("price");
        return new Supplement(name, price);
    }

    @Override
    public Map<String, Supplement> loadAll(Collection keys) {
        System.out.println("LoadAll " + keys);
        HashMap<String, Supplement> result = new HashMap<String, Supplement>();

        FindIterable<Document> id = collection.find(in("_id", keys));
        for (Document document : id) {
            String name = (String) document.get("name");
            Integer price = document.getInteger("price");
            result.put(document.get("_id").toString(), new Supplement(name, price));
        }
        return result;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        System.out.println("LoadAllKeys");
        List<String> keys = new LinkedList<String>();
        FindIterable<Document> ids = collection.find().projection(Projections.include("_id"));
        for (Document document : ids) {
            keys.add(document.get("_id").toString());
        }
        return keys;
    }

    @Override
    public void store(String key, Supplement value) {
        Document doc = new Document("name", value.getName()).append("price", value.getPrice()).append("_id", key);
        this.collection.insertOne(doc);
        //this.collection.replaceOne(doc, doc);
    }

    @Override
    public void storeAll(Map<String, Supplement> map) {
        List<InsertOneModel> batch = new LinkedList<InsertOneModel>();
        for (Map.Entry<String, Supplement> entry : map.entrySet()) {
            String key = entry.getKey();
            Supplement value = entry.getValue();
            batch.add(new InsertOneModel(
                    new Document("name", value.getName()).append("price", value.getPrice())
                            .append("_id", key)));
        }
        this.collection.bulkWrite(batch, new BulkWriteOptions().ordered(false));
    }

    @Override
    public void delete(String key) {
        this.collection.deleteOne(eq("_id", key));
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        this.collection.deleteMany(in("_id", keys));
    }
}