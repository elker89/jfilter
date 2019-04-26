package com.jfilter.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Serializers;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.jfilter.converter.ConverterMapperModifier;
import com.jfilter.converter.SerializationConfig;
import com.jfilter.filter.FilterFields;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * This class allows to add SerializerModifier to ObjectMapper
 */
public class FilterObjectMapper {
    private ObjectMapper objectMapper;
    private FilterFields filterFields;
    private SerializationConfig serializationConfig;

    public FilterObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FilterObjectMapper withFilterFields(FilterFields filterFields) {
        this.filterFields = filterFields;
        return this;
    }

    public FilterObjectMapper withSetSerializationConfig(SerializationConfig serializationConfig) {
        this.serializationConfig = serializationConfig;
        return this;
    }

    /**
     * Build configured ObjectMapper
     *
     * @return {@link ObjectMapper}
     */
    public ObjectMapper build() {
        //Create SerializerFactory and set default Serializers
        SerializerFactory factory;

        //Set SerializerModifier if filterFields not null
        factory = filterFields != null ? BeanSerializerFactory.instance
                .withSerializerModifier(new ConverterMapperModifier(filterFields)) : BeanSerializerFactory.instance;

        //Set default serializers if option is enabled
        if (serializationConfig.isDefaultSerializersEnabled()) {
            factory.withAdditionalSerializers(new SimpleSerializers())
                    .withAdditionalSerializers(new Jdk8Serializers())
                    .withAdditionalKeySerializers(new SimpleSerializers());
        }

        objectMapper.setSerializerFactory(factory);

        //Set dateTimeModule if option is enabled
        if (serializationConfig.isDateTimeModuleEnabled()) {
            //Add JavaTimeModule to fix issue with LocalDate/LocalDateTime serialization
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalTime.class, LocalTimeSerializer.INSTANCE);
            javaTimeModule.addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE);
            javaTimeModule.addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE);
            javaTimeModule.addDeserializer(LocalTime.class, LocalTimeDeserializer.INSTANCE);
            javaTimeModule.addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
            javaTimeModule.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
            objectMapper.registerModule(javaTimeModule);
            objectMapper.findAndRegisterModules();
        }

        return objectMapper;
    }
}