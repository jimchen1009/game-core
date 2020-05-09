package com.game.db.mongodb;

import com.mongodb.ServerAddress;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MongoDBConfig {

    public static final List<ServerAddress> STANDALONE_ADDRESS = Collections.singletonList(new ServerAddress("127.0.0.1", 27017));

    public static final List<ServerAddress> REPLICA_ADDRESS = Arrays.asList(
            new ServerAddress("127.0.0.1", 27018),
            new ServerAddress("127.0.0.1", 27019),
            new ServerAddress("127.0.0.1", 27020)
    );

    public static final List<ServerAddress> SHARDING_ADDRESS = Arrays.asList(
            new ServerAddress("127.0.0.1", 27027),
            new ServerAddress("127.0.0.1", 27028)
    );
}
