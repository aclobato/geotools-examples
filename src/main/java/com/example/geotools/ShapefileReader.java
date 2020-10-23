package com.example.geotools;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapefileReader {
    FeatureSource<SimpleFeatureType, SimpleFeature> source;

    public ShapefileReader(File shpFile) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("url", shpFile.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        source = dataStore.getFeatureSource(typeName);
    }

    public Boolean isTransformationRequiredToWsg84() throws FactoryException {
        SimpleFeatureType schema = source.getSchema();
        if (schema.getCoordinateReferenceSystem() != null) {
            System.out.println(schema.getCoordinateReferenceSystem().toString());
            System.out.println("É WGS-84: " + !CRS.isTransformationRequired(schema.getCoordinateReferenceSystem(), DefaultGeographicCRS.WGS84));
            return CRS.isTransformationRequired(schema.getCoordinateReferenceSystem(), DefaultGeographicCRS.WGS84);
        } else {
            System.out.println("Sem sistema de coordenadas definido");
            return false;
        }
    }

    public List<Geometry> readGeometries() throws IOException {

        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        List<Geometry> geometries = new ArrayList<>();
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                geometries.add((Geometry) feature.getDefaultGeometryProperty().getValue());

                System.out.print(feature.getID());
                System.out.print(": ");
                System.out.print("(" + feature.getDefaultGeometryProperty().getValue().getClass().getName() + ") ");
                System.out.println(feature.getDefaultGeometryProperty().getValue().toString().substring(0,150) + "...");
            }
        }

        return geometries;
    }


    public static void main(String[] args) throws IOException, FactoryException {
        File file1 = new ClassPathResource("data/teste.shp").getFile();
        File file2 = new ClassPathResource("data/teste2.shp").getFile();
        File file3 = new ClassPathResource("data/teste3.shp").getFile();

        System.out.println("\n Arquivo 1");
        ShapefileReader shapefileReader1 = new ShapefileReader(file1);
        shapefileReader1.isTransformationRequiredToWsg84();
        shapefileReader1.readGeometries();

        System.out.println("\n Arquivo 2");
        ShapefileReader shapefileReader2 = new ShapefileReader(file2);
        shapefileReader2.isTransformationRequiredToWsg84();
        shapefileReader2.readGeometries();

        System.out.println("\n Arquivo 3");
        ShapefileReader shapefileReader3 = new ShapefileReader(file3);
        shapefileReader3.isTransformationRequiredToWsg84();
        shapefileReader3.readGeometries();
    }
}
