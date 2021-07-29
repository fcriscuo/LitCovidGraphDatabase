package bioc.util;

/**
 * Created by fcriscuo on 7/25/21.
 */

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;

public class InMemoryDemo {

    public static void main(String[] args) throws Exception  {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader("xml/NLMIAT.BioC.xml"));
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
                String filename = baseName + count.toString() +".xml";
                File file = new File(filename);
                t.transform(new StAXSource(xsr), new StreamResult(file));
            }
            xsr.next();
        }

    }

}
