package gov.tn.dhs.quarkus.box;

import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.Quarkus;

// useful entry point to have for testing in IDE

@QuarkusMain
public class Main {

    public static void main(String ... args) {
        Quarkus.run(args);
    }

}