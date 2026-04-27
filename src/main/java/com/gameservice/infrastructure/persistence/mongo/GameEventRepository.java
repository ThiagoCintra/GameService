package com.gameservice.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameEventRepository extends MongoRepository<GameEventDocument, String> {
    Optional<GameEventDocument> findByEventId(String eventId);
}
