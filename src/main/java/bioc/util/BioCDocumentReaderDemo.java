package bioc.util;

/**
 * Created by fcriscuo on 7/25/21.
 */
import bioc.BioCDocument;
import bioc.io.BioCDocumentReader;
import bioc.io.standard.BioCDocumentReaderImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

public class BioCDocumentReaderDemo {

    public static void main(String[] args) throws Exception  {
        String fileName = (args.length> 0) ? args[0] : "data/xml/NLMIAT.BioC.xml";
        System.out.println("Processing BioC file: " +fileName);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(fileName));
        while(!xsr.isStartElement()) {
            xsr.next();
        }
        System.out.println(xsr.getLocalName());
        //xsr.nextTag(); // Advance to statements element

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        String baseName = "out/Document_";
        Integer count = 0;
        while(xsr.hasNext()){
            if(xsr.isStartElement() && xsr.getLocalName() == "document"){
                count++;
                //String filename = baseName + count.toString() +".xml";
               // File file = new File(filename);
                StreamResult result = new StreamResult(new StringWriter());
                t.transform(new StAXSource(xsr), result);
                String xmlString = result.getWriter().toString();
                InputStream inputXml = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
                BioCDocumentReader reader = new BioCDocumentReaderImpl(inputXml);
                BioCDocument document = reader.readDocument();
                System.out.println(document.getID());
            }
            xsr.next();
        }
        System.out.println("++++ Number of documents processed = " + count);

    }

}
