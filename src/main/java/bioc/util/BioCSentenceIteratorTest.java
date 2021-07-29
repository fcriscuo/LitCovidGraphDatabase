package bioc.util;

import java.io.FileReader;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.BioCSentence;
import bioc.io.BioCCollectionReader;
import bioc.io.BioCFactory;

/**
 * Test BioCCollectionReader and BioCCollectionWriter
 */
public class BioCSentenceIteratorTest {

  public static void main(String[] args)
      throws IOException, XMLStreamException {
    BioCSentenceIteratorTest test = new BioCSentenceIteratorTest();
    //test.test("xml/PMID-8557975-simplified-sentences.xml", BioCFactory.STANDARD);
    test.test("out/Document_1.xml", BioCFactory.STANDARD);
  }

  public void test(String inXML, String flags)
      throws XMLStreamException, IOException {
    BioCFactory factory = BioCFactory.newFactory(flags);

    BioCCollectionReader reader = factory
        .createBioCCollectionReader(new FileReader(inXML));
    BioCCollection collection = reader.readCollection();
    BioCDocument document = collection.getDocument(0);
    reader.close();
    System.out.println(document.getID());

//    BioCSentenceIterator itr = new BioCSentenceIterator(collection);
//    while (itr.hasNext()) {
//      BioCSentence sentence = itr.next();
//      System.out.println(sentence.getText());
//    }
  }

}
