package com.alex.jsinterpreter.domain.mapper;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.domain.dto.JSCodeCommonResponse;
import com.alex.jsinterpreter.domain.dto.JSCodeDetailedResponse;
import org.mapstruct.Mapper;

/**
 * This mapper is used to mapping {@link JSCode} document
 *
 * @author Oleksandr Myronenko
 */
@Mapper(componentModel = "spring")
public interface JSCodeMapper {
    /**
     * method is used to map document to incomplete response
     *
     * @param jsCode document
     * @return js code document in incomplete response
     */
    JSCodeCommonResponse documentMapToIncompleteResponse(JSCode jsCode);

    /**
     * method is used to map document to detailed response
     *
     * @param jsCode document
     * @return js code document in detailed response
     */
    JSCodeDetailedResponse documentMapToDetailedResponse(JSCode jsCode);

    /**
     * method is used to map detailed response to document
     *
     * @param jsCodeDetailedResponse js code in detailed response
     * @return js code in document
     */
    JSCode detailedResponseMapToDocument(JSCodeDetailedResponse jsCodeDetailedResponse);
}
