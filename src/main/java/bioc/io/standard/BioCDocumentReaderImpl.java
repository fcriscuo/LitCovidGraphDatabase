package bioc.io.standard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.io.BioCDocumentReader;

public class BioCDocumentReaderImpl extends BioCReader implements BioCDocumentReader {

  public BioCDocumentReaderImpl(InputStream inputStream)
      throws FactoryConfigurationError, XMLStreamException {
    super(inputStream);
    atDocumentLevel = true;
    //read();
    readBioCDocument();
  }

  BioCDocumentReaderImpl(Reader in)
      throws FactoryConfigurationError, XMLStreamException {
    super(in);
    atDocumentLevel = true;
    read();
  }

  BioCDocumentReaderImpl(File inputFile)
      throws FactoryConfigurationError, XMLStreamException,
      FileNotFoundException {
    this(new FileInputStream(inputFile));
  }

  BioCDocumentReaderImpl(String inputFilename)
      throws FactoryConfigurationError, XMLStreamException,
      FileNotFoundException {
    this(new FileInputStream(inputFilename));
  }

  @Override
  public BioCDocument readDocument()
      throws XMLStreamException {
    BioCDocument thisDocument = document;

    sentence = null;
    passage = null;
    document = null;
    readBioCDocument();

    return thisDocument;
  }

  @Override
  public BioCCollection readCollectionInfo()
      throws XMLStreamException {
    return collection;
  }

  @Override
  public Iterator<BioCDocument> iterator() {
    return new Iterator<BioCDocument>() {

      @Override
      public boolean hasNext() {
        return document != null;
      }

      @Override
      public BioCDocument next() {
    	BioCDocument thisDocument = document;
    	sentence = null;
    	passage = null;
    	document = null;
    	try {
          read();
    	} catch (XMLStreamException e) {
          throw new NoSuchElementException( e.getMessage() );
    	}
        return thisDocument;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove is not supported");
      }

    };
  }

  public static void main(String[] args) throws XMLStreamException, FileNotFoundException {
    File xmlFile = new File("out/Document_4.xml");
    BioCDocumentReader reader = new BioCDocumentReaderImpl(xmlFile);
    BioCDocument document = reader.readDocument();
    System.out.println(document.getID());
  }
}
