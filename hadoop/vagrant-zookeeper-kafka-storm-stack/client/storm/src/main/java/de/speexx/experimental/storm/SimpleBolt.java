package de.speexx.experimental.storm;

import java.util.Map;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sascha.kohlmann
 */
public class SimpleBolt extends BaseRichBolt {

    static final Logger LOG = LoggerFactory.getLogger(SimpleBolt.class);
    private OutputCollector collector;

    @Override
    public void declareOutputFields(final OutputFieldsDeclarer ofd) {
    }

    @Override
    public void prepare(final Map map, final TopologyContext tc, final OutputCollector oc) {
        this.collector = collector;
    }

    @Override
    public void execute(final Tuple in) {
        LOG.info("TUPLE:" + in);
        System.out.println("TUPLE:" + in);
        if (this.collector != null) {
            this.collector.ack(in);
        } else {
        LOG.info("No collector");
        System.out.println("No collector");
        }
    }
}
