import twitter4j.Paging;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * TwitterUserAnalyticsMain class used to analyze user twitter activities for past seven days.
 * Plotting the same in bar chart representation as a separate chart for User Tweets, Retweets and Favorites count on each day based on time(every six hours).
 * 
 * @author Prasat Madeshwaran
 *
 */
public class TwitterUserAnalyticsMain extends ApplicationFrame {
	
	private static final String ACCESS_TOKEN = "1132409715109928960-LyexUBk9EK2RKotMvcPL68ZWpfunRT";
	private static final String ACCESS_SECRET = "oczcBO1mnRb2Q6itOHWLXTvV5OAt75CxBnbynQt8UdTSm";
	private static final String CONSUMER_TOKEN = "FQbbk7UGrvOij1qhmB5D1t588";
	private static final String CONSUMER_SECRET = "66E5lMpmDr5eHqj8lLLy4a2DKVhMV7TQNtB8Yj16WNtw9uq0rO";
	
	
	public TwitterUserAnalyticsMain(String title, Map<String,List<Status>> tweetDataMap) {
	      super(title);
	      
	      CategoryPlot plot = new CategoryPlot();
	      JFreeChart chart = new JFreeChart(plot);
	      if(title.equals("Tweet Count")) {
		      CategoryItemRenderer tweetBarRenderer = new BarRenderer();
		      plot.setDataset(0, createWholeDatasetTweet(tweetDataMap));
		      plot.setRenderer(0, tweetBarRenderer);
		      plot.setDomainAxis(new CategoryAxis("Day"));
		      plot.setRangeAxis(new NumberAxis("Tweets"));
		      chart.setTitle("Tweet Analysis");
	      }else if(title.equals("Retweet Count")) {
	    	  CategoryItemRenderer rtBarRenderer = new BarRenderer();
		      plot.setDataset(1, createWholeDatasetRT(tweetDataMap));
		      plot.setRenderer(1, rtBarRenderer);
		      plot.setDomainAxis(new CategoryAxis("Day"));
		      plot.setRangeAxis(new NumberAxis("Retweets"));
		      chart.setTitle("Retweets Analysis");
	      }
	      else {
		      CategoryItemRenderer favBarRenderer = new BarRenderer();
		      plot.setDataset(2, createWholeDatasetFav(tweetDataMap));
		      plot.setRenderer(2, favBarRenderer);
		      plot.setDomainAxis(new CategoryAxis("Day"));
		      plot.setRangeAxis(new NumberAxis("Favorites"));
		      chart.setTitle("Favorites Analysis");
	      }
	      ChartPanel panel = new ChartPanel(chart);
	      setContentPane(panel);
	   }
	
