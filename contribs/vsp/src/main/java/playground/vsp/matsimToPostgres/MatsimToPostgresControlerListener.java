package playground.vsp.matsimToPostgres;

import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


public class MatsimToPostgresControlerListener implements ShutdownListener {
    private final Logger log = Logger.getLogger(MatsimToPostgresControlerListener.class);

    @Inject
    private Config config;

    @Override
    public void notifyShutdown(ShutdownEvent event) {
        final String runID = config.controler().getRunId();
        final String outputDir = config.controler().getOutputDirectory();

        // create db connections
        final PostgresExporterConfigGroup exporterConfigGroup = (PostgresExporterConfigGroup)config.getModules().get(PostgresExporterConfigGroup.GROUP_NAME);
        DBParameters params = new DBParameters(exporterConfigGroup.dbParamFile);
        Connection connection = params.createDBConnection();

        if (connection != null) {
            log.info("Connected to the database!");
        } else {
            log.error("Failed to make connection!");
        }

        try{
            // Import tripsCSV
            String tripsCSVFile = outputDir + "/" + runID + ".output_trips.csv.gz";
            CsvToPostgresExporter tripsExporter = new CsvToPostgresExporter(connection, tripsCSVFile, params.getDatabaseName(), runID);
            tripsExporter.export(params.getDatabaseName(), runID);

            // Import legsCSV
            String legsCSVFile = outputDir + "/" + runID + ".output_legs.csv.gz";
            CsvToPostgresExporter legsExporter = new CsvToPostgresExporter(connection, legsCSVFile, params.getDatabaseName(), runID);
            legsExporter.export(params.getDatabaseName(), runID);

            assert connection != null;
            connection.close();

        } catch (IOException e) {
            log.error("Csv output files were not found hence cannot export trips and legs to database. This probably means that the WriteTripsInterval value is not set correctly.");
            e.printStackTrace();
        } catch (SQLException e){
            log.error("Some sql error. See Strack Trace...");
            e.printStackTrace();
        }



    }


}
