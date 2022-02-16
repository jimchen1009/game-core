package com.game.core.mq.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

public class MyProducerRecord<K, V> extends ProducerRecord<K, V> {

	public MyProducerRecord(String topic, K key, V value) {
		super(topic, key, value);
	}
}