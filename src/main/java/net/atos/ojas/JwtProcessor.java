package net.atos.ojas;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import net.atos.ojas.jwt.util;

public class JwtProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        List<String> groups = Arrays.asList("foo","bar");

        try {
            String token = util.createToken(groups);
            util.validateToken(token);
            exchange.getOut().setBody(token);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.getOut().setBody(e.getMessage());
        }

    } 

}