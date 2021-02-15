package edgar;

import static edgar.Insider1.read_filings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

//https://www.sec.gov/Archives/edgar/data/1402479/000089924321000379/xslF345X03/doc4.xml

public class NewClass {      
         
public static void main(String[] args) throws Exception {       


        XMLEventReader reader;
        XMLEvent evento;
        XMLEvent next_evento;
        StartElement se;
        String new_xml_link;
        String xml_link = "https://www.sec.gov/Archives/edgar/data/1402479/000089924321000379/xslF345X03/doc4.xml";
        

        compan obj = new compan();
        obj.flag = 0;

            
            new_xml_link = xml_link.replace("xslF345X03/", "");

            try {

                BufferedReader xmlin = new BufferedReader(new InputStreamReader(new URL(new_xml_link).openStream()));

                XMLInputFactory factory = XMLInputFactory.newInstance();
                reader = factory.createXMLEventReader(xmlin);

                while (reader.hasNext()) {

                    evento = reader.nextEvent();

                    if (evento.getEventType() == XMLStreamConstants.START_ELEMENT) {
                        se = evento.asStartElement();

                        if (se.getName().getLocalPart().equals("issuerName")) {
                            Characters compName = (Characters) reader.nextEvent();
                            String company_name = compName.getData();
                            company_name = company_name.replace(",", "");
                            company_name = company_name.replace("&", " ");

                            obj = new compan();
                            obj.flag = 0;

                            reader.nextTag();
                            reader.nextTag();

                            compName = (Characters) reader.nextEvent();
                            String ticker = compName.getData();
                            obj.name = company_name + "[" + ticker.trim() + "]";

                        }
                        if (se.getName().getLocalPart().equals("isDirector")) {

                            obj.director = reader.nextEvent().asCharacters().getData();
                            reader.nextTag();
                            reader.nextTag();
                            obj.officer = reader.nextEvent().asCharacters().getData();;
                            reader.nextTag();
                            reader.nextTag();
                            obj.tenpercent = reader.nextEvent().asCharacters().getData();;

                        }

                        if (se.getName().getLocalPart().equals("transactionCode")) {
                            Characters nameDataEvent = (Characters) reader.nextEvent();

                            if (nameDataEvent.getData().equals("P")) {
                                /////////// Found one
                                 
                                obj.link = xml_link;
                                obj.flag = 1;

                            }
                        }
                        
                        // If flaged as a purchase
                        if (obj.flag == 1) // Qualifying event 
                        {
                           // obj.fechas.add(fecha);

                            if (se.getName().getLocalPart().equals("transactionShares")) {

                                //reader.nextTag();
                                //reader.nextEvent();
                                Characters nxt = (Characters) reader.nextEvent();
                                obj.shares.add(Double.parseDouble(nxt.getData()));

                            }
                            if (se.getName().getLocalPart().equals("transactionPricePerShare")) {

                                reader.nextTag();
                                reader.nextEvent();
                                Characters nxt = (Characters) reader.nextEvent();
                                obj.price.add(Double.parseDouble(nxt.getData()));
                                
                                System.out.println("DFSDFSD");
                                //Addit(obj);
                                //purch_counter++;
                                obj.flag = 0;
                            }
     
                        }

                        if (se.getName().getLocalPart().equals("derivativeTable")) {
                            continue;
                        }

                    }
                    
                }
                xmlin.close();
            } catch (IOException io) {
                System.out.println(new_xml_link);
            } catch (XMLStreamException xm) {
   
                System.out.println("xml exception: " + new_xml_link);

            } 
//            catch (ClassCastException cc) {
//                System.out.println("ClassCast " + new_xml_link);
//                //System.out.println(se.getName().getLocalPart());
//            }
//            
   }

//    public static void Addit(compan obj) throws Exception{
//
//        obj.calc();
//        
//        csvWriter_purchases_b.append(obj.name + " ,");
//        csvWriter_purchases_b.append(String.valueOf(obj.weighted_average) + " ,");
//        csvWriter_purchases_b.append(String.valueOf(obj.purch_total) + " ,");
//        csvWriter_purchases_b.append(obj.link + " ,");
//        csvWriter_purchases_b.append(obj.director + ", ");
//        csvWriter_purchases_b.append(obj.officer + ", ");
//        csvWriter_purchases_b.append(obj.tenpercent + ", ");
//        csvWriter_purchases_b.append("\r\n");
//    }

}

class compan {

    String name;
    String link;
    String director;
    String officer;
    String tenpercent;

    int flag;
    ArrayList<Double> shares = new ArrayList<>();
    ArrayList<Double> price = new ArrayList<>();
    ArrayList<String> fechas = new ArrayList<>();
    double weighted_average;
    double weighted_shares;
    DecimalFormat numberFormat = new DecimalFormat("#.00");
    double purch_total;
    String nombre;
    double shares_bought;
    double price_paid;
    String fecha;

    void calc() {
        try {
            for (int i = 0; i <= (this.shares.size() - 1); i++) {
                // System.out.println(shares.get(i));
                weighted_shares = weighted_shares + shares.get(i);
                weighted_average = weighted_average + shares.get(i) * price.get(i);

            }
            //System.out.println(weighted_average);
            purch_total = weighted_average;
            weighted_average = weighted_average / weighted_shares;
        } catch (IndexOutOfBoundsException iob) {
            System.out.println("Index oob: " + link);
        }

    }
}