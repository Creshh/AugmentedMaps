package test;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.MapNodeService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.MapNodeServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MapNode;


/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */
public class Tester {

    String query1 = "node[\"place\"=\"city\"][\"name\"=\"Chemnitz\"]->.center;"
                 + "(node(around.center:5000)[\"place\"~\"(town)|(village)|(city)\"];"
                 + "node(around.center:5000)[\"natural\"~\"(peak)|(rock)\"];);"
                 + "out;";

    String query2 = "<osm-script>"
            + "  <union into=\"_\">"
            + "    <query into=\"_\" type=\"node\">"
            + "      <around into=\"_\" lat=\"50.8322608\" lon=\"12.9252977\" radius=\"5000\"/>"
            + "      <has-kv k=\"place\" modv=\"\" regv=\"(town)|(village)|(city)\"/>"
            + "    </query>"
            + "    <query into=\"_\" type=\"node\">"
            + "      <around into=\"_\" lat=\"50.8322608\" lon=\"12.9252977\" radius=\"5000\"/>"
            + "      <has-kv k=\"natural\" modv=\"\" regv=\"(peak)|(rock)\"/>"
            + "    </query>"
            + "  </union>"
            + "  <print e=\"\" from=\"_\" geometry=\"skeleton\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>"
            + "</osm-script>";

    @Test
    public void test() throws ParserConfigurationException, SAXException, IOException {
        MapNodeService mapService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
//        List<MapNode> nodes = opService.getMapPointsInProximity(new Location(50.8322608f, 12.9252977f), null, 5000);
        List<MapNode> nodes = mapService.getMapPointsInProximity(new Location(50.83f, 12.9f), null, 10000);
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for(MapNode node : nodes){
            System.out.println(node.toString());
        }
        System.out.println("----------------------------------------------------------------------------------------------------------");
    }
}