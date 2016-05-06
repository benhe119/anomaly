import static org.elasticsearch.node.NodeBuilder.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket;
import org.elasticsearch.search.aggregations.bucket.histogram.*;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.aggregations.support.format.ValueFormat.DateTime;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.*;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.mail.*;
import org.apache.commons.mail.SimpleEmail;

public class App {

	public static void main(String[] args) throws UnknownHostException, EmailException {

		Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch_olek_dev")
				.put("client.transport.sniff", true).build();
		Client client = TransportClient.builder().settings(settings).build()
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

		SearchResponse sr = client.prepareSearch("logstash-2016.04.28").setQuery(QueryBuilders.matchAllQuery())

				.addAggregation(AggregationBuilders.dateHistogram("histo").field("@timestamp")
						.interval(DateHistogramInterval.SECOND))
				.execute().actionGet();

		Histogram agg = sr.getAggregations().get("histo");
		List buckets = agg.getBuckets();

		int window = 6;
		StandardDeviation std = new StandardDeviation();

		for (int i = window; i < buckets.size(); i++) {
			long docCount = ((Bucket) buckets.get(i)).getDocCount();

			double[] arr2 = new double[window];
			int k = i;

			for (int j = 0; j < window; j++) {
				arr2[j] = ((Bucket) buckets.get(k - 1)).getDocCount();
				k--;
			}

			double avg = StatUtils.mean(arr2);
			double dev = Math.sqrt(StatUtils.variance(arr2));

			double threshold = avg + 3 * dev;
			if (docCount > threshold) {
				System.out.println("Anomaly detected at " + ((Bucket) buckets.get(i)).getKeyAsString());
				SendEmail.sendEmail("Anomaly detected at " + ((Bucket) buckets.get(i)).getKeyAsString());
			}
			System.out.println(docCount + " " + avg + " " + dev + " " + threshold);
		}

	}

}
