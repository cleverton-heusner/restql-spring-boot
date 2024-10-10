package io.github.cleverton.heusner.restql;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import static io.github.cleverton.heusner.restql.RestQlResponseWrapper.CLAZZ_NODE_NAME;

public class RestQlTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder
{
    public RestQlTypeResolverBuilder(final PolymorphicTypeValidator typeValidator)
    {
        super(ObjectMapper.DefaultTyping.NON_FINAL, typeValidator);
    }

    public StdTypeResolverBuilder init() {
        return super.init(JsonTypeInfo.Id.CLASS, null)
                .typeProperty(CLAZZ_NODE_NAME)
                .inclusion(JsonTypeInfo.As.PROPERTY);
    }

    @Override
    public boolean useForType(final JavaType javaType)
    {
        return javaType.getRawClass().isAnnotationPresent(RestQl.class);
    }
}