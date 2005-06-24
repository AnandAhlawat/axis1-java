/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.om.impl.serializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.om.AbstractTestCase;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.llom.OMOutputer;
import org.apache.axis.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis.soap.SOAPBody;
import org.apache.axis.soap.SOAPEnvelope;

public class ElementSerializerTest extends AbstractTestCase {
    private XMLStreamReader reader;
    private OMOutputer outputer;
    private OMXMLParserWrapper builder;
    private File tempFile;

    public ElementSerializerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        reader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/soapmessage.xml")));
        tempFile = File.createTempFile("temp", "xml");
        outputer = new OMOutputer(XMLOutputFactory.newInstance().
                createXMLStreamWriter(new FileOutputStream(tempFile)));
        builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMAbstractFactory.getSOAP11Factory(), reader);
    }

    public void testElementSerilization() throws Exception {
        OMElement elt = builder.getDocumentElement();
        elt.serializeWithCache(outputer);

    }

    public void testElementSerilizationCacheOff() throws Exception {
        OMElement elt = builder.getDocumentElement();
        elt.serializeWithCache(outputer);

    }

    public void testElementSerilizationChild() throws Exception {
        OMElement elt = builder.getDocumentElement();
        OMNode node = elt.getFirstChild().getNextSibling();
        node.serializeWithCache(outputer);

    }

    public void testElementSerilizationSOAPBodyCacheOff() throws Exception {
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        OMNode node = env.getBody();
        node.serializeWithCache(outputer);
    }

    public void testElement() throws Exception {
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        SOAPBody body = env.getBody();
        body.serializeWithCache(outputer);
    }

    public void testCompleteElement() throws Exception {
        SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
        env.serializeWithCache(outputer);
    }

    public void testDualNamespaces1() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns1 = factory.createOMNamespace("bar", "x");
        OMNamespace ns2 = factory.createOMNamespace("bar", "y");
        OMElement root = factory.createOMElement("root", ns1);
        OMElement elt11 = factory.createOMElement("foo1", ns1);
        OMElement elt12 = factory.createOMElement("foo2", ns1);
        OMElement elt21 = factory.createOMElement("yuck", ns2);
        OMElement elt22 = factory.createOMElement("yuck", ns2);
        elt11.addChild(elt21);
        elt12.addChild(elt22);
        root.addChild(elt11);
        root.addChild(elt12);
        root.serializeWithCache(outputer);
    }

    public void testDualNamespaces2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns1 = factory.createOMNamespace("bar", "x");
        OMElement root = factory.createOMElement("root", ns1);
        OMNamespace ns2 = root.declareNamespace("bar", "y");
        OMElement elt1 = factory.createOMElement("foo", ns1);
        OMElement elt2 = factory.createOMElement("yuck", ns2);
        OMText txt1 = factory.createText(elt2, "blah");
        elt2.addChild(txt1);
        elt1.addChild(elt2);
        root.addChild(elt1);
        root.serializeWithCache(outputer);
    }

    protected void tearDown() throws Exception {
        outputer.flush();
        tempFile.delete();
    }
}