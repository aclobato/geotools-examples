package com.example.geotools;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class GeotoolsApplication {

	public static void main(String[] args) throws FactoryException, TransformException {
		SpringApplication.run(GeotoolsApplication.class, args);

		GeotoolsUtil geotoolsUtil = new GeotoolsUtil();
		GeoJsonPolygon polygon = geotoolsUtil.transformCircleToPolygon(new GeoJsonPoint(-43.9341315, -19.9401751), 10000);
		System.out.println(polygon);

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
