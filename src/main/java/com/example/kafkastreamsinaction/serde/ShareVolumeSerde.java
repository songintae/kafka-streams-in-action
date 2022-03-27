package com.example.kafkastreamsinaction.serde;

import com.example.kafkastreamsinaction.model.ShareVolume;
import org.springframework.kafka.support.serializer.JsonSerde;

public class ShareVolumeSerde extends JsonSerde<ShareVolume> {
}
