package edgar;

import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.net.URL;
import java.text.DecimalFormat;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.List;
import java.nio.channels.*;
import java.util.concurrent.TimeUnit;

public class Insider1 {
    
    static FileWriter csvWriter_link;
    static FileWriter csvWriter_form;
    static FileWriter csvWriter_purchases;
    static BufferedWriter read_filing_writer; //= new BufferedWriter(csvWriter_form);
    static BufferedWriter csvWriter_purchases_b;
    static String g;
    static ArrayList<String> filing_link_from_file = new ArrayList<String>();
    static ArrayList<String> xml_link_from_file = new ArrayList<String>();
    static ArrayList<String> read_filings = new ArrayList<String>();
    static int form_counter = 0;
    static int form_ctr = 0;
    static int purch_counter=0;
    
    
    public static void main(String[] args) throws Exception {

        int year = 2020;
        int qtr = 4;
        String src;
        String URLstring = null;

        try {
            Scanner scan_src = new Scanner(System.in);
            System.out.println("Download or use file? (F) FIle (D) Download");
            src = scan_src.nextLine();

            if (src.equals("D")) {
                URLstring = "https://www.sec.gov/Archives/edgar/full-index/" + year + "/QTR" + qtr + "/crawler.idx";
                System.out.println("Downloading from: " + URLstring);

                ReadableByteChannel readChannel = Channels.newChannel(new URL(URLstring).openStream());
                FileOutputStream fileOS = new FileOutputStream("crawler.idx");
                FileChannel writeChannel = fileOS.getChannel();
                writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);

            }

        } catch (IOException io) {

            //System.out.println(io.printStackTrace());
        }
        open_file();
        getForm44();

    }
    
    public static void open_file() throws Exception {
        File link = new File("xml_link.csv");
        File form = new File("filing_read.csv");
        File purchases = new File("new_total.csv");

        if (link.exists() == true) {
            Scanner sc = new Scanner(link);

            while (sc.hasNext()) {

                g = sc.nextLine().trim();
                if (!g.equals("")) {

                    filing_link_from_file.add(g);
                    if (sc.hasNext()) {

                    xml_link_from_file.add(sc.nextLine().trim());
                        // System.out.println("Form 4 links read from local file " + (h/2);
                    }
                }
            }
            sc.close();
            csvWriter_link = new FileWriter("xml_link.csv", true);
        } else {

            csvWriter_link = new FileWriter("xml_link.csv");
        }
    

        if (form.exists() == true) {
            Scanner scc = new Scanner(form);
            while (scc.hasNext()) {
                read_filings.add(scc.nextLine());
            }
            csvWriter_form = new FileWriter(form, true);
        } else {
            csvWriter_form = new FileWriter(form);
        }

        if (purchases.exists() == true) {
            csvWriter_purchases = new FileWriter(purchases, true);
            csvWriter_purchases_b = new BufferedWriter(csvWriter_purchases);
        } else {
            csvWriter_purchases = new FileWriter(purchases);
            csvWriter_purchases_b = new BufferedWriter(csvWriter_purchases);
            
            csvWriter_purchases_b.write("Company Name, ");
            csvWriter_purchases_b.write("Price Shares, ");
            csvWriter_purchases_b.write("Purchase price, ");
            csvWriter_purchases_b.write("Link, ");
            csvWriter_purchases_b.write("Director, ");
            csvWriter_purchases_b.write("Officer, ");
            csvWriter_purchases_b.write("Ten Percent, ");
            csvWriter_purchases_b.write("\r\n");
        }
        
    read_filing_writer = new BufferedWriter(csvWriter_form); 
    
        
    }

    public static void getForm44() throws Exception {

        ArrayList<String> link_list = new ArrayList<>();
        int http;
        int high = 0;
        int low = 0;

        int counter = 0;
        String thisLine;
        BufferedReader in;
        List<String> small_list = new ArrayList<>();

        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream("crawler.idx")));

            while ((thisLine = in.readLine()) != null) {
                try {

                    String form = String.valueOf(thisLine.charAt(62)) + String.valueOf(thisLine.charAt(63));

                    if (form.equals("4 ")) {
                        http = thisLine.indexOf("http");
                        link_list.add(thisLine.substring(http));
                        counter++;
                    }
                } catch (StringIndexOutOfBoundsException e) {
                }
            }
            in.close();
        } catch (IOException io) {
            System.out.println("Problem reading the website");
        }

        try {
            System.out.println(counter + " Form 4 found!!!");
            System.out.println("This process will take a long time. Lets execute it by ranges");
            System.out.println("Lets enter a range to execute from. Enter the lower bound and press enter (Example: 0)");

            Scanner in_low = new Scanner(System.in);
            low = in_low.nextInt();

            System.out.println("Enter the higher bound and press enter (Example: 50)");
            Scanner in_high = new Scanner(System.in);
            high = in_high.nextInt();

        } catch (Exception e) {
            System.out.println("There was a problem with your input");
        }

        small_list = link_list.subList(low, high);

        getXMLlink(small_list);

    }

    public static void getXMLlink(List<String> link_list) throws Exception {

        int counter = 0;
        int count = 0;
        int cont = 0;
        int master_index;
        //ArrayList<String> xml_link = new ArrayList<String>();
        BufferedWriter csvWriter = new BufferedWriter(csvWriter_link);
        
        String Pass_XML_link = null;

        System.out.println("Pulling form 4 filings...");

        try {
            for (String direccion_filing : link_list) {
                master_index = filing_link_from_file.indexOf(direccion_filing.trim());
                
                // If the link to the HTML is not on the file, find it and add the XML link in 
                // variable direccion_form to ArrayList xml_link and save it in the file
                if (master_index < 0) {
                    cont++;
                    Thread.sleep(400);
                    Document document = Jsoup.connect(direccion_filing).get();
                    Elements allElements = document.getElementsByTag("a");

                    for (Element element : allElements) {
                        String direccion_form = element.attr("abs:href");
                        if (direccion_form.contains("xml")) {  //&& !st.contains("xsl")
                            //xml_link.add(direccion_form);
                            Pass_XML_link = direccion_form;

                            csvWriter.append(direccion_filing);
                            csvWriter.append("\r\n");
                            csvWriter.append(direccion_form);
                            csvWriter.append("\r\n");

                            System.out.print(".");
                            counter++;
                            break;
                        }
                    }
                
                // If the link was on the file, then add its corresponding XML link to xml_link array
                } else {
                    Pass_XML_link = xml_link_from_file.get(master_index);
                    Pass_XML_link = Pass_XML_link.trim();//        .trim();
                    count++;
                    
                }
                                   
                    getTransactionCodes(Pass_XML_link); 
                    
            }

        }
        
        finally {
            System.out.println();
            System.out.println("Links obtained from web: " + counter);
            System.out.println("Links obtained from file: " + count);
            System.out.println("New forms read " + form_counter);
            System.out.println("Forms that had already been read " + form_ctr);
            System.out.println("Purchases found " + purch_counter);
            
            
            csvWriter.flush();
            csvWriter.close();
            read_filing_writer.flush();
            read_filing_writer.close();
            csvWriter_purchases_b.flush();
            csvWriter_purchases_b.close();
            
        }

    }
    


    public static void getTransactionCodes(String xml_link) throws Exception {

        XMLEventReader reader;
        XMLEvent evento;
        XMLEvent next_evento;
        StartElement se;
        String new_xml_link;
        company obj = new company();
  
        
        //xml_link = "https://www.sec.gov/Archives/edgar/data/1402479/000089924321000379/xslF345X03/doc4.xml";
        
        
        if (!(read_filings.contains(xml_link))) {
            form_counter++;
            
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

                            obj = new company();
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

                                reader.nextTag();
                                if(reader.peek().isCharacters()){
                                    //System.out.println(reader.peek());
                               
                                Characters nxt = (Characters) reader.nextEvent();
                                obj.shares = (Double.parseDouble(nxt.getData()));
                                }
                            }
                            if (se.getName().getLocalPart().equals("transactionPricePerShare")) {

                                reader.nextTag();
                                if(reader.peek().isCharacters()){
                                    //System.out.println(reader.peek());
                               
                                Characters nxt = (Characters) reader.nextEvent();
                                obj.price = (Double.parseDouble(nxt.getData()));
                                }
                                //System.out.println("ASDASDASD");
                                Addit(obj);
                                obj.flag = 0;
                                purch_counter++;
                                //obj = new company();
                                
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
            catch (ClassCastException cc) {
                System.out.println("ClassCast " + new_xml_link);
                //System.out.println(se.getName().getLocalPart());
            }
            
            read_filing_writer.append(xml_link);
            read_filing_writer.append("\r\n");
            System.out.print("*");
            //read_filing_writer.append(xml_link);
        } else {
            
            form_ctr++;
        }
        read_filings.add(xml_link);

   }

    public static void Addit(company obj) throws Exception{

        
        csvWriter_purchases_b.append(obj.name + " ,");
        csvWriter_purchases_b.append(obj.price + " ,");
        csvWriter_purchases_b.append((obj.price * obj.shares) + " ,");
        csvWriter_purchases_b.append(obj.link + " ,");
        csvWriter_purchases_b.append(obj.director + ", ");
        csvWriter_purchases_b.append(obj.officer + ", ");
        csvWriter_purchases_b.append(obj.tenpercent + ", ");
        csvWriter_purchases_b.append("\r\n");
    }

}

class company {
//    public company(){
//        int flag = 0;
//    }

    String name;
    String link;
    String director;
    String officer;
    String tenpercent;
    int flag;
    double shares;
    double price;


}
