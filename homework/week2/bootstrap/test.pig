register target/bootstrap-0.0.1-SNAPSHOT.jar

a = load 'studenttab10k' using com.example.pig.BootstrapSampleLoader();
dump a;
