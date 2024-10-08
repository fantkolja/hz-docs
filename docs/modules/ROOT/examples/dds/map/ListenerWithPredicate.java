import com.hazelcast.config.Config;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.query.Predicates;

//tag::lwp[]
public class ListenerWithPredicate {

    public static void main(String[] args) {
        Config config = new Config();
        config.setProperty("hazelcast.map.entry.filtering.natural.event.types", "true");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IMap<String, HazelcastJsonValue> map = hz.getMap("map");
        map.addEntryListener(new MyEntryListener(),
                Predicates.sql("surname=smith"), true);
        System.out.println("Entry Listener registered");
    }

    static class MyEntryListener
            implements EntryAddedListener<String, HazelcastJsonValue>,
            EntryUpdatedListener<String, HazelcastJsonValue>,
            EntryRemovedListener<String, HazelcastJsonValue> {
        @Override
        public void entryAdded(EntryEvent<String, HazelcastJsonValue> event) {
            System.out.println("Entry Added:" + event);
        }

        @Override
        public void entryRemoved(EntryEvent<String, HazelcastJsonValue> event) {
            System.out.println("Entry Removed:" + event);
        }

        @Override
        public void entryUpdated(EntryEvent<String, HazelcastJsonValue> event) {
            System.out.println("Entry Updated:" + event);
        }
    }
}
//end::lwp[]
