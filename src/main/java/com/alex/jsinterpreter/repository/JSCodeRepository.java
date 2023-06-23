package com.alex.jsinterpreter.repository;

import com.alex.jsinterpreter.document.JSCode;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * This interface interact with {@link JSCode}
 *
 * @author Oleksandr Myronenko
 */
public interface JSCodeRepository extends MongoRepository<JSCode, String> {
}
