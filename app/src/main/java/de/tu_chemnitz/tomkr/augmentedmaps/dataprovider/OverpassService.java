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

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MissingParameterException;


/**
 * Created by Tom Kretzschmar on 21.09.2017.
 * <br>
 * An {@link MapNodeService} which uses the overpass api to query mapnodes around the user device.<br>
 * The acquired information is based on OpenStreetMap data.
 * See: https://wiki.openstreetmap.org/wiki/Overpass_API
 */
public class OverpassService implements MapNodeService {
    /**
     * Tag for logging
     */
    private static final String TAG = OverpassService.class.getName();

    /**
     * Target url for overpass api
     * PP Changed 02102019
     */
    private static final String OVERPASS_API = "https://z.overpass-api.de/api/interpreter";

    /**
     * Constant for xml element name
     */
    private static final String TAG_NAME = "name";

    /**
     * Constant for xml element elevation
     */
    private static final String TAG_ELEVATION = "ele";

    /**
     * Constant for xml element place
     */
    private static final String TAG_PLACE = "place";

    /**
     * Constant for xml element natural
     */
    private static final String TAG_NATURAL = "natural";

    /**
     * Placeholder key for latitude in query template.
     */
    private static final String __LAT__ = "__LAT__";

    /**
     * Placeholder key for longitude in query template.
     */
    private static final String __LON__ = "__LON__";

    /**
     * Placeholder key for radius in query template.
     */
    private static final String __RAD__ = "__RAD__";

    /**
     * Placeholder key for tag keys in query template.
     */
    private static final String __KEY__ = "__KEY__";

    /**
     * Placeholder key for tag values in query template.
     */
    private static final String __VALUES__ = "__VALUES__";

    /**
     * Placeholder key for query in script template.
     */
    private static final String __QUERY__ = "__QUERY__";


    /**
     * Template for an overpass script. Can contain multiple queries at once. Will be filled with queries dynamically.
     */
    private static final String SCRIPT_TEMPLATE  = "<osm-script>"
                                                + "  <union into=\"_\">"
                                                +     __QUERY__
                                                + "  </union>"
                                                + "  <print e=\"\" from=\"_\" geometry=\"skeleton\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>"
                                                + "</osm-script>";

    /**
     * Template for an overpass query. Will be filled with information dynamically.
     */
    private static final String QUERY_TEMPLATE  = "    <query into=\"_\" type=\"node\">"
                                                + "      <around into=\"_\" lat=\"" + __LAT__ + "\" lon=\"" + __LON__ + "\" radius=\"" + __RAD__ + "\"/>"
                                                + "      <has-kv k=\"" + __KEY__ + "\" modv=\"\" regv=\"" + __VALUES__ + "\"/>" // place --> (town)|(village)|(city)
                                                + "    </query>";


    /**
     * Get a list of {@link MapNode} objects which represent geolocations corresponding to the given OpenStreetMap tags using overpass.
     * @param loc Current user device location.
     * @param tags Map of OSM tags, which define the type of the nodes which should be returned.
     * @param maxDistance Maximum distance to the user device the points should have
     * @return A list of geolocations corresponding to the given attributes
     * @throws MissingParameterException An exception if parameters are missing or not suitable for the implementation.
     */
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
     * Helper function to build a connection and send the query to the target.
     * @param query The overpass query string
     * @return The xml document containing the nodes corresponding to the given query.
     */
    private Document getNodesViaOverpass(String query) throws IOException, ParserConfigurationException, SAXException {
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
     * Helper function to parse the xml document retrieved from the overpass query.
     * @param xmlDocument The xml document retrieved from the Overpass API
     * @return A list of MapNodes extracted from the retrieved xml document
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
