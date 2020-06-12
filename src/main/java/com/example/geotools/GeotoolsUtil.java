package com.example.geotools;

import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.iso.text.WKTParser;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeotoolsUtil {

    public Double calculateArea(GeoJsonPolygon polygon) throws FactoryException, TransformException {
        GeometryFactory geomFactory = new GeometryFactory();
        List<Coordinate> coordinates = new ArrayList<>();
        polygon.forEach(point -> coordinates.add(new Coordinate(point.getX(), point.getY())));

        Polygon geometry = geomFactory.createPolygon(coordinates.toArray(Coordinate[]::new));
        Geometry utmGeometry = transformToUtm(geometry);

        return utmGeometry.getArea();


    }

    public Double calculateLength(List<GeoJsonPoint> points) throws FactoryException, TransformException {
        GeometryFactory geomFactory = new GeometryFactory();
        List<Coordinate> coordinates = new ArrayList<>();
        points.forEach(point -> coordinates.add(new Coordinate(point.getX(), point.getY())));

        LineString lineString = geomFactory.createLineString(coordinates.toArray(Coordinate[]::new));
        Geometry utmGeometry = transformToUtm(lineString);

        return utmGeometry.getLength();
    }

    public GeoJsonPolygon transformCircleToPolygon(GeoJsonPoint center, Integer radiusMeters)
    {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(center.getX(), center.getY()));

        GeodeticCalculator calc = new  GeodeticCalculator(DefaultGeographicCRS.WGS84);
        calc.setStartingGeographicPoint(point.getX(), point.getY());
        calc.setDirection(0.0, radiusMeters);
        Point2D p2 = calc.getDestinationGeographicPoint();
        calc.setDirection(90.0, radiusMeters);
        Point2D p3 = calc.getDestinationGeographicPoint();

        double dy = p2.getY() - point.getY();
        double dx = p3.getX() - point.getX();
        double distance = (dy + dx) / 2.0;
        Polygon polygon = (Polygon) point.buffer(distance);
        return transformToGeoJsonPolygon(polygon);
    }

    private GeoJsonPolygon transformToGeoJsonPolygon(Polygon polygon) {
        List<Coordinate> coordinates = Arrays.asList(polygon.getCoordinates());
        List<org.springframework.data.geo.Point> points = new ArrayList<org.springframework.data.geo.Point>();
        coordinates.forEach(coordinate -> points.add(new org.springframework.data.geo.Point(coordinate.x, coordinate.y)));
        return new GeoJsonPolygon(points);
    }

    private Geometry transformToUtm(Geometry geometry) throws FactoryException, TransformException {
        Point centroid = geometry.getCentroid();
        CoordinateReferenceSystem utm = CRS.decode(String.format("AUTO2:42001,%s,%s", centroid.getX() , centroid.getY()), true);
        MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, utm);
        return JTS.transform(geometry, transform);
    }
}
