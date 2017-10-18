package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import android.text.TextUtils;

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
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MissingParameterException;


/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class OverpassService implements MapNodeService {

    private static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter";

    private static final String TAG_NAME = "name";
    private static final String TAG_ELEVATION = "ele";
    private static final String TAG_PLACE = "place";
    private static final String TAG_NATURAL = "natural";

    private static final String __LAT__ = "__LAT__";
    private static final String __LON__ = "__LON__";
    private static final String __RAD__ = "__RAD__";
    private static final String __KEY__ = "__KEY__";
    private static final String __VALUES__ = "__VALUES__";
    private static final String __QUERY__ = "__QUERY__";

    private static final String SCRIPT_TEMPLATE  = "<osm-script>"
                                                + "  <union into=\"_\">"
                                                +     __QUERY__
                                                + "  </union>"
                                                + "  <print e=\"\" from=\"_\" geometry=\"skeleton\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>"
                                                + "</osm-script>";

    private static final String QUERY_TEMPLATE  = "    <query into=\"_\" type=\"node\">"
                                                + "      <around into=\"_\" lat=\"" + __LAT__ + "\" lon=\"" + __LON__ + "\" radius=\"" + __RAD__ + "\"/>"
                                                + "      <has-kv k=\"" + __KEY__ + "\" modv=\"\" regv=\"" + __VALUES__ + "\"/>" // place --> (town)|(village)|(city)
                                                + "    </query>";


    @Override
    public List<MapNode> getMapPointsInProximity(Location loc, Map<String, List<String>> tags, int maxDistance) throws MissingParameterException {
        String query;
        if(tags != null && !tags.isEmpty()){
            StringBuilder queries = new StringBuilder();
            for(String key : tags.keySet()){
                queries.append(QUERY_TEMPLATE.replace(__LAT__, String.valueOf(loc.getLat()))
                        .replace(__LON__, String.valueOf(loc.getLon()))
                        .replace(__RAD__, String.valueOf(maxDistance))
                        .replace(__KEY__, key)
                        .replace(__VALUES__, "(" + TextUtils.join(")|(", tags.get(key)) + ")"));
            }
            query = SCRIPT_TEMPLATE.replace(__QUERY__, queries.toString());
        } else {
            throw new MissingParameterException();
        }

        Document doc;
        try {
            doc = getNodesViaOverpass(query);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
    private List<MapNode> getNodes(Document xmlDocument) {
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
                    int ele = (int) (tags.get(TAG_ELEVATION) == null ? -1f : Float.parseFloat(tags.get(TAG_ELEVATION).replaceAll(",", ".")));
                    String tag = "";
                    for(Map.Entry<String, String> tagEntry : tags.entrySet()){
                        if(tagEntry.getKey().equals(TAG_NATURAL) || tagEntry.getKey().equals(TAG_PLACE)){
                            tag = tagEntry.getValue();
                        }
                    }

                    MapNode mapNode = new MapNode(id, name, new Location(Float.parseFloat(lat), Float.parseFloat(lon), ele), tag);

                    mapNodes.add(mapNode);
                }
            }
        }
        return mapNodes;
    }


}
