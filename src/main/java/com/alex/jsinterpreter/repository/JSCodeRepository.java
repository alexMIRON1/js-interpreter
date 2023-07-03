package com.alex.jsinterpreter.repository;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * This interface interact with {@link JSCode}
 *
 * @author Oleksandr Myronenko
 */
public interface JSCodeRepository extends MongoRepository<JSCode, String> {
    /**
     * method is used to find list of js codes by status code
     *
     * @param jsCodeStatus js code status
     * @return list of js code by status
     */
    List<JSCode> findByStatusCode(JSCodeStatus jsCodeStatus);

    /**
     * method is used to find list of js codes by status code and sorting by sort param
     *
     * @param jsCodeStatus js code status
     * @param sort         sort object for sorting js code list
     * @return list {@link JSCode}
     */
    List<JSCode> findByStatusCode(JSCodeStatus jsCodeStatus, Sort sort);
}
