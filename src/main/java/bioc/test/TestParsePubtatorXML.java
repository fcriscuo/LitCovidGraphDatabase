package bioc.test;

/**
 * Test parsing pubtator output data in BioC format
 * Based on the supplied TestParseXMLCollection code
 **/

import bioc.BioCCollection;
import bioc.io.BioCCollectionWriter;
import bioc.io.BioCFactory;
import bioc.io.woodstox.ConnectorWoodstox;
import bioc.util.CopyConverter;

import javax.xml.stream.XMLStreamException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class TestParsePubtatorXML {

  public static void main(String[] args)
      throws XMLStreamException, IOException {

    if (args.length != 1) {
      System.err.println("usage: java TestParseXMLCollection in.xml ");
      System.exit(-1);
    }
    System.out.println("Parsing " +args[0]);
    TestParsePubtatorXML parse_xml = new TestParsePubtatorXML();
    parse_xml.parseBioC(args[0]);
  }

  public void parseBioC(String inBioC)
  {
    try {
    } catch (Exception e){
      System.err.println(e.getMessage());
    }
  }


  public BioCCollection resolveCollection(String inXML)
      throws XMLStreamException, IOException {
    ConnectorWoodstox connector = new ConnectorWoodstox();
    BioCCollection collection = 
        connector.parseXMLCollection( new FileReader(inXML) );

   return collection;
  }
}
