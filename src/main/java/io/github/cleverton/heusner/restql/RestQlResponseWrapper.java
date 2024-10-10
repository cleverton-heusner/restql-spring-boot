package io.github.cleverton.heusner.restql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cleverton.heusner.exception.*;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RestQlResponseWrapper extends ContentCachingResponseWrapper {

    public static final String CLAZZ_NODE_NAME = "@class";

    private final ObjectMapper objectMapper;

    public RestQlResponseWrapper(final ServletResponse response) {
        this(response, new ObjectMapper());
    }

    public RestQlResponseWrapper(final ServletResponse response, final ObjectMapper objectMapper) {
        super((HttpServletResponse) response);
        this.objectMapper = objectMapper;
    }

    public Object readEntity() {
        final JsonNode entityTree = readEntityTree();

        if (!entityTree.has(CLAZZ_NODE_NAME)) {
            throw new MissingRestQlAnnotationException("Annotation '@RestQl' not found in entity.");
        }

        return objectMapper.convertValue(entityTree, loadEntityClass(entityTree));
    }

    private Class<?> loadEntityClass(final JsonNode entityTree) {
        try {
            return Class.forName(entityTree.get(CLAZZ_NODE_NAME).asText());
        } catch (final ClassNotFoundException e) {
            throw new EntityClassLoadingException("Error loading entity class from JSON node.", e);
        }
    }

    public void writeEntityWithSelectedFields(final Map<String, Object> entityWithSelectedFields) {
        try {
            writeResponse(objectMapper.writeValueAsString(entityWithSelectedFields));
        } catch (final JsonProcessingException e) {
            throw new EntitySerializationException("Error serializing entity with selected fields to JSON.", e);
        }
    }

    public void writeEntityWithAllFields() {
        try {
            final JsonNode entityTree = readEntityTree();
            removeClassNodeFromTree(entityTree);
            writeResponse(objectMapper.writeValueAsString(entityTree));
        } catch (final IOException e) {
            throw new EntitySerializationException("Error serializing entity with all fields to JSON.", e);
        }
    }

    private JsonNode readEntityTree() {
        try {
            return objectMapper.readTree(new String(getContentAsByteArray(), StandardCharsets.UTF_8));
        } catch (final JsonProcessingException e) {
            throw new EntityDeserializationException("Error deserializing entity.", e);
        }
    }

    private void removeClassNodeFromTree(final JsonNode tree) {
        if (tree.has(RestQlResponseWrapper.CLAZZ_NODE_NAME) && tree.isObject()) {
            ((ObjectNode) tree).remove(RestQlResponseWrapper.CLAZZ_NODE_NAME);
        }
    }

    private void writeResponse(final String content) {
        try {
            resetBuffer();
            getWriter().write(content);
            copyBodyToResponse();
        } catch (final IOException e) {
            throw new ResponseProcessingException("Error writing response content to HTTP response.", e);
        }
    }
}