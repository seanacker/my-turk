package de.mackeprm.myturk.mturk.externalhit;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@Service
public class MturkExternalHITXmlGenerator {

    public String generate(String externalURL, int frameHeight) throws ParserConfigurationException, TransformerException {
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        // root elements
        final Document doc = docBuilder.newDocument();
        final Element externalQuestionElement = doc.createElement("ExternalQuestion");
        externalQuestionElement.setAttribute("xmlns", "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd");
        doc.appendChild(externalQuestionElement);
        
        final Element externalUrl = doc.createElement("ExternalURL");
        externalUrl.setTextContent(externalURL);
        externalQuestionElement.appendChild(externalUrl);

        final Element frameHeightNode = doc.createElement("FrameHeight");
        frameHeightNode.setTextContent(Integer.toString(frameHeight));
        externalQuestionElement.appendChild(frameHeightNode);

        //Write output:
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");

        final StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