	/**
	 * createWholeDatasetTweet method creates dataSet for tweets of user.
	 * 
	 * @param tweetDataMap
	 * @return the dataset of type DefaultCategoryDataset
	 */
	private DefaultCategoryDataset createWholeDatasetTweet(Map<String,List<Status>> tweetDataMap ) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		tweetDataMap.forEach((day,value)->{
			Map<String,List<Status>> tweetTimeMap = new HashMap<>();
			prepareDataMap(tweetTimeMap,value);
			tweetTimeMap.forEach((tweetTimeKey, statusList)->{
				String key = prepareMessage(tweetTimeKey);
				dataset.addValue(statusList.size(),"Tweets between "+key,day);
			});
		});
		return dataset;
	}
	
	/**
	 * createWholeDatasetTweet method creates dataSet for retweets of user.
	 * 
	 * @param tweetDataMap
	 * @return the dataset of type DefaultCategoryDataset
	 */
	private DefaultCategoryDataset createWholeDatasetRT(Map<String,List<Status>> tweetDataMap ) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		tweetDataMap.forEach((day,value)->{
			Map<String,List<Status>> tweetTimeMap = new HashMap<>();
			prepareDataMap(tweetTimeMap,value);
			Map<String, Long> rtCountMap = new HashMap<>();
			tweetTimeMap.forEach((tweetTimeKey,statusList)->{
				Long rtSum =new Long(0);
				for (Status status : statusList) {
					rtSum+=status.getRetweetCount();
				}
				rtCountMap.put(tweetTimeKey+"_rt_count", rtSum);
			});
			tweetTimeMap.forEach((tweetTimeKey, statusList)->{
				String key = prepareMessage(tweetTimeKey);
				dataset.addValue(rtCountMap.get(tweetTimeKey+"_rt_count"),"RT between "+key,day);
			});
		});
		return dataset;
	}
	
	/**
	 * createWholeDatasetFav method creates dataSet for favorites of user.
	 * 
	 * @param tweetDataMap
	 * @return the dataset of type DefaultCategoryDataset
	 */
	private DefaultCategoryDataset createWholeDatasetFav(Map<String,List<Status>> tweetDataMap ) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		tweetDataMap.forEach((day,value)->{
			Map<String,List<Status>> tweetTimeMap = new HashMap<>();
			prepareDataMap(tweetTimeMap,value);
			Map<String, Long> favCountMap = new HashMap<>();
			tweetTimeMap.forEach((tweetTimeKey,statusList)->{
				Long favSum=new Long(0);
				for (Status status : statusList) {
					favSum+=status.getFavoriteCount();
				}
				favCountMap.put(tweetTimeKey+"_fav_count", favSum);
			});
			tweetTimeMap.forEach((tweetTimeKey, statusList)->{
				String key = prepareMessage(tweetTimeKey);
				dataset.addValue(favCountMap.get(tweetTimeKey+"_fav_count"),"Favorites between "+key,day);
			});
		});
		return dataset;
	}
	
	/**
	 * prepareMessage method returns string to show graphs details.
	 * 
	 * @param tweetTimeKey
	 * @return the String key
	 */
	private String prepareMessage(String tweetTimeKey) {
		tweetTimeKey = tweetTimeKey.replace("tweets", "");
		StringTokenizer st = new StringTokenizer(tweetTimeKey,"_");
		String key = "";
		while (st.hasMoreTokens()) {  
	         key+=st.nextToken()+" ";
	     }  
		key+="hrs";
		return key;
	}

	/**
	 * getTweetTimeMap method creates map for each day activity.
	 * 
	 * @param tweetTimeMap
	 * @param tweet
	 * @param key
	 */
	private void getTweetTimeMap(Map<String,List<Status>> tweetTimeMap, Status tweet, String key){
		if (tweetTimeMap.containsKey(key)) {
			List<Status> tweetList = tweetTimeMap.get(key);
			tweetList.add(tweet);
			tweetTimeMap.put(key, tweetList);
		} else {
			List<Status> tweetList = new ArrayList<>();
			tweetList.add(tweet);
			tweetTimeMap.put(key, tweetList);
		}
	}
	
	/**
	 * prepareDataMap method separates user activities by 4 time slots(12am to 6am, 6am to 12pm, 12pm to 6pm & 6pm to 12am). 
	 * 
	 * @param tweetTimeMap
	 * @param value
	 */
	private void prepareDataMap(Map<String,List<Status>> tweetTimeMap ,List<Status> value) {
		int num = value.size();
		value.forEach(tweet->{
			if(tweet.getCreatedAt().getHours()>=0 && tweet.getCreatedAt().getHours()<=5) {
				getTweetTimeMap(tweetTimeMap,tweet,"tweets_0_5");
			}else if(tweet.getCreatedAt().getHours()>=6 && tweet.getCreatedAt().getHours()<=11) {
				getTweetTimeMap(tweetTimeMap,tweet,"tweets_6_11");
			}else if(tweet.getCreatedAt().getHours()>=12 && tweet.getCreatedAt().getHours()<=17){
				getTweetTimeMap(tweetTimeMap,tweet,"tweets_12_17");
			}else if(tweet.getCreatedAt().getHours()>=18 && tweet.getCreatedAt().getHours()<=23){
				getTweetTimeMap(tweetTimeMap,tweet,"tweets_18_23");
			}
		});
	}

	/**
	 * The main class
	 * 
	 * @param args the twitter handle of the user (ex: kattyperry).
	 */
	public static void main(String[] args) {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		configBuilder.setDebugEnabled(true).setOAuthConsumerKey(CONSUMER_TOKEN)
				.setOAuthConsumerSecret(CONSUMER_SECRET)
				.setOAuthAccessToken(ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(ACCESS_SECRET);
		TwitterFactory twitterFactory = new TwitterFactory(configBuilder.build());
		Twitter twitter = twitterFactory.getInstance();
		Paging page = new Paging(1, 100);
		try {
			List<Status> statuses;
			String user;
			if (args.length == 1) {
				user = args[0];
				statuses = twitter.getUserTimeline(user, page);
			} else {
				user = twitter.verifyCredentials().getScreenName();
				statuses = twitter.getUserTimeline();

			}
			System.out.println("Showing @" + user + "'s user timeline for past 7 days");
			Instant now = Instant.now(); 
			Instant before = now.minus(Duration.ofDays(7));
			Date dateBefore = Date.from(before);
			Map<String, List<Status>> tweetsPerDayMap = new HashMap<>();
			List<Status> lastSevedDaysTweets = new ArrayList<>();
			lastSevedDaysTweets = statuses.stream()
					.filter(status -> (status.getCreatedAt().getTime() >= dateBefore.getTime()))
					.collect(Collectors.toList());
			lastSevedDaysTweets.forEach(status -> {
				Date tweetDate = status.getCreatedAt();
				SimpleDateFormat simpleDateformat = new SimpleDateFormat("E"); 
				String tweetDay = simpleDateformat.format(tweetDate);
				if (tweetsPerDayMap.containsKey(tweetDay)) {
					List<Status> tweetList = tweetsPerDayMap.get(tweetDay);
					tweetList.add(status);
					tweetsPerDayMap.put(tweetDay, tweetList);
				} else {
					List<Status> tweetList = new ArrayList<>();
					tweetList.add(status);
					tweetsPerDayMap.put(tweetDay, tweetList);
				}
			});
			SwingUtilities.invokeLater(() -> {
				TwitterUserAnalyticsMain tweetUserAnalytics = new TwitterUserAnalyticsMain(
		               "Tweet Count",tweetsPerDayMap);
		         tweetUserAnalytics.setSize(800, 400);
		         tweetUserAnalytics.setLocationRelativeTo(null);
		         tweetUserAnalytics.setVisible(true);
		      });
			SwingUtilities.invokeLater(() -> {
				TwitterUserAnalyticsMain retweetUserAnalytics = new TwitterUserAnalyticsMain(
		               "Retweet Count",tweetsPerDayMap);
		         retweetUserAnalytics.setSize(800, 400);
		         retweetUserAnalytics.setLocationRelativeTo(null);
		         retweetUserAnalytics.setVisible(true);
		      });
			SwingUtilities.invokeLater(() -> {
				TwitterUserAnalyticsMain favoritesUserAnalytics = new TwitterUserAnalyticsMain(
		               "Favorites Count",tweetsPerDayMap);
		         favoritesUserAnalytics.setSize(800, 400);
		         favoritesUserAnalytics.setLocationRelativeTo(null);
		         favoritesUserAnalytics.setVisible(true);
		      });
			for (Status status : lastSevedDaysTweets) {
				if (status.getCreatedAt().getTime() >= dateBefore.getTime()) {
					System.out.println("@" + status.getUser().getScreenName() + " tweeted on " + status.getCreatedAt() + " retweeted by "
							+ status.getRetweetCount() + " favorited by " + status.getFavoriteCount());
				}
			}
		} catch (TwitterException twitterException) {
			twitterException.printStackTrace();
			System.out.println("Failed to get timeline: " + twitterException.getMessage());
			System.exit(-1);
		}
	}
}