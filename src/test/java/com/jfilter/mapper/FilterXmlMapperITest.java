package com.jfilter.mapper;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.jfilter.components.FilterConfiguration;
import com.jfilter.mock.config.WSConfigurationEnabled;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = WSConfigurationEnabled.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class FilterXmlMapperITest {
    private FilterXmlMapper objectMapper;

    @Autowired
    private FilterConfiguration filterConfiguration;

    @Before
    public void init() {
        objectMapper = new FilterXmlMapper(filterConfiguration);
    }

    @Test
    public void testWriterNotNull() {
        assertNotNull(objectMapper.writer());
    }

    @Test
    public void testWriterWithViewNotNull() {
        assertNotNull(objectMapper.writerWithView(null));
    }

    @Test
    public void testWriterWithfilterProviderNotNull() {
        assertNotNull(objectMapper.writer((FilterProvider) null));
    }
}
