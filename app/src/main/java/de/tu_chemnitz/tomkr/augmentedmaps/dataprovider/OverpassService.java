package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Tag;


/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class OverpassService implements MapNodeService {

    private static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter";
    private static final String OPENSTREETMAP_API_06 = "http://www.openstreetmap.org/api/0.6/";

    private static final String TAG_NAME = "name";
    private static final String TAG_ELEVATION = "ele";
    private static final String TAG_PLACE = "place";
    private static final String TAG_NATURAL = "natural";

    private static final String QUERY_TEMPLATE  = "<osm-script>"
                                                + "  <union into=\"_\">"
                                                + "    <query into=\"_\" type=\"node\">"
                                                + "      <around into=\"_\" lat=\"__LAT__\" lon=\"__LON__\" radius=\"__RAD__\"/>"
                                                + "      <has-kv k=\"place\" modv=\"\" regv=\"(town)|(village)|(city)\"/>"
                                                + "    </query>"
                                                + "    <query into=\"_\" type=\"node\">"
                                                + "      <around into=\"_\" lat=\"__LAT__\" lon=\"__LON__\" radius=\"__RAD__\"/>"
                                                + "      <has-kv k=\"natural\" modv=\"\" regv=\"(peak)|(rock)\"/>"
                                                + "    </query>"
                                                + "  </union>"
                                                + "  <print e=\"\" from=\"_\" geometry=\"skeleton\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>"
                                                + "</osm-script>";


    @Override
    public List<MapNode> getMapPointsInProximity(Location loc, Tag tags[], int maxDistance) {
        String query = QUERY_TEMPLATE.replaceAll("__LAT__", String.valueOf(loc.getLat()))
                .replaceAll("__LON__", String.valueOf(loc.getLon()))
                .replaceAll("__RAD__", String.valueOf(maxDistance));
        Document doc = null;
        try {
            doc = getNodesViaOverpass(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getNodes(doc);
    }


    /**
     * @param query the overpass query
     * @return the nodes in the formulated query
     */
    public Document getNodesViaOverpass(String query) throws IOException, ParserConfigurationException, SAXException {
        URL osm = new URL(OVERPASS_API);
        HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
        printout.writeBytes("data=" + URLEncoder.encode(query, "utf-8"));
        printout.flush();
        printout.close();

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder.parse(connection.getInputStream());
    }

    /**
     *
     * @param xmlDocument
     * @return a list of openstreetmap nodes extracted from xml
     */
    @SuppressWarnings("nls")
    public List<MapNode> getNodes(Document xmlDocument) {
        List<MapNode> mapNodes = new ArrayList<>();

        // Document xml = getXML(8.32, 49.001);
        Node osmRoot = xmlDocument.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("node")) {
                NamedNodeMap attributes = item.getAttributes();
                NodeList tagXMLNodes = item.getChildNodes();
                Map<String, String> tags = new HashMap<>();
                for (int j = 1; j < tagXMLNodes.getLength(); j++) {
                    Node tagItem = tagXMLNodes.item(j);
                    NamedNodeMap tagAttributes = tagItem.getAttributes();
                    if (tagAttributes != null) {
                        tags.put(tagAttributes.getNamedItem("k").getNodeValue(), tagAttributes.getNamedItem("v")
                                .getNodeValue());
                    }
                }
                Node namedItemID = attributes.getNamedItem("id");
                Node namedItemLat = attributes.getNamedItem("lat");
                Node namedItemLon = attributes.getNamedItem("lon");

                String name = tags.get(TAG_NAME);
                if(name != null){
                    String id = namedItemID.getNodeValue();
                    String lat = namedItemLat.getNodeValue();
                    String lon = namedItemLon.getNodeValue();
                    float ele = tags.get(TAG_ELEVATION) == null ? -1f : Float.parseFloat(tags.get(TAG_ELEVATION));
                    String tag = "";
                    for(Map.Entry<String, String> tagEntry : tags.entrySet()){
                        if(tagEntry.getKey().equals(TAG_NATURAL) || tagEntry.getKey().equals(TAG_PLACE)){
                            tag = tagEntry.getValue();
                        }
                    }

                    MapNode mapNode = new MapNode(id, name, lat, lon, tag, ele);

                    mapNodes.add(mapNode);
                }
            }
        }
        return mapNodes;
    }


}
