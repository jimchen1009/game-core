package com.game.core.db.mongodb;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.EvnCoreType;
import com.game.common.config.IEvnConfig;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import com.mongodb.event.ConnectionAddedEvent;
import com.mongodb.event.ConnectionCheckedInEvent;
import com.mongodb.event.ConnectionCheckedOutEvent;
import com.mongodb.event.ConnectionPoolClosedEvent;
import com.mongodb.event.ConnectionPoolListener;
import com.mongodb.event.ConnectionPoolOpenedEvent;
import com.mongodb.event.ConnectionPoolWaitQueueEnteredEvent;
import com.mongodb.event.ConnectionPoolWaitQueueExitedEvent;
import com.mongodb.event.ConnectionRemovedEvent;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MongoDbManager {

    private static final Logger logger = LoggerFactory.getLogger(MongoDbManager.class);

    private static final Map<String, MongoDbManager> managers = new HashMap<>();


    public static void init(){
        List<IEvnConfig> configList = EvnCoreConfigs.getInstance(EvnCoreType.MONGO).getConfigList("mongodb");
        for (IEvnConfig iEvnConfig : configList) {
            MongoDbManager manager = new MongoDbManager(Objects.requireNonNull(iEvnConfig));
            for (String s : manager.names) {
                managers.put(s, manager);
            }
        }
    }

    public static void destroy(){
        for (MongoDbManager manager : managers.values()) {
            manager.close();
        }
    }

    public static MongoDbManager get(String name){
        return managers.get(name);
    }

    private  MongoClient client;
    private final List<String> names;

    private MongoDbManager(IEvnConfig mongodbConfig) {
        this.names = Collections.unmodifiableList(mongodbConfig.getList("names"));
        reload(mongodbConfig);
    }

    private synchronized void reload(IEvnConfig mongodbConfig){
        List<IEvnConfig> addressConfigList = mongodbConfig.getConfigList("sharding");
        List<ServerAddress> addressList = addressConfigList.stream().map(configs -> new ServerAddress(configs.getString("host"), configs.getInt("port"))).collect(Collectors.toList());
        String application = mongodbConfig.getString("application");
        int connectTimeout = (int)mongodbConfig.getDuration("connectTimeout", TimeUnit.MILLISECONDS);
        int readTimeout = (int)mongodbConfig.getDuration("readTimeout", TimeUnit.MILLISECONDS);
        int maxConnection = mongodbConfig.getInt("maxConnection");
        MongoClientSettings settings = MongoClientSettings.builder()
                .readPreference(ReadPreference.secondary())
                .addCommandListener(new MyCommandListener())
                .applicationName(application)
                .applyToSslSettings( builder -> builder.enabled(false))
                .applyToSocketSettings(builder -> builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS).readTimeout(readTimeout, TimeUnit.MILLISECONDS))
                .applyToConnectionPoolSettings(builder -> builder.addConnectionPoolListener(new MyConnectionPoolListener()).maxSize(maxConnection))
                .applyToClusterSettings(builder -> builder.hosts(addressList).build())
                .build();
        this.close();
        this.client = MongoClients.create(settings);
    }

    public synchronized void close(){
        if (client == null){
            return;
        }
        client.close();
        client = null;
    }

    public MongoDatabase getDb(String name){
        return client.getDatabase(name);
    }

    public MongoCollection<Document> getDocument(String dbName, String collection){
        MongoDatabase db = Objects.requireNonNull(getDb(dbName));
        return db.getCollection(collection);
    }

    private final class MyCommandListener implements CommandListener {

        @Override
        public void commandStarted(CommandStartedEvent event) {
            logger.debug("{} {} {} ", event.getCommandName(), event.getRequestId(), event.getDatabaseName());
        }

        @Override
        public void commandSucceeded(CommandSucceededEvent event) {
            logger.debug("{} {} {}(ms) ", event.getCommandName(), event.getRequestId(), event.getElapsedTime(TimeUnit.MILLISECONDS));
        }

        @Override
        public void commandFailed(CommandFailedEvent event) {
            logger.debug("{} {}", event.getCommandName(), event.getRequestId(), event.getThrowable());
        }
    }


    private final class MyConnectionPoolListener implements ConnectionPoolListener {

        @Override
        public void connectionPoolOpened(ConnectionPoolOpenedEvent event) {

        }

        @Override
        public void connectionPoolClosed(ConnectionPoolClosedEvent event) {

        }

        @Override
        public void connectionCheckedOut(ConnectionCheckedOutEvent event) {

        }

        @Override
        public void connectionCheckedIn(ConnectionCheckedInEvent event) {

        }

        @Override
        public void waitQueueEntered(ConnectionPoolWaitQueueEnteredEvent event) {

        }

        @Override
        public void waitQueueExited(ConnectionPoolWaitQueueExitedEvent event) {

        }

        @Override
        public void connectionAdded(ConnectionAddedEvent event) {

        }

        @Override
        public void connectionRemoved(ConnectionRemovedEvent event) {

        }
    }
}
