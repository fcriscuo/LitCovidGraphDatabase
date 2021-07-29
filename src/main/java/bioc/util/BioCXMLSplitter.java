package bioc.util;

/**
 * Source: https://gist.github.com/lachlan/709ead50f1401ee3afa4
 */
public class BioCXMLSplitter {
    public static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        BioCXMLSplitter main = new BioCXMLSplitter();

        if (args.length < 2) {
            System.out.println("Usage: ");
            System.out.println("\tjava " + main.getClass().getName() + " input_file qualified_element_name\n");
            System.out.println("\tinput_file            \tfile name of the XML file to be split, for example C:\\example.xml");
            System.out.println("\tqualified_element_name\tqualified name of the element in the XML to split, in the format {namespace_uri}element_name");
            System.out.println("\t                      \trefer: <http://docs.oracle.com/javase/8/docs/api/javax/xml/namespace/QName.html#valueOf-java.lang.String->");
            return;
        }

        java.io.File inputFile = new java.io.File(args[0]);
        javax.xml.namespace.QName elementToSplitOn = javax.xml.namespace.QName.valueOf(args[1]);
        java.io.InputStream inputStream = null;

        try {
            System.out.println("Splitting '" + inputFile.toURI() + "' on element '" + elementToSplitOn + "'");

            inputStream = new java.io.BufferedInputStream(new java.io.FileInputStream(inputFile), BUFFER_SIZE);

            javax.xml.stream.XMLInputFactory inputFactory = javax.xml.stream.XMLInputFactory.newInstance();
            javax.xml.stream.XMLOutputFactory outputFactory = javax.xml.stream.XMLOutputFactory.newInstance();
            javax.xml.stream.XMLEventReader reader = inputFactory.createXMLEventReader(inputStream);
            javax.xml.stream.XMLEventWriter writer = null;

            int i = 1;
            while (reader.hasNext()) {
                javax.xml.stream.events.XMLEvent event = reader.nextEvent();

                switch(event.getEventType()) {
                    case javax.xml.stream.XMLStreamConstants.START_ELEMENT:
                        javax.xml.stream.events.StartElement startElement = (javax.xml.stream.events.StartElement)event;
                        if (startElement.getName().equals(elementToSplitOn)) {
                            java.io.File outputFile = new java.io.File(inputFile.getParent(), String.format("%s.split.%03d", inputFile.getName(), i++));
                            System.out.println(String.format("Element '%s' found, splitting to file: '%s'", elementToSplitOn, outputFile.toURI()));
                            writer = outputFactory.createXMLEventWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputFile), BUFFER_SIZE));
                            if (writer != null) writer.add(event);
                        }

                        break;

                    case javax.xml.stream.XMLStreamConstants.END_ELEMENT:
                        javax.xml.stream.events.EndElement endElement = (javax.xml.stream.events.EndElement)event;
                        if (endElement.getName().equals(elementToSplitOn)) {
                            if (writer != null) writer.add(event);
                            writer.close();
                            writer = null;
                        }
                        break;

                    default:
                        if (writer != null) writer.add(event);
                        break;
                }

            }
            reader.close();
            if (writer != null) writer.close();
        } catch(Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (java.io.IOException ex) {
                    // do nothing
                }
            }
        }
    }
}
