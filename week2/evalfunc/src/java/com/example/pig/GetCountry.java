package com.example.pig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.util.Utils;
import org.apache.pig.parser.ParserException;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

public class GetCountry extends EvalFunc<Tuple> {
    LookupService cl;
	@Override
	public Tuple exec(Tuple t) throws IOException {
	    if (cl == null) {
	        cl = new LookupService("GeoLiteCity.dat",
	                LookupService.GEOIP_MEMORY_CACHE );
	    }
	    Location loc = cl.getLocation((String)t.get(0));
	    if (loc == null) {
	        return null;
	    }
	    Tuple r = TupleFactory.getInstance().newTuple();
	    r.append(loc.countryName);
	    r.append(loc.city);
	    return r;
	}
	@Override
    public List<String> getShipFiles() {
        List<String> shipFiles = new ArrayList<String>();
        shipFiles.add("GeoLiteCity.dat");
        return shipFiles;
    }
	@Override
    public Schema outputSchema(Schema input) {
	    try {
            return Utils.getSchemaFromString("(country:chararray, city:chararray)");
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
    }
}