package com.game.db.mongodb;

import com.game.common.config.Config;
import com.game.common.config.IConfig;
import com.game.common.util.CommonUtil;
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

    public static MongoDbManager get(String name){
        MongoDbManager manager = managers.get(name);
        if (manager == null){
            synchronized (managers){
                manager = managers.get(name);
                if (manager == null){
                    List<IConfig> configList = Config.getInstance().getConfigList("db.mongodb");
                    IConfig mongodbConfig = CommonUtil.findOneIf(configList, config -> {
                        List<String> names = config.getList("names");
                        return names.contains(name);
                    });
                    manager = new MongoDbManager(Objects.requireNonNull(mongodbConfig));
                    for (String s : manager.names) {
                        managers.put(s, manager);
                    }
                }
            }
        }
        return manager;
    }

    private final MongoClient client;
    private final List<String> names;

    private MongoDbManager(IConfig mongodbConfig) {
        this.names = Collections.unmodifiableList(mongodbConfig.getList("names"));
        List<IConfig> addressConfigList = mongodbConfig.getConfigList("sharding");
        List<ServerAddress> addressList = addressConfigList.stream().map(configs -> new ServerAddress(configs.getString("host"), configs.getInt("port"))).collect(Collectors.toList());
        String application = mongodbConfig.getString("application");
        int connectTimeout = (int)mongodbConfig.getDuration("connectTimeout", TimeUnit.MILLISECONDS);
        int readTimeout = (int)mongodbConfig.getDuration("readTimeout", TimeUnit.MILLISECONDS);
        int connectionMaxSize = mongodbConfig.getInt("connectionMaxSize");
        MongoClientSettings settings = MongoClientSettings.builder()
                .readPreference(ReadPreference.secondary())
                .addCommandListener(new MyCommandListener())
                .applicationName(application)
                .applyToSslSettings( builder -> builder.enabled(false))
                .applyToSocketSettings(builder -> builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS).readTimeout(readTimeout, TimeUnit.MILLISECONDS))
                .applyToConnectionPoolSettings(builder -> builder.addConnectionPoolListener(new MyConnectionPoolListener()).maxSize(connectionMaxSize))
                .applyToClusterSettings(builder -> builder.hosts(addressList).build())
                .build();
        this.client = MongoClients.create(settings);
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
            logger.info("{} {} {} ", event.getCommandName(), event.getRequestId(), event.getDatabaseName());
        }

        @Override
        public void commandSucceeded(CommandSucceededEvent event) {
            logger.info("{} {} {}(ms) ", event.getCommandName(), event.getRequestId(), event.getElapsedTime(TimeUnit.MILLISECONDS));
        }

        @Override
        public void commandFailed(CommandFailedEvent event) {
            logger.error("{} {}", event.getCommandName(), event.getRequestId(), event.getThrowable());
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
