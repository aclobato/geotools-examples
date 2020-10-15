package com.example.geotools;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class GeotoolsApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(GeotoolsApplication.class, args);

		testGeotoolsShapefile();
		testGeotoolsUtil();
	}

	private static void testGeotoolsShapefile() throws Exception {
		File file = new ClassPathResource(
				"data/ti_sirgasPolygon.shp").getFile();
		Map<String, Object> map = new HashMap<>();
		map.put("url", file.toURI().toURL());

		DataStore dataStore = DataStoreFinder.getDataStore(map);
		String typeName = dataStore.getTypeNames()[0];

		FeatureSource<SimpleFeatureType, SimpleFeature> source =
				dataStore.getFeatureSource(typeName);
		Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

		SimpleFeatureType schema = source.getSchema();
		System.out.println(schema.getCoordinateReferenceSystem().toString());
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
		try (FeatureIterator<SimpleFeature> features = collection.features()) {
			while (features.hasNext()) {
				SimpleFeature feature = features.next();
				System.out.print(feature.getID());
				System.out.print(": ");
				System.out.println(feature.getDefaultGeometryProperty().getValue());
			}
		}

		System.out.println("");
	}

	private static void testGeotoolsUtil() throws FactoryException, TransformException {
		GeotoolsUtil geotoolsUtil = new GeotoolsUtil();
		GeoJsonPolygon polygon = geotoolsUtil.transformCircleToPolygon(new GeoJsonPoint(-43.9341315, -19.9401751), 10000);
		System.out.println(polygon);

		System.out.println("Area círculo = " + geotoolsUtil.calculateArea(polygon) + "m2");

		List<Point> points = new ArrayList<>();
		points.add(new Point(-43.941080484075798, -19.938139184239901));
		points.add(new Point(-43.941011741941495, -19.938145098132498));
		points.add(new Point(-43.940858457709496, -19.938198748327004));
		points.add(new Point(-43.939791777814897, -19.938454816800899));
		points.add(new Point(-43.940122204571296, -19.9396961306831));
		points.add(new Point(-43.941432589315298, -19.939392007865298));
		points.add(new Point(-43.941080484075798, -19.938139184239901));
		GeoJsonPolygon area = new GeoJsonPolygon(points);
		// Deve ser aproximadamente 19984,792 m²
		System.out.println("Area = " + geotoolsUtil.calculateArea(area) + "m2");
	}

}
