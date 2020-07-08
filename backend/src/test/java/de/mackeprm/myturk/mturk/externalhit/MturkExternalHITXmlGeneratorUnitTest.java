package de.mackeprm.myturk.mturk.externalhit;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class MturkExternalHITXmlGeneratorUnitTest {

    @Test
    void generate() throws TransformerException, ParserConfigurationException, IOException, SAXException {
        final MturkExternalHITXmlGenerator generator = new MturkExternalHITXmlGenerator();
        final String externalHITXml = generator.generate("http://TESTING.TEST", 12345);

        assertThat(externalHITXml).contains("http://TESTING.TEST");
        assertThat(externalHITXml).contains("12345");
        final Resource cachedExternalHitXsd = new ClassPathResource("mturk/ExternalQuestion.xsd");
        final SchemaFactory factory = SchemaFactory.newDefaultInstance();
        final Schema schema = factory.newSchema(new StreamSource(cachedExternalHitXsd.getInputStream()));
        final Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(externalHITXml)));
    }
}