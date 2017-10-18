package test;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MissingParameterException;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.InputType;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.MapNodeService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.MapNodeServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessorProvider;

import static android.R.attr.data;
import static android.R.id.input;


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
    public void test() throws ParserConfigurationException, SAXException, IOException, MissingParameterException {
        MapNodeService mapService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
//        List<MapNode> nodes = opService.getMapPointsInProximity(new Location(50.8322608f, 12.9252977f), null, 5000);
        List<MapNode> nodes = mapService.getMapPointsInProximity(new Location(50.83f, 12.9f), null, 10000);
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for(MapNode node : nodes){
            System.out.println(node.toString());
        }
        System.out.println("----------------------------------------------------------------------------------------------------------");
    }

//    @Test
    public void testDistance(){
        Location locBase = new Location(50.83592f, 12.923312f); // Karl-Marx-Kopf Chemnitz

        Location loc1 = new Location(50.82421f, 12.900267f);    // Industriemuseum Chemnitz
        Location loc2 = new Location(50.833128f, 12.818298f);   // Burg Rabenstein

        Location loc3 = new Location(50.812931f, 13.099308f);   // Schloss Augustusburg

        Location loc4 = new Location(50.64859f, 12.930436f);     // Naturbühne Greifensteine


        Location loc5 = new Location(50.429081f, 12.954426f);   // Fichtelberg
        Location loc6 = new Location(51.051889f, 13.741493f);   // Frauenkirche Dresden

        System.out.println("DistanceCorr=" + locBase.getDistanceCorr(loc1) + " DistanceApprx=" + locBase.getDistanceApprx(loc1) + " Diff=" + Math.abs(locBase.getDistanceCorr(loc1)-locBase.getDistanceApprx(loc1)));
        System.out.println("DistanceCorr=" + locBase.getDistanceCorr(loc2) + " DistanceApprx=" + locBase.getDistanceApprx(loc2) + " Diff=" + Math.abs(locBase.getDistanceCorr(loc2)-locBase.getDistanceApprx(loc2)));
        System.out.println("DistanceCorr=" + locBase.getDistanceCorr(loc3) + " DistanceApprx=" + locBase.getDistanceApprx(loc3) + " Diff=" + Math.abs(locBase.getDistanceCorr(loc3)-locBase.getDistanceApprx(loc3)));
        System.out.println("DistanceCorr=" + locBase.getDistanceCorr(loc4) + " DistanceApprx=" + locBase.getDistanceApprx(loc4) + " Diff=" + Math.abs(locBase.getDistanceCorr(loc4)-locBase.getDistanceApprx(loc4)));
        System.out.println("DistanceCorr=" + locBase.getDistanceCorr(loc5) + " DistanceApprx=" + locBase.getDistanceApprx(loc5) + " Diff=" + Math.abs(locBase.getDistanceCorr(loc5)-locBase.getDistanceApprx(loc5)));
        System.out.println("DistanceCorr=" + locBase.getDistanceCorr(loc6) + " DistanceApprx=" + locBase.getDistanceApprx(loc6) + " Diff=" + Math.abs(locBase.getDistanceCorr(loc6)-locBase.getDistanceApprx(loc6)));

        System.out.println("Bearing=" + locBase.getBearingTo(loc1));
        System.out.println("Bearing=" + locBase.getBearingTo(loc2));
        System.out.println("Bearing=" + locBase.getBearingTo(loc3));
        System.out.println("Bearing=" + locBase.getBearingTo(loc4));
        System.out.println("Bearing=" + locBase.getBearingTo(loc4));
        System.out.println("Bearing=" + locBase.getBearingTo(loc5));
        System.out.println("Bearing=" + locBase.getBearingTo(loc6));
    }

//    @Test
    public void testCalculatePosition(){

        float o = 22.5f;

        float betaV = o > 180 ? 360 - o : -o;
        float h = -250;
        float d = 500;
        float alphaV = (float) Math.toDegrees(Math.atan2(h,d));
        float diffV = betaV + alphaV;
        System.out.println(""+diffV);
        float offsetV = -1;
        if(Math.abs(diffV) < (45/2f)){
            offsetV = diffV / (45/2f);
            //offsetV = ((offsetV + 1) /2f);
        }
        System.out.println("" + offsetV);
    }
}